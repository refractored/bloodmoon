package net.refractored.bloodmoon.commands;

import net.refractored.bloodmoon.Bloodmoon;
import net.refractored.bloodmoon.PeriodicNightCheck;
import net.refractored.bloodmoon.managers.BloodmoonManager;
import net.refractored.bloodmoon.readers.LocaleReader;
import org.bukkit.World;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class BloodmoonLevelSet {
    @CommandPermission("bloodmoon.admin.level.set")
    @Description("Changes the bloodmoon level")
    @Command("bloodmoon level set")
    public void BloodmoonLevelSet(BukkitCommandActor actor, @Range(min=1, max=3) int targetlevel, @Optional World targetworld) {
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

        if (Bloodmoon.GetInstance().getConfigReader(world).GetBloodMoonLevelsEnabledConfig()) {
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
        BloodmoonManager.GetActuator(world).setBloodMoonLevel(targetlevel);
        actor.reply(String.format("&aSet bloodmoom level to %d in world \"%s\"", targetlevel, world.getName()));
    }
}
