package net.refractored.bloodmoon.commands;

import net.refractored.bloodmoon.Bloodmoon;
import net.refractored.bloodmoon.readers.LocaleReader;
import net.refractored.bloodmoon.PeriodicNightCheck;
import org.bukkit.World;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class BloodmoonStop {

    @CommandPermission("bloodmoon.admin.stop")
    @Description("Starts the bloodmoon")
    @Command("bloodmoon stop")
    public void bloodmoonStart(BukkitCommandActor actor) {

        Player player = actor.getAsPlayer();
        World playerWorld = player.getWorld();
        if (Bloodmoon.GetInstance().getConfigReader(playerWorld).GetPermanentBloodMoonConfig())
        {
            LocaleReader.MessageLocale("CannotStopBloodMoon", null, null, player);
            return;
        }

        PeriodicNightCheck nightCheck = PeriodicNightCheck.GetPeriodicNightCheck(playerWorld);
        if (nightCheck == null)
        {
            LocaleReader.MessageLocale("NoBloodMoonInWorld", null, null, player);
            return;
        }

        nightCheck.SetCheckAfter(0);
        nightCheck.SetDaysRemaining(nightCheck.GetBloodMoonInterval() - 1);
        playerWorld.setTime(0);
        return;
    }
}
