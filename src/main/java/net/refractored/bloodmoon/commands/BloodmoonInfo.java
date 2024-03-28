package net.refractored.bloodmoon.commands;

import net.refractored.bloodmoon.Bloodmoon;
import net.refractored.bloodmoon.managers.BloodmoonManager;
import net.refractored.bloodmoon.PeriodicNightCheck;
import net.refractored.bloodmoon.readers.ConfigReader;
import net.refractored.bloodmoon.readers.LocaleReader;
import org.bukkit.World;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class BloodmoonInfo {
    @CommandPermission("bloodmoon.info")
    @Description("Check when the next bloodmoon is")
    @Command("bloodmoon info")
    public void bloodmoonInfo(BukkitCommandActor actor) {
        World world = actor.getAsPlayer().getWorld();
        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
        if (world.getEnvironment() != World.Environment.NORMAL) {
            actor.reply("&cThis command can only be used in the overworld.");
            return;
        }

        if (configReader.GetPermanentBloodMoonConfig())
        {
            actor.reply(localeReader.GetLocaleString("WorldIsPermanentBloodMoon"));
            return;
        }

        BloodmoonManager worldActuator = BloodmoonManager.GetActuator(world);

        if (worldActuator == null)
        {
            actor.reply(localeReader.GetLocaleString("NoBloodMoonInWorld"));
            return;
        }

        if (BloodmoonManager.isInProgress())
        {
            actor.reply(localeReader.GetLocaleString("BloodMoonRightNow"));
            return;
        }

        int remainingDays = PeriodicNightCheck.GetDaysRemaining(world);

        if (remainingDays < 0) {
            System.out.println("[Error] remainingDays is lower than 0. Please regenerate both the bloodmoon cache and the config for world " + world.getName());
            actor.reply(localeReader.GetLocaleString("GeneralError"));
            return;
        }

        actor.reply(String.format("&aThere are %d days remaining until the bloodmoon in world \"%s\".", remainingDays, world.getName()));
        actor.reply(String.format("&aThe bloodmoon is level %d", worldActuator.getBloodMoonLevel()));


    }
}
