package net.refractored.bloodmoon.listeners;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import static net.refractored.bloodmoon.managers.BloodmoonManager.*;


public class PlayerRespawnListener implements Listener {
    @EventHandler
    public void onPlayerRespawn (PlayerRespawnEvent event) {
        World from = event.getPlayer().getWorld();
        World to = event.getRespawnLocation().getWorld();
        if (to != world && from != world) return; //None of our concern

        if (from != to)
        {
            if (to == world && isInProgress())
            {
                //Someone respawned in our bm world
                HandleReconnectingPlayer(event.getPlayer());
            }
            if (from == world)
            {
                //Someone respawned out of our bm world
                HideNightBarPlayer(event.getPlayer());
            }
        }
    }
}
