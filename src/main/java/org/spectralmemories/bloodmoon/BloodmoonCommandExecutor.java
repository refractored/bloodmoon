package org.spectralmemories.bloodmoon;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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

            LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
            ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(playerWorld);

            if (args.length == 0)
            {
                LocaleReader.MessageLocale("AllowedCommands", new String[]{"$d"}, new String[]{"show, start, stop, reload, spawnzombieboss, killbosses [rewards]"}, player);
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
            else if (arg0.equalsIgnoreCase("spawnzombieboss"))
            {
                return ExecuteSpawnZombieBoss(player);
            }
            else if (arg0.equalsIgnoreCase("killbosses"))
            {
                boolean param = false;
                if (args.length >= 2) {
                    param = Boolean.parseBoolean(args[1]);
                }

                return ExecuteKillBosses(player, param);
            }
            else if (arg0.equalsIgnoreCase("spawnlimit"))
            {
                int param = 0;
                if (args.length >= 2) {
                    param = Integer.parseInt(args[1]);
                }

                return ExecuteChangeSpawnMax(player, param);
            }
            else
            {
                LocaleReader.MessageLocale("CommandNotFound", new String[]{"$d"}, new String[]{arg0}, player);
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

    private boolean ExecuteChangeSpawnMax (Player playerSender, int value)
    {
        playerSender.getWorld().setMonsterSpawnLimit(value);
        playerSender.sendMessage("Monster Spawn set to " + String.valueOf(playerSender.getWorld().getMonsterSpawnLimit()));
        return true;
    }

    private boolean ExecuteShow (Player playerSender)
    {
        World playerWorld = playerSender.getWorld();

        if (! CheckPermission(playerSender, "show"))
        {
            LocaleReader.MessageLocale("NoPermission", null, null, playerSender);
            return false;
        }

        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(playerWorld);

        if (configReader.GetPermanentBloodMoonConfig())
        {
            LocaleReader.MessageLocale("WorldIsPermanentBloodMoon", null, null, playerSender);
            return true;
        }


        BloodmoonActuator worldActuator = BloodmoonActuator.GetActuator(playerWorld);

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

        int remainingDays = PeriodicNightCheck.GetDaysRemaining(playerWorld);
        if (remainingDays < 0)
        {
            System.out.println("[Error] remainingDays was inferior to 0. Please regenerate both the cache and the config for world " + playerWorld.getName());
            LocaleReader.MessageLocale("GeneralError", null, null, playerSender);
        }
        else
        {
            LocaleReader.MessageLocale("DaysBeforeBloodMoon", new String[]{"$d"}, new String[]{String.valueOf(remainingDays)}, playerSender);
        }

        return true;
    }

    private boolean ExecuteStart (Player playerSender)
    {
        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();



        if (! CheckPermission(playerSender, "start"))
        {
            LocaleReader.MessageLocale("NoPermission", null, null, playerSender);
            return false;
        }

        World world = playerSender.getWorld();

        if (Bloodmoon.GetInstance().getConfigReader(world).GetPermanentBloodMoonConfig())
        {
            LocaleReader.MessageLocale("WorldIsPermanentBloodMoon", null, null, playerSender);
            return true;
        }

        PeriodicNightCheck nightCheck = PeriodicNightCheck.GetPeriodicNightCheck(world);
        if (nightCheck == null)
        {
            LocaleReader.MessageLocale("NoBloodMoonInWorld", null, null, playerSender);
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
            LocaleReader.MessageLocale("NoPermission", null, null, playerSender);
            return false;
        }

        World world = playerSender.getWorld();

        if (Bloodmoon.GetInstance().getConfigReader(world).GetPermanentBloodMoonConfig())
        {
            LocaleReader.MessageLocale("CannotStopBloodMoon", null, null, playerSender);
            return true;
        }

        PeriodicNightCheck nightCheck = PeriodicNightCheck.GetPeriodicNightCheck(world);
        if (nightCheck == null)
        {
            LocaleReader.MessageLocale("NoBloodMoonInWorld", null, null, playerSender);
            return true;
        }

        nightCheck.SetCheckAfter(0);
        nightCheck.SetDaysRemaining(nightCheck.GetBloodMoonInterval() - 1);
        world.setTime(0);
        return true;
    }

    private boolean ExecuteSpawnZombieBoss(Player playerSender) {
        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        if (!CheckPermission(playerSender, "spawnzombieboss")) {
            LocaleReader.MessageLocale("NoPermission", null, null, playerSender);
            return false;
        } else {
            BloodmoonActuator actuator = BloodmoonActuator.GetActuator(playerSender.getWorld());
            if (actuator == null) {
            }

            actuator.SpawnZombieBoss();
            return true;
        }
    }

    private boolean ExecuteKillBosses(Player playerSender, boolean giveRewards) {
        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        if (!CheckPermission(playerSender, "killbosses")) {
            LocaleReader.MessageLocale("NoPermission", null, null, playerSender);
            return false;
        } else {
            BloodmoonActuator actuator = BloodmoonActuator.GetActuator(playerSender.getWorld());
            if (actuator == null) {
                LocaleReader.MessageLocale("NoBloodMoonInWorld", null, null, playerSender);
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
            if (! CheckPermission((Player) sender, "reload"))
            {
                LocaleReader.MessageLocale("NoPermission", null, null, (Player) sender);
                return false;
            }
            LocaleReader.MessageLocale("PluginReloaded", null, null, (Player) sender);
        }

        localeReader.RefreshLocales();

        ConfigReader[] configReaders = Bloodmoon.GetInstance().getAllConfigReaders();
        for (ConfigReader configReader : configReaders)
        {
            configReader.RefreshConfigs();
        }

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
