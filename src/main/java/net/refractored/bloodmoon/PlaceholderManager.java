package net.refractored.bloodmoon;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public class PlaceholderManager extends PlaceholderExpansion {
    private Bloodmoon plugin;

    public PlaceholderManager(Bloodmoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "bloodmoon";
    }

    @Override
    public @NotNull String getAuthor() {
        return "refractored";
    }

    @Override
    public @NotNull String getVersion() {
        return "0.8.2";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if(params.equalsIgnoreCase("status")) {

        }
    }
}