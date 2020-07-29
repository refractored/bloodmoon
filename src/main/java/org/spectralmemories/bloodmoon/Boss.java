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
import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

public abstract class Boss implements IBoss, Listener {
    protected ConfigReader reader;
    protected LocaleReader locales;
    protected Monster host;
    protected List<Integer> tasks;
    protected BukkitScheduler scheduler;
    protected World world;
    public final int CHANNEL_TIME_TICKS = 30;

    public Boss() {
    }

    protected void ParseSpells() {
        String[] spells = reader.GetZombieBossPowerSet();
        int spellsCount = spells.length;
        int spellIndex = 0;

        while(spellIndex < spellsCount) {
            String spell = spells[spellIndex];
            String[] parts = spell.split(",");
            String spellName = parts[0];
            final int range = Integer.parseInt(parts[1]);
            int cd = (int)Math.ceil((double)(Float.parseFloat(parts[2]) * 20.0F));
            int thirdParameter = 0;
            int fourthParameter = 0;

            switch(spellName) {
                //4 parameters
                case "WITHER":
                case "POISON":
                    fourthParameter = Integer.parseInt(parts[4]);
                //3 parameters
                case "STUN":
                case "BLIND":
                case "UNDERLING":
                case "FIRE":
                case "SPRINT":
                    thirdParameter = Integer.parseInt(parts[3]);
                default:
                    switch(spellName) {
                        case "LIGHTNING":
                            tasks.add(scheduler.scheduleSyncRepeatingTask(Bloodmoon.GetInstance(), new Runnable() {
                                public void run() {
                                    if (HasTarget()) {
                                        Lightning(range);
                                    }

                                }
                            }, (long)cd, (long)cd));
                            break;
                        case "BLINK":
                            tasks.add(scheduler.scheduleSyncRepeatingTask(Bloodmoon.GetInstance(), new Runnable() {
                                public void run() {
                                    if (HasTarget()) {
                                        Blink(range);
                                    }

                                }
                            }, (long)cd, (long)cd));
                            break;
                        case "FIRE":
                            int finalThirdParameter4 = thirdParameter;
                            tasks.add(scheduler.scheduleSyncRepeatingTask(Bloodmoon.GetInstance(), new Runnable() {
                                public void run() {
                                    if (HasTarget()) {
                                        Fire(range, (float) finalThirdParameter4);
                                    }

                                }
                            }, (long)cd, (long)cd));
                            break;
                        case "BLIND":
                            int finalThirdParameter1 = thirdParameter;
                            tasks.add(scheduler.scheduleSyncRepeatingTask(Bloodmoon.GetInstance(), new Runnable() {
                                public void run() {
                                    if (HasTarget()) {
                                        Blind(range, (float) finalThirdParameter1);
                                    }

                                }
                            }, (long)cd, (long)cd));
                            break;
                        case "UNDERLING":
                            int finalThirdParameter = thirdParameter;
                            tasks.add(scheduler.scheduleSyncRepeatingTask(Bloodmoon.GetInstance(), new Runnable() {
                                public void run() {
                                    if (HasTarget()) {
                                        Underlings(range, finalThirdParameter);
                                    }

                                }
                            }, (long)cd, (long)cd));
                            break;
                        case "SPRINT":
                            int finalThirdParameter5 = thirdParameter;
                            tasks.add(scheduler.scheduleSyncRepeatingTask(Bloodmoon.GetInstance(), new Runnable() {
                                public void run() {
                                    if (HasTarget()) {
                                        Sprint(range, finalThirdParameter5);
                                    }

                                }
                            }, (long)cd, (long)cd));
                            break;
                        case "POISON":
                            int finalFourthParameter = fourthParameter;
                            int finalThirdParameter3 = thirdParameter;
                            tasks.add(scheduler.scheduleSyncRepeatingTask(Bloodmoon.GetInstance(), new Runnable() {
                                public void run() {
                                    if (HasTarget()) {
                                        Poison(range, (float) finalThirdParameter3, finalFourthParameter);
                                    }
                                }
                            }, (long)cd, (long)cd));
                            break;
                        case "WITHER":
                            int finalFourthParameter1 = fourthParameter;
                            int finalThirdParameter2 = thirdParameter;
                            tasks.add(scheduler.scheduleSyncRepeatingTask(Bloodmoon.GetInstance(), new Runnable() {
                                public void run() {
                                    if (HasTarget()) {
                                        Wither(range, (float) finalThirdParameter2, finalFourthParameter1);
                                    }

                                }
                            }, (long)cd, (long)cd));
                            break;
                        default:
                            LocaleReader.BroadcastLocale("GeneralError", null, null);
                    }

                    ++spellIndex;
            }
        }

    }

    /**
     * @return Does the host has an actively hunted target
     */
    private boolean HasTarget() {
        if (host != null) {
            return host.getTarget() != null;
        } else {
            return false;
        }
    }

    /**
     * Finds all instances of LivingEntity in a range
     * @param range The range in meter (blocks), used as a radius
     * @return The fixed 1D array of LivingEntity
     */
    private LivingEntity[] GetNearbyEntities(int range) {
        List<LivingEntity> entities = new ArrayList();
        Iterator entityIterator = world.getNearbyEntities(host.getLocation(), (double)range, (double)range, (double)range).iterator();

        while(entityIterator.hasNext()) {
            Entity entity = (Entity)entityIterator.next();
            if (entity instanceof LivingEntity && entity != host && !(entity instanceof Monster)) {
                entities.add((LivingEntity)entity);
            }
        }

        return entities.toArray(new LivingEntity[0]);
    }

    protected void Lightning(final int range) {
        world.spawnParticle(Particle.EXPLOSION_HUGE, host.getLocation(), 100);
        world.playSound(host.getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0F, 1.0F);
        host.setAI(false);
        scheduler.scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable() {
            public void run() {
                world.spawnParticle(Particle.EXPLOSION_HUGE, host.getLocation(), 100);
                world.playSound(host.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
                LivingEntity[] entities = GetNearbyEntities(range);
                int entityCount = entities.length;

                for(int i = 0; i < entityCount; ++i) {
                    LivingEntity entity = entities[i];
                    world.strikeLightning(entity.getLocation());
                }

                host.setAI(true);
            }
        }, CHANNEL_TIME_TICKS);
    }

    protected void Fire(final int range, final float duration) {
        world.spawnParticle(Particle.LAVA, host.getLocation(), 100);
        world.playSound(host.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0F, 1.0F);
        host.setAI(false);
        scheduler.scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable() {
            public void run() {
                world.spawnParticle(Particle.LAVA, host.getLocation(), 100);
                world.playSound(host.getLocation(), Sound.BLOCK_FURNACE_FIRE_CRACKLE, 1.0F, 1.0F);
                LivingEntity[] var1 = GetNearbyEntities(range);
                int var2 = var1.length;

                for(int var3 = 0; var3 < var2; ++var3) {
                    LivingEntity entity = var1[var3];
                    entity.setFireTicks((int)Math.ceil((double)(duration * 20.0F)));
                }

                host.setAI(true);
            }
        }, CHANNEL_TIME_TICKS);
    }

    protected void Blind(final int range, final float duration) {
        world.spawnParticle(Particle.CRIT_MAGIC, host.getLocation(), 100);
        world.playSound(host.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 1.0F, 1.0F);
        host.setAI(false);
        scheduler.scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable() {
            public void run() {
                world.spawnParticle(Particle.CRIT_MAGIC, host.getLocation(), 100);
                world.playSound(host.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F);
                LivingEntity[] var1 = GetNearbyEntities(range);
                int var2 = var1.length;

                for(int var3 = 0; var3 < var2; ++var3) {
                    LivingEntity entity = var1[var3];
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int)Math.ceil((double)(duration * 20.0F)), 1));
                }

                host.setAI(true);
            }
        }, CHANNEL_TIME_TICKS);
    }

    protected void Sprint (final int duration, final int amplifier){

    }

    protected void Blink (final int range){
        world.spawnParticle(Particle.END_ROD, host.getLocation(), 100);
        world.playSound(host.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1.0F, 1.0F);
        host.setAI(false);
        Random random = new Random();
        scheduler.scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable() {
            public void run() {
                world.spawnParticle(Particle.EXPLOSION_NORMAL, host.getLocation(), 100);
                world.playSound(host.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                LivingEntity[] livingEntities = GetNearbyEntities(range);
                int len = livingEntities.length;

                if(len > 0){
                    int chosenOneIndex = random.nextInt(len);
                    LivingEntity chosenOne = livingEntities[chosenOneIndex];

                    Location tpLocation = chosenOne.getLocation().clone();
                    tpLocation = tpLocation.subtract(chosenOne.getLocation().getDirection().normalize().multiply(2.5));
                    tpLocation.setY(world.getHighestBlockYAt(tpLocation));
                    host.teleport(tpLocation);
                }

                host.setAI(true);
            }
        }, CHANNEL_TIME_TICKS);
    }

    protected void Underlings(final int range, final int amount) {
        world.spawnParticle(Particle.WATER_BUBBLE, host.getLocation(), 100);
        world.playSound(host.getLocation(), Sound.BLOCK_WATER_AMBIENT, 1.0F, 1.0F);
        final Random rnd = new Random();
        final BloodmoonActuator actuator = BloodmoonActuator.GetActuator(world);
        host.setAI(false);
        scheduler.scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable() {
            public void run() {
                for(int i = 0; i < amount; ++i) {
                    Location location = host.getLocation().clone();
                    location.add((double)rnd.nextInt(range), 0.0D, (double)rnd.nextInt(range));
                    location.setY((double)world.getHighestBlockYAt(location));
                    Entity entity = world.spawn(location, host.getClass());
                    world.spawnParticle(Particle.PORTAL, location, 100);
                    world.playSound(location, Sound.ENTITY_ZOMBIE_AMBIENT, 1.0F, 1.0F);
                    if (actuator != null && entity instanceof LivingEntity) {
                        actuator.AddToBlacklist((LivingEntity)entity);
                    }
                }

                host.setAI(true);
            }
        }, CHANNEL_TIME_TICKS);
    }

    protected void Poison(final int range, final float duration, final int amplifier) {
        world.spawnParticle(Particle.SPIT, host.getLocation(), 100);
        world.playSound(host.getLocation(), Sound.ENTITY_VEX_AMBIENT, 1.0F, 1.0F);
        host.setAI(false);
        scheduler.scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable() {
            public void run() {
                world.spawnParticle(Particle.SPIT, host.getLocation(), 100);
                world.playSound(host.getLocation(), Sound.ENTITY_VEX_CHARGE, 1.0F, 1.0F);
                LivingEntity[] var1 = GetNearbyEntities(range);
                int var2 = var1.length;

                for(int var3 = 0; var3 < var2; ++var3) {
                    LivingEntity entity = var1[var3];
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int)Math.ceil((double)(duration * 20.0F)), amplifier));
                }

                host.setAI(true);
            }
        }, CHANNEL_TIME_TICKS);
    }

    protected void Wither(final int range, final float duration, final int amplifier) {
        world.spawnParticle(Particle.DAMAGE_INDICATOR, host.getLocation(), 100);
        world.playSound(host.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1.0F, 1.0F);
        host.setAI(false);
        scheduler.scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable() {
            public void run() {
                world.spawnParticle(Particle.DAMAGE_INDICATOR, host.getLocation(), 100);
                world.playSound(host.getLocation(), Sound.ENTITY_BLAZE_DEATH, 1.0F, 1.0F);
                LivingEntity[] var1 = GetNearbyEntities(range);
                int var2 = var1.length;

                for(int var3 = 0; var3 < var2; ++var3) {
                    LivingEntity entity = var1[var3];
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, (int)Math.ceil((double)(duration * 20.0F)), amplifier));
                }

                host.setAI(true);
            }
        }, CHANNEL_TIME_TICKS);
    }
}
