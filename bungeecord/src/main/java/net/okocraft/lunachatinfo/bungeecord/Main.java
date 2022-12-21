package net.okocraft.lunachatinfo.bungeecord;

import com.github.ucchyocean.lc3.LunaChatAPI;
import com.github.ucchyocean.lc3.LunaChatBungee;
import com.github.ucchyocean.lc3.channel.ChannelManager;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class Main extends Plugin implements Listener {

    public static final String PMC_NAME = "lunachat:info";

    private DelegateChannelManager modifiedLunaChatAPI;

    @Override
    public void onEnable() {
        getProxy().registerChannel(PMC_NAME);
        modifiedLunaChatAPI = new DelegateChannelManager((ChannelManager) LunaChatBungee.getInstance().getLunaChatAPI());
        setChannelManager(modifiedLunaChatAPI);
        modifiedLunaChatAPI.setDefaultChannel("lazy_gon", "lazy");
        getProxy().getPluginManager().registerListener(this, this);
    }

    @Override
    public void onDisable() {
        getProxy().getPluginManager().unregisterListeners(this);
        setChannelManager(modifiedLunaChatAPI.getDelegate());
        modifiedLunaChatAPI.uninjectDefaultChannels();
        getProxy().unregisterChannel(PMC_NAME);
    }

    private void setChannelManager(ChannelManager channelManager) {
        try {
            Field managerField = LunaChatBungee.class.getDeclaredField("manager");
            managerField.setAccessible(true);
            managerField.set(LunaChatBungee.getInstance(), channelManager);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getTag().equals(PMC_NAME)) {
            return;
        }

        try (
                ByteArrayInputStream byteIn = new ByteArrayInputStream(event.getData());
                DataInputStream dataIn = new DataInputStream(byteIn);
        ) {
            String signature = dataIn.readUTF();
            if (dataIn.readUTF().equals("serverbound")) {
                return;
            }
            if (signature.equals("default_channel_get")) {
                String playerName = dataIn.readUTF();
                LunaChatAPI api = LunaChatBungee.getInstance().getLunaChatAPI();
                api.setDefaultChannel(playerName, api.getDefaultChannel(playerName).getName());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
