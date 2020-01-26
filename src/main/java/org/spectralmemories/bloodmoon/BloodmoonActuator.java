package org.spectralmemories.bloodmoon;


import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.*;


public class BloodmoonActuator implements Listener, Runnable
{
    //Eligible mobs
    public final EntityType[] rewardedTypes = {
            EntityType.ZOMBIE,
            EntityType.SKELETON,
            EntityType.SPIDER,
            EntityType.CREEPER,
            EntityType.HUSK,
            EntityType.DROWNED,
            EntityType.WITCH,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.PHANTOM
    };

    private static Map<World, BloodmoonActuator> actuators;

    private World world;
    private boolean inProgress;

    private BossBar nightBar;
    private ActuatorPeriodic actuatorPeriodic;

    private List<LivingEntity> blacklistedMobs;

    private void AddActuator (BloodmoonActuator instance)
    {
        if (actuators == null) actuators = new HashMap<>();

        actuators.put(instance.world, instance);
    }

    public static BloodmoonActuator GetActuator (World world)
    {
        try
        {
            return actuators.get(world);
        }
        catch (Exception ignored){}
        return null;
    }


    public BloodmoonActuator(World world)
    {
        this.world = world;
        inProgress = false;
        AddActuator(this);
        blacklistedMobs = new ArrayList<>();
    }

    public void StartBloodMoon ()
    {
        inProgress = true;

        ShowNightBar();
        BroadcastBloodMoonWarning();

        actuatorPeriodic = new ActuatorPeriodic(world);
        actuatorPeriodic.run();
    }

    public void StopBloodMoon ()
    {
        inProgress = false;

        StopStorm ();
        HideNightBar();

        actuatorPeriodic.Stop();
        actuatorPeriodic = null;
    }

    private void StopStorm ()
    {
        world.setStorm(false);
        world.setThundering(false);
    }

    private void ShowNightBar ()
    {
        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);

        if (configReader.GetDarkenSkyConfig())
        {
            nightBar = Bukkit.createBossBar(localeReader.GetLocaleString("BloodMoonTitleBar"),
                    BarColor.RED,
                    BarStyle.SEGMENTED_12,
                    BarFlag.CREATE_FOG,
                    BarFlag.DARKEN_SKY
            );
        }
        else
        {
            nightBar = Bukkit.createBossBar(localeReader.GetLocaleString("BloodMoonTitleBar"),
                    BarColor.RED,
                    BarStyle.SEGMENTED_12
            );
        }
        nightBar.setProgress(0.0);
        Bloodmoon.GetInstance().GetScheduler().runTaskLater(Bloodmoon.GetInstance(), this,0);

        List<Player> players = world.getPlayers();
        for (Player player : players)
        {
            nightBar.addPlayer(player);
        }

        UpdateNightBar();
    }

    private void HideNightBar ()
    {
        if (nightBar != null) nightBar.removeAll();
        nightBar = null;
    }

    private void HideNightBarPlayer (Player player)
    {
        if (nightBar != null) nightBar.removePlayer(player);
    }

    private void UpdateNightBar ()
    {
        long timeTotal = 12000;
        long currentTime = world.getTime();
        long timeLeft = PeriodicNightCheck.DAY - currentTime;

        double percent = (double) timeLeft / (double) timeTotal;

        if (nightBar != null && percent >= 0.0 && percent <= 1.0f) nightBar.setProgress(1.0 - percent);
    }

    private void HandleReconnectingPlayer (Player player)
    {
        if (isInProgress() && nightBar != null) nightBar.addPlayer(player);
        BroadcastBloodMoonWarningPlayer(player);
    }

    private void BroadcastBloodMoonWarning ()
    {

        for (Player player : world.getPlayers())
        {
            BroadcastBloodMoonWarningPlayer(player);
        }
    }

    private void BroadcastBloodMoonWarningPlayer (Player player)
    {
        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);

        player.sendMessage(localeReader.GetLocaleString("BloodMoonWarningTitle"));
        player.sendMessage(localeReader.GetLocaleString("BloodMoonWarningBody"));

        if (configReader.GetInventoryLossConfig())
        {
            player.sendMessage(localeReader.GetLocaleString("DyingResultsInInventoryLoss"));
        }
        if (configReader.GetExperienceLossConfig())
        {
            player.sendMessage(localeReader.GetLocaleString("DyingResultsInExperienceLoss"));
        }
    }

    private Material GetRandomBonus ()
    {

        Random random = new Random(); //We want to regenerate it every time to ensure randomness

        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
        Material[] selection = configReader.GetItemListConfig();

        int amount = selection.length;

        return selection [random.nextInt(amount)];

    }

    private void ApplySpecialEffect (Player player, EntityType mob)
    {
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);

        String[] configs = configReader.GetMobEffectConfig(mob.name());

        for (String str : configs)
        {
            if (str.equals("LIGHTNING"))
            {
                world.strikeLightning(player.getLocation());
                continue;
            }
            String[] parts = str.split(",");
            PotionEffectType[] types = PotionEffectType.values();
            String effectName = parts[0];
            int ticks = (int) (20f * Float.parseFloat(parts[1]));
            int amp = Integer.parseInt(parts[2]);

            for (PotionEffectType type : types)
            {
                if (type.getName().equals(effectName))
                {
                    player.addPotionEffect(new PotionEffect(type, ticks, amp));
                    break;
                }
            }
            //Effect not found. Meh
        }
    }

    public boolean isInProgress ()
    {
        return inProgress;
    }



    //Events
    @EventHandler
    public void onPlayerConnect (PlayerJoinEvent event)
    {
        if (isInProgress() && event.getPlayer().getWorld() == world)
        {
            HandleReconnectingPlayer (event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerTeleport (PlayerTeleportEvent event)
    {
         World to = event.getTo().getWorld();
         World from = event.getFrom().getWorld();
         if (to != world && from != world) return; //None of our concern

         if (from != to)
         {
            if (to == world && isInProgress())
            {
                //Someone entered our bm world
                HandleReconnectingPlayer(event.getPlayer());
            }
            if (from == world && isInProgress())
            {
                //Someone left our bm world
                HideNightBarPlayer(event.getPlayer());
            }
         }
    }

    @EventHandler
    public void onPlayerRespawn (PlayerRespawnEvent event)
    {
        World from = event.getPlayer().getWorld();
        World to = event.getRespawnLocation().getWorld();
        if (to != world && from != world) return; //None of our concern

        if (from != to)
        {
            if (to == world && isInProgress())
            {
                //Someone respawned in our bm world
                HandleReconnectingPlayer(event.getPlayer());
            }
            if (from == world && isInProgress())
            {
                //Someone respawned out of our bm world
                HideNightBarPlayer(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerDeath (PlayerDeathEvent event)
    {
        if (!isInProgress()) return; //Only during BloodMoon

        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);

        Player deadplayer = event.getEntity();
        if (deadplayer.getWorld() != world) return; //Wrong world

        if (configReader.GetLightningEffectConfig()) world.strikeLightningEffect(deadplayer.getLocation());

        String deathMessage = event.getDeathMessage();

        if (! deathMessage.contains(localeReader.GetLocaleString("DeathSuffix")))
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

    @EventHandler
    public void onPlayerSleeps (PlayerBedEnterEvent event)
    {
        if (event.getPlayer().getWorld() == world)
        {
            if (isInProgress())
            {
                ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
                if (configReader.GetPreventSleepingConfig())
                {
                    LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();

                    event.getPlayer().sendMessage(localeReader.GetLocaleString("BedNotAllowed"));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onMobSpawn (SpawnerSpawnEvent event)
    {
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

    @EventHandler
    public void onMobDeath (EntityDeathEvent event)
    {
        if (!isInProgress()) return; //Only during BloodMoon



        if (event.getEntity() instanceof Player) return; //Handled in another method

        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);

        LivingEntity entity = event.getEntity();

        if (entity.getWorld() != world) return; //Wrong world

        if (blacklistedMobs.contains(entity))
        {
            //This mob was explicitely blacklisted. ignore it
            blacklistedMobs.remove(entity);
            return;
        }

        boolean eligible = false;
        for (EntityType type : rewardedTypes)
        {
            if (entity.getType() == type) eligible = true;
        }

        if (! eligible) return; //Not eligible for reward

        event.setDroppedExp(event.getDroppedExp() * configReader.GetExpMultConfig());

        if (configReader.GetMobDeathThunderConfig())
            world.strikeLightningEffect(event.getEntity().getLocation());

        List<ItemStack> bonusDrops = new ArrayList<>();

        int min = configReader.GetMinItemsDropConfig();
        int max= configReader.GetMaxItemsDropConfig();

        int itemCount = (max - min <= 0) ? min : new Random().nextInt(max - min) + min;

        for (int i = 0; i < itemCount; i++)
        {
            bonusDrops.add(new ItemStack(GetRandomBonus())); //Add the drops
        }

        for (ItemStack item : bonusDrops)
        {
            world.dropItemNaturally(entity.getLocation(), item); //Drop items
        }
    }

    @EventHandler
    public void onEntityDamage (EntityDamageByEntityEvent event)
    {
        if (!isInProgress()) return; //Only during BloodMoon

        Entity receiver = event.getEntity();
        Entity damager = event.getDamager();

        if (receiver.getWorld() != world || damager.getWorld() != world) return; //Wrong world
        if (damager instanceof Projectile) //if its arrow damage
        {
            ProjectileSource source = ((Projectile) damager).getShooter(); //Get the shooter as Source
            if (source instanceof LivingEntity) //Source is alive
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
                        event.setDamage(event.getDamage() * configReader.GetMobDamageMultConfig());
                        ApplySpecialEffect((Player) receiver, type);
                        if (configReader.GetPlayerDamageSoundConfig())
                            ((Player) receiver).playSound(receiver.getLocation(), Sound.AMBIENT_CAVE, 80.0f, 1.5f);
                    }
                }
             }
             else if (damager instanceof Player)
             {
                 for (EntityType type : rewardedTypes)
                 {
                     if (receiver.getType() == type)
                     {
                         //Player dealt damage to monster
                         event.setDamage((int) Math.ceil(event.getDamage() / configReader.GetMobHealthMultConfig()));
                     }
                 }
             }
        }
    }

    @Override
    public void run()
    {
        if (isInProgress())
        {
            UpdateNightBar();
            Bloodmoon.GetInstance().GetScheduler().runTaskLater(Bloodmoon.GetInstance(), this,20);
        }
    }
}
