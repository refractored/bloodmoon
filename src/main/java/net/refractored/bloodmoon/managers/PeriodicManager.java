package net.refractored.bloodmoon.managers;

import net.refractored.bloodmoon.Bloodmoon;
import net.refractored.bloodmoon.readers.ConfigReader;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;


import java.io.Closeable;
import java.util.Random;

/**
 * ActuatorPeriodic is a periodic runner designed to be ran once at the start of the bloodmoon
 * You must close it manually upon bloodmoon end
 */
public class PeriodicManager implements Runnable, Closeable
{
    World world;
    Random random;

    boolean mustStop;

    /**
     * Default constructor
     * @param world world to affect
     */
    public PeriodicManager(World world)
    {
        mustStop = false;
        this.world = world;
        random = new Random();
    }

    /**
     * Closes the perdiodic actuator and stops all actions
     */
    public void close ()
    {
        mustStop = true;
    }

    /**
     * Runs the periodic actuator
     */
    @Override
    public void run()
    {
        if (mustStop) return;
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);

        if (configReader.GetBloodMoonPeriodicSoundConfig())
        {
            for (Player player : world.getPlayers())
            {
                player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 50.0f, 0.7f);
            }
        }
        if (configReader.GetThunderingConfig())
        {
            world.setStorm(true);
            world.setThundering(true);
            world.setThunderDuration(20 * 10 * 60);
            world.setWeatherDuration(20 * 10 * 60);
        }
        Bloodmoon.GetInstance().GetScheduler().runTaskLater(Bloodmoon.GetInstance(), this, random.nextInt(200) + 320);
    }
}
