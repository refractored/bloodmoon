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

        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(playerWorld);

        if (configReader.GetPermanentBloodMoonConfig())
        {
            LocaleReader.MessageLocale("WorldIsPermanentBloodMoon", null, null, player);
            return;
        }


        BloodmoonManager worldActuator = BloodmoonManager.GetActuator(playerWorld);

        if (worldActuator == null)
        {
            LocaleReader.MessageLocale("NoBloodMoonInWorld", null, null, player);
            return;
        }

        if (worldActuator.isInProgress())
        {
            LocaleReader.MessageLocale("BloodMoonRightNow", null, null, player);
            return;
        }

        int remainingDays = PeriodicNightCheck.GetDaysRemaining(playerWorld);
        if (remainingDays < 0)
        {
            System.out.println("[Error] remainingDays was inferior to 0. Please regenerate both the cache and the config for world " + playerWorld.getName());
            LocaleReader.MessageLocale("GeneralError", null, null, player);
        }
        else
        {
            LocaleReader.MessageLocale("DaysBeforeBloodMoon", new String[]{"$d"}, new String[]{String.valueOf(remainingDays)}, player);
        }

        return;
    }
}
