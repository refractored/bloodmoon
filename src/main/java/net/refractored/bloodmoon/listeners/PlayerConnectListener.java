package net.refractored.bloodmoon.listeners;

import net.refractored.bloodmoon.managers.BloodmoonManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static net.refractored.bloodmoon.managers.BloodmoonManager.*;


public class PlayerConnectListener implements Listener {
    @EventHandler
    public void onPlayerConnect (PlayerJoinEvent event) {
        if (BloodmoonManager.GetActuator(world).isInProgress() && event.getPlayer().getWorld() == world)
        {
            HandleReconnectingPlayer(event.getPlayer());
        }
    }

}
