package net.okocraft.lunachatinfo.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PluginMessageListener {

    private final ChannelIdentifier identifier;
    private final DelegateChannelManager manager;

    public PluginMessageListener(ChannelIdentifier identifier, DelegateChannelManager manager) {
        this.identifier = identifier;
        this.manager = manager;
    }

    @Subscribe
    public void onPluginMessageReceived(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(this.identifier)) {
            return;
        }

        event.setResult(PluginMessageEvent.ForwardResult.handled());

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
                this.manager.setDefaultChannel(playerName, this.manager.getDefaultChannel(playerName).getName());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
