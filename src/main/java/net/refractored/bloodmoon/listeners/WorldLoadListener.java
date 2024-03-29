package net.refractored.bloodmoon.listeners;

import net.refractored.bloodmoon.Bloodmoon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldLoadListener implements Listener
{
    @EventHandler
    public void onWorldLoad (WorldLoadEvent event)
    {
        //This method will check if the world is blacklisted itself
        Bloodmoon.GetInstance().LoadWorld(event.getWorld());
    }
}
