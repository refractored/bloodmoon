package net.refractored.bloodmoon.commands;

import revxrsal.commands.bukkit.BukkitCommandHandler;

public class
RegisterCommands {
    public static void register(BukkitCommandHandler handler) {
        handler.register(new BloodmoonStart());
        handler.register(new BloodmoonStop());
        handler.register(new BloodmoonShow());
        handler.register(new BloodmoonSpawnHorde());
    }
}
