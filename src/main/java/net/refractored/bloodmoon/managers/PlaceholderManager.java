package net.refractored.bloodmoon.managers;

import net.refractored.bloodmoon.Bloodmoon;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderManager extends PlaceholderExpansion {

    private Bloodmoon plugin;

    public PlaceholderManager(Bloodmoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "Bloodmoon";
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
        if (params.startsWith("inprogress_")) {
            String worldName = params.replace("inprogress_", "");
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                boolean isInProgress = BloodmoonManager.GetActuator(world).isInProgress();
                return String.valueOf(isInProgress);
            } else {
                return "false";
            }
        }
        if (params.startsWith("level_")) {
            String worldName = params.replace("level_", "");
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                int level = BloodmoonManager.GetActuator(world).getBloodMoonLevel();
                return String.valueOf(level);
            } else {
                return "Unknown World";
            }
        }
        if (params.startsWith("days_")) {
            String worldName = params.replace("days_", "");
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                int days = BloodmoonManager.GetActuator(world).getBloodMoonDays();
                return String.valueOf(days);
            } else {
                return "Unknown World";
            }
        }
        return params;
    }
}