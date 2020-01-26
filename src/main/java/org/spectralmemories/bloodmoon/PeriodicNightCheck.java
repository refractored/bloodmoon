package org.spectralmemories.bloodmoon;

import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.spectralmemories.sqlaccess.FieldType;
import org.spectralmemories.sqlaccess.SQLAccess;
import org.spectralmemories.sqlaccess.SQLField;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class PeriodicNightCheck implements Runnable, Listener
{

    public static final int DAY = 24000;

    private static Map<World, PeriodicNightCheck> nightChecks;

    private long checkupAfter;
    private int daysBeforeBloodMoon;

    private World world;
    private BloodmoonActuator actuator;

    public PeriodicNightCheck(World world, BloodmoonActuator actuator)
    {
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
        this.actuator = actuator;
        this.world = world;
        daysBeforeBloodMoon = (configReader.GetIntervalConfig() - 1); //Workaround
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
        try
        {
            return nightChecks.get(world);
        }
        catch (Exception ignored){}
        return null;
    }

    public int GetBloodMoonInterval ()
    {
        return Bloodmoon.GetInstance().getConfigReader(world).GetIntervalConfig();
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
            e.printStackTrace();
        }

        String sql;
        int days = (actuator.isInProgress()) ? -1 : daysBeforeBloodMoon;
        long checkAt = (actuator.isInProgress()) ? 0 : checkupAfter;
        if (exists)
        {
            sql = "UPDATE " + tableName + " SET days = " + (days + 1) + ", checkAt = " + checkAt;
            sql += " WHERE world = '" + worldUid + "';";
        }
        else
        {
            sql = "INSERT INTO " + tableName + " VALUES('" + worldUid + "', " + (days + 1) + ", " + checkAt + ");";
        }

        try
        {
            if (! access.ExecuteSQLOperation(sql))
            {
                System.out.println("[Warning] There was an issue updating plugin");
            }
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
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
        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        if (world.getTime() > 11000)
        {
            if (world.getFullTime() <= checkupAfter) return;
            if (daysBeforeBloodMoon > 0)
            {

                if (daysBeforeBloodMoon == 1)
                {

                    for (Player player : world.getPlayers())
                    {
                        player.sendMessage(localeReader.GetLocaleString("BloodMoonTomorrow"));
                    }

                }
                else
                {
                    for (Player player : world.getPlayers())
                    {
                        String message = localeReader.GetLocaleString("DaysBeforeBloodMoon");
                        player.sendMessage(message.replace("$d", String.valueOf(daysBeforeBloodMoon)));
                    }
                }
                SetDaysRemaining(daysBeforeBloodMoon - 1);
                checkupAfter = getNextEvening();
                return;
            }

            //Day 0: prepare for Blood Moon

            for (Player player : world.getPlayers())
            {
                player.sendMessage(localeReader.GetLocaleString("BloodMoonTonight"));
            }
            checkupAfter = getTodayZero() + 12000;
        }
    }
    private void Check13 ()
    {
        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
        //Check if its Blood Moon night, then time is over 13000, and if its the day after the day 0 warning
        if (world.getFullTime() >= checkupAfter && world.getTime() >= 12000 && daysBeforeBloodMoon == 0)
        {

            actuator.StartBloodMoon();

            checkupAfter = getNextEvening();
            daysBeforeBloodMoon = (configReader.GetIntervalConfig() - 1);
        }
    }
    private void CheckDay ()
    {
        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
        if (actuator.isInProgress())
        {
            //If Blood Moon is in progress but its daytime, stop it
            if (world.getTime() < 12000)
            {

                actuator.StopBloodMoon();
                for (Player player : world.getPlayers())
                {
                    player.sendMessage(localeReader.GetLocaleString("BloodMoonEndingMessage"));
                    if (configReader.GetBloodMoonEndSoundConfig())
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 100.0f, 1.2f);
                }
                checkupAfter = 0; //This avoids some bugs when players use /time set 0
            }

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


    //We need to make sure the checkAt var is reset when time is manually changed
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
