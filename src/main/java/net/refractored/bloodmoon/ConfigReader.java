package net.refractored.bloodmoon;

import org.bukkit.World;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class ConfigReader implements Closeable
{
    public static final String BLOOD_MOON_INTERVAL = "BloodMoonInterval";
    public static final String PLAYER_LOSES_ITEM_UPON_BLOOD_MOON_DEATH = "ItemDespawnUponDeath";
    public static final String PLAYER_LOSES_EXP_UPON_BLOOD_MOON_DEATH = "ExperienceDespawnsUponDeath";
    public static final String NULL_CONFIG = "null";
    public static final int DEFAULT_INTERVAL = 5;
    public static final boolean LIGTHNINGEFFECT_DEFAULT = true;
    public static final boolean DEFAULT_INV_LOSS = true;
    public static final boolean DEFAULT_EXP_LOSS = true;
    public static final String ITEM_DROPS_MAXIMUM = "ItemDropsMaximum";
    public static final String ITEM_DROPS_MINIMUM = "ItemDropsMinimum";
    public static final int MAX_ITEM_DROP_DEFAULT = 4;
    public static final int MIN_ITEM_DROP_DEFAULT = 0;
    public static final String MOB_HEALTH_MULT = "MobHealthMultiplicator";
    public static final String MOB_DAMAGE_MULT = "MobDamageMultiplicator";
    public static final double MOB_DAMAGE_MULT_DEFAULT = 3.0;
    public static final double MOB_HEALTH_MULT_DEFAULT = 3.0;
    public static final String CONFIG_VERSION = "ConfigVersion";
    public static final String DROP_ITEM_LIST = "DropItemList";
    public static final String ZOMBIEEFFECTS = "ZOMBIEEffects";
    public static final String SKELETONEFFECTS = "SKELETONEffects";
    public static final String CREEPEREFFECTS = "CREEPEREffects";
    public static final String PHANTOMEFFECTS = "PHANTOMEffects";
    public static final String SPIDEREFFECTS = "SPIDEREffects";
    public static final String LIGHTNING_EFFECT_ON_PLAYER_DEATH = "LightningEffectOnPlayerDeath";
    public static final String PLAY_SOUND_UPON_BLOOD_MOON_END = "PlaySoundUponBloodMoonEnd";
    public static final String PLAY_PERIODIC_SOUNDS_DURING_BLOOD_MOON = "PlayPeriodicSoundsDuringBloodMoon";
    public static final boolean PLAY_SOUND_ON_END_DEFAULT = true;
    public static final boolean PLAY_PERIODIC_SOUND_DEFAULT = true;
    public static final String ZOMBIE_VILLAGEREFFECT = "ZOMBIE_VILLAGEREffects";
    public static final String DROWNEDEFFECT = "DROWNEDEffects";
    public static final String HUSKEFFECT = "HUSKEffects";
    public static final String DARKEN_SKY = "DarkenSky";
    public static final boolean DARKEN_SKY_DEFAULT = true;
    public static final String PLAY_SOUND_UPON_HIT = "PlaySoundUponHit";
    public static final String LIGHTNING_EFFECT_ON_MOB_DEATH = "LightningEffectOnMobDeath";
    public static final String THUNDER_DURING_BLOOD_MOON = "ThunderDuringBloodMoon";
    public static final boolean MOB_LIGHTNING_EFFECT_DEFAULT = true;
    public static final boolean SOUND_ON_HIT_DEFAULT = true;
    public static final boolean THUNDER_DEFAULT = true;
    public static final String EXP_MULTIPLICATOR = "ExperienceDropMult";
    public static final int EXP_MULTIPLICATOR_DEFAULT = 4;
    public static final String IS_BLACKLISTED = "IsBlacklisted";
    public static final boolean IS_BLACKLISTED_DEFAULT = false;
    public static final String VERSION_CONFIG = "ConfigVersion";
    public static final String PREVENT_SLEEPING = "PreventSleeping";
    public static final boolean PREVENT_SLEEPING_DEFAULT = true;
    public static final String MOBS_FROM_SPAWNER_NO_REWARD = "MobsFromSpawnerNoReward";
    public static final boolean MOBS_FROM_SPAWNER_NO_REWARD_DEFAULT = false;
    public static final String SHIELD_PREVENTS_EFFECTS = "ShieldPreventsEffects";
    public static final boolean SHIELD_PREVENTS_EFFECTS_DEFAULT = true;
    public static final String PERMANENT_BLOOD_MOON = "PermanentBloodMoon";
    public static final boolean PERMANENT_BLOODMOON_DEFAULT = false;
    public static final String ZOMBIEBOSSEFFECTS = "ZOMBIEBOSSEffects";
    public static final String ENABLE_ZOMBIE_BOSS = "EnableZombieBoss";
    public static final String ZOMBIE_BOSS_HEALTH = "ZombieBossHealth";
    public static final String ZOMBIE_BOSS_DAMAGE = "ZombieBossDamage";
    public static final String ZOMBIE_BOSS_POWER_SET = "ZombieBossPowerSet";
    public static final String ZOMBIE_BOSS_RESPAWN = "ZombieBossRespawn";
    public static final long DEFAULT_ZOMBIE_RESPAWN_TIME = 24000;
    public static final boolean ENABLE_ZOMBIE_BOSS_DEFAULT = false;
    public static final int ZOMBIE_BOSS_HEALTH_DEFAULT = 50;
    public static final int ZOMBIE_BOSS_DAMAGE_DEFAULT = 7;
    public static final String ZOMBIE_BOSS_ITEM_MULTIPLIER = "ZombieBossItemMultiplier";
    public static final String ZOMBIE_BOSS_EXP_MULTIPLIER = "ZombieBossExpMultiplier";
    public static final int DEFAULT_ZOMBIE_BOSS_ITEM_MULTIPLIER = 10;
    public static final int DEFAULT_ZOMBIE_BOSS_EXP_MULTIPLIER = 10;
    public static final String BLOOD_MOON_SPAWN_MOB_RATE = "BloodMoonSpawnMobRate";
    public static final int DEFAULT_BLOOD_MOON_SPAWN_MOB_RATE = 25;
    public static final String PLAYER_HIT_PARTICLE_EFFECT = "PlayerHitParticleEffect";
    public static final boolean DEFAULT_PLAYER_HIT_PARTICLE_EFFECT = true;
    public static final String MOB_HIT_PARTICLE_EFFECT = "MobHitParticleEffect";
    public static final boolean DEFAULT_MOB_HIT_PARTICLE_EFFECT = true;
    public static final String BASELINE_HORDE_SPAWNRATE = "BaselineHordeSpawnrate";
    public static final int BASELINE_HORDE_SPAWNRATE_DEFAULT = 800;
    public static final String HORDE_SPAWNRATE_VARIATION = "HordeSpawnrateVariation";
    public static final int HORDE_SPAWNRATE_VARIATION_DEFAULT = 200;
    public static final String HORDE_MOB_WHITELIST = "HordeMobWhitelist";
    public static final String HORDE_SPAWN_DISTANCE = "HordeSpawnDistance";
    public static final int HORDE_SPAWN_DISTANCE_DEFAULT = 12;
    public static final String HORDE_MIN_POPULATION = "HordeMinPopulation";
    public static final int HORDE_MIN_POPULATION_DEFAULT = 3;
    public static final String HORDE_MAX_POPULATION = "HordeMaxPopulation";
    public static final int HORDE_MAX_POPULATION_DEFAULT = 10;
    public static final String COMMANDS_ON_END = "CommandsOnEnd";
    public static final String COMMANDS_ON_START = "CommandsOnStart";
    public static final String HORDES_ENABLED = "HordesEnabled";
    public static final boolean HORDES_ENABLED_DEFAULT = true;

    private File configFile;
    private Map <String, Object> cache;
    private World world;


    public ConfigReader (File file, World world)
    {
        configFile = file;
        this.world = world;
    }

    public void GenerateDefaultFile ()
    {
        try
        {
            FileWriter writer = new FileWriter(configFile, true);

            writer.write("#Plugin version. Please do not tamper\n");
            writer.write(CONFIG_VERSION + ": " + Bloodmoon.GetInstance().getDescription().getVersion() + "\n\n");
            writer.write("#Config file for world:" + world.getName() + " (UUID: " + world.getUID().toString() + ")\n\n");
            writer.write("#Wether or not a BloodMoon happens in this world\n#Requires a server restart upon changes\n");
            writer.write(IS_BLACKLISTED + ": " + String.valueOf(IS_BLACKLISTED_DEFAULT) + "\n");
            writer.write("#Sets a permanent BloodMoon in this world\n#Obviously the interval option is ignored when this is on\n");
            writer.write(PERMANENT_BLOOD_MOON + ": " + String.valueOf(PERMANENT_BLOODMOON_DEFAULT) + "\n");
            writer.write("#Interval in days between BloodMoons\n");
            writer.write(BLOOD_MOON_INTERVAL + ": " + String.valueOf(DEFAULT_INTERVAL) + "\n");
            writer.write("#Do items despawn upon death?\n");
            writer.write(PLAYER_LOSES_ITEM_UPON_BLOOD_MOON_DEATH + ": " + String.valueOf(DEFAULT_INV_LOSS) +"\n");
            writer.write("#Does experience despawn upon death?\n");
            writer.write(PLAYER_LOSES_EXP_UPON_BLOOD_MOON_DEATH + ": " + String.valueOf(DEFAULT_EXP_LOSS) + "\n");
            writer.write("#Maximum item amount to drop per mob death\n");
            writer.write(ITEM_DROPS_MAXIMUM + ": " + String.valueOf(MAX_ITEM_DROP_DEFAULT) + "\n");
            writer.write("#Minimum item amount to drop per mob death\n");
            writer.write(ITEM_DROPS_MINIMUM + ": " + String.valueOf(MIN_ITEM_DROP_DEFAULT) + "\n");
            writer.write("#Mob experience drop multiplicator. Whole number only\n");
            writer.write(EXP_MULTIPLICATOR + ": " + String.valueOf(EXP_MULTIPLICATOR_DEFAULT) + "\n");
            writer.write("#Mob damage multiplier. Whole number only\n");
            writer.write(MOB_DAMAGE_MULT + ": " + String.valueOf(MOB_DAMAGE_MULT_DEFAULT) + "\n");
            writer.write("#Mob health multiplier. Whole number only\n");
            writer.write(MOB_HEALTH_MULT + ": " + String.valueOf(MOB_HEALTH_MULT_DEFAULT) + "\n");
            writer.write("#Should there be a lightning effect on player death?\n");
            writer.write(LIGHTNING_EFFECT_ON_PLAYER_DEATH + ": " + String.valueOf(LIGTHNINGEFFECT_DEFAULT) + "\n");
            writer.write("#Adds a lightning effect when a mob dies\n");
            writer.write(LIGHTNING_EFFECT_ON_MOB_DEATH + ": " + String.valueOf(MOB_LIGHTNING_EFFECT_DEFAULT) + "\n");
            writer.write("#Should there be a jingle when a BloodMoon ends?\n");
            writer.write(PLAY_SOUND_UPON_BLOOD_MOON_END + ": " + String.valueOf(PLAY_SOUND_ON_END_DEFAULT) + "\n");
            writer.write("#Should there be periodic creepy sounds during a BloodMoon?\n");
            writer.write(PLAY_PERIODIC_SOUNDS_DURING_BLOOD_MOON + ": " + String.valueOf(PLAY_PERIODIC_SOUND_DEFAULT) + "\n");
            writer.write("#Adds a dark tone during a BloodMoon\n");
            writer.write(DARKEN_SKY + ": " + String.valueOf(DARKEN_SKY_DEFAULT) + "\n");
            writer.write("#Plays a sound when a player gets hit\n");
            writer.write(PLAY_SOUND_UPON_HIT + ": " + String.valueOf(SOUND_ON_HIT_DEFAULT) + "\n");
            writer.write("#Plays a particle effect when a mob and / or player gets hit\n");
            writer.write(PLAYER_HIT_PARTICLE_EFFECT + ": " + String.valueOf(DEFAULT_PLAYER_HIT_PARTICLE_EFFECT) + "\n");
            writer.write(MOB_HIT_PARTICLE_EFFECT + ": " + String.valueOf(DEFAULT_MOB_HIT_PARTICLE_EFFECT) + "\n");
            writer.write("#Effect of rain and thunder during the BloodMoon\n");
            writer.write(THUNDER_DURING_BLOOD_MOON + ": " + String.valueOf(THUNDER_DEFAULT) + "\n");
            writer.write("#Prevents sleeping during a BloodMoon\n");
            writer.write(PREVENT_SLEEPING + ": " + String.valueOf(PREVENT_SLEEPING_DEFAULT) + "\n");
            writer.write("#Prevents mob created by spawner from dropping any reward\n");
            writer.write(MOBS_FROM_SPAWNER_NO_REWARD + ": " + String.valueOf(MOBS_FROM_SPAWNER_NO_REWARD_DEFAULT) + "\n");
            writer.write("#Prevents special effects from being applied when a player raises their shield\n");
            writer.write(SHIELD_PREVENTS_EFFECTS + ": " + String.valueOf(SHIELD_PREVENTS_EFFECTS_DEFAULT) + "\n");
            writer.write("#Mob spawn rate during a BloodMoon.\n#0 means no mob at all, 25 is average, and anything above 100 is honestly insane\n");
            writer.write(BLOOD_MOON_SPAWN_MOB_RATE + ": " + String.valueOf(DEFAULT_BLOOD_MOON_SPAWN_MOB_RATE) + "\n");
            writer.write("#Decides if a zombie boss will spawn each BloodMoon\n");
            writer.write(ENABLE_ZOMBIE_BOSS + ": " + String.valueOf(ENABLE_ZOMBIE_BOSS_DEFAULT) + "\n");
            writer.write("#When in a permanent BloodMoon, how long (in ticks) before respawning zombie boss after death?\n");
            writer.write("#20 ticks equals a second, 24000 ticks equals a minecraft day\n");
            writer.write(ZOMBIE_BOSS_RESPAWN + ": " + String.valueOf(DEFAULT_ZOMBIE_RESPAWN_TIME) + "\n");
            writer.write("ZombieBossDamage: 7\n");
            writer.write("ZombieBossExpMultiplier: 10\n");
            writer.write("ZombieBossHealth: 50\n");
            writer.write("ZombieBossItemMultiplier: 10\n");
            writer.write("#List of items that can drop, using the\n");
            writer.write("#\"[ITEM_CODE]:[STACK AMOUNT]:[WEIGHT]:$name [META NAME]:$desc [META DESCRIPTION]:$enchant [ENCHANTMENT LIST]\" format\n");
            writer.write("#$name, $desc and $enchant are of course optional\n");
            writer.write("#The enchantment list argument is formatted as follow: \"$enchant [ENCHANT NAME],[LEVEL];\"\n");
            writer.write("#The percent chance of an item dropping is equal to [item weight] / [total weight] * 100\n");
            writer.write("#Please refer to https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html for the list of items\n");
            writer.write("#Enchantment names are exactly as they appear in-game, except with \"_\" replacing spaces. Case is irrelevant\n");
            writer.write(DROP_ITEM_LIST + ":\n");
            writer.write("  - \"IRON_INGOT:5:10\"\n");
            writer.write("  - \"GOLD_INGOT:2:5\"\n");
            writer.write("  - \"DIAMOND:1:1\"\n");
            writer.write("  - \"IRON_BLOCK:1:5\"\n");
            writer.write("  - \"GOLD_BLOCK:1:2\"\n");
            writer.write("#Here is an example of a special item drop with fire aspect I and sharpness III:\n");
            writer.write("# - \"DIAMOND_SWORD:1:1:$name Excalibur:$desc The legendary Excalibur$nSword of King Arthur:$enchant FIRE_ASPECT,1;sharpness,3\"\n");
            writer.write("#These are AOE powers affecting all players around the boss\n");
            writer.write("#Accepted values are:\n");
            writer.write("#LIGHTNING,[range],[cooldown]\n");
            writer.write("#BLINK,[range],[cooldown]\n");
            writer.write("#FIRE,[range],[cooldown],[duration]\n");
            writer.write("#UNDERLING,[range],[cooldown],[amount]\n");
            writer.write("#SPRINT,[duration],[cooldown],[amplifier]\n");
            writer.write("#BLIND,[range],[cooldown],[duration]\n");
            writer.write("#POISON,[range],[cooldown],[duration],[amplifier]\n");
            writer.write("#WITHER,[range],[cooldown],[duration],[amplifier]\n");
            writer.write("ZombieBossPowerSet:\n");
            writer.write("  - \"WITHER,12,20,5,1\"\n");
            writer.write("#Mob effects on hit. Format (with no spaces in between):\n");
            writer.write("#[Effect],[Duration in seconds],[Effect amplifier. Use 1 if you're unsure]\n");
            writer.write("#For a complete list of effects, refer to https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html\n");
            writer.write("#Additional effects include: 'LIGHTNING'\n");
            writer.write(ZOMBIEEFFECTS + ":\n");
            writer.write("  - \"wither,7,1\"\n");
            writer.write(HUSKEFFECT + ":\n");
            writer.write("  - \"wither,7,1\"\n");
            writer.write(DROWNEDEFFECT + ":\n");
            writer.write("  - \"wither,7,1\"\n");
            writer.write(ZOMBIE_VILLAGEREFFECT + ":\n");
            writer.write("  - \"wither,7,1\"\n");
            writer.write(SKELETONEFFECTS + ":\n");
            writer.write("  - \"slowness,3.5,1\"\n");
            writer.write(CREEPEREFFECTS + ":\n");
            writer.write("  - \"lightning\"\n");
            writer.write(PHANTOMEFFECTS + ":\n");
            writer.write("  - \"levitation,1.5,3\"\n");
            writer.write(SPIDEREFFECTS + ":\n");
            writer.write("  - \"poison,4,1\"\n");
            writer.write("  - \"nausea,6,9999\"\n");
            writer.write("ENDERMANEffects:\n");
            writer.write("  - \"slowness,2.5,2\"\n");
            writer.write("ZOMBIEBOSSEffects:\n");
            writer.write("  - \"wither,9,2\"\n");
            writer.write("\n#These commands will be ran right as a BloodMoon starts\n");
            writer.write("#You must append a ;s, ;f or ;p at the end of the command:\n");
            writer.write("#using ;s runs the command as the server. Once\n");
            writer.write("#using ;f runs the command as the server, for each player. use $p as the player placeholder\n");
            writer.write("#using ;p runs the command as the player, for each player. $p also applies\n");
            writer.write("#For all options, $w will be replaced by the world name\n");
            writer.write("#Note that these commands will be the very first and very last operations\n#ran when starting and ending a BloodMoon\n");
            writer.write(COMMANDS_ON_START + ":\n");
            writer.write("#  - \"some command;s\"\n");
            writer.write("#Commands ran at the end of the BloodMoon\n");
            writer.write(COMMANDS_ON_END + ":\n");
            writer.write("#  - \"command on player $p on world $w;p\"\n");
            writer.write("\n#Hordes parameter\n");
            writer.write("#Are hordes even enabled in this world?\n");
            writer.write(HORDES_ENABLED + ": " + String.valueOf(HORDES_ENABLED_DEFAULT) + "\n");
            writer.write("#The baseline spawn rate of hordes in ticks (1/5th of a second)\n");
            writer.write(BASELINE_HORDE_SPAWNRATE + ": " + String.valueOf(BASELINE_HORDE_SPAWNRATE_DEFAULT) + "\n");
            writer.write("#The variation of hordes spawn time in ticks\n");
            writer.write(HORDE_SPAWNRATE_VARIATION + ": " + String.valueOf(HORDE_SPAWNRATE_VARIATION_DEFAULT) + "\n");
            writer.write("#Decides which mobs are allowed in hordes\n");
            writer.write(HORDE_MOB_WHITELIST + ":\n");
            writer.write(" - \"ZOMBIE\"\n");
            writer.write(" - \"SKELETON\"\n");
            writer.write(" - \"SPIDER\"\n");
            writer.write("#The distance from players in block at which hordes will spawn\n");
            writer.write(HORDE_SPAWN_DISTANCE + ": " + String.valueOf(HORDE_SPAWN_DISTANCE_DEFAULT) + "\n");
            writer.write("#The minimum number of mobs in a horde\n");
            writer.write(HORDE_MIN_POPULATION + ": " + String.valueOf(HORDE_MIN_POPULATION_DEFAULT) + "\n");
            writer.write("#The maximum number of mobs in a horde\n");
            writer.write(HORDE_MAX_POPULATION + ": " + String.valueOf(HORDE_MAX_POPULATION_DEFAULT) + "\n");


            writer.close();
        }
        catch (IOException e)
        {
            System.out.println("Error: could not generate " + Bloodmoon.CONFIG_FILE);
        }
    }

    //Useful to ensure every nodes are setup
    public void ReadAllSettings ()
    {
        if (GetIsBlacklistedConfig()) return; //skip others

        GetExperienceLossConfig();
        GetInventoryLossConfig();
        GetIntervalConfig();
        GetMaxItemsDropConfig();
        GetMinItemsDropConfig();
        GetMobDamageMultConfig();
        GetMobHealthMultConfig();
        GetBloodMoonPeriodicSoundConfig();
        GetBloodMoonEndSoundConfig();
        GetItemListConfig();
        GetPreventSleepingConfig();
        GetMobsFromSpawnerNoRewardConfig ();
        GetShieldPreventEffects ();
        GetPermanentBloodMoonConfig ();
        GetPreBloodMoonCommands();
        GetPostBloodMoonCommands();
        GetZombieBossDamage();
        GetZombieBossExpMultiplier();
        GetZombieBossHealth();
        GetZombieBossItemMultiplier();
        GetEnableZombieBossConfig();
        GetZombieBossPowerSet();
    }

    public void RefreshConfigs ()
    {
        cache = null;
    }

    //============================================================================================================

    public String GetFileVersion ()
    {
        try
        {
            Object interval = GetConfig(VERSION_CONFIG);
            if (interval == null)
            {
                interval = "NaN";
            }
            return String.valueOf(interval);
        }
        catch (FileNotFoundException e)
        {
            return "NaN";
        }
    }

    public String[] GetItemListConfig ()
    {
        try
        {
            Object interval = GetConfig(DROP_ITEM_LIST);
            if (interval == null || String.valueOf(interval).equals(NULL_CONFIG))
            {
                System.out.println("Warning: could not load item list!");
                return new String[0];
            }
            ArrayList<String> list = (ArrayList<String>) interval;

            return list.toArray(new String[0]);
        }
        catch (Exception e)
        {
            System.out.println("Warning: could not load item list!");
            return new String[0];
        }
    }

    public String[] GetHordeMobWhitelist ()
    {
        try
        {
            Object interval = GetConfig(HORDE_MOB_WHITELIST);
            if (interval == null || String.valueOf(interval).equals(NULL_CONFIG))
            {
                System.out.println("Warning: could not load mob whitelist!");
                return new String[0];
            }
            ArrayList<String> list = (ArrayList<String>) interval;

            return list.toArray(new String[0]);
        }
        catch (Exception e)
        {
            System.out.println("Warning: could not load mob whitelist!");
            return new String[0];
        }
    }

    public String[] GetMobEffectConfig (String mob)
    {
        try
        {
            Object interval = GetConfig(mob + "Effects");
            if (interval == null || String.valueOf(interval).equals(NULL_CONFIG))
            {
                return new String[0];
            }
            ArrayList<String> effects = (ArrayList<String>) interval;
            return effects.toArray(new String[effects.size()]);
        }
        catch (FileNotFoundException e)
        {
            return new String[0];
        }
    }

    public boolean GetIsBlacklistedConfig ()
    {
        try
        {
            Object interval = GetConfig(IS_BLACKLISTED);
            if (interval == null)
            {
                CreateConfig(IS_BLACKLISTED, String.valueOf(IS_BLACKLISTED_DEFAULT));
                interval = IS_BLACKLISTED_DEFAULT;
            }
            return (boolean) interval;
        }
        catch (FileNotFoundException e)
        {
            return IS_BLACKLISTED_DEFAULT;
        }
    }

    public boolean GetHordeEnabled ()
    {
        try
        {
            Object interval = GetConfig(HORDES_ENABLED);
            if (interval == null)
            {
                CreateConfig(HORDES_ENABLED, String.valueOf(HORDES_ENABLED_DEFAULT));
                interval = HORDES_ENABLED_DEFAULT;
            }
            return (boolean) interval;
        }
        catch (FileNotFoundException e)
        {
            return HORDES_ENABLED_DEFAULT;
        }
    }

    public boolean GetPermanentBloodMoonConfig ()
    {
        try
        {
            Object interval = GetConfig(PERMANENT_BLOOD_MOON);
            if (interval == null)
            {
                CreateConfig(PERMANENT_BLOOD_MOON, String.valueOf(PERMANENT_BLOODMOON_DEFAULT));
                interval = PERMANENT_BLOODMOON_DEFAULT;
            }
            return (boolean) interval;
        }
        catch (FileNotFoundException e)
        {
            return PERMANENT_BLOODMOON_DEFAULT;
        }
    }

    public boolean GetShieldPreventEffects ()
    {
        try
        {
            Object interval = GetConfig(SHIELD_PREVENTS_EFFECTS);
            if (interval == null)
            {
                CreateConfig(SHIELD_PREVENTS_EFFECTS, String.valueOf(SHIELD_PREVENTS_EFFECTS_DEFAULT));
                interval = SHIELD_PREVENTS_EFFECTS_DEFAULT;
            }
            return (boolean) interval;
        }
        catch (FileNotFoundException e)
        {
            return SHIELD_PREVENTS_EFFECTS_DEFAULT;
        }
    }

    public boolean GetMobsFromSpawnerNoRewardConfig ()
    {
        try
        {
            Object interval = GetConfig(MOBS_FROM_SPAWNER_NO_REWARD);
            if (interval == null)
            {
                CreateConfig(MOBS_FROM_SPAWNER_NO_REWARD, String.valueOf(MOBS_FROM_SPAWNER_NO_REWARD_DEFAULT));
                interval = MOBS_FROM_SPAWNER_NO_REWARD_DEFAULT;
            }
            return (boolean) interval;
        }
        catch (FileNotFoundException e)
        {
            return MOBS_FROM_SPAWNER_NO_REWARD_DEFAULT;
        }
    }

    public boolean GetPreventSleepingConfig ()
    {
        try
        {
            Object interval = GetConfig(PREVENT_SLEEPING);
            if (interval == null)
            {
                CreateConfig(PREVENT_SLEEPING, String.valueOf(PREVENT_SLEEPING_DEFAULT));
                interval = PREVENT_SLEEPING_DEFAULT;
            }
            return (boolean) interval;
        }
        catch (FileNotFoundException e)
        {
            return PREVENT_SLEEPING_DEFAULT;
        }
    }

    public int GetIntervalConfig ()
    {
        try
        {
            Object interval = GetConfig(BLOOD_MOON_INTERVAL);
            if (interval == null)
            {
                CreateConfig(BLOOD_MOON_INTERVAL, String.valueOf(DEFAULT_INTERVAL));
                interval = DEFAULT_INTERVAL;
            }
            return (int) interval;
        }
        catch (FileNotFoundException e)
        {
            return DEFAULT_INTERVAL;
        }
    }

    public int GetExpMultConfig ()
    {
        try
        {
            Object interval = GetConfig(EXP_MULTIPLICATOR);
            if (interval == null)
            {
                CreateConfig(EXP_MULTIPLICATOR, String.valueOf(EXP_MULTIPLICATOR_DEFAULT));
                interval = EXP_MULTIPLICATOR_DEFAULT;
            }
            return (int) interval;
        }
        catch (FileNotFoundException e)
        {
            return EXP_MULTIPLICATOR_DEFAULT;
        }
    }

    public int GetSpawnRateConfig ()
    {
        try
        {
            Object interval = GetConfig(BLOOD_MOON_SPAWN_MOB_RATE);
            if (interval == null)
            {
                CreateConfig(BLOOD_MOON_SPAWN_MOB_RATE, String.valueOf(DEFAULT_BLOOD_MOON_SPAWN_MOB_RATE));
                interval = DEFAULT_BLOOD_MOON_SPAWN_MOB_RATE;
            }
            return (int) interval;
        }
        catch (FileNotFoundException e)
        {
            return DEFAULT_BLOOD_MOON_SPAWN_MOB_RATE;
        }
    }

    public boolean GetBloodMoonEndSoundConfig ()
    {
        try
        {
            Object interval = GetConfig(PLAY_SOUND_UPON_BLOOD_MOON_END);
            if (interval == null)
            {
                CreateConfig(PLAY_SOUND_UPON_BLOOD_MOON_END, String.valueOf(PLAY_SOUND_ON_END_DEFAULT));
                interval = PLAY_SOUND_ON_END_DEFAULT;
            }
            return (boolean) interval;
        }
        catch (FileNotFoundException e)
        {
            return PLAY_SOUND_ON_END_DEFAULT;
        }
    }

    public boolean GetDarkenSkyConfig ()
    {
        try
        {
            Object interval = GetConfig(DARKEN_SKY);
            if (interval == null)
            {
                CreateConfig(DARKEN_SKY, String.valueOf(DARKEN_SKY_DEFAULT));
                interval = DARKEN_SKY_DEFAULT;
            }
            return (boolean) interval;
        }
        catch (FileNotFoundException e)
        {
            return DARKEN_SKY_DEFAULT;
        }
    }

    public boolean GetMobHitParticleConfig ()
    {
        try
        {
            Object interval = GetConfig(MOB_HIT_PARTICLE_EFFECT);
            if (interval == null)
            {
                CreateConfig(MOB_HIT_PARTICLE_EFFECT, String.valueOf(DEFAULT_MOB_HIT_PARTICLE_EFFECT));
                interval = DEFAULT_MOB_HIT_PARTICLE_EFFECT;
            }
            return (boolean) interval;
        }
        catch (FileNotFoundException e)
        {
            return DEFAULT_MOB_HIT_PARTICLE_EFFECT;
        }
    }

    public boolean GetPlayerHitParticleConfig ()
    {
        try
        {
            Object interval = GetConfig(PLAYER_HIT_PARTICLE_EFFECT);
            if (interval == null)
            {
                CreateConfig(PLAYER_HIT_PARTICLE_EFFECT, String.valueOf(DEFAULT_PLAYER_HIT_PARTICLE_EFFECT));
                interval = DEFAULT_PLAYER_HIT_PARTICLE_EFFECT;
            }
            return (boolean) interval;
        }
        catch (FileNotFoundException e)
        {
            return DEFAULT_PLAYER_HIT_PARTICLE_EFFECT;
        }
    }

    public boolean GetPlayerDamageSoundConfig ()
    {
        try
        {
            Object interval = GetConfig(PLAY_SOUND_UPON_HIT);
            if (interval == null)
            {
                CreateConfig(PLAY_SOUND_UPON_HIT, String.valueOf(SOUND_ON_HIT_DEFAULT));
                interval = SOUND_ON_HIT_DEFAULT;
            }
            return (boolean) interval;
        }
        catch (FileNotFoundException e)
        {
            return SOUND_ON_HIT_DEFAULT;
        }
    }

    public boolean GetThunderingConfig ()
    {
        try
        {
            Object interval = GetConfig(THUNDER_DURING_BLOOD_MOON);
            if (interval == null)
            {
                CreateConfig(THUNDER_DURING_BLOOD_MOON, String.valueOf(THUNDER_DEFAULT));
                interval = THUNDER_DEFAULT;
            }
            return (boolean) interval;
        }
        catch (FileNotFoundException e)
        {
            return THUNDER_DEFAULT;
        }
    }

    public String[] GetPreBloodMoonCommands() {
        try {
            Object interval = GetConfig("CommandsOnStart");
            if (interval != null && !String.valueOf(interval).equals("null")) {
                ArrayList<String> effects = (ArrayList)interval;
                return (String[])effects.toArray(new String[effects.size()]);
            } else {
                return new String[0];
            }
        } catch (FileNotFoundException var3) {
            return new String[0];
        }
    }

    public String[] GetPostBloodMoonCommands() {
        try {
            Object interval = GetConfig("CommandsOnEnd");
            if (interval != null && !String.valueOf(interval).equals("null")) {
                ArrayList<String> effects = (ArrayList)interval;
                return (String[])effects.toArray(new String[effects.size()]);
            } else {
                return new String[0];
            }
        } catch (FileNotFoundException var3) {
            return new String[0];
        }
    }

    public boolean GetMobDeathThunderConfig ()
    {
        try
        {
            Object interval = GetConfig(LIGHTNING_EFFECT_ON_MOB_DEATH);
            if (interval == null)
            {
                CreateConfig(LIGHTNING_EFFECT_ON_MOB_DEATH, String.valueOf(MOB_LIGHTNING_EFFECT_DEFAULT));
                interval = MOB_LIGHTNING_EFFECT_DEFAULT;
            }
            return (boolean) interval;
        }
        catch (FileNotFoundException e)
        {
            return MOB_LIGHTNING_EFFECT_DEFAULT;
        }
    }

    public boolean GetBloodMoonPeriodicSoundConfig ()
    {
        try
        {
            Object interval = GetConfig(PLAY_PERIODIC_SOUNDS_DURING_BLOOD_MOON);
            if (interval == null)
            {
                CreateConfig(PLAY_PERIODIC_SOUNDS_DURING_BLOOD_MOON, String.valueOf(PLAY_PERIODIC_SOUND_DEFAULT));
                interval = PLAY_PERIODIC_SOUND_DEFAULT;
            }
            return (boolean) interval;
        }
        catch (FileNotFoundException e)
        {
            return PLAY_PERIODIC_SOUND_DEFAULT;
        }
    }

    public boolean GetLightningEffectConfig ()
    {
        try
        {
            Object interval = GetConfig(LIGHTNING_EFFECT_ON_PLAYER_DEATH);
            if (interval == null)
            {
                CreateConfig(LIGHTNING_EFFECT_ON_PLAYER_DEATH, String.valueOf(LIGTHNINGEFFECT_DEFAULT));
                interval = LIGTHNINGEFFECT_DEFAULT;
            }
            return (boolean) interval;
        }
        catch (FileNotFoundException e)
        {
            return LIGTHNINGEFFECT_DEFAULT;
        }
    }

    public boolean GetInventoryLossConfig ()
    {
        try
        {
            Object interval = GetConfig(PLAYER_LOSES_ITEM_UPON_BLOOD_MOON_DEATH);
            if (interval == null)
            {
                CreateConfig(PLAYER_LOSES_ITEM_UPON_BLOOD_MOON_DEATH, String.valueOf(DEFAULT_INV_LOSS));
                interval = DEFAULT_INTERVAL;
            }
            return (boolean) interval;
        }
        catch (FileNotFoundException e)
        {
            return DEFAULT_INV_LOSS;
        }
    }

    public boolean GetExperienceLossConfig ()
    {
        try
        {
            Object interval = GetConfig(PLAYER_LOSES_EXP_UPON_BLOOD_MOON_DEATH);
            if (interval == null)
            {
                CreateConfig(PLAYER_LOSES_EXP_UPON_BLOOD_MOON_DEATH, String.valueOf(DEFAULT_EXP_LOSS));
                interval = DEFAULT_INTERVAL;
            }
            return (boolean) interval;
        }
        catch (FileNotFoundException e)
        {
            return DEFAULT_EXP_LOSS;
        }
    }

    public int GetMinItemsDropConfig ()
    {
        try
        {
            Object interval = GetConfig(ITEM_DROPS_MINIMUM);
            if (interval == null)
            {
                CreateConfig(ITEM_DROPS_MINIMUM, String.valueOf(MIN_ITEM_DROP_DEFAULT));
                interval = MIN_ITEM_DROP_DEFAULT;
            }
            return (int) interval;
        }
        catch (FileNotFoundException e)
        {
            return MIN_ITEM_DROP_DEFAULT;
        }
    }

    public int GetHordeMinPopulation ()
    {
        try
        {
            Object interval = GetConfig(HORDE_MIN_POPULATION);
            if (interval == null)
            {
                CreateConfig(HORDE_MIN_POPULATION, String.valueOf(HORDE_MIN_POPULATION_DEFAULT));
                interval = HORDE_MIN_POPULATION_DEFAULT;
            }
            return (int) interval;
        }
        catch (FileNotFoundException e)
        {
            return HORDE_MIN_POPULATION_DEFAULT;
        }
    }

    public int GetHordeSpawnrateBaseline ()
    {
        try
        {
            Object interval = GetConfig(BASELINE_HORDE_SPAWNRATE);
            if (interval == null)
            {
                CreateConfig(BASELINE_HORDE_SPAWNRATE, String.valueOf(BASELINE_HORDE_SPAWNRATE_DEFAULT));
                interval = BASELINE_HORDE_SPAWNRATE_DEFAULT;
            }
            return (int) interval;
        }
        catch (FileNotFoundException e)
        {
            return BASELINE_HORDE_SPAWNRATE_DEFAULT;
        }
    }

    public int GetHordeSpawnrateVariation ()
    {
        try
        {
            Object interval = GetConfig(HORDE_SPAWNRATE_VARIATION);
            if (interval == null)
            {
                CreateConfig(HORDE_SPAWNRATE_VARIATION, String.valueOf(HORDE_SPAWNRATE_VARIATION_DEFAULT));
                interval = HORDE_SPAWNRATE_VARIATION_DEFAULT;
            }
            return (int) interval;
        }
        catch (FileNotFoundException e)
        {
            return HORDE_SPAWNRATE_VARIATION_DEFAULT;
        }
    }

    public int GetHordeSpawnDistance ()
    {
        try
        {
            Object interval = GetConfig(HORDE_SPAWN_DISTANCE);
            if (interval == null)
            {
                CreateConfig(HORDE_SPAWN_DISTANCE, String.valueOf(HORDE_SPAWN_DISTANCE_DEFAULT));
                interval = HORDE_SPAWN_DISTANCE_DEFAULT;
            }
            return (int) interval;
        }
        catch (FileNotFoundException e)
        {
            return HORDE_SPAWN_DISTANCE_DEFAULT;
        }
    }


    public int GetHordeMaxPopulation ()
    {
        try
        {
            Object interval = GetConfig(HORDE_MAX_POPULATION);
            if (interval == null)
            {
                CreateConfig(HORDE_MAX_POPULATION, String.valueOf(HORDE_MAX_POPULATION_DEFAULT));
                interval = HORDE_MAX_POPULATION_DEFAULT;
            }
            return (int) interval;
        }
        catch (FileNotFoundException e)
        {
            return HORDE_MAX_POPULATION_DEFAULT;
        }
    }

    public int GetMaxItemsDropConfig ()
    {
        try
        {
            Object interval = GetConfig(ITEM_DROPS_MAXIMUM);
            if (interval == null)
            {
                CreateConfig(ITEM_DROPS_MAXIMUM, String.valueOf(MAX_ITEM_DROP_DEFAULT));
                interval = MAX_ITEM_DROP_DEFAULT;
            }
            return (int) interval;
        }
        catch (FileNotFoundException e)
        {
            return MAX_ITEM_DROP_DEFAULT;
        }
    }

    public String[] GetZombieBossPowerSet() {
        try {
            Object interval = GetConfig("ZombieBossPowerSet");
            if (interval != null && !String.valueOf(interval).equals("null")) {
                ArrayList<String> effects = (ArrayList)interval;
                return (String[])effects.toArray(new String[effects.size()]);
            } else {
                return new String[0];
            }
        } catch (FileNotFoundException var3) {
            return new String[0];
        }
    }

    public boolean GetEnableZombieBossConfig() {
        try {
            Object interval = GetConfig("EnableZombieBoss");
            if (interval == null) {
                CreateConfig("EnableZombieBoss", String.valueOf(false));
                interval = false;
            }

            return (Boolean)interval;
        } catch (FileNotFoundException var2) {
            return false;
        }
    }

    public int GetZombieBossItemMultiplier() {
        try {
            Object interval = GetConfig("ZombieBossItemMultiplier");
            if (interval == null) {
                CreateConfig("ZombieBossItemMultiplier", String.valueOf(10));
                interval = 10;
            }

            return (Integer)interval;
        } catch (FileNotFoundException var2) {
            return 10;
        }
    }

    public long GetBossRespawnTime(String bossType) {
        try {
            Object interval = GetConfig(bossType + "BossRespawn");
            if (interval == null) {
                CreateConfig(bossType + "BossRespawn", String.valueOf(DEFAULT_ZOMBIE_RESPAWN_TIME));
                interval = DEFAULT_ZOMBIE_RESPAWN_TIME;
            }

            return (Integer)interval;
        } catch (FileNotFoundException var2) {
            return DEFAULT_ZOMBIE_RESPAWN_TIME;
        }
    }

    public int GetZombieBossExpMultiplier() {
        try {
            Object interval = GetConfig("ZombieBossExpMultiplier");
            if (interval == null) {
                CreateConfig("ZombieBossExpMultiplier", String.valueOf(10));
                interval = 10;
            }

            return (Integer)interval;
        } catch (FileNotFoundException var2) {
            return 10;
        }
    }

    public int GetZombieBossHealth() {
        try {
            Object interval = GetConfig("ZombieBossHealth");
            if (interval == null) {
                CreateConfig("ZombieBossHealth", String.valueOf(50));
                interval = 50;
            }

            return (Integer)interval;
        } catch (FileNotFoundException var2) {
            return 50;
        }
    }

    public int GetZombieBossDamage() {
        try {
            Object interval = GetConfig("ZombieBossDamage");
            if (interval == null) {
                CreateConfig("ZombieBossDamage", String.valueOf(7));
                interval = 7;
            }

            return (Integer)interval;
        } catch (FileNotFoundException var2) {
            return 7;
        }
    }

    public double GetMobDamageMultConfig ()
    {
        try
        {
            Object interval = GetConfig(MOB_DAMAGE_MULT);
            if (interval == null)
            {
                CreateConfig(MOB_DAMAGE_MULT, String.valueOf(MOB_DAMAGE_MULT_DEFAULT));
                interval = MOB_DAMAGE_MULT_DEFAULT;
            }
            return (double) interval;
        }
        catch (FileNotFoundException e)
        {
            return MOB_DAMAGE_MULT_DEFAULT;
        }
    }

    public double GetMobHealthMultConfig ()
    {
        try
        {
            Object interval = GetConfig(MOB_HEALTH_MULT);
            if (interval == null)
            {
                CreateConfig(MOB_HEALTH_MULT, String.valueOf(MOB_HEALTH_MULT_DEFAULT));
                interval = MOB_HEALTH_MULT_DEFAULT;
            }
            return (double) interval;
        }
        catch (FileNotFoundException e)
        {
            return MOB_HEALTH_MULT_DEFAULT;
        }
    }

    //============================================================================================================


    private void CreateConfig (String config, String value) throws FileNotFoundException
    {
        String finalString = config + ": " + value + "\n";

        try
        {
            FileWriter writer = new FileWriter(configFile, true);
            writer.append(finalString);
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        RefreshConfigs();
    }



    private Object GetConfig (String config) throws FileNotFoundException
    {
        if (cache == null)
        {
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream (configFile);

            cache = yaml.load(inputStream);
            try
            {
                inputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return cache.get(config);
    }



    @Override
    public void close() throws IOException
    {

    }
}
