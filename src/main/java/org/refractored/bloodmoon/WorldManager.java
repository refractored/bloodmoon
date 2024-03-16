package org.refractored.bloodmoon;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldManager implements Listener
{
    @EventHandler
    public void onWorldLoad (WorldLoadEvent event)
    {
        //This method will check if the world is blacklisted itself
        Bloodmoon.GetInstance().LoadWorld(event.getWorld());
    }
}
