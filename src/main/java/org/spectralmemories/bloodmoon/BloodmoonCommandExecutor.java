package org.spectralmemories.bloodmoon;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

/**
 * This class is the only class capable of interpreting commands
 */
public class BloodmoonCommandExecutor implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            World playerWorld = player.getWorld();

            if (args.length == 0)
            {
                LocaleReader.MessageLocale("AllowedCommands", new String[]{"$d"}, new String[]{"show, start, stop, reload, spawnzombieboss, killbosses [rewards]"}, player);
                return false;
            }

            String arg0 = args[0];
            if (arg0.equalsIgnoreCase("start"))
            {
                return ExecuteStart(player, playerWorld);
            }
            else if (arg0.equalsIgnoreCase("stop"))
            {
                return ExecuteStop(player, playerWorld);
            }
            else if (arg0.equalsIgnoreCase("reload"))
            {
                return ExecuteReload(player);
            }
            else if (arg0.equalsIgnoreCase("show"))
            {
                return ExecuteShow(player, playerWorld);
            }
            else if (arg0.equalsIgnoreCase("spawnzombieboss"))
            {
                return ExecuteSpawnZombieBoss(player, playerWorld);
            }
            else if (arg0.equalsIgnoreCase("killbosses"))
            {
                boolean param = false;
                if (args.length >= 2) {
                    param = Boolean.parseBoolean(args[1]);
                }

                return ExecuteKillBosses(player, playerWorld,  param);
            }
            else
            {
                LocaleReader.MessageLocale("CommandNotFound", new String[]{"$d"}, new String[]{arg0}, player);
                return false;
            }
        }
        else if(sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender)
        {
            if (args.length < 1)
            {
                LocaleReader.MessageLocale("AllowedCommands", new String[]{"$d"}, new String[]{"show [world], start [world], stop [world], reload, spawnzombieboss [world], killbosses [world] [rewards]"}, sender);
                return false;
            }
            String arg0 = args[0];
            if (args.length < 2 && !arg0.equals("reload")){
                sender.sendMessage("Please suffix your command with the world name");
                return false;
            }

            World chosenWorld = null;
            if(!arg0.equals("reload")) chosenWorld = Bukkit.getWorld(args[1]);

            if(chosenWorld == null && !arg0.equals("reload")){
                sender.sendMessage("[Error] No world named " + args[1] + " could be found!");
                return false;
            }


            if (arg0.equalsIgnoreCase("start"))
            {
                return ConfirmToConsole(sender,ExecuteStart(sender, chosenWorld), arg0);
            }
            else if (arg0.equalsIgnoreCase("stop"))
            {
                return ConfirmToConsole(sender,ExecuteStop(sender, chosenWorld), arg0);
            }
            else if (arg0.equalsIgnoreCase("reload"))
            {
                return ConfirmToConsole(sender,ExecuteReload(sender), arg0);
            }
            else if (arg0.equalsIgnoreCase("show"))
            {
                return ExecuteShow(sender, chosenWorld);
            }
            else if (arg0.equalsIgnoreCase("spawnzombieboss"))
            {
                return ConfirmToConsole(sender,ExecuteSpawnZombieBoss(sender, chosenWorld), arg0);
            }
            else if (arg0.equalsIgnoreCase("killbosses"))
            {
                boolean param = false;
                if (args.length >= 3) {
                    param = Boolean.parseBoolean(args[2]);
                }

                return ConfirmToConsole(sender,ExecuteKillBosses(sender, chosenWorld, param), arg0);
            }
            else
            {
                LocaleReader.MessageLocale("CommandNotFound", new String[]{"$d"}, new String[]{arg0}, sender);
                return false;
            }
        }else{
            sender.sendMessage("[Error] Subclass of CommandSender sender is not valid");
            sender.sendMessage("Expected Player or ConsoleCommandSender or RemoteConsoleCommandSender, got " + sender.getClass().getName());
            return false;
        }
    }

    private boolean ExecuteShow (CommandSender playerSender, World world)
    {
        if (! CheckPermission(playerSender, "show"))
        {
            LocaleReader.MessageLocale("NoPermission", null, null, playerSender);
            return false;
        }

        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);

        if (configReader.GetPermanentBloodMoonConfig())
        {
            LocaleReader.MessageLocale("WorldIsPermanentBloodMoon", null, null, playerSender);
            return true;
        }


        BloodmoonActuator worldActuator = BloodmoonActuator.GetActuator(world);

        if (worldActuator == null)
        {
            LocaleReader.MessageLocale("NoBloodMoonInWorld", null, null, playerSender);
            return true;
        }

        if (worldActuator.isInProgress())
        {
            LocaleReader.MessageLocale("BloodMoonRightNow", null, null, playerSender);
            return true;
        }

        int remainingDays = PeriodicNightCheck.GetDaysRemaining(world);
        if (remainingDays < 0)
        {
            System.out.println("[Error] remainingDays was inferior to 0. Please regenerate both the cache and the config for world " + world.getName());
            LocaleReader.MessageLocale("GeneralError", null, null, playerSender);
        }
        else
        {
            LocaleReader.MessageLocale("DaysBeforeBloodMoon", new String[]{"$d"}, new String[]{String.valueOf(remainingDays)}, playerSender);
        }

        return true;
    }

    private boolean ExecuteStart (CommandSender sender, World world)
    {
        if (! CheckPermission(sender, "start"))
        {
            LocaleReader.MessageLocale("NoPermission", null, null, sender);
            return false;
        }

        if (Bloodmoon.GetInstance().getConfigReader(world).GetPermanentBloodMoonConfig())
        {
            LocaleReader.MessageLocale("WorldIsPermanentBloodMoon", null, null, sender);
            return true;
        }

        PeriodicNightCheck nightCheck = PeriodicNightCheck.GetPeriodicNightCheck(world);
        if (nightCheck == null)
        {
            LocaleReader.MessageLocale("NoBloodMoonInWorld", null, null, sender);
            return true;
        }
        nightCheck.SetCheckAfter(0);
        nightCheck.SetDaysRemaining(0);
        world.setTime(12001);
        return true;
    }

    private boolean ExecuteStop (CommandSender sender, World world)
    {
        if (! CheckPermission(sender, "stop"))
        {
            LocaleReader.MessageLocale("NoPermission", null, null, sender);
            return false;
        }

        if (Bloodmoon.GetInstance().getConfigReader(world).GetPermanentBloodMoonConfig())
        {
            LocaleReader.MessageLocale("CannotStopBloodMoon", null, null, sender);
            return true;
        }

        PeriodicNightCheck nightCheck = PeriodicNightCheck.GetPeriodicNightCheck(world);
        if (nightCheck == null)
        {
            LocaleReader.MessageLocale("NoBloodMoonInWorld", null, null, sender);
            return true;
        }

        nightCheck.SetCheckAfter(0);
        nightCheck.SetDaysRemaining(nightCheck.GetBloodMoonInterval() - 1);
        world.setTime(0);
        return true;
    }

    private boolean ExecuteSpawnZombieBoss(CommandSender sender, World world) {
        if (!CheckPermission(sender, "spawnzombieboss")) {
            LocaleReader.MessageLocale("NoPermission", null, null, sender);
            return false;
        } else {
            BloodmoonActuator actuator = BloodmoonActuator.GetActuator(world);
            if (actuator == null) {
                LocaleReader.MessageLocale("NoBloodMoonInWorld", null, null, sender);
                return true;
            }
            actuator.SpawnZombieBoss();
            return true;
        }
    }

    private boolean ExecuteKillBosses(CommandSender sender, World world, boolean giveRewards) {
        if (!CheckPermission(sender, "killbosses")) {
            LocaleReader.MessageLocale("NoPermission", null, null, sender);
            return false;
        } else {
            BloodmoonActuator actuator = BloodmoonActuator.GetActuator(world);
            if (actuator == null) {
                LocaleReader.MessageLocale("NoBloodMoonInWorld", null, null, sender);
                return true;
            } else {
                actuator.KillBosses(giveRewards, true, false);
                return true;
            }
        }
    }

    private boolean ExecuteReload (CommandSender sender)
    {

        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();

        if (sender instanceof Player)
        {
            if (! CheckPermission(sender, "reload"))
            {
                LocaleReader.MessageLocale("NoPermission", null, null, sender);
                return false;
            }
            LocaleReader.MessageLocale("PluginReloaded", null, null, sender);
        }

        localeReader.RefreshLocales();

        ConfigReader[] configReaders = Bloodmoon.GetInstance().getAllConfigReaders();
        for (ConfigReader configReader : configReaders)
        {
            configReader.RefreshConfigs();
        }

        return true;
    }

    private boolean CheckPermission (CommandSender sender, String node)
    {
        if(sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) return true;
        return CheckFullPermission (sender, "bloodmoon." + node);
    }
    private boolean CheckFullPermission (CommandSender sender, String fullNode)
    {
        if(sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) return true;
        return sender.hasPermission(fullNode);
    }

    private boolean ConfirmToConsole(CommandSender console, boolean success, String command){
        if(success){
            console.sendMessage("Command " + command + " has been successfully ran");
        }else{
            console.sendMessage("There was a problem running command " + command);
        }
        return success;
    }
}
