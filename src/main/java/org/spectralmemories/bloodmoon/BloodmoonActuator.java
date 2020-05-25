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
            EntityType.PHANTOM,
            EntityType.ENDERMAN
    };

    private int originalMaxSpawn = 0;

    private static Map<World, BloodmoonActuator> actuators;

    private World world;
    private boolean inProgress;

    private BossBar nightBar;
    private ActuatorPeriodic actuatorPeriodic;

    private List<LivingEntity> blacklistedMobs;
    private List<IBoss> bosses;

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

        if (Bloodmoon.GetInstance().getConfigReader(world).GetPermanentBloodMoonConfig())
        {
            StartBloodMoon();
        }

        bosses = new ArrayList<>();
    }

    public void StartBloodMoon ()
    {
        inProgress = true;

        ShowNightBar();
        BroadcastBloodMoonWarning();

        actuatorPeriodic = new ActuatorPeriodic(world);
        actuatorPeriodic.run();

        SpawnBosses();
        RunPreCommand();

        ConfigReader reader = Bloodmoon.GetInstance().getConfigReader(world);

        originalMaxSpawn = world.getMonsterSpawnLimit();
        world.setMonsterSpawnLimit(reader.GetSpawnRateConfig());
    }

    public void StopBloodMoon ()
    {
        if (Bloodmoon.GetInstance().getConfigReader(world).GetPermanentBloodMoonConfig())
        {
            return;
        }
        inProgress = false;

        StopStorm ();
        HideNightBar();

        actuatorPeriodic.Stop();
        actuatorPeriodic = null;
        blacklistedMobs.clear();
        KillBosses();
        RunPostCommand();
        world.setMonsterSpawnLimit(originalMaxSpawn);
    }

    public void KillBosses(boolean giveRewards) {
        Iterator var2 = this.bosses.iterator();

        while(var2.hasNext()) {
            IBoss IBoss = (IBoss)var2.next();
            IBoss.Kill(giveRewards);
        }

        this.bosses.clear();
    }

    public void KillBosses() {
        this.KillBosses(false);
    }

    private void RunPreCommand() {
        String[] commands = Bloodmoon.GetInstance().getConfigReader(this.world).GetPreBloodMoonCommands();
        String[] var2 = commands;
        int var3 = commands.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String command = var2[var4];
            String[] components = command.split(";");
            if (components[1].equalsIgnoreCase("s")) {
                String finalCommand = components[0].replace("$w", this.world.getName());
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), finalCommand);
            } else {
                Iterator var7;
                Player player;
                String finalCommand;
                if (components[1].equalsIgnoreCase("p")) {
                    var7 = this.world.getPlayers().iterator();

                    while(var7.hasNext()) {
                        player = (Player)var7.next();
                        finalCommand = components[0].replace("$w", this.world.getName()).replace("$p", player.getName());
                        player.performCommand(finalCommand);
                    }
                } else if (components[1].equalsIgnoreCase("f")) {
                    var7 = this.world.getPlayers().iterator();

                    while(var7.hasNext()) {
                        player = (Player)var7.next();
                        finalCommand = components[0].replace("$w", this.world.getName()).replace("$p", player.getName());
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), finalCommand);
                    }
                } else {
                    System.out.println("[Warning] Could not interpret command '" + command + "'");
                }
            }
        }

    }

    private void RunPostCommand() {
        String[] commands = Bloodmoon.GetInstance().getConfigReader(this.world).GetPostBloodMoonCommands();
        String[] var2 = commands;
        int var3 = commands.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String command = var2[var4];
            String[] components = command.split(";");
            if (components[1].equalsIgnoreCase("s")) {
                String finalCommand = components[0].replace("$w", this.world.getName());
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), finalCommand);
            } else {
                Iterator var7;
                Player player;
                String finalCommand;
                if (components[1].equalsIgnoreCase("p")) {
                    var7 = this.world.getPlayers().iterator();

                    while(var7.hasNext()) {
                        player = (Player)var7.next();
                        finalCommand = components[0].replace("$w", this.world.getName()).replace("$p", player.getName());
                        player.performCommand(finalCommand);
                    }
                } else if (components[1].equalsIgnoreCase("f")) {
                    var7 = this.world.getPlayers().iterator();

                    while(var7.hasNext()) {
                        player = (Player)var7.next();
                        finalCommand = components[0].replace("$w", this.world.getName()).replace("$p", player.getName());
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), finalCommand);
                    }
                } else {
                    System.out.println("[Warning] Could not interpret command '" + command + "'");
                }
            }
        }

    }

    public void SpawnBosses() {
        Bloodmoon.GetInstance().getServer().getScheduler().scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable() {
            public void run() {
                BloodmoonActuator.this.SpawnZombieBoss();
            }
        }, (long)((new Random()).nextInt(2000) + 400));
    }

    public void SpawnZombieBoss() {
        ConfigReader reader = Bloodmoon.GetInstance().getConfigReader(this.world);
        if (reader.GetEnableZombieBossConfig() && this.world.getPlayers().size() > 0) {
            List<Player> players = this.world.getPlayers();
            Random rnd = new Random();
            int index = rnd.nextInt(players.size());
            Player chosenOne = (Player)players.get(index);
            Location spawn = chosenOne.getLocation();
            Location newLocation = spawn.clone();
            newLocation.add((double)(rnd.nextInt(10) + 10), 0.0D, (double)(rnd.nextInt(10) + 10));
            newLocation.setY((double)this.world.getHighestBlockYAt(newLocation));
            ZombieIBoss zombieBoss = new ZombieIBoss(newLocation);
            zombieBoss.Start();
            this.bosses.add(zombieBoss);
        }

    }

    public void AddToBlacklist(LivingEntity entity) {
        this.blacklistedMobs.add(entity);
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

        if (configReader.GetPermanentBloodMoonConfig()) return; //disable nightbar if permanent BM

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
        try
        {
            if (nightBar != null) nightBar.removePlayer(player);
        } catch (Exception ignored){}
    }

    private void UpdateNightBar ()
    {
        if (Bloodmoon.GetInstance().getConfigReader(world).GetPermanentBloodMoonConfig())
        {
            if (nightBar != null) nightBar.setProgress (1.0);
            return;
        }
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
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);

        LocaleReader.MessageLocale("BloodMoonWarningTitle", null, null, player);
        LocaleReader.MessageLocale("BloodMoonWarningBody", null, null, player);

        if (configReader.GetInventoryLossConfig())
        {
            LocaleReader.MessageLocale("DyingResultsInInventoryLoss", null, null, player);
        }
        if (configReader.GetExperienceLossConfig())
        {
            LocaleReader.MessageLocale("DyingResultsInExperienceLoss", null, null, player);
        }
    }

    public Material GetRandomBonus ()
    {

        Random random = new Random(); //We want to regenerate it every time to ensure randomness

        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
        Material[] selection = configReader.GetItemListConfig();

        int amount = selection.length;

        return selection [random.nextInt(amount)];

    }

    private void ApplySpecialEffect (Player player, LivingEntity mob)
    {
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
        String mobTypeName = mob.getName().toUpperCase();
        for (IBoss boss : bosses)
        {
            if(boss.GetHost() == mob)
            {
                mobTypeName += "BOSS";
                break;
            }
        }

        String[] configs = configReader.GetMobEffectConfig(mobTypeName);

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
            if (from == world)
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

                    LocaleReader.MessageLocale("BedNotAllowed", null, null, event.getPlayer());
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
        LivingEntity entity = event.getEntity();

        for (IBoss boss : bosses) {
            if (entity == boss.GetHost()) {
                Player killer = boss.GetHost().getKiller();
                if (killer != null) {

                    LocaleReader.MessageAllLocale("BossSlain", new String[]{"$p", "$b"}, new String[]{boss.GetName(), killer.getName()}, world);
                }

                boss.Kill(killer != null && isInProgress());
                bosses.remove(boss);
                return;
            }
        }

        if (!isInProgress()) return; //Only during BloodMoon



        if (event.getEntity() instanceof Player) return; //Handled in another method

        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);


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
                        if (event.getFinalDamage() == 0 && configReader.GetShieldPreventEffects()) return; //Hit was shielded. We shall not apply configs
                        event.setDamage(event.getDamage() * configReader.GetMobDamageMultConfig());
                        ApplySpecialEffect((Player) receiver, (LivingEntity) damager);
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
