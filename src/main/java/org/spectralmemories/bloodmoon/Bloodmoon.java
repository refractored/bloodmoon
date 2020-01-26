package org.spectralmemories.bloodmoon;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.spectralmemories.sqlaccess.FieldType;
import org.spectralmemories.sqlaccess.SQLAccess;
import org.spectralmemories.sqlaccess.SQLField;
import org.spectralmemories.sqlaccess.SQLTable;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Bloodmoon extends JavaPlugin
{
    public static final String CACHE_DB = "cache.db";
    public static final String CONFIG_FILE = "config.yml";
    public static final String SLASH = "/";
    public static final String LOCALES_YML = "locales.yml";
    public final static long NIGHT_CHECK_DELAY = 40;

    private static SQLAccess sqlAccess;
    private static LocaleReader localeReader;

    private static Bloodmoon instance;

    private static List<PeriodicNightCheck> nightChecks;
    private static List<BloodmoonActuator> actuators;
    private static List<World> overworlds;
    private static Map<World, ConfigReader> configReaders;
    private static List<ConfigReader> allConfigReaders;

    public static Bloodmoon GetInstance ()
    {
        return instance;
    }

    private List<World> BlackListedWorlds;

    public List<World> getBlacklistedWorlds()
    {
        return BlackListedWorlds;
    }

    public static List<World> GetOverworlds ()
    {
        if (overworlds == null)
        {
            overworlds = new ArrayList<>();
            List<World> worlds = Bloodmoon.GetInstance().getServer().getWorlds();
            for (World world : worlds)
            {
                if (world.getEnvironment() == World.Environment.NORMAL) overworlds.add(world);
            }
        }

        return overworlds;
    }

    public BukkitScheduler GetScheduler ()
    {
        return this.getServer().getScheduler();
    }

    private void InitializeSQLAccess ()
    {
        boolean mustCreateDb = ! (new File(getDataFolder().getAbsolutePath() + SLASH + CACHE_DB).exists());
        try
        {
            DriverManager.getConnection(SQLAccess.JDBC_SQLITE + getDataFolder().getAbsolutePath() + SLASH + CACHE_DB); //create db if it does not exist
            File db = new File(getDataFolder().getAbsoluteFile() + SLASH + CACHE_DB);

            sqlAccess = new SQLAccess(db);

            if (mustCreateDb)
            {
                List<SQLField> fields = new ArrayList<>();
                fields.add(new SQLField("world", FieldType.TEXT, true, false));
                fields.add(new SQLField("days", FieldType.INTEGER, false, false));
                fields.add(new SQLField("checkAt", FieldType.INTEGER, false, false));

                SQLTable bloodMoonTable = new SQLTable("lastBloodMoon",  fields);

                sqlAccess.CreateTable(bloodMoonTable);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public SQLAccess getSqlAccess ()
    {
        if (sqlAccess == null) InitializeSQLAccess();
        return sqlAccess;
    }

    public ConfigReader getConfigReader (World world)
    {
        try
        {
            return configReaders.get(world);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public ConfigReader[] getAllConfigReaders ()
    {
        return allConfigReaders.toArray(new ConfigReader[allConfigReaders.size()]);
    }

    public LocaleReader getLocaleReader ()
    {
        if (localeReader == null)
        {
            File localeFile = new File (getDataFolder () + SLASH + LOCALES_YML);

            try
            {
                if (! localeFile.exists())
                {
                    localeFile.createNewFile();

                    localeReader = new LocaleReader (localeFile);
                    localeReader.GenerateDefaultFile();
                }
            }
            catch (IOException e)
            {
                System.out.println("Error: Could not create locale file");
            }

            localeReader = new LocaleReader (localeFile);
            localeReader.ReadAllEntries();
        }

        return localeReader;
    }

    public void CreateFolder ()
    {
        File folder = getDataFolder();
        folder.mkdir();
    }

    private void LoadCache (World world)
    {
        try
        {
            SQLAccess access = getSqlAccess();
            boolean exists = access.EntryExist("lastBloodMoon", new SQLField("world", FieldType.TEXT, true, false), world.getUID().toString());

            if (exists)
            {
                ResultSet set = access.ExecuteSQLQuery("SELECT days, checkAt FROM lastBloodMoon WHERE world = '" + world.getUID().toString() + "';");
                set.next();

                PeriodicNightCheck nightCheck = PeriodicNightCheck.GetPeriodicNightCheck(world);

                nightCheck.SetDaysRemaining(set.getInt("days"));
                nightCheck.SetCheckAfter(set.getInt("checkAt"));
                set.close();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    //Create a config reader, setting it up if it does not exist
    private ConfigReader CreateSingleConfigReader (World world)
    {
        File worldFolder = new File (getDataFolder() + SLASH + world.getName());
        if (! worldFolder.exists()) worldFolder.mkdir();

        File configFile = new File (worldFolder.getAbsolutePath() + SLASH + CONFIG_FILE);
        if (! configFile.exists())
        {
            try
            {
                configFile.createNewFile();
                ConfigReader reader = new ConfigReader(configFile);
                reader.GenerateDefaultFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return null;
            }
        }
        ConfigReader reader = new ConfigReader(configFile);
        reader.ReadAllSettings();
        configReaders.put(world, reader);
        allConfigReaders.add(reader);
        return reader;
    }

    @Override
    public void onEnable()
    {
        instance = this;

        CreateFolder();

        getSqlAccess();
        getLocaleReader();

        nightChecks = new ArrayList<>();
        actuators = new ArrayList<>();

        BlackListedWorlds = new ArrayList<>();
        configReaders = new HashMap<>();
        allConfigReaders = new ArrayList<>();

        for (World world : GetOverworlds())
        {
            ConfigReader configReader = CreateSingleConfigReader(world);
            if (configReader.GetIsBlacklistedConfig())
            {
                BlackListedWorlds.add(world);
                continue;
            }

            BloodmoonActuator actuator = new BloodmoonActuator(world);
            PeriodicNightCheck nightCheck = new PeriodicNightCheck(world, actuator);

            GetScheduler().runTaskLater(this, nightCheck, 0);
            getServer().getPluginManager().registerEvents(nightCheck, this);
            getServer().getPluginManager().registerEvents(actuator, this);

            nightChecks.add(nightCheck);
            actuators.add(actuator);

            LoadCache(world);
        }

        getCommand("bloodmoon").setExecutor(new BloodmoonCommandExecutor());

        //TODO: remove before release
        getCommand("testsuite").setExecutor(new TestCommandExecutor());

        CheckOlderConfigs();
    }

    @Override
    public void onDisable()
    {

        for (PeriodicNightCheck nightCheck : nightChecks)
        {
            nightCheck.UpdateCacheDatabase();
        }

        for (BloodmoonActuator actuator : actuators)
        {
            if (actuator.isInProgress()) actuator.StopBloodMoon();
        }

        if (sqlAccess != null) sqlAccess.close();
        for (ConfigReader configReader : allConfigReaders)
        {
            if (configReader != null)
            {
                try
                {
                    configReader.close();
                }
                catch (IOException e)
                {
                    System.out.println("[Error");
                    e.printStackTrace();
                }
            }
        }
        try
        {
            localeReader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void CheckOlderConfigs ()
    {
        System.out.println("[BloodMoon] This plugin is still in its infancy. If you encounter a bug, please report it to https://www.spigotmc.org/threads/bloodmoon.412741/");

        File oldConfig = new File (getDataFolder() + SLASH + CONFIG_FILE);
        if (oldConfig.exists()) System.out.println("[Deprecated] BloodMoon/config.yml is no longer used. Use per-world configuration instead");

        String localesVersion = getLocaleReader().GetFileVersion();
        if (localesVersion.equals("NaN"))
        {
            System.out.println("[Error] locales.yml has no valid version tag. Consider regenerating it");
            return;
        }
        if (! GetMajorVersions(localesVersion).equals(GetMajorVersions(getDescription().getVersion())))
            System.out.println("[Warning] Locales file was not updated since the last major update. Regenerating it is *highly* recommended");
        for (World world : overworlds)
        {
            if (BlackListedWorlds.contains(world)) continue;

            String configVersion = getConfigReader(world).GetFileVersion();
            if (configVersion.equals("NaN"))
            {
                System.out.println("[Error] " + world.getName() + "/config.yml has no valid version tag. Consider regenerating it");
                return;
            }
            if (! GetMajorVersions(configVersion).equals(GetMajorVersions(getDescription().getVersion())))
                System.out.println("[Warning] Config file for world " + world.getName() + " was not updated since the last major update. Regenerating it is *highly* recommended");
        }
    }

    private static String GetMajorVersions (String version)
    {
        String[] segments = version.split("\\.");
        return segments[0] + "." + segments[1];
    }
}
