package org.spectralmemories.bloodmoon;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;

public class ConfigReader implements Closeable
{
    public static final String BLOOD_MOON_INTERVAL = "BloodMoonInterval";
    public static final String PLAYER_LOSES_ITEM_UPON_BLOOD_MOON_DEATH = "ItemDespawnUponDeath";
    public static final String PLAYER_LOSES_EXP_UPON_BLOOD_MOON_DEATH = "ExperienceDespawnsUponDeath";
    public static final int DEFAULT_INTERVAL = 5;
    public static final boolean DEFAULT_INV_LOSS = true;
    public static final boolean DEFAULT_EXP_LOSS = true;
    public static final String ITEM_DROPS_MAXIMUM = "ItemDropsMaximum";
    public static final String ITEM_DROPS_MINIMUM = "ItemDropsMinimum";
    public static final int MAX_ITEM_DROP_DEFAULT = 4;
    public static final int MIN_ITEM_DROP_DEFAULT = 0;
    public static final String MOB_HEALTH_MULT = "MobHealthMultiplicator";
    public static final String MOB_DAMAGE_MULT = "MobDamageMultiplicator";
    public static final int MOB_DAMAGE_MULT_DEFAULT = 3;
    public static final int MOB_HEALTH_MULT_DEFAULT = 4;

    File configFile;
    Map <String, Object> cache;

    public ConfigReader (File file)
    {
        configFile = file;
    }

    public void GenerateDefaultFile ()
    {
        try
        {
            FileWriter writer = new FileWriter(configFile, true);

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
            writer.write("#Mob damage multiplier. Whole number only\n");
            writer.write(MOB_DAMAGE_MULT + ": " + String.valueOf(MOB_DAMAGE_MULT_DEFAULT) + "\n");
            writer.write("#Mob health multiplier. Whole number only\n");
            writer.write(MOB_HEALTH_MULT + ": " + String.valueOf(MOB_HEALTH_MULT_DEFAULT) + "\n");

            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //Useful to ensure every nodes are setup
    public void ReadAllSettings ()
    {
        GetExperienceLossConfig();
        GetInventoryLossConfig();
        GetIntervalConfig();
        GetMaxItemsDropConfig();
        GetMinItemsDropConfig();
        GetMobDamageMultConfig();
        GetMobHealthMultConfig();
    }

    public void RefreshConfigs ()
    {
        cache = null;
    }

    //============================================================================================================

    //TODO: add per-world overrides

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

    public int GetMobDamageMultConfig ()
    {
        try
        {
            Object interval = GetConfig(MOB_DAMAGE_MULT);
            if (interval == null)
            {
                CreateConfig(MOB_DAMAGE_MULT, String.valueOf(MOB_DAMAGE_MULT_DEFAULT));
                interval = MOB_DAMAGE_MULT_DEFAULT;
            }
            return (int) interval;
        }
        catch (FileNotFoundException e)
        {
            return MOB_DAMAGE_MULT_DEFAULT;
        }
    }

    public int GetMobHealthMultConfig ()
    {
        try
        {
            Object interval = GetConfig(MOB_HEALTH_MULT);
            if (interval == null)
            {
                CreateConfig(MOB_HEALTH_MULT, String.valueOf(MOB_HEALTH_MULT_DEFAULT));
                interval = MOB_HEALTH_MULT_DEFAULT;
            }
            return (int) interval;
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
