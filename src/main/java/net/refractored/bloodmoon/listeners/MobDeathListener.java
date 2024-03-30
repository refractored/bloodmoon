package net.refractored.bloodmoon.listeners;

import net.refractored.bloodmoon.Bloodmoon;
import net.refractored.bloodmoon.commands.BloodmoonLevelAdd;
import net.refractored.bloodmoon.managers.BloodmoonManager;
import net.refractored.bloodmoon.boss.IBoss;
import net.refractored.bloodmoon.readers.ConfigReader;
import net.refractored.bloodmoon.readers.LocaleReader;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.Random;

import static net.refractored.bloodmoon.managers.BloodmoonManager.*;

public class MobDeathListener implements Listener {
    @EventHandler
    public void onMobDeath (EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        for (IBoss boss : bosses)
        {
            if (entity == boss.GetHost())
            {
                Player killer = boss.GetHost().getKiller();
                if (killer != null)
                {

                    LocaleReader.MessageAllLocale("BossSlain", new String[]{"$b", "$p"}, new String[]{boss.GetName(), killer.getName()}, world);
                }

                boss.Kill(killer != null && BloodmoonManager.isInProgress());
                bosses.remove(boss);
                return;
            }
        }

        if (!BloodmoonManager.isInProgress()) return; //Only during BloodMoon

        if (entity.getKiller() == null) return; //No killer, no reward

        if (!entity.hasLineOfSight(entity.getKiller())) return; //No line of sight, no reward

        if (event.getEntity() instanceof Player) return; //Handled in another method

        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);


        if (entity.getWorld() != world) return; //Wrong world

        if (blacklistedMobs.contains(entity))
        {
            //This mob was explicitly blacklisted. ignore it
            blacklistedMobs.remove(entity);
            return;
        }

        boolean eligible = false;
        for (EntityType type : rewardedTypes)
        {
            if (entity.getType() == type) eligible = true;
        }

        if (!eligible) return; //Not eligible for reward

        event.setDroppedExp((int) (event.getDroppedExp() * configReader.GetExpMultConfig()[GetActuator(world).getBloodMoonLevel()]));

        if (configReader.GetMobDeathThunderConfig())
            world.strikeLightningEffect(event.getEntity().getLocation());

        List<ItemStack> bonusDrops = new ArrayList<>();

        int min = configReader.GetMinItemsDropConfig();
        int max = configReader.GetMaxItemsDropConfig();

        int itemCount = (max - min <= 0) ? min : new Random().nextInt(max - min) + min;

        for (int i = 0; i < itemCount; i++)
        {
            bonusDrops.add(GetRandomBonus()); //Add the drops
        }

        for (ItemStack item : bonusDrops)
        {
            if (item == null) continue;
            world.dropItemNaturally(entity.getLocation(), item); //Drop items
        }
    }
}
