package net.refractored.bloodmoon.listeners;

import net.refractored.bloodmoon.Bloodmoon;
import net.refractored.bloodmoon.readers.ConfigReader;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import static net.refractored.bloodmoon.managers.BloodmoonManager.*;

public class EntityDamageListener implements Listener {
    @EventHandler
    public void onEntityDamage (EntityDamageByEntityEvent event) {
        if (!isInProgress()) return; //Only during BloodMoon

        Entity receiver = event.getEntity();
        Entity damager = event.getDamager();

        if (receiver.getWorld() != world || damager.getWorld() != world) return; //Wrong world
        if (damager instanceof Projectile) //if its any damage dealing projectile
        {
            ProjectileSource source = ((Projectile) damager).getShooter(); //Get the shooter as Source
            if (source instanceof LivingEntity) //Source is a mob, not a block
            {
                damager = (LivingEntity) source;
            }
        }


        if (receiver instanceof LivingEntity && damager instanceof LivingEntity)
        {
            ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
            if (receiver instanceof Player)
            {
                for (EntityType type : rewardedTypes)
                {
                    if (damager.getType() == type)
                    {
                        //Player is damaged by monster
                        if (event.getFinalDamage() == 0 && configReader.GetShieldPreventEffects())
                            return; //Hit was shielded. We shall not apply configs
                        event.setDamage(event.getDamage() * configReader.GetMobDamageMultConfig());
                        ApplySpecialEffect((Player) receiver, (LivingEntity) damager);
                        if (configReader.GetPlayerDamageSoundConfig())
                            ((Player) receiver).playSound(receiver.getLocation(), Sound.AMBIENT_CAVE, 80.0f, 1.5f);
                        if (configReader.GetPlayerHitParticleConfig())
                            world.spawnParticle(Particle.FLAME, receiver.getLocation(), 60);
                    }
                }
            } else if (damager instanceof Player)
            {
                for (EntityType type : rewardedTypes)
                {
                    if (receiver.getType() == type)
                    {
                        //Player dealt damage to monster
                        event.setDamage((int) Math.ceil(event.getDamage() / configReader.GetMobHealthMultConfig()));
                        if (configReader.GetMobHitParticleConfig())
                            world.spawnParticle(Particle.CRIT_MAGIC, receiver.getLocation(), 60);

                    }
                }
            }
        }
    }
}
