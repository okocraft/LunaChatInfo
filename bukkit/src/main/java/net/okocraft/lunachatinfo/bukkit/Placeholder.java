package net.okocraft.lunachatinfo.bukkit;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Placeholder extends PlaceholderExpansion {

    private final Main plugin;

    public Placeholder(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getRequiredPlugin() {
        return plugin.getName();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] args = params.split("_", -1);
        if (args.length == 1) {
            return plugin.getDefaultChannel(player.getName());
        }

        if (args[0].equals("defaultchannel")) {
            if (args.length == 2) {
                return plugin.getDefaultChannel(player.getName());
            } else {
                return plugin.getDefaultChannel(args[2]);
            }
        }

        return "null";
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        return onRequest(player, params);
    }

    @Override
    public @NotNull String getIdentifier() {
        return "lunachatinfo";
    }

    @Override
    public @NotNull String getAuthor() {
        return "lazy_gon";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }
}
