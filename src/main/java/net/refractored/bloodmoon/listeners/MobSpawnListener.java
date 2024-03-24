package net.refractored.bloodmoon.listeners;

import net.refractored.bloodmoon.Bloodmoon;
import net.refractored.bloodmoon.readers.ConfigReader;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;

import static net.refractored.bloodmoon.managers.BloodmoonManager.*;

public class MobSpawnListener implements Listener {
    @EventHandler
    public void onMobSpawn (SpawnerSpawnEvent event) {
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);

        if (configReader.GetMobsFromSpawnerNoRewardConfig() && event.getEntity().getWorld() == world && isInProgress())
        {
            for (EntityType type : rewardedTypes)
            {
                if (event.getEntityType() == type)
                {
                    if (event.getEntity() instanceof LivingEntity)
                    {
                        blacklistedMobs.add((LivingEntity) event.getEntity());
                        break;
                    }
                }
            }
        }
    }
}
