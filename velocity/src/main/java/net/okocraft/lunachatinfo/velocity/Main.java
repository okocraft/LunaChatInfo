package net.okocraft.lunachatinfo.velocity;

import com.github.ucchyocean.lc3.PluginInterface;
import com.github.ucchyocean.lc3.channel.ChannelManager;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.nio.file.Path;

public class Main {

    public static final String PMC_NAME = "lunachat:info";


    public final ProxyServer server;
    public final Logger logger;
    public final Path dataDirectory;

    private DelegateChannelManager modifiedLunaChatAPI;

    @Inject
    public Main(@NotNull ProxyServer server, @NotNull Logger logger,
                @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe(priority = Short.MIN_VALUE)
    public void onEnable(ProxyInitializeEvent ignored) {
        Object plugin = this.server.getPluginManager().getPlugin("lunachat").flatMap(PluginContainer::getInstance).orElse(null);
        if (plugin == null) {
            throw new IllegalStateException("LunaChat is not found.");
        }

        if (!(plugin instanceof PluginInterface pluginInterface)) {
            throw new IllegalStateException("LunaChat does not implement PluginInterface.");
        }

        if (pluginInterface.getLunaChatAPI() == null) {
            throw new IllegalStateException("LunaChatAPI is null. LunaChat may not be loaded correctly...?");
        }

        ChannelManager lunaChatAPI = (ChannelManager) pluginInterface.getLunaChatAPI();

        ChannelIdentifier identifier = MinecraftChannelIdentifier.from(PMC_NAME);
        this.server.getChannelRegistrar().register(identifier);
        this.modifiedLunaChatAPI = new DelegateChannelManager(identifier, this.server, lunaChatAPI);
        setChannelManager(pluginInterface, this.modifiedLunaChatAPI);
        this.server.getEventManager().register(this, new PluginMessageListener(identifier, this.modifiedLunaChatAPI));
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent ignored) {
        this.server.getEventManager().unregisterListeners(this);
        this.server.getChannelRegistrar().unregister(MinecraftChannelIdentifier.from(PMC_NAME));
        if (this.server.getPluginManager().getPlugin("lunachat").orElse(null) instanceof PluginInterface pluginInterface && this.modifiedLunaChatAPI != null) {
            setChannelManager(pluginInterface, this.modifiedLunaChatAPI.getDelegate());
            this.modifiedLunaChatAPI.uninjectDefaultChannels();
        }
    }

    private static void setChannelManager(PluginInterface target, ChannelManager channelManager) {
        try {
            Field managerField = Class.forName("com.github.ucchyocean.lc3.LunaChatVelocity").getDeclaredField("manager");
            managerField.setAccessible(true);
            managerField.set(target, channelManager);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}
