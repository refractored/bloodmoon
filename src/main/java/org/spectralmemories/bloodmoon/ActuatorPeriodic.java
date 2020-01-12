package org.spectralmemories.bloodmoon;

import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;


import java.util.Random;

public class ActuatorPeriodic implements Runnable
{
    World world;
    Random random;

    boolean mustStop;

    public ActuatorPeriodic(World world)
    {
        mustStop = false;
        this.world = world;
        random = new Random();
    }

    public void Stop ()
    {
        mustStop = true;
    }

    @Override
    public void run()
    {
        if (mustStop) return;

        for (Player player : world.getPlayers())
        {
            player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 50.0f, 0.7f);
        }
        world.setStorm(true);
        world.setThundering(true);
        world.setThunderDuration(20 * 10 * 60);
        world.setWeatherDuration(20 * 10 * 60);
        Bloodmoon.GetInstance().GetScheduler().runTaskLater(Bloodmoon.GetInstance(), this, random.nextInt(200) + 320);
    }
}
