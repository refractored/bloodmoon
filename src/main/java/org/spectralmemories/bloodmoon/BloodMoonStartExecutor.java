package org.spectralmemories.bloodmoon;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BloodMoonStartExecutor implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (sender instanceof Player)
        {
            Player playerSender;
            playerSender = (Player) sender;

            World world = playerSender.getWorld();
            PeriodicNightCheck.GetPeriodicNightCheck(world).SetDaysRemaining(0);
            world.setTime(12000);
            return true;
        }
        return false;
    }
}
