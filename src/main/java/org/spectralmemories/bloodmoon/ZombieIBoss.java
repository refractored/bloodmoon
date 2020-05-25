//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

/**
 * Not gonna lie I lost this class and had to decompile a more recent build of this plugin
 * This class will be rewritten for clarity as needed
 */


package org.spectralmemories.bloodmoon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class ZombieIBoss extends Boss {
    Zombie zombieHost;

    public ZombieIBoss(Location location) {
        this.host = (Monster) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
        this.zombieHost = (Zombie)this.host;
        this.world = this.host.getWorld();
        this.reader = Bloodmoon.GetInstance().getConfigReader(this.world);
        this.locales = Bloodmoon.GetInstance().getLocaleReader();
        this.scheduler = Bloodmoon.GetInstance().GetScheduler();
        this.tasks = new ArrayList();
    }

    public void Start() {
        this.host.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2147483647, this.reader.GetZombieBossHealth()));
        this.host.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 2147483647, this.reader.GetZombieBossDamage()));
        this.host.setCustomName(this.locales.GetLocaleString("ZombieBossName"));
        this.host.setCustomNameVisible(true);
        this.zombieHost.setBaby(false);
        this.Announce();
        this.tasks.add(this.scheduler.scheduleSyncRepeatingTask(Bloodmoon.GetInstance(), new Runnable() {
            public void run() {
                ZombieIBoss.this.world.strikeLightningEffect(ZombieIBoss.this.host.getLocation());
            }
        }, 0L, 238L));
        this.tasks.add(this.scheduler.scheduleSyncRepeatingTask(Bloodmoon.GetInstance(), new Runnable() {
            public void run() {
                ZombieIBoss.this.world.spawnParticle(Particle.PORTAL, ZombieIBoss.this.host.getLocation(), 60);
            }
        }, 0L, 4L));
        this.ParseSpells();
        MonitorDeath();
    }

    public void Kill(boolean reward) {
        Iterator var2 = tasks.iterator();

        while(var2.hasNext()) {
            Integer task = (Integer)var2.next();
            scheduler.cancelTask(task);
        }

        this.tasks.clear();

        for(int i = 0; i < 5; ++i) {
            world.strikeLightningEffect(this.host.getLocation());
        }

        int delayBase = 10;
        int strikes = 8;

        for(int i = 0; i < strikes; ++i) {
            scheduler.scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable() {
                public void run() {
                    ZombieIBoss.this.world.strikeLightningEffect(ZombieIBoss.this.host.getLocation());
                    ZombieIBoss.this.world.spawnParticle(Particle.EXPLOSION_HUGE, ZombieIBoss.this.host.getLocation(), 20);
                }
            }, (long)(delayBase * i));
        }

        if (reward) {
            scheduler.scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable() {
                public void run() {
                    ZombieIBoss.this.Reward();
                }
            }, (long)(delayBase * strikes + 40));
        }

        host.remove();
    }

    public LivingEntity GetHost() {
        return this.host;
    }

    public String GetName() {
        return this.locales.GetLocaleString("ZombieBossName");
    }

    public void Reward() {
        int max = this.reader.GetMaxItemsDropConfig();
        int min = this.reader.GetMinItemsDropConfig();
        Random rnd = new Random();
        int amount = rnd.nextInt(max - min) + min;
        if (amount == 0) {
            ++amount;
        }

        amount *= this.reader.GetZombieBossItemMultiplier();
        BloodmoonActuator actuator = BloodmoonActuator.GetActuator(this.world);

        int expDrop;
        for(expDrop = 0; expDrop < amount; ++expDrop) {
            this.world.dropItemNaturally(this.host.getLocation(), new ItemStack(actuator.GetRandomBonus()));
        }

        expDrop = rnd.nextInt(2) + 1;
        expDrop *= this.reader.GetZombieBossExpMultiplier() * this.reader.GetExpMultConfig();

        for(int i = 0; i < expDrop; ++i) {
            ExperienceOrb orb = (ExperienceOrb)this.world.spawn(this.host.getLocation(), ExperienceOrb.class);
            orb.setExperience(100);
        }

        this.world.playSound(this.host.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
        this.world.spawnParticle(Particle.CLOUD, this.host.getLocation(), 100);
    }

    @Override
    public void Announce() {
        LocaleReader.MessageAllLocale("ZombieBossSpawned", new String[]{"$b"}, new String[]{GetName()}, world);
    }
}
