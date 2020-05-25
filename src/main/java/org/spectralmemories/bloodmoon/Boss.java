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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

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
        String[] spells = this.reader.GetZombieBossPowerSet();
        String[] var2 = spells;
        int var3 = spells.length;
        int var4 = 0;

        while(var4 < var3) {
            String spell = var2[var4];
            String[] parts = spell.split(",");
            String spellName = parts[0];
            final int range = Integer.parseInt(parts[1]);
            int cd = (int)Math.ceil((double)(Float.parseFloat(parts[2]) * 20.0F));
            int thirdParameter = 0;
            int fourthParameter = 0;
            byte var13 = -1;
            switch(spellName.hashCode()) {
                case -1929420024:
                    if (spellName.equals("POISON")) {
                        var13 = 1;
                    }
                    break;
                case -1734240269:
                    if (spellName.equals("WITHER")) {
                        var13 = 0;
                    }
                    break;
                case 2158134:
                    if (spellName.equals("FIRE")) {
                        var13 = 5;
                    }
                    break;
                case 2556090:
                    if (spellName.equals("STUN")) {
                        var13 = 2;
                    }
                    break;
                case 63289141:
                    if (spellName.equals("BLIND")) {
                        var13 = 3;
                    }
                    break;
                case 1759631022:
                    if (spellName.equals("UNDERLING")) {
                        var13 = 4;
                    }
            }

            switch(var13) {
                case 0:
                case 1:
                    fourthParameter = Integer.parseInt(parts[4]);
                case 2:
                case 3:
                case 4:
                case 5:
                    thirdParameter = Integer.parseInt(parts[3]);
                default:
                    var13 = -1;
                    switch(spellName.hashCode()) {
                        case -1929420024:
                            if (spellName.equals("POISON")) {
                                var13 = 4;
                            }
                            break;
                        case -1734240269:
                            if (spellName.equals("WITHER")) {
                                var13 = 5;
                            }
                            break;
                        case -821927254:
                            if (spellName.equals("LIGHTNING")) {
                                var13 = 0;
                            }
                            break;
                        case 2158134:
                            if (spellName.equals("FIRE")) {
                                var13 = 1;
                            }
                            break;
                        case 63289141:
                            if (spellName.equals("BLIND")) {
                                var13 = 2;
                            }
                            break;
                        case 1759631022:
                            if (spellName.equals("UNDERLING")) {
                                var13 = 3;
                            }
                    }

                    switch(var13) {
                        case 0:
                            this.tasks.add(this.scheduler.scheduleSyncRepeatingTask(Bloodmoon.GetInstance(), new Runnable() {
                                public void run() {
                                    if (Boss.this.HasTarget()) {
                                        Boss.this.Lightning(range);
                                    }

                                }
                            }, (long)cd, (long)cd));
                            break;
                        case 1:
                            int finalThirdParameter4 = thirdParameter;
                            this.tasks.add(this.scheduler.scheduleSyncRepeatingTask(Bloodmoon.GetInstance(), new Runnable() {
                                public void run() {
                                    if (Boss.this.HasTarget()) {
                                        Boss.this.Fire(range, (float) finalThirdParameter4);
                                    }

                                }
                            }, (long)cd, (long)cd));
                            break;
                        case 2:
                            int finalThirdParameter1 = thirdParameter;
                            this.tasks.add(this.scheduler.scheduleSyncRepeatingTask(Bloodmoon.GetInstance(), new Runnable() {
                                public void run() {
                                    if (Boss.this.HasTarget()) {
                                        Boss.this.Blind(range, (float) finalThirdParameter1);
                                    }

                                }
                            }, (long)cd, (long)cd));
                            break;
                        case 3:
                            int finalThirdParameter = thirdParameter;
                            this.tasks.add(this.scheduler.scheduleSyncRepeatingTask(Bloodmoon.GetInstance(), new Runnable() {
                                public void run() {
                                    if (Boss.this.HasTarget()) {
                                        Boss.this.Underlings(range, finalThirdParameter);
                                    }

                                }
                            }, (long)cd, (long)cd));
                            break;
                        case 4:
                            int finalFourthParameter = fourthParameter;
                            int finalThirdParameter3 = thirdParameter;
                            this.tasks.add(this.scheduler.scheduleSyncRepeatingTask(Bloodmoon.GetInstance(), new Runnable() {
                                public void run() {
                                    if (Boss.this.HasTarget()) {
                                        Boss.this.Poison(range, (float) finalThirdParameter3, finalFourthParameter);
                                    }

                                }
                            }, (long)cd, (long)cd));
                            break;
                        case 5:
                            int finalFourthParameter1 = fourthParameter;
                            int finalThirdParameter2 = thirdParameter;
                            this.tasks.add(this.scheduler.scheduleSyncRepeatingTask(Bloodmoon.GetInstance(), new Runnable() {
                                public void run() {
                                    if (Boss.this.HasTarget()) {
                                        Boss.this.Wither(range, (float) finalThirdParameter2, finalFourthParameter1);
                                    }

                                }
                            }, (long)cd, (long)cd));
                            break;
                        default:
                            LocaleReader.BroadcastLocale("GeneralError", null, null);
                    }

                    ++var4;
            }
        }

    }

    private boolean HasTarget() {
        Monster monster = (Monster)this.host;
        if (monster != null) {
            return monster.getTarget() != null;
        } else {
            return false;
        }
    }

    @EventHandler
    public void OnMobDeath (EntityDeathEvent event)
    {
        if (event.getEntity() == host)
        {
            Kill(event.getEntity().getKiller() != null);
        }
    }

    protected void MonitorDeath()
    {
        Bloodmoon.GetInstance().getServer().getPluginManager().registerEvents(this, Bloodmoon.GetInstance());
    }

    private LivingEntity[] GetNearbyEntities(int range) {
        List<LivingEntity> entities = new ArrayList();
        Iterator var3 = this.world.getNearbyEntities(this.host.getLocation(), (double)range, (double)range, (double)range).iterator();

        while(var3.hasNext()) {
            Entity entity = (Entity)var3.next();
            if (entity instanceof LivingEntity && entity != this.host && !(entity instanceof Monster)) {
                entities.add((LivingEntity)entity);
            }
        }

        return (LivingEntity[])entities.toArray(new LivingEntity[0]);
    }

    protected void Lightning(final int range) {
        this.world.spawnParticle(Particle.EXPLOSION_HUGE, this.host.getLocation(), 100);
        this.world.playSound(this.host.getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0F, 1.0F);
        this.host.setAI(false);
        this.scheduler.scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable() {
            public void run() {
                Boss.this.world.spawnParticle(Particle.EXPLOSION_HUGE, Boss.this.host.getLocation(), 100);
                Boss.this.world.playSound(Boss.this.host.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
                LivingEntity[] var1 = Boss.this.GetNearbyEntities(range);
                int var2 = var1.length;

                for(int var3 = 0; var3 < var2; ++var3) {
                    LivingEntity entity = var1[var3];
                    Boss.this.world.strikeLightning(entity.getLocation());
                }

                Boss.this.host.setAI(true);
            }
        }, 30L);
    }

    protected void Fire(final int range, final float duration) {
        this.world.spawnParticle(Particle.LAVA, this.host.getLocation(), 100);
        this.world.playSound(this.host.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0F, 1.0F);
        this.host.setAI(false);
        this.scheduler.scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable() {
            public void run() {
                Boss.this.world.spawnParticle(Particle.LAVA, Boss.this.host.getLocation(), 100);
                Boss.this.world.playSound(Boss.this.host.getLocation(), Sound.BLOCK_FURNACE_FIRE_CRACKLE, 1.0F, 1.0F);
                LivingEntity[] var1 = Boss.this.GetNearbyEntities(range);
                int var2 = var1.length;

                for(int var3 = 0; var3 < var2; ++var3) {
                    LivingEntity entity = var1[var3];
                    entity.setFireTicks((int)Math.ceil((double)(duration * 20.0F)));
                }

                Boss.this.host.setAI(true);
            }
        }, 30L);
    }

    protected void Blind(final int range, final float duration) {
        this.world.spawnParticle(Particle.CRIT_MAGIC, this.host.getLocation(), 100);
        this.world.playSound(this.host.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 1.0F, 1.0F);
        this.host.setAI(false);
        this.scheduler.scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable() {
            public void run() {
                Boss.this.world.spawnParticle(Particle.CRIT_MAGIC, Boss.this.host.getLocation(), 100);
                Boss.this.world.playSound(Boss.this.host.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F);
                LivingEntity[] var1 = Boss.this.GetNearbyEntities(range);
                int var2 = var1.length;

                for(int var3 = 0; var3 < var2; ++var3) {
                    LivingEntity entity = var1[var3];
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int)Math.ceil((double)(duration * 20.0F)), 1));
                }

                Boss.this.host.setAI(true);
            }
        }, 30L);
    }

    protected void Underlings(final int range, final int amount) {
        this.world.spawnParticle(Particle.WATER_BUBBLE, this.host.getLocation(), 100);
        this.world.playSound(this.host.getLocation(), Sound.BLOCK_WATER_AMBIENT, 1.0F, 1.0F);
        final Random rnd = new Random();
        final BloodmoonActuator actuator = BloodmoonActuator.GetActuator(this.world);
        this.host.setAI(false);
        this.scheduler.scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable() {
            public void run() {
                for(int i = 0; i < amount; ++i) {
                    Location location = Boss.this.host.getLocation().clone();
                    location.add((double)rnd.nextInt(range), 0.0D, (double)rnd.nextInt(range));
                    location.setY((double)Boss.this.world.getHighestBlockYAt(location));
                    Entity entity = Boss.this.world.spawn(location, Boss.this.host.getClass());
                    Boss.this.world.spawnParticle(Particle.PORTAL, location, 100);
                    Boss.this.world.playSound(location, Sound.ENTITY_ZOMBIE_AMBIENT, 1.0F, 1.0F);
                    if (actuator != null && entity instanceof LivingEntity) {
                        actuator.AddToBlacklist((LivingEntity)entity);
                    }
                }

                Boss.this.host.setAI(true);
            }
        }, 30L);
    }

    protected void Poison(final int range, final float duration, final int amplifier) {
        this.world.spawnParticle(Particle.SPIT, this.host.getLocation(), 100);
        this.world.playSound(this.host.getLocation(), Sound.ENTITY_VEX_AMBIENT, 1.0F, 1.0F);
        this.host.setAI(false);
        this.scheduler.scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable() {
            public void run() {
                Boss.this.world.spawnParticle(Particle.SPIT, Boss.this.host.getLocation(), 100);
                Boss.this.world.playSound(Boss.this.host.getLocation(), Sound.ENTITY_VEX_CHARGE, 1.0F, 1.0F);
                LivingEntity[] var1 = Boss.this.GetNearbyEntities(range);
                int var2 = var1.length;

                for(int var3 = 0; var3 < var2; ++var3) {
                    LivingEntity entity = var1[var3];
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int)Math.ceil((double)(duration * 20.0F)), amplifier));
                }

                Boss.this.host.setAI(true);
            }
        }, 30L);
    }

    protected void Wither(final int range, final float duration, final int amplifier) {
        this.world.spawnParticle(Particle.DAMAGE_INDICATOR, this.host.getLocation(), 100);
        this.world.playSound(this.host.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1.0F, 1.0F);
        this.host.setAI(false);
        this.scheduler.scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable() {
            public void run() {
                Boss.this.world.spawnParticle(Particle.DAMAGE_INDICATOR, Boss.this.host.getLocation(), 100);
                Boss.this.world.playSound(Boss.this.host.getLocation(), Sound.ENTITY_BLAZE_DEATH, 1.0F, 1.0F);
                LivingEntity[] var1 = Boss.this.GetNearbyEntities(range);
                int var2 = var1.length;

                for(int var3 = 0; var3 < var2; ++var3) {
                    LivingEntity entity = var1[var3];
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, (int)Math.ceil((double)(duration * 20.0F)), amplifier));
                }

                Boss.this.host.setAI(true);
            }
        }, 30L);
    }
}
