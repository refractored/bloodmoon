package net.refractored.bloodmoon.commands;

import net.refractored.bloodmoon.Bloodmoon;
import net.refractored.bloodmoon.managers.BloodmoonManager;
import net.refractored.bloodmoon.readers.LocaleReader;
import org.bukkit.World;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class BloodmoonSpawnHorde {
    @CommandPermission("bloodmoon.command.spawnhorde")
    @Description("Check when the next bloodmoon is")
    @Command("bloodmoon spawn horde")
    public void bloodmoonSpawnHorde(BukkitCommandActor actor, @Optional Player target) {
        boolean targetBlank = false;
        if (target == null) {
            if (actor.isConsole()){
                actor.reply("&cYou must specify a player to spawn a horde on.");
                return;
            }
            targetBlank = true;
            target = actor.getAsPlayer();
        }
        BloodmoonManager actuator = BloodmoonManager.GetActuator(target.getWorld());
        if (target.getWorld().getEnvironment() != World.Environment.NORMAL) {
            actor.reply(String.format("&cPlayer \"%s\" is in world \"%s\", which is not a overworld. ", target.getName(), target.getWorld().getName()));
            return;
        }
        if (actuator == null) {
            actor.reply(String.format("&cPlayer \"%s\" is in world \"%s\", which has bloodmoons disabled.", target.getName(), target.getWorld().getName()));
            return;
        }
        if (targetBlank){
            actuator.SpawnHorde();
            actor.reply("&cSpawned a horde in your world on a random player.");
            actor.reply("&7&oIf no players are in your world, unvanished or in survival mode then no horde will spawn.");
            return;
        }
        actor.reply(String.format("&aSpawned horde on \"%s\" which is in world \"%s\"", target.getName(), target.getWorld().getName()));
        actuator.SpawnHorde(target);
    }
}