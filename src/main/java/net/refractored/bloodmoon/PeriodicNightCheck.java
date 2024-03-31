package net.refractored.bloodmoon;

import com.willfp.eco.core.data.ServerProfile;
import com.willfp.eco.core.data.keys.PersistentDataKey;
import com.willfp.eco.core.data.keys.PersistentDataKeyType;
import com.willfp.eco.core.factory.RunnableFactory;
import com.willfp.eco.util.NamespacedKeyUtils;
import net.refractored.bloodmoon.managers.BloodmoonManager;
import net.refractored.bloodmoon.managers.DatabaseManager;
import net.refractored.bloodmoon.readers.ConfigReader;
import net.refractored.bloodmoon.readers.LocaleReader;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.spectralmemories.sqlaccess.FieldType;
import org.spectralmemories.sqlaccess.SQLAccess;
import org.spectralmemories.sqlaccess.SQLField;
import com.willfp.eco.core.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class PeriodicNightCheck implements Runnable, Listener
{

    public static final int DAY = 24000;

    private static Map<World, PeriodicNightCheck> nightChecks;

    private long checkupAfter;
    private int daysBeforeBloodMoon;


    private World world;
    private BloodmoonManager actuator;

    public PeriodicNightCheck(World world, BloodmoonManager actuator) {
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
        this.actuator = actuator;
        this.world = world;
        daysBeforeBloodMoon = (configReader.GetIntervalConfig() - 1); //Workaround
        checkupAfter = world.getFullTime();

        AddCheck(this);
    }

    private static void AddCheck (PeriodicNightCheck instance) {
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
        SQLAccess access = DatabaseManager.getSqlAccess();
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
            sql = String.format("UPDATE %s SET days = %d, checkAt = %d WHERE world = '%s';", tableName, (days + 1), checkAt, worldUid);
        }
        else
        {
            sql = String.format("INSERT INTO %s VALUES('%s', %d, %d);", tableName, worldUid, (days + 1), checkAt);
        }

        try
        {
            if (! access.ExecuteSQLOperation(sql))
            {
                System.out.println("[Warning] There was an issue updating the cache");
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

        CheckTomorrow();
        CheckBloodmoonNight();
        CheckDay();
    }

    /**
     * This method checks if the next day is the day before the blood moon
     */
    private void CheckTomorrow()
    {
        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        if (world.getTime() > 11000)
        {
            if (world.getFullTime() <= checkupAfter) return;
            if (daysBeforeBloodMoon > 0)
            {

                if (daysBeforeBloodMoon == 1)
                {
                    LocaleReader.MessageAllLocale("BloodMoonTomorrow", null, null, world);
                }
                else
                {
                    LocaleReader.MessageAllLocale("DaysBeforeBloodMoon", new String[]{"$d"}, new String[]{String.valueOf(daysBeforeBloodMoon)}, world);
                }
                SetDaysRemaining(daysBeforeBloodMoon - 1);
                checkupAfter = getNextEvening();
                return;
            }

            //Day 0: prepare for Blood Moon
            LocaleReader.MessageAllLocale("BloodMoonTonight", null, null, world);
            checkupAfter = getTodayZero() + 12000;
        }
    }
    private void CheckBloodmoonNight()
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
                LocaleReader.MessageAllLocale("BloodMoonEndingMessage", null, null, world);
                for (Player player : world.getPlayers())
                {
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
}
