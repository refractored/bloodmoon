package net.refractored.bloodmoon.listeners;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import static net.refractored.bloodmoon.BloodmoonActuator.*;


public class PlayerTeleportListener implements Listener {
    @EventHandler
    public void onPlayerTeleport (PlayerTeleportEvent event) {
        World to = event.getTo().getWorld();
        World from = event.getFrom().getWorld();
        if (to != world && from != world) return; //None of our concern

        if (from != to)
        {
            if (to == world && isInProgress())
            {
                //Someone entered our bm world
                HandleReconnectingPlayer(event.getPlayer());
            }
            if (from == world && isInProgress())
            {
                //Someone left our bm world
                HideNightBarPlayer(event.getPlayer());
            }
        }
    }
}
