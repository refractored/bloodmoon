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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;


import java.util.*;


public class BloodmoonActuator implements Listener, Runnable
{
    public static final String DURING_A_BLOOD_MOON = " during a BloodMoon!";
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

    private void AddActuator (BloodmoonActuator instance)
    {
        if (actuators == null) actuators = new HashMap<>();

        actuators.put(instance.world, instance);
    }

    public static BloodmoonActuator GetActuator (World world)
    {
        return actuators.get(world);
    }


    public BloodmoonActuator(World world)
    {
        this.world = world;
        inProgress = false;
        AddActuator(this);
    }

    public void StartBloodMoon ()
    {
        inProgress = true;

        ShowNightBar();

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
        nightBar = Bukkit.createBossBar("BloodMoon", BarColor.RED, BarStyle.SEGMENTED_12, BarFlag.CREATE_FOG, BarFlag.DARKEN_SKY);
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
    }

    private Material GetRandomBonus ()
    {
        Material[] materials = {
                Material.GOLD_INGOT,
                Material.GOLD_INGOT,
                Material.GOLD_INGOT,
                Material.GOLD_INGOT,
                Material.GOLD_BLOCK,
                Material.GOLD_BLOCK,
                Material.IRON_INGOT,
                Material.IRON_INGOT,
                Material.IRON_INGOT,
                Material.IRON_INGOT,
                Material.IRON_BLOCK,
                Material.IRON_BLOCK,
                Material.IRON_BLOCK,
                Material.DIAMOND,
                Material.DIAMOND,
                Material.DIAMOND,
                Material.TOTEM_OF_UNDYING,
                Material.DIAMOND_BLOCK,
                Material.COAL_BLOCK,
                Material.COAL_BLOCK,
                Material.COAL_BLOCK,
                Material.REDSTONE_BLOCK,
                Material.REDSTONE_BLOCK,
                Material.REDSTONE_BLOCK,
                Material.REDSTONE_BLOCK,
                Material.LAPIS_BLOCK,
                Material.LAPIS_BLOCK,
                Material.LAPIS_BLOCK,
                Material.LAPIS_BLOCK,
        };

        Material[] discs = {
                Material.MUSIC_DISC_11,
                Material.MUSIC_DISC_13,
                Material.MUSIC_DISC_BLOCKS,
                Material.MUSIC_DISC_CAT,
                Material.MUSIC_DISC_CHIRP,
                Material.MUSIC_DISC_FAR,
                Material.MUSIC_DISC_MALL,
                Material.MUSIC_DISC_MELLOHI,
                Material.MUSIC_DISC_STRAD,
                Material.MUSIC_DISC_STAL,
                Material.MUSIC_DISC_WARD,
                Material.MUSIC_DISC_WAIT
        };

        Material[] patterns = {
                Material.SKULL_BANNER_PATTERN,
                Material.FLOWER_BANNER_PATTERN,
                Material.CREEPER_BANNER_PATTERN,
                Material.GLOBE_BANNER_PATTERN
        };

        Random random = new Random();

        int zeroToHundred = random.nextInt(100);
        Material[] selection;

        if (zeroToHundred < 97)
        {
            selection = materials;
        }
        else if (zeroToHundred < 98)
        {
            selection = discs;
        }
        else
        {
            selection = patterns;
        }
        int amount = selection.length;

        return selection [random.nextInt(amount)];

    }

    private void ApplySpecialEffect (Player player, EntityType mob)
    {
        switch (mob)
        {
            case CREEPER:
                world.strikeLightning(player.getLocation());
                break;
            case ZOMBIE:
            case HUSK:
            case DROWNED:
            case ZOMBIE_VILLAGER:
                player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 140,1));
                break;
            case SKELETON:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 70, 1));
                break;
            case SPIDER:
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 160, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 220, 300));
                break;
            case PHANTOM:
                player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 30, 3));
                break;
        }
    }

    public boolean isInProgress ()
    {
        return inProgress;
    }

    //Todo: check if all events are happening in our world
    //Todo: watch for playerTP to other dimensions

    //Events
    @EventHandler
    public void onPlayerConnect (PlayerJoinEvent event)
    {
        if (isInProgress())
        {
            HandleReconnectingPlayer (event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerDeath (PlayerDeathEvent event)
    {
        if (!isInProgress()) return; //Only during BloodMoon

        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader();

        Player deadplayer = event.getEntity();

        world.strikeLightningEffect(deadplayer.getLocation());

        String deathMessage = event.getDeathMessage();

        if (! deathMessage.contains("BloodMoon"))
        {
            deathMessage += DURING_A_BLOOD_MOON;

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
    public void onMobDeath (EntityDeathEvent event)
    {
        if (!isInProgress()) return; //Only during BloodMoon



        if (event.getEntity() instanceof Player) return; //Handled in another method

        world.strikeLightningEffect(event.getEntity().getLocation());
        event.setDroppedExp(event.getDroppedExp() * 4); //4x exp
        LivingEntity entity = event.getEntity();


        boolean eligible = false;
        for (EntityType type : rewardedTypes)
        {
            if (entity.getType() == type) eligible = true;
        }

        if (! eligible) return; //Not eligible for reward

        List<ItemStack> bonusDrops = new ArrayList<>();

        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader();
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
            ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader();
             if (receiver instanceof Player)
             {

                for (EntityType type : rewardedTypes)
                {
                    if (damager.getType() == type)
                    {
                        //Player is damaged by monster
                        event.setDamage(event.getDamage() * configReader.GetMobDamageMultConfig());
                        ApplySpecialEffect((Player) receiver, type);
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
