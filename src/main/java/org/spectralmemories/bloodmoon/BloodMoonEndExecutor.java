package org.spectralmemories.bloodmoon;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BloodMoonEndExecutor implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (sender instanceof Player)
        {
            Player playerSender;
            playerSender = (Player) sender;
            LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();

            World world = playerSender.getWorld();
            PeriodicNightCheck nightCheck = PeriodicNightCheck.GetPeriodicNightCheck(world);
            if (nightCheck == null)
            {
                playerSender.sendMessage(localeReader.GetLocaleString("NoBloodMoonInWorld"));
                return true;
            }

            nightCheck.SetCheckAfter(0);
            nightCheck.SetDaysRemaining(nightCheck.GetBloodMoonInterval() - 1);
            world.setTime(0);
            return true;
        }
        return false;
    }
}
