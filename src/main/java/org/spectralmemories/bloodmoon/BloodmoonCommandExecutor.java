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
            Player playerSender;
            playerSender = (Player) sender;
            World playerWorld = playerSender.getWorld();

            if (BloodmoonActuator.GetActuator(playerWorld).isInProgress())
            {
                playerSender.sendMessage(ChatColor.RED + "BloodMoon is taking place right now");
                return true;
            }

            int remainingDays = PeriodicNightCheck.GetDaysRemaining(playerWorld);
            playerSender.sendMessage("There is " + remainingDays + " days until the next " + ChatColor.DARK_RED + "BloodMoon");
            return true;
        }
        return false;
    }
}
