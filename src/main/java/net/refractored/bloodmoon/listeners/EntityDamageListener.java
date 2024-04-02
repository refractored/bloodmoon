package net.refractored.bloodmoon.listeners;

import com.willfp.eco.core.particle.Particles;
import net.refractored.bloodmoon.Bloodmoon;
import net.refractored.bloodmoon.managers.BloodmoonManager;
import net.refractored.bloodmoon.readers.ConfigReader;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.io.ObjectInputFilter;
import java.util.Arrays;
import java.util.logging.Logger;

import static net.refractored.bloodmoon.managers.BloodmoonManager.*;

public class EntityDamageListener implements Listener {
    @EventHandler
    public void onEntityDamage (EntityDamageByEntityEvent event) {
        if (!BloodmoonManager.GetActuator(world).isInProgress()) return; //Only during BloodMoon

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
            Double[] Damage = configReader.GetMobDamageMultConfig();
            int Bloodmoonlevel = (GetActuator(world).getBloodMoonLevel() - 1);
            if (receiver instanceof Player)
            {
                for (EntityType type : rewardedTypes)
                {
                    if (damager.getType() == type)
                    {
                        //Player is damaged by monster
                        if (event.getFinalDamage() == 0 && configReader.GetShieldPreventEffects()) return;
                        //Hit was shielded. We shall not apply configs
                        event.setDamage(event.getDamage() * Damage[Bloodmoonlevel]);
                        ApplySpecialEffect((Player) receiver, (LivingEntity) damager);
                        if (configReader.GetPlayerDamageSoundConfig())
                            ((Player) receiver).playSound(receiver.getLocation(), Sound.AMBIENT_CAVE, 80.0f, 1.5f);
                        if (configReader.GetPlayerHitParticleConfig())
                             Particles.lookup("FLAME").spawn(receiver.getLocation(), 60);

                    }
                }
            } else if (damager instanceof Player)
            {
                for (EntityType type : rewardedTypes)
                {
                    if (receiver.getType() == type)
                    {
                        Double[] Health = configReader.GetMobDamageMultConfig();
                        //Player dealt damage to monster
                        event.setDamage((int) Math.ceil(event.getDamage() / Health[Bloodmoonlevel]));
                        if (configReader.GetMobHitParticleConfig())
                            Particles.lookup("CRIT_MAGIC").spawn(receiver.getLocation(), 60);
                    }
                }
            }
        }
    }
}
