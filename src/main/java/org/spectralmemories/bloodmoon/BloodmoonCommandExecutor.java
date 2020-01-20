package org.spectralmemories.bloodmoon;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BloodmoonCommandExecutor implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (sender instanceof Player)
        {
            LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
            Player playerSender;
            playerSender = (Player) sender;
            World playerWorld = playerSender.getWorld();

            BloodmoonActuator worldActuator = BloodmoonActuator.GetActuator(playerWorld);

            if (worldActuator == null)
            {
                playerSender.sendMessage(localeReader.GetLocaleString("NoBloodMoonInWorld"));
                return true;
            }

            if (worldActuator.isInProgress())
            {
                playerSender.sendMessage(ChatColor.RED + localeReader.GetLocaleString("BloodMoonRightNow"));
                return true;
            }

            int remainingDays = PeriodicNightCheck.GetDaysRemaining(playerWorld);
            String remainingDaysString = localeReader.GetLocaleString("DaysBeforeBloodMoon");

            playerSender.sendMessage(remainingDaysString.replace("$d", String.valueOf(remainingDays)));

            return true;
        }
        return false;
    }
}
