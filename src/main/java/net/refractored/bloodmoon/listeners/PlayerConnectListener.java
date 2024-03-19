package net.refractored.bloodmoon.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static net.refractored.bloodmoon.BloodmoonActuator.*;


public class PlayerConnectListener implements Listener {
    @EventHandler
    public void onPlayerConnect (PlayerJoinEvent event) {
        if (isInProgress() && event.getPlayer().getWorld() == world)
        {
            HandleReconnectingPlayer(event.getPlayer());
        }
    }

}
