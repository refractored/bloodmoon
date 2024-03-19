package net.refractored.bloodmoon.commands;

import net.refractored.bloodmoon.Bloodmoon;
import net.refractored.bloodmoon.BloodmoonActuator;
import net.refractored.bloodmoon.PeriodicNightCheck;
import net.refractored.bloodmoon.readers.ConfigReader;
import net.refractored.bloodmoon.readers.LocaleReader;
import org.bukkit.World;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.awt.*;
import java.lang.annotation.Target;
import java.util.ArrayList;

public class BloodmoonSpawnHorde {
    @CommandPermission("bloodmoon.command.spawnhorde")
    @Description("Check when the next bloodmoon is")
    @Command("bloodmoon spawnhorde")
    public void bloodmoonSpawnHorde(BukkitCommandActor actor, @Optional Player target) {
        if (target == null) {
            BloodmoonActuator.GetActuator(actor.getAsPlayer().getWorld()).SpawnHorde();
            actor.reply("&cSpawned a horde in your world on a random player.");
            actor.reply("&7&oIf no players are online, unvanished or in survival mode then no horde will spawn.");
            return;
        }
        Player player = actor.getAsPlayer();
        BloodmoonActuator actuator = BloodmoonActuator.GetActuator(target.getWorld());
        if (actuator == null) {
            LocaleReader.MessageLocale("NoBloodMoonInWorld", null, null, player);
            return;
        }
        actuator.SpawnHorde(target);
    }
}