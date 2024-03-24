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
    public void bloodmoonStart(BukkitCommandActor actor) {
        Player player = actor.getAsPlayer();
        World playerWorld = player.getWorld();
        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(playerWorld);

        if (playerWorld.getEnvironment() != World.Environment.NORMAL) {
            actor.reply("&cThis command can only be used in the overworld.");
            return;
        }

        if (configReader.GetPermanentBloodMoonConfig())
        {
            actor.reply(localeReader.GetLocaleString("WorldIsPermanentBloodMoon"));
            return;
        }


        BloodmoonManager worldActuator = BloodmoonManager.GetActuator(playerWorld);

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

        int remainingDays = PeriodicNightCheck.GetDaysRemaining(playerWorld);
        if (remainingDays < 0)
        {
            System.out.println("[Error] remainingDays is lower than 0. Please regenerate both the bloodmoon cache and the config for world " + playerWorld.getName());
            actor.reply(localeReader.GetLocaleString("GeneralError"));
        }
        else
        {
            LocaleReader.MessageLocale("DaysBeforeBloodMoon", new String[]{"$d"}, new String[]{String.valueOf(remainingDays)}, player);
        }

        return;
    }
}
