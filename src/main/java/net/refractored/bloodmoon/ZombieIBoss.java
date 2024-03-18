//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

/**
 * Not gonna lie I lost this class and had to decompile a more recent build of this plugin
 * This class will be rewritten for clarity as needed
 */


package net.refractored.bloodmoon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class ZombieIBoss extends Boss {
    Zombie zombieHost;

    public ZombieIBoss(Location location) {
        host = (Monster) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
        zombieHost = (Zombie)host;
        world = host.getWorld();
        reader = Bloodmoon.GetInstance().getConfigReader(world);
        locales = Bloodmoon.GetInstance().getLocaleReader();
        scheduler = Bloodmoon.GetInstance().GetScheduler();
        tasks = new ArrayList();
    }

    public void Start() {
        host.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, reader.GetZombieBossHealth()));
        host.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, reader.GetZombieBossDamage()));
        host.setCustomName(locales.GetLocaleString("ZombieBossName"));
        host.setCustomNameVisible(true);
        zombieHost.setAdult();
        Announce();
        tasks.add(scheduler.scheduleSyncRepeatingTask(Bloodmoon.GetInstance(), new Runnable() {
            public void run() {
                world.strikeLightningEffect(host.getLocation());
            }
        }, 0L, 238L));
        tasks.add(scheduler.scheduleSyncRepeatingTask(Bloodmoon.GetInstance(), new Runnable() {
            public void run() {
                world.spawnParticle(Particle.PORTAL, host.getLocation(), 60);
            }
        }, 0L, 3L));
        world.spawnParticle(Particle.EXPLOSION_HUGE, host.getLocation(), 60);
        ParseSpells();
    }

    @Override
    public void Kill(boolean reward)
    {
        Kill(reward, true);
    }
    @Override
    public void Kill(boolean reward, boolean effects)
    {
        Kill(reward, effects, true);
    }
    @Override
    public void Kill(boolean reward, boolean effects, boolean respawn)
    {
        Iterator taskIterator = tasks.iterator();

        while(taskIterator.hasNext()) {
            Integer task = (Integer)taskIterator.next();
            scheduler.cancelTask(task);
        }

        tasks.clear();

        for(int i = 0; i < 5; ++i) {
            world.strikeLightningEffect(host.getLocation());
        }

        int delayBase = 10;
        int strikes = 8;
        if(effects) {

            for (int i = 0; i < strikes; ++i) {
                scheduler.scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable() {
                    public void run() {
                        world.strikeLightningEffect(host.getLocation());
                        world.spawnParticle(Particle.EXPLOSION_HUGE, host.getLocation(), 20);
                    }
                }, (long) (delayBase * i));
            }
        }

        if (reward) {
            if(effects) {
                scheduler.scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable() {
                    public void run() {
                        Reward();
                    }
                }, (long) (delayBase * strikes + 40));
            }else{
                Reward();
            }
        }

        //Respawn boss if in perma-bloodmoon
        if(reader.GetPermanentBloodMoonConfig() && effects && respawn)
        {
            scheduler.scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable() {
                @Override
                public void run() {
                    BloodmoonActuator.GetActuator(world).SpawnZombieBoss();
                }
            }, reader.GetBossRespawnTime("Zombie"));
        }

        host.remove();
    }



    public Monster GetHost() {
        return host;
    }

    public String GetName() {
        return locales.GetLocaleString("ZombieBossName");
    }

    public void Reward() {
        int max = reader.GetMaxItemsDropConfig();
        int min = reader.GetMinItemsDropConfig();
        Random rnd = new Random();
        int amount = rnd.nextInt(max - min) + min;
        if (amount == 0) {
            ++amount;
        }

        amount *= reader.GetZombieBossItemMultiplier();
        BloodmoonActuator actuator = BloodmoonActuator.GetActuator(world);

        int expDrop;
        for(expDrop = 0; expDrop < amount; ++expDrop) {
            world.dropItemNaturally(host.getLocation(), actuator.GetRandomBonus());
        }

        expDrop = rnd.nextInt(2) + 1;
        expDrop *= reader.GetZombieBossExpMultiplier() * reader.GetExpMultConfig();

        for(int i = 0; i < expDrop; ++i) {
            ExperienceOrb orb = (ExperienceOrb)world.spawn(host.getLocation(), ExperienceOrb.class);
            orb.setExperience(100);
        }

        world.playSound(host.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
        world.spawnParticle(Particle.CLOUD, host.getLocation(), 100);
    }

    @Override
    public void Announce() {
        LocaleReader.MessageAllLocale("ZombieBossSpawned", new String[]{"$b"}, new String[]{GetName()}, world);
    }
}
