package net.okocraft.lunachatinfo.bungeecord;

import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.channel.ChannelManager;
import com.github.ucchyocean.lc3.japanize.JapanizeType;
import com.github.ucchyocean.lc3.member.ChannelMember;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.api.ProxyServer;

/**
 * Delegate class to prevent reload command from changing map instance.
 */
public class DelegateChannelManager extends ChannelManager {

    private static final Field DEFAULT_CHANNELS_FIELD;
    private static final Method SAVE_ALL_CHANNELS_METHOD;
    static {
        try {
            DEFAULT_CHANNELS_FIELD = ChannelManager.class.getDeclaredField("defaultChannels");
            DEFAULT_CHANNELS_FIELD.setAccessible(true);
            SAVE_ALL_CHANNELS_METHOD = ChannelManager.class.getDeclaredMethod("saveAllChannels");
            SAVE_ALL_CHANNELS_METHOD.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final ChannelManager delegate;

    ChannelManager getDelegate() {
        return this.delegate;
    }

    public DelegateChannelManager(ChannelManager delegate) {
        super(); // <- do reloadAllData with null delegate instance. sad.
        this.delegate = delegate;
        reloadAllData();
    }

    private HashMap<String, String> createModifiedMap(Map<String, String> origin) {
        return new HashMap<>(origin) {
            @Override
            public String put(String key, String value) {
                String old = super.put(key, value);
                if (old != null && !old.equals(value)) {
                    sendDefaultChannelRemove(key, old);
                }
                sendDefaultChannelPut(key, value);
                return old;
            }

            @Override
            public boolean remove(Object key, Object value) {
                boolean removed = super.remove(key, value);
                if (removed) {
                    sendDefaultChannelRemove((String) key, (String) value);
                }
                return removed;
            }

            @Override
            public void clear() {
                sendDefaultChannelClear();
                super.clear();
            }
        };
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, String> getDefaultChannelsMap() {
        try {
            return (HashMap<String, String>) DEFAULT_CHANNELS_FIELD.get(delegate);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    void uninjectDefaultChannels() {
        injectDefaultChannels(new HashMap<>(getDefaultChannelsMap()));
    }

    private void injectDefaultChannels(HashMap<String, String> toInject) {
        try {
            DEFAULT_CHANNELS_FIELD.set(delegate, toInject);
        } catch (IllegalAccessException ignored) {
        }
    }

    private void sendStrings(String... strings) {
        try (
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                DataOutputStream dataOut = new DataOutputStream(byteOut);
        ) {
            for (String data : strings) {
                dataOut.writeUTF(data);
            }
            ProxyServer.getInstance().getServers().forEach(
                    (serverName, serverInfo) -> serverInfo.sendData(Main.PMC_NAME, byteOut.toByteArray(), false)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendDefaultChannelPut(String playerName, String newChannel) {
        sendStrings("default_channel_put", "serverbound", playerName, newChannel);
    }

    private void sendDefaultChannelClear() {
        sendStrings("default_channel_clear", "serverbound");
    }

    private void sendDefaultChannelRemove(String player, String oldChannel) {
        sendStrings("default_channel_remove", "serverbound", player, oldChannel);
    }

    @Override
    public void reloadAllData() {
        if (delegate != null) {
            delegate.reloadAllData();
            injectDefaultChannels(createModifiedMap(getDefaultChannelsMap()));
        }
    }

    @Override
    public void removeAllDefaultChannels() {
        delegate.removeAllDefaultChannels();
    }

    @Override
    public boolean isPlayerJapanize(String playerName) {
        return delegate.isPlayerJapanize(playerName);
    }

    @Override
    public boolean isExistChannel(String channelName) {
        return delegate.isExistChannel(channelName);
    }

    @Override
    public Collection<Channel> getChannels() {
        return delegate.getChannels();
    }

    @Override
    public Collection<Channel> getChannelsByPlayer(String playerName) {
        return delegate.getChannelsByPlayer(playerName);
    }

    @Override
    public Channel getDefaultChannel(String playerName) {
        return delegate.getDefaultChannel(playerName);
    }

    @Override
    public void setDefaultChannel(String playerName, String channelName) {
        delegate.setDefaultChannel(playerName, channelName);
    }

    @Override
    public void removeDefaultChannel(String playerName) {
        delegate.removeDefaultChannel(playerName);
    }

    @Override
    public Channel getChannel(String channelName) {
        return delegate.getChannel(channelName);
    }

    @Override
    public Channel createChannel(String channelName) {
        return delegate.createChannel(channelName);
    }

    @Override
    public Channel createChannel(String channelName, ChannelMember member) {
        return delegate.createChannel(channelName, member);
    }

    @Override
    public boolean removeChannel(String channelName) {
        return delegate.removeChannel(channelName);
    }

    @Override
    public boolean removeChannel(String channelName, ChannelMember member) {
        return delegate.removeChannel(channelName, member);
    }

    @Override
    public String getTemplate(String id) {
        return delegate.getTemplate(id);
    }

    @Override
    public void setTemplate(String id, String template) {
        delegate.setTemplate(id, template);
    }

    @Override
    public void removeTemplate(String id) {
        delegate.removeTemplate(id);
    }

    @Override
    public HashMap<String, String> getAllDictionary() {
        return delegate.getAllDictionary();
    }

    @Override
    public void setDictionary(String key, String value) {
        delegate.setDictionary(key, value);
    }

    @Override
    public void removeDictionary(String key) {
        delegate.removeDictionary(key);
    }

    @Override
    public List<ChannelMember> getHidelist(ChannelMember key) {
        return delegate.getHidelist(key);
    }

    @Override
    public ArrayList<ChannelMember> getHideinfo(ChannelMember player) {
        return delegate.getHideinfo(player);
    }

    @Override
    public void addHidelist(ChannelMember player, ChannelMember hided) {
        delegate.addHidelist(player, hided);
    }

    @Override
    public void removeHidelist(ChannelMember player, ChannelMember hided) {
        delegate.removeHidelist(player, hided);
    }

    @Override
    public String japanize(String message, JapanizeType type) {
        return delegate.japanize(message, type);
    }

    @Override
    public void setPlayersJapanize(String playerName, boolean doJapanize) {
        delegate.setPlayersJapanize(playerName, doJapanize);
    }

    @Override
    protected void saveAllChannels() {
        try {
            SAVE_ALL_CHANNELS_METHOD.invoke(delegate);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
