package net.refractored.bloodmoon.listeners;

import net.refractored.bloodmoon.Bloodmoon;
import net.refractored.bloodmoon.readers.ConfigReader;
import net.refractored.bloodmoon.readers.LocaleReader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

import static net.refractored.bloodmoon.managers.BloodmoonManager.*;

public class PlayerSleepListener implements Listener {
    @EventHandler
    public void onPlayerSleeps (PlayerBedEnterEvent event) {
        if (event.getPlayer().getWorld() == world)
        {
            if (isInProgress())
            {
                ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
                if (configReader.GetPreventSleepingConfig())
                {
                    LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();

                    LocaleReader.MessageLocale("BedNotAllowed", null, null, event.getPlayer());
                    event.setCancelled(true);
                }
            }
        }
    }

}
