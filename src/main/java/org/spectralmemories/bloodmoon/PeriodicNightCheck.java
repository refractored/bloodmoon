package org.spectralmemories.bloodmoon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.spectralmemories.sqlaccess.FieldType;
import org.spectralmemories.sqlaccess.SQLAccess;
import org.spectralmemories.sqlaccess.SQLField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class PeriodicNightCheck implements Runnable, Listener
{

    public static final int DAY = 24000;
    public static final String A_BLOOD_MOON_IS_ON_ITS_WAY_TOMORROW = "A BloodMoon is on its way tomorrow...";
    public static final String DAYS_UNTIL_NEXT_BLOOD_MOON = " days until next BloodMoon";
    public static final String THE_SKY_IS_DARKER_THAN_USUAL_TONIGHT = "The sky is darker than usual tonight..";
    public static final String THE_BLOOD_MOON_IS_UPON_US = "The BloodMoon is upon us";
    public static final String DYING_DURING_A_BLOOD_MOON_RESULTS_IN_INVENTORY_AND_EXP_LOSS_BEWARE = "Dying during a BloodMoon results in inventory and exp loss. Beware";
    public static final String EXPERIENCE_IS_QUADRUPLED_AND_MOBS_HAVE_RANDOM_DROPS = "Experience is quadrupled, and mobs have random drops";
    public static final String MOBS_ARE_STRONGER_AND_APPLY_SPECIAL_EFFECTS = "Mobs are stronger and apply special effects";
    public static final String THE_BLOOD_MOON_FADES_AWAY_FOR_NOW = "The BloodMoon fades away... For now";

    private static Map<World, PeriodicNightCheck> nightChecks;

    private long checkupAfter;
    private int daysBeforeBloodMoon;

    private World world;
    private BloodmoonActuator actuator;

    private static int DAYS_BEFORE_BLOOD_MOON;

    public PeriodicNightCheck(World world, BloodmoonActuator actuator)
    {
        DAYS_BEFORE_BLOOD_MOON = Bloodmoon.GetInstance().getConfigReader().GetIntervalConfig();
        this.actuator = actuator;
        this.world = world;
        daysBeforeBloodMoon = (DAYS_BEFORE_BLOOD_MOON - 1); //Workaround
        checkupAfter = world.getFullTime();

        AddCheck(this);
    }

    private static void AddCheck (PeriodicNightCheck instance)
    {
        if (nightChecks == null) nightChecks = new HashMap<>();

        nightChecks.put(instance.GetWorld(), instance);
    }

    public static int GetDaysRemaining (World world)
    {
        PeriodicNightCheck instance = nightChecks.get(world);

        if (instance != null) return (instance.GetRemainingDays() + 1);

        return -1;
    }

    public World GetWorld ()
    {
        return world;
    }

    public static PeriodicNightCheck GetPeriodicNightCheck (World world)
    {
        return nightChecks.get(world);
    }

    public static int GetBloodMoonInterval ()
    {
        return DAYS_BEFORE_BLOOD_MOON;
    }

    public int GetRemainingDays ()
    {
        return daysBeforeBloodMoon;
    }

    public void SetDaysRemaining (int remaining)
    {
        daysBeforeBloodMoon = remaining;
    }

    public void SetCheckAfter (long time)
    {
        checkupAfter = time;
    }

    public long GetCheckAfter ()
    {
        return checkupAfter;
    }

    public void UpdateCacheDatabase ()
    {

        String worldUid = world.getUID().toString();
        String tableName = "lastBloodMoon";
        SQLAccess access = Bloodmoon.GetInstance().getSqlAccess();
        boolean exists = false;
        try
        {
            exists = access.EntryExist (tableName, new SQLField(
                            "world",
                            FieldType.TEXT,
                            true,
                            false), worldUid);
        }
        catch (SQLException e)
        {
            Bukkit.broadcastMessage(e.getMessage());
        }

        String sql;
        int days = (actuator.isInProgress()) ? 0 : daysBeforeBloodMoon;
        if (exists)
        {
            sql = "UPDATE " + tableName + " SET days = " + days + ", checkAt = " + checkupAfter;
            sql += " WHERE world = '" + worldUid + "';";
        }
        else
        {
            sql = "INSERT INTO " + tableName + " VALUES('" + worldUid + "', " + days + ", " + checkupAfter + ");";
        }

        try
        {
            if (! access.ExecuteSQLOperation(sql)) throw new SQLException();
        }
        catch (SQLException e)
        {
            Bukkit.broadcastMessage(e.getMessage());
        }

    }

    @Override
    public void run()
    {
        Bloodmoon.GetInstance().GetScheduler().runTaskLater(Bloodmoon.GetInstance(), this, Bloodmoon.NIGHT_CHECK_DELAY);

        /*

        This code is quite messy honestly. A complete rewrite of the class should be planned

        but, it works, so for this alpha version, this is fine

         */

        Check11();
        Check13();
        CheckDay();
    }

    private void Check11 ()
    {
        if (world.getTime() > 11000)
        {
            if (world.getFullTime() <= checkupAfter) return;
            if (daysBeforeBloodMoon > 0)
            {

                if (daysBeforeBloodMoon == 1)
                {

                    for (Player player : world.getPlayers())
                    {
                        player.sendMessage(ChatColor.GOLD + A_BLOOD_MOON_IS_ON_ITS_WAY_TOMORROW);
                    }

                }
                else
                {
                    for (Player player : world.getPlayers())
                    {
                        player.sendMessage("" + daysBeforeBloodMoon + "" + ChatColor.DARK_GREEN + DAYS_UNTIL_NEXT_BLOOD_MOON);
                    }
                }
                SetDaysRemaining(daysBeforeBloodMoon - 1);
                checkupAfter = getNextEvening();
                return;
            }

            //Day 0: prepare for Blood Moon

            for (Player player : world.getPlayers())
            {
                player.sendMessage(ChatColor.DARK_PURPLE + THE_SKY_IS_DARKER_THAN_USUAL_TONIGHT);
            }
            checkupAfter = getTodayZero() + 12000;
        }
    }
    private void Check13 ()
    {
        //Check if its Blood Moon night, then time is over 13000, and if its the day after the day 0 warning
        if (world.getFullTime() >= checkupAfter && world.getTime() >= 12000 && daysBeforeBloodMoon == 0)
        {

            for (Player player : world.getPlayers())
            {
                player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + THE_BLOOD_MOON_IS_UPON_US);
                player.sendMessage(ChatColor.RED + DYING_DURING_A_BLOOD_MOON_RESULTS_IN_INVENTORY_AND_EXP_LOSS_BEWARE);
                player.sendMessage(ChatColor.RED + EXPERIENCE_IS_QUADRUPLED_AND_MOBS_HAVE_RANDOM_DROPS);
                player.sendMessage(ChatColor.RED + MOBS_ARE_STRONGER_AND_APPLY_SPECIAL_EFFECTS);
            }

            actuator.StartBloodMoon();

            checkupAfter = getNextEvening();
            daysBeforeBloodMoon = (DAYS_BEFORE_BLOOD_MOON - 1);
        }
    }
    private void CheckDay ()
    {
        if (actuator.isInProgress())
        {
            //If Blood Moon is in progress but its daytime, stop it
            if (world.getTime() < 12000)
            {

                actuator.StopBloodMoon();
                for (Player player : world.getPlayers())
                {
                    player.sendMessage(ChatColor.GREEN + THE_BLOOD_MOON_FADES_AWAY_FOR_NOW);
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 100.0f, 1.2f);
                }
                checkupAfter = 0; //This avoids some bugs when players use /time set 0, which resets time to 0
            }

            //We have a night bar now
            /*else if (world.getFullTime() > bloodMoonTimeReminder)
            {
                long timeLeft = 24000 - world.getTime();
                timeLeft = (long) Math.ceil(timeLeft / 1000f);
                for (Player player : world.getPlayers())
                {
                    player.sendMessage(ChatColor.GOLD + "" + timeLeft + "h until BloodMoon ends");
                }
                bloodMoonTimeReminder = world.getFullTime() + 1000;
            }*/
        }

    }

    private long getNextEvening ()
    {
        long currentTime = world.getFullTime();
        long remaining = currentTime % DAY;

        return ((currentTime - remaining) + DAY + 11000);
    }

    private long getTodayZero ()
    {
        long currentTime = world.getFullTime();
        long remaining = currentTime % DAY;

        return (currentTime - remaining);
    }


    @EventHandler
    public void onCommandIssued (PlayerCommandPreprocessEvent event)
    {
        Player sender = event.getPlayer();
        String command = event.getMessage();

        if (command.startsWith("/time set"))
        {
            if (sender.getWorld() == world)
            {
                SetCheckAfter(0);
            }
        }
    }
}
