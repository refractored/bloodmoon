package org.spectralmemories.bloodmoon;


import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

/**
 * This is the class that handles most interaction during a BloodMoon
 */
public class BloodmoonActuator implements Listener, Runnable, Closeable
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
        } catch (Exception ignored)
        {
        }
        return null;
    }


    public BloodmoonActuator (World world)
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
        RunPreCommand();

        ShowNightBar();
        BroadcastBloodMoonWarning();

        actuatorPeriodic = new ActuatorPeriodic(world);
        actuatorPeriodic.run();

        SpawnBosses();
        StartSpawningOfHordes();

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

        StopStorm();
        HideNightBar();

        actuatorPeriodic.close();
        actuatorPeriodic = null;
        blacklistedMobs.clear();
        KillBosses();
        world.setMonsterSpawnLimit(originalMaxSpawn);
        RunPostCommand();
    }

    public void KillBosses ()
    {
        KillBosses(false);
    }

    public void KillBosses (boolean giveRewards)
    {
        KillBosses(giveRewards, true);
    }

    public void KillBosses (boolean giveRewards, boolean effects)
    {
        KillBosses(giveRewards, effects, true);
    }

    public void KillBosses (boolean giveRewards, boolean effects, boolean respawn)
    {
        Iterator var2 = bosses.iterator();

        while (var2.hasNext())
        {
            IBoss IBoss = (IBoss) var2.next();
            IBoss.Kill(giveRewards, effects, respawn);
        }

        bosses.clear();
    }

    public void SpawnHorde ()
    {
        Random random = new Random();
        Player[] players = world.getPlayers().toArray(new Player[0]);
        if(players.length > 0)
        {
            SpawnHorde(players[random.nextInt(players.length)]);
        }
    }

    public void SpawnHorde (Player target)
    {
        ConfigReader reader = Bloodmoon.GetInstance().getConfigReader(world);
        if (!reader.GetHordeEnabled()) return;

        Random random = new Random();

        Location hordeSpawnLocation;
        hordeSpawnLocation = target.getLocation().clone();

        int minMob = reader.GetHordeMinPopulation();
        int maxMob = reader.GetHordeMaxPopulation();
        int mobAmount = random.nextInt(maxMob - minMob) + minMob;
        int maxDistance = reader.GetHordeSpawnDistance();

        for (int i = 0; i < mobAmount; i++)
        {
            String[] mobList = reader.GetHordeMobWhitelist();
            int mobListLen = mobList.length;
            EntityType mobType = EntityType.valueOf(mobList[random.nextInt(mobListLen)]);

            Location newMobLocation = hordeSpawnLocation.clone();
            if (random.nextBoolean())
            {
                newMobLocation = newMobLocation.add(random.nextInt(maxDistance), 0, random.nextInt(maxDistance));
            } else
            {
                newMobLocation = newMobLocation.subtract(random.nextInt(maxDistance), 0, random.nextInt(maxDistance));
            }

            newMobLocation.setY(world.getHighestBlockYAt(newMobLocation));

            world.spawnEntity(newMobLocation, mobType);
            world.strikeLightningEffect(newMobLocation);
        }
        LocaleReader.MessageAllLocale("HordeArrived", new String[]{"$p"}, new String[]{target.getDisplayName()}, world);
    }

    public void StartSpawningOfHordes ()
    {
        ConfigReader reader = Bloodmoon.GetInstance().getConfigReader(world);
        Random random = new Random();

        if (!reader.GetHordeEnabled()) return;

        int min = reader.GetHordeSpawnrateBaseline() - reader.GetHordeSpawnrateVariation();
        int max = reader.GetHordeSpawnrateBaseline() + reader.GetHordeSpawnrateVariation();

        Bloodmoon.GetInstance().GetScheduler().scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable()
        {
            @Override
            public void run ()
            {
                if (isInProgress())
                {
                    SpawnHorde();
                    Bloodmoon.GetInstance().GetScheduler().scheduleSyncDelayedTask(Bloodmoon.GetInstance(), this, random.nextInt(max - min) + max);
                }
            }
        }, random.nextInt(max - min) + max);
    }


    private void RunPreCommand ()
    {
        String[] commands = Bloodmoon.GetInstance().getConfigReader(world).GetPreBloodMoonCommands();
        String[] var2 = commands;
        int var3 = commands.length;

        for (int var4 = 0; var4 < var3; ++var4)
        {
            String command = var2[var4];
            String[] components = command.split(";");
            if (components[1].equalsIgnoreCase("s"))
            {
                String finalCommand = components[0].replace("$w", world.getName());
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), finalCommand);
            } else
            {
                Iterator var7;
                Player player;
                String finalCommand;
                if (components[1].equalsIgnoreCase("p"))
                {
                    var7 = world.getPlayers().iterator();

                    while (var7.hasNext())
                    {
                        player = (Player) var7.next();
                        finalCommand = components[0].replace("$w", world.getName()).replace("$p", player.getName());
                        player.performCommand(finalCommand);
                    }
                } else if (components[1].equalsIgnoreCase("f"))
                {
                    var7 = world.getPlayers().iterator();

                    while (var7.hasNext())
                    {
                        player = (Player) var7.next();
                        finalCommand = components[0].replace("$w", world.getName()).replace("$p", player.getName());
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), finalCommand);
                    }
                } else
                {
                    System.out.println("[Warning] Could not interpret command '" + command + "'");
                }
            }
        }

    }

    private void RunPostCommand ()
    {
        String[] commands = Bloodmoon.GetInstance().getConfigReader(world).GetPostBloodMoonCommands();
        String[] var2 = commands;
        int var3 = commands.length;

        for (int var4 = 0; var4 < var3; ++var4)
        {
            String command = var2[var4];
            String[] components = command.split(";");
            if (components[1].equalsIgnoreCase("s"))
            {
                String finalCommand = components[0].replace("$w", world.getName());
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), finalCommand);
            } else
            {
                Iterator var7;
                Player player;
                String finalCommand;
                if (components[1].equalsIgnoreCase("p"))
                {
                    var7 = world.getPlayers().iterator();

                    while (var7.hasNext())
                    {
                        player = (Player) var7.next();
                        finalCommand = components[0].replace("$w", world.getName()).replace("$p", player.getName());
                        player.performCommand(finalCommand);
                    }
                } else if (components[1].equalsIgnoreCase("f"))
                {
                    var7 = world.getPlayers().iterator();

                    while (var7.hasNext())
                    {
                        player = (Player) var7.next();
                        finalCommand = components[0].replace("$w", world.getName()).replace("$p", player.getName());
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), finalCommand);
                    }
                } else
                {
                    System.out.println("[Warning] Could not interpret command '" + command + "'");
                }
            }
        }
    }

    public void SpawnBosses ()
    {
        ConfigReader reader = Bloodmoon.GetInstance().getConfigReader(world);
        Bloodmoon.GetInstance().getServer().getScheduler().scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable()
        {
            public void run ()
            {
                if (reader.GetEnableZombieBossConfig()) SpawnZombieBoss();
            }
        }, (long) ((new Random()).nextInt(2000) + 400));
    }

    public void SpawnZombieBoss ()
    {
        if (world.getPlayers().size() > 0)
        {
            List<Player> players = world.getPlayers();
            Random rnd = new Random();
            int index = rnd.nextInt(players.size());
            Player chosenOne = (Player) players.get(index);
            Location spawn = chosenOne.getLocation();
            Location newLocation = spawn.clone();
            newLocation.add((double) (rnd.nextInt(10) + 10), 0.0D, (double) (rnd.nextInt(10) + 10));
            newLocation.setY((double) world.getHighestBlockYAt(newLocation));
            ZombieIBoss zombieBoss = new ZombieIBoss(newLocation);
            zombieBoss.Start();
            bosses.add(zombieBoss);
        }
    }

    public void AddToBlacklist (LivingEntity entity)
    {
        blacklistedMobs.add(entity);
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
        } else
        {
            nightBar = Bukkit.createBossBar(localeReader.GetLocaleString("BloodMoonTitleBar"),
                    BarColor.RED,
                    BarStyle.SEGMENTED_12
            );
        }
        nightBar.setProgress(0.0);
        Bloodmoon.GetInstance().GetScheduler().runTaskLater(Bloodmoon.GetInstance(), this, 0);

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
        } catch (Exception ignored)
        {
        }
    }

    private void UpdateNightBar ()
    {
        if (Bloodmoon.GetInstance().getConfigReader(world).GetPermanentBloodMoonConfig())
        {
            if (nightBar != null) nightBar.setProgress(1.0);
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

    /**
     * Generates a random item to be used as a reward
     *
     * @return
     */
    public ItemStack GetRandomBonus ()
    {

        Random random = new Random(); //We want to regenerate it every time to ensure randomness
        Material itemMaterial;

        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
        String[] items = configReader.GetItemListConfig();
        Map<String, Integer[]> indexes = new HashMap<>();
        int totalWeight = 0;

        for (String entry : items)
        {
            String[] parts = entry.split(":");
            int itemWeight = Integer.parseInt(parts[2]);

            indexes.put(entry, new Integer[]{totalWeight, totalWeight + itemWeight});
            totalWeight += itemWeight;
        }

        int rng = random.nextInt(totalWeight);

        for (Map.Entry<String, Integer[]> entry : indexes.entrySet())
        {
            int min = entry.getValue()[0];
            int max = entry.getValue()[1];

            if (rng >= min && rng < max)
            {
                String[] parts = entry.getKey().split(":");
                itemMaterial = Material.valueOf(parts[0]);

                ItemStack itemStack = new ItemStack(itemMaterial, Integer.parseInt(parts[1]));

                for (int i = 3; i < 6; i++)
                {
                    if (parts.length <= i) break;

                    String line = parts[i];
                    if (line.startsWith("$name"))
                    {
                        line = line.substring("$name".length() + 1);

                        ItemMeta meta = itemStack.getItemMeta();
                        meta.setDisplayName(line);
                        itemStack.setItemMeta(meta);
                    } else if (line.startsWith("$desc"))
                    {
                        line = line.substring("$desc".length() + 1);

                        ItemMeta meta = itemStack.getItemMeta();
                        meta.setLore(Arrays.asList(line.split("\\$n")));
                        itemStack.setItemMeta(meta);
                    } else if (line.startsWith("$enchant"))
                    {
                        line = line.substring("$enchant".length() + 1);

                        String[] enchantLines = line.split(";");
                        for (String enchantLine : enchantLines)
                        {
                            String[] enchant = enchantLine.split(",");
                            itemStack.addEnchantment(
                                    Enchantment.getByKey(NamespacedKey.minecraft(enchant[0].toLowerCase()))
                                    , Integer.parseInt(enchant[1]));
                        }
                    }
                }

                return itemStack;
            }
        }
        return null;
    }

    public boolean IsInProtectedWGRegion (Player player){
        try
        {
            RegionContainer rc = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery rq = rc.createQuery();
            ApplicableRegionSet rs = rq.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));

            if (rs == null || rs.size() == 0) return false;

            boolean isProtected = !rs.testState(null, Flags.MOB_DAMAGE);
            return isProtected;
        }catch (NoClassDefFoundError e){
            //Server likely does not have WG
            return false;
        }
    }

    private void ApplySpecialEffect (Player player, LivingEntity mob)
    {
        if(IsInProtectedWGRegion(player)) return;

        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
        String mobTypeName = mob.getType().name().toUpperCase();
        for (IBoss boss : bosses)
        {
            if (boss.GetHost() == mob)
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
        ConfigReader reader = Bloodmoon.GetInstance().getConfigReader(world);
        return inProgress || reader.GetPermanentBloodMoonConfig();
    }


    //Events
    @EventHandler
    public void onPlayerConnect (PlayerJoinEvent event)
    {
        if (isInProgress() && event.getPlayer().getWorld() == world)
        {
            HandleReconnectingPlayer(event.getPlayer());
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

        if (!deathMessage.contains(localeReader.GetLocaleString("DeathSuffix")))
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

        for (IBoss boss : bosses)
        {
            if (entity == boss.GetHost())
            {
                Player killer = boss.GetHost().getKiller();
                if (killer != null)
                {

                    LocaleReader.MessageAllLocale("BossSlain", new String[]{"$b", "$p"}, new String[]{boss.GetName(), killer.getName()}, world);
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

        if (!eligible) return; //Not eligible for reward

        event.setDroppedExp(event.getDroppedExp() * configReader.GetExpMultConfig());

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

    /**
     * Runs the actuator's checkup routine. Called internally, you don't need to call it yourself
     */
    @Override
    public void run ()
    {
        if (isInProgress())
        {
            UpdateNightBar();
            Bloodmoon.GetInstance().GetScheduler().runTaskLater(Bloodmoon.GetInstance(), this, 20);
        }
    }

    /**
     * Closes the actuator. You should discard it after doing so
     */
    @Override
    public void close ()
    {
        if (bosses.isEmpty()) return; //Nothing to do

        KillBosses(false, false);
        world.save();
    }
}
