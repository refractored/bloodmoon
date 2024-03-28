package net.refractored.bloodmoon.commands;

import net.refractored.bloodmoon.Bloodmoon;
import net.refractored.bloodmoon.managers.BloodmoonManager;
import net.refractored.bloodmoon.readers.LocaleReader;
import net.refractored.bloodmoon.PeriodicNightCheck;
import org.bukkit.World;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import static net.refractored.bloodmoon.Bloodmoon.localeReader;

public class BloodmoonStart {
    @CommandPermission("bloodmoon.admin.start")
    @Description("Starts the bloodmoon")
    @Command("bloodmoon start")
    public void bloodmoonStart(BukkitCommandActor actor, @Optional World targetworld) {
        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        World world;
        if (targetworld == null) {
            if (actor.isConsole()){
                actor.reply("&cYou must specify a world.");
                return;
            }
            world = actor.getAsPlayer().getWorld();
        } else {
            world = targetworld;
        }
        if (world.getEnvironment() != World.Environment.NORMAL) {
            actor.reply(String.format("World \"%s\" is not a overworld.",world.getName()));
            return;
        }
        if (Bloodmoon.GetInstance().getConfigReader(world).GetPermanentBloodMoonConfig()) {
            actor.reply(localeReader.GetLocaleString("WorldIsPermanentBloodMoon"));
            return;
        }

        PeriodicNightCheck nightCheck = PeriodicNightCheck.GetPeriodicNightCheck(world);
        if (nightCheck == null) {
            actor.reply(String.format("&cBloodmoons are disabled in world \"%s\".",world.getName()));
            return;
        }

        if (BloodmoonManager.GetActuator(world).isInProgress())
        {
            actor.reply(String.format("&cA Bloodmoon is already in progress in world \"%s\"",world.getName()));
            return;
        }
        nightCheck.SetCheckAfter(0);
        nightCheck.SetDaysRemaining(0);
        world.setTime(12001);
        actor.reply(String.format("&aStarted the bloodmoon in world \"%s\".",world.getName()));
    }
}
