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

public class BloodmoonStop {

    @CommandPermission("bloodmoon.admin.stop")
    @Description("Stops the bloodmoon")
    @Command("bloodmoon stop")
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
            actor.reply("&cTarget must be a overworld.");
            return;
        }
        if (Bloodmoon.GetInstance().getConfigReader(world).GetPermanentBloodMoonConfig())
        {
            actor.reply(localeReader.GetLocaleString("CannotStopBloodMoon"));
            return;
        }

        PeriodicNightCheck nightCheck = PeriodicNightCheck.GetPeriodicNightCheck(world);
        if (nightCheck == null)
        {
            actor.reply(localeReader.GetLocaleString("NoBloodMoonInWorld"));
            return;
        }

        if (!BloodmoonManager.GetActuator(world).isInProgress())
        {
            actor.reply("&cA bloodmoon is not in progress in this world.");
            return;
        }
        nightCheck.SetCheckAfter(0);
        nightCheck.SetDaysRemaining(nightCheck.GetBloodMoonInterval() - 1);
        world.setTime(0);
    }
}
