package org.spectralmemories.bloodmoon;

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
            Player player = (Player) sender;
            World playerWorld = player.getWorld();

            LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
            ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(playerWorld);

            if (args.length == 0)
            {
                player.sendMessage(localeReader.GetLocaleString("AllowedCommands").replace("$d", "show, start, stop, reload"));
                return false;
            }

            String arg0 = args[0];
            if (arg0.equalsIgnoreCase("start"))
            {
                return ExecuteStart(player);
            }
            else if (arg0.equalsIgnoreCase("stop"))
            {
                return ExecuteStop(player);
            }
            else if (arg0.equalsIgnoreCase("reload"))
            {
                return ExecuteReload(player);
            }
            else if (arg0.equalsIgnoreCase("show"))
            {
                return ExecuteShow(player);
            }
            else if (arg0.equalsIgnoreCase("end"))
            {
                player.sendMessage("Command end is deprecated. Please use 'stop'");
                return ExecuteStop(player);
            }
            else
            {
                player.sendMessage(localeReader.GetLocaleString("CommandNotFound").replace("$d", arg0));
                return false;
            }
        }
        else
        {
            //From console
            System.out.println("[TODO] No console command yet. Stay tuned!");
            return true;
        }
    }

    private boolean ExecuteShow (Player playerSender)
    {
        World playerWorld = playerSender.getWorld();

        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();

        if (! CheckPermission(playerSender, "show"))
        {
            playerSender.sendMessage("NoPermission");
            return false;
        }

        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(playerWorld);

        if (configReader.GetPermanentBloodMoonConfig())
        {
            playerSender.sendMessage(localeReader.GetLocaleString("WorldIsPermanentBloodMoon"));
            return true;
        }


        BloodmoonActuator worldActuator = BloodmoonActuator.GetActuator(playerWorld);

        if (worldActuator == null)
        {
            playerSender.sendMessage(localeReader.GetLocaleString("NoBloodMoonInWorld"));
            return true;
        }

        if (worldActuator.isInProgress())
        {
            playerSender.sendMessage(localeReader.GetLocaleString("BloodMoonRightNow"));
            return true;
        }

        int remainingDays = PeriodicNightCheck.GetDaysRemaining(playerWorld);
        String remainingDaysString;
        if (remainingDays < 0)
        {
            System.out.println("[Error] remainingDays was inferior to 0. Please regenerate both the cache and the config for world " + playerWorld.getName());
            remainingDaysString = localeReader.GetLocaleString("GeneralError");
        }
        else
        {
            remainingDaysString = localeReader.GetLocaleString("DaysBeforeBloodMoon");
        }

        playerSender.sendMessage(remainingDaysString.replace("$d", String.valueOf(remainingDays)));
        return true;
    }

    private boolean ExecuteStart (Player playerSender)
    {
        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();



        if (! CheckPermission(playerSender, "start"))
        {
            playerSender.sendMessage("NoPermission");
            return false;
        }

        World world = playerSender.getWorld();

        if (Bloodmoon.GetInstance().getConfigReader(world).GetPermanentBloodMoonConfig())
        {
            playerSender.sendMessage(localeReader.GetLocaleString("WorldIsPermanentBloodMoon"));
            return true;
        }

        PeriodicNightCheck nightCheck = PeriodicNightCheck.GetPeriodicNightCheck(world);
        if (nightCheck == null)
        {
            playerSender.sendMessage(localeReader.GetLocaleString("NoBloodMoonInWorld"));
            return true;
        }
        nightCheck.SetCheckAfter(0);
        nightCheck.SetDaysRemaining(0);
        world.setTime(12001);
        return true;
    }

    private boolean ExecuteStop (Player playerSender)
    {
        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();

        if (! CheckPermission(playerSender, "stop"))
        {
            playerSender.sendMessage("NoPermission");
            return false;
        }

        World world = playerSender.getWorld();

        if (Bloodmoon.GetInstance().getConfigReader(world).GetPermanentBloodMoonConfig())
        {
            playerSender.sendMessage(localeReader.GetLocaleString("CannotStopBloodMoon"));
            return true;
        }

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

    private boolean ExecuteReload (CommandSender sender)
    {

        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();

        if (sender instanceof Player)
        {
            if (! CheckPermission((Player) sender, "reload"))
            {
                sender.sendMessage("NoPermission");
                return false;
            }
        }

        localeReader.RefreshLocales();

        ConfigReader[] configReaders = Bloodmoon.GetInstance().getAllConfigReaders();
        for (ConfigReader configReader : configReaders)
        {
            configReader.RefreshConfigs();
        }

        sender.sendMessage(localeReader.GetLocaleString("PluginReloaded"));

        return true;
    }

    private boolean CheckPermission (Player player, String node)
    {
        return CheckFullPermission (player, "bloodmoon." + node);
    }
    private boolean CheckFullPermission (Player player, String fullNode)
    {
        return player.hasPermission(fullNode);
    }
}
