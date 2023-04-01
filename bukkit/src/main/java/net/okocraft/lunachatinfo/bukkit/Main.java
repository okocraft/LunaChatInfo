package net.okocraft.lunachatinfo.bukkit;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.channel.Channel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class Main extends JavaPlugin implements PluginMessageListener, Listener {

    private static final boolean FOLIA;

    static {
        boolean isFolia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
            isFolia = true;
        } catch (ClassNotFoundException e) {
            isFolia = false;
        }

        FOLIA = isFolia;
    }

    private static final String PMC_NAME = "lunachat:info";

    private final Map<String, String> defaultChannels = new ConcurrentHashMap<>();

    private final Placeholder placeholderAPIHook = new Placeholder(this);

    private boolean isBungeeMode = false;

    @Override
    public void onEnable() {
        getServer().getMessenger().registerIncomingPluginChannel(this, PMC_NAME, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, PMC_NAME);
        getServer().getPluginManager().registerEvents(this, this);
        placeholderAPIHook.register();

        getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .forEach(this::scheduleGettingDefaultChannelTask);
    }

    @Override
    public void onDisable() {
        placeholderAPIHook.unregister();
        HandlerList.unregisterAll((Plugin) this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        scheduleGettingDefaultChannelTask(event.getPlayer().getName());
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        defaultChannels.remove(event.getPlayer().getName());
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (!channel.equals(PMC_NAME)) {
            return;
        }

        isBungeeMode = true;

        try (
                ByteArrayInputStream byteIn = new ByteArrayInputStream(message);
                DataInputStream dataIn = new DataInputStream(byteIn);
        ) {
            String signature = dataIn.readUTF();
            String bound = dataIn.readUTF();
            if (bound.equals("bungeebound")) {
                return;
            }
            switch (signature) {
                case "default_channel_put" -> {
                    String playerName = dataIn.readUTF();
                    String ch = dataIn.readUTF();
                    defaultChannels.put(playerName, ch);
                }
                case "default_channel_clear" -> defaultChannels.clear();
                case "default_channel_remove" -> {
                    String playerName = dataIn.readUTF();
                    String ch = dataIn.readUTF();
                    defaultChannels.remove(playerName, ch);
                }
            }
        } catch (IOException ignored) {
        }
    }

    private void scheduleGettingDefaultChannelTask(String playerName) {
        runTaskLater(() -> sendDefaultChannelGet(playerName));
    }

    private void runTaskLater(@NotNull Runnable task) {
        if (FOLIA) {
            getServer().getGlobalRegionScheduler().runDelayed(this, $ -> task.run(), 20L);
        } else {
            getServer().getScheduler().runTaskLater(this, task, 20L);
        }
    }

    private void sendStrings(String... strings) {
        List<? extends Player> online = getServer().getOnlinePlayers().stream()
                .sorted(Comparator.comparing(Player::getName))
                .toList();
        if (online.isEmpty()) {
            return;
        }
        try (
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                DataOutputStream dataOut = new DataOutputStream(byteOut);
        ) {
            for (String data : strings) {
                dataOut.writeUTF(data);
            }
            online.get(new Random().nextInt(0, online.size()))
                    .sendPluginMessage(this, PMC_NAME, byteOut.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendDefaultChannelGet(String playerName) {
        sendStrings("default_channel_get", "bungeebound", playerName);
    }

    public String getDefaultChannel(String playerName) {
        if (isBungeeMode) {
            String defaultChannelName = defaultChannels.get(playerName);
            if (defaultChannelName != null) {
                return defaultChannelName;
            } else {
                sendDefaultChannelGet(playerName);
                return "null";
            }
        } else {
            // Try LunaChat spigot
            if (Bukkit.getPluginManager().getPlugin("LunaChat") != null) {
                return Optional.ofNullable(LunaChat.getAPI().getDefaultChannel(playerName))
                        .map(Channel::getName)
                        .orElse("null");
            } else {
                return "null";
            }
        }
    }
}
