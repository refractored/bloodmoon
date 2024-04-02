package net.refractored.bloodmoon.commands;

import net.refractored.bloodmoon.Bloodmoon;
import net.refractored.bloodmoon.PeriodicNightCheck;
import net.refractored.bloodmoon.managers.BloodmoonManager;
import net.refractored.bloodmoon.readers.LocaleReader;
import org.bukkit.World;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class BloodmoonLevelAdd {
    @CommandPermission("bloodmoon.admin.level.add")
    @Description("Adds bloodmoon levels")
    @Command("bloodmoon level add")
    public void BloodmoonLevelAdd(BukkitCommandActor actor, @Default("1") @Optional @Range(min=1, max=2) int targetlevel, @Optional World targetworld) {
        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        World world;
        if (targetworld == null) {
            if (actor.isConsole()) {
                actor.reply("&cYou must specify a world.");
                return;
            }
            world = actor.getAsPlayer().getWorld();
        } else {
            world = targetworld;
        }
        if (world.getEnvironment() != World.Environment.NORMAL) {
            actor.reply(String.format("World \"%s\" is not a overworld.", world.getName()));
            return;
        }

        if (!Bloodmoon.GetInstance().getConfigReader(world).GetBloodMoonLevelsEnabledConfig()) {
            actor.reply(String.format("&cBloodmoon levels are disabled in world \"%s\".", world.getName()));
            return;
        }

        if (Bloodmoon.GetInstance().getConfigReader(world).GetPermanentBloodMoonConfig()) {
            actor.reply(localeReader.GetLocaleString("WorldIsPermanentBloodMoon"));
            return;
        }

        PeriodicNightCheck nightCheck = PeriodicNightCheck.GetPeriodicNightCheck(world);
        if (nightCheck == null) {
            actor.reply(String.format("&cBloodmoons are disabled in world \"%s\".", world.getName()));
            return;
        }

        if (BloodmoonManager.GetActuator(world).isInProgress()) {
            actor.reply(String.format("&cA Bloodmoon is already in progress in world \"%s\"", world.getName()));
            return;
        }
        int finalLevel = BloodmoonManager.GetActuator(world).getBloodMoonLevel() + targetlevel;
        if (finalLevel > 3) {
            actor.reply(String.format("&cBloodmoon level cannot be more than 3 in world \"%s\"", world.getName()));
            return;
        }
        BloodmoonManager.GetActuator(world).setBloodMoonLevel(finalLevel);
        actor.reply(String.format("&aAdded %d level(s) from the bloodmoom level. Bloodmoon level is now %d in world \"%s\"", targetlevel, finalLevel, world.getName()));
    }
}
