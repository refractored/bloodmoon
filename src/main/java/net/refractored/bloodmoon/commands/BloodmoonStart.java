package net.refractored.bloodmoon.commands;

import com.sk89q.worldedit.extension.platform.Actor;
import net.refractored.bloodmoon.Bloodmoon;
import net.refractored.bloodmoon.LocaleReader;
import net.refractored.bloodmoon.PeriodicNightCheck;
import org.bukkit.World;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class BloodmoonStart {
    @CommandPermission("bloodmoon.admin.start")
    @Description("Starts the bloodmoon")
    @Command("bloodmoon start")
    public void bloodmoonStart(BukkitCommandActor actor) {

        Player player = actor.getAsPlayer();
        World playerWorld = player.getWorld();
        if (Bloodmoon.GetInstance().getConfigReader(playerWorld).GetPermanentBloodMoonConfig()) {
            LocaleReader.MessageLocale("WorldIsPermanentBloodMoon", null, null, player);
            return;
        }

        PeriodicNightCheck nightCheck = PeriodicNightCheck.GetPeriodicNightCheck(playerWorld);
        if (nightCheck == null) {
            LocaleReader.MessageLocale("NoBloodMoonInWorld", null, null, player);
            return;
        }
        nightCheck.SetCheckAfter(0);
        nightCheck.SetDaysRemaining(0);
        playerWorld.setTime(12001);
        return;
    }
}
