package net.refractored.bloodmoon;

import net.refractored.bloodmoon.managers.BloodmoonManager;
import net.refractored.bloodmoon.readers.ConfigReader;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import java.util.HashMap;
import java.util.Map;


public class PeriodicNightCheck implements Runnable, Listener
{

    public static final int DAY = 24000;

    private static Map<World, PeriodicNightCheck> nightChecks;

    private World world;
    private BloodmoonManager actuator;

    public PeriodicNightCheck(World world, BloodmoonManager actuator) {
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
        this.actuator = actuator;
        this.world = world;
        actuator.setBloodMoonCheckAt((int) world.getFullTime());

        AddCheck(this);
    }

    private static void AddCheck (PeriodicNightCheck instance) {
        if (nightChecks == null) nightChecks = new HashMap<>();

        nightChecks.put(instance.GetWorld(), instance);
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
            if (world.getFullTime() <= actuator.getBloodMoonCheckAt()) return;
            if (actuator.getBloodMoonDays() > 0)
            {

                if (actuator.getBloodMoonDays() == 1)
                {
                    LocaleReader.MessageAllLocale("BloodMoonTomorrow", null, null, world);
                }
                else
                {
                    LocaleReader.MessageAllLocale("DaysBeforeBloodMoon", new String[]{"$d"}, new String[]{String.valueOf(actuator.getBloodMoonDays())}, world);
                }
                actuator.setBloodMoonDays((actuator.getBloodMoonDays() - 1));
                actuator.setBloodMoonCheckAt(getNextEvening());
                return;
            }

            //Day 0: prepare for Blood Moon
            LocaleReader.MessageAllLocale("BloodMoonTonight", null, null, world);
            actuator.setBloodMoonCheckAt((int) (getTodayZero() + 12000));
        }
    }
    private void CheckBloodmoonNight()
    {
        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
        //Check if its Blood Moon night, then time is over 13000, and if its the day after the day 0 warning
        if (world.getFullTime() >= actuator.getBloodMoonCheckAt() && world.getTime() >= 12000 && actuator.getBloodMoonDays() == 0)
        {

            actuator.StartBloodMoon();

            actuator.setBloodMoonCheckAt(getNextEvening());
            actuator.setBloodMoonDays((configReader.GetIntervalConfig() - 1));
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
                me
                LocaleReader.MessageAllLocale("BloodMoonEndingMessage", null, null, world);
                for (Player player : world.getPlayers())
                {
                    if (configReader.GetBloodMoonEndSoundConfig())
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 100.0f, 1.2f);
                }
                actuator.setBloodMoonCheckAt(0); //This avoids some bugs when players use /time set 0
            }

        }

    }

    private int getNextEvening ()
    {
        long currentTime = world.getFullTime();
        long remaining = currentTime % DAY;

        return (int) ((currentTime - remaining) + DAY + 11000);
    }

    private long getTodayZero ()
    {
        long currentTime = world.getFullTime();
        long remaining = currentTime % DAY;

        return (currentTime - remaining);
    }
}
