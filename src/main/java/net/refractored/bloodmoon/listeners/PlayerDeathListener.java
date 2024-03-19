package net.refractored.bloodmoon.listeners;

import net.refractored.bloodmoon.Bloodmoon;
import net.refractored.bloodmoon.readers.ConfigReader;
import net.refractored.bloodmoon.readers.LocaleReader;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import static net.refractored.bloodmoon.BloodmoonActuator.*;

public class PlayerDeathListener implements Listener {
    @EventHandler
    public void onPlayerDeath (PlayerDeathEvent event) {
        if (!isInProgress()) return; //Only during BloodMoon

        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);

        Player deadplayer = event.getEntity();
        if (deadplayer.getWorld() != world) return; //Wrong world

        if (configReader.GetLightningEffectConfig()) world.strikeLightningEffect(deadplayer.getLocation());

        String deathMessage = event.getDeathMessage();

        if (!deathMessage.contains(localeReader.GetLocaleString("DeathSuffix")))
        {
            deathMessage += " " + localeReader.GetLocaleString("DeathSuffix");

            event.setDeathMessage(deathMessage);
        }

        if (configReader.GetExperienceLossConfig())
        {
            event.setNewTotalExp(0);
            event.setDroppedExp(0);
        }


        if (configReader.GetInventoryLossConfig()) event.getDrops().clear();
    }


}
