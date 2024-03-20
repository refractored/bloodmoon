package net.refractored.bloodmoon;


import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.refractored.bloodmoon.boss.IBoss;
import net.refractored.bloodmoon.boss.ZombieIBoss;
import net.refractored.bloodmoon.readers.ConfigReader;
import net.refractored.bloodmoon.readers.LocaleReader;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.Closeable;
import java.util.*;

/**
 * This is the class that handles most interaction during a BloodMoon
 */
public class BloodmoonActuator implements Runnable, Closeable {
    /**
     * The constant rewardedTypes.
     */
//Eligible mobs
    public static final EntityType[] rewardedTypes = {
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

    /**
     * The constant world.
     */
    public static World world;
    private static boolean inProgress;
    private int bloodMoonLevel = 1;
    private static BossBar nightBar;
    private ActuatorPeriodic actuatorPeriodic;

    /**
     * The Blacklisted mobs.
     */
    public static List<LivingEntity> blacklistedMobs;
    /**
     * The Bosses.
     */
    public static List<IBoss> bosses;

    private void AddActuator (BloodmoonActuator instance) {
        if (actuators == null) actuators = new HashMap<>();

        actuators.put(instance.world, instance);
    }

    /**
     * Get actuator bloodmoon actuator.
     *
     * @param world the world
     * @return the bloodmoon actuator
     */
    public static BloodmoonActuator GetActuator (World world) {
        try
        {
            return actuators.get(world);
        } catch (Exception ignored)
        {
        }
        return null;
    }


    /**
     * Instantiates a new Bloodmoon actuator.
     *
     * @param world the world
     */
    public BloodmoonActuator (World world) {
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

    /**
     * Start blood moon.
     */
    public void StartBloodMoon () {
        inProgress = true;
        RunPreCommand();

        ShowNightBar();
        BroadcastBloodMoonWarning();

        actuatorPeriodic = new ActuatorPeriodic(world);
        actuatorPeriodic.run();

        SpawnBosses();
        StartSpawningOfHordes();

        ConfigReader reader = Bloodmoon.GetInstance().getConfigReader(world);

        originalMaxSpawn = world.getSpawnLimit(SpawnCategory.MONSTER);
        world.setSpawnLimit(SpawnCategory.MONSTER, reader.GetSpawnRateConfig());
    }

    /**
     * Stop blood moon.
     */
    public void StopBloodMoon () {
        if (Bloodmoon.GetInstance().getConfigReader(world).GetPermanentBloodMoonConfig())
        {
            return;
        }
        inProgress = false;
        bloodMoonLevel = 1;

        StopStorm();
        HideNightBar();

        actuatorPeriodic.close();
        actuatorPeriodic = null;
        blacklistedMobs.clear();
        KillBosses();
        world.setSpawnLimit(SpawnCategory.MONSTER, originalMaxSpawn);
        RunPostCommand();
    }

    /**
     * Kill bosses.
     */
    public void KillBosses ()
    {
        KillBosses(false);
    }

    /**
     * Kill bosses.
     *
     * @param giveRewards the give rewards
     */
    public void KillBosses (boolean giveRewards)
    {
        KillBosses(giveRewards, true);
    }

    /**
     * Kill bosses.
     *
     * @param giveRewards the give rewards
     * @param effects     the effects
     */
    public void KillBosses (boolean giveRewards, boolean effects)
    {
        KillBosses(giveRewards, effects, true);
    }

    /**
     * Kill bosses.
     *
     * @param giveRewards the give rewards
     * @param effects     the effects
     * @param respawn     the respawn
     */
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

    /**
     * Gets eligible players.
     *
     * @return the eligible players
     */
    public List<Player> getEligiblePlayers() {
        List<Player> eligiblePlayers = new ArrayList<>();
        for (Player player : world.getPlayers()) {
            if (!isVanished(player) && player.getGameMode() == GameMode.SURVIVAL) {
                eligiblePlayers.add(player);
            }
        }
        return eligiblePlayers;
    }
    
    private boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }

    /**
     * Spawn horde.
     */
    public void SpawnHorde () {
        Random random = new Random();
        ArrayList<Player> players = new ArrayList<>(getEligiblePlayers());
        if (players.isEmpty()) return;
        SpawnHorde(players.get(random.nextInt(players.size())));
    }


    /**
     * Spawn horde.
     *
     * @param target the target
     */
    public void SpawnHorde (Player target) {
        if (target == null) return;

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

    /**
     * Start spawning of hordes.
     */
    public void StartSpawningOfHordes () {
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


    private void RunPreCommand () {
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

    private void RunPostCommand () {
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

    /**
     * Spawn bosses.
     */
    public void SpawnBosses () {
        ConfigReader reader = Bloodmoon.GetInstance().getConfigReader(world);
        Bloodmoon.GetInstance().getServer().getScheduler().scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable()
        {
            public void run ()
            {
                if (reader.GetEnableZombieBossConfig()) SpawnZombieBoss();
            }
        }, (long) ((new Random()).nextInt(2000) + 400));
    }

    /**
     * Spawn zombie boss.
     */
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

    /**
     * Add to blacklist.
     *
     * @param entity the entity
     */
    public void AddToBlacklist (LivingEntity entity)
    {
        blacklistedMobs.add(entity);
    }

    private void StopStorm ()
    {
        world.setStorm(false);
        world.setThundering(false);
    }

    private void ShowNightBar () {
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


    private void HideNightBar () {
        if (nightBar != null) nightBar.removeAll();
        nightBar = null;
    }

    /**
     * Hide night bar player.
     *
     * @param player the player
     */
    public static void HideNightBarPlayer (Player player) {
        try
        {
            if (nightBar != null) nightBar.removePlayer(player);
        } catch (Exception ignored)
        {
        }
    }

    private void UpdateNightBar () {
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

    /**
     * Handle reconnecting player.
     *
     * @param player the player
     */
    public static void HandleReconnectingPlayer (Player player) {
        if (isInProgress() && nightBar != null) nightBar.addPlayer(player);
        BroadcastBloodMoonWarningPlayer(player);
    }

    private void BroadcastBloodMoonWarning () {
        for (Player player : world.getPlayers())
        {
            BroadcastBloodMoonWarningPlayer(player);
        }
    }


    private static void BroadcastBloodMoonWarningPlayer(Player player) {
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
     * @return item stack
     */
    public static ItemStack GetRandomBonus () {

        Random random = new Random(); //We want to regenerate it every time to ensure randomness
        Material itemMaterial;

        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
        String[] items = configReader.GetItemListConfig(); //Get the list of items
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
                            Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchant[0].toLowerCase()));
                            if (enchantment == null) continue; //Enchantment not found. Meh
                            itemStack.addEnchantment(enchantment, Integer.parseInt(enchant[1]));
                        }
                    }
                }

                return itemStack;
            }
        }
        return null;
    }

    /**
     * Is in protected wg region boolean.
     *
     * @param player the player
     * @return the boolean
     */
    public static boolean IsInProtectedWGRegion(Player player){
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

    /**
     * Apply special effect.
     *
     * @param player the player
     * @param mob    the mob
     */
    public static void ApplySpecialEffect (Player player, LivingEntity mob) {
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
            if (str.equals("lightning"))
            {
                world.strikeLightning(player.getLocation());
                continue;
            }


            String[] parts = str.split(",");
            PotionEffectType type = Registry.EFFECT.get(NamespacedKey.minecraft(parts[0]));
            if (type == null) continue; //Effect not found. Meh
            String effectName = parts[0];
            int ticks = (int) (20f * Float.parseFloat(parts[1]));
            int amp = Integer.parseInt(parts[2]);

            player.addPotionEffect(new PotionEffect(type, ticks, amp));

        }
    }

    /**
     * Is in progress boolean.
     *
     * @return the boolean
     */
    public static boolean isInProgress() {
        ConfigReader reader = Bloodmoon.GetInstance().getConfigReader(world);
        return inProgress || reader.GetPermanentBloodMoonConfig();
    }

    /**
     * Runs the actuator's checkup routine. Called internally, you don't need to call it yourself
     */
    @Override
    public void run () {
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
    public void close () {
        if (bosses.isEmpty()) return; //Nothing to do

        KillBosses(false, false);
        world.save();
    }

    /**
     * Gets blood moon level.
     *
     * @return the blood moon level
     */
    public int getBloodMoonLevel() {
        return bloodMoonLevel;
    }

    /**
     * Sets blood moon level.
     *
     * @param bloodMoonLevel the blood moon level
     */
    public void setBloodMoonLevel(int bloodMoonLevel) {
        this.bloodMoonLevel = bloodMoonLevel;
    }
}
