package org.spectralmemories.bloodmoon;

import org.bukkit.Bukkit;
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
import java.util.List;

public final class Bloodmoon extends JavaPlugin
{
    public static final String CACHE_DB = "cache.db";
    public static final String CONFIG_FILE = "config.yml";
    public static final String SLASH = "/";
    private static SQLAccess sqlAccess;
    private static ConfigReader configReader;

    public final static long NIGHT_CHECK_DELAY = 40;
    private static Bloodmoon instance;

    private static List<PeriodicNightCheck> nightChecks;
    private static List<BloodmoonActuator> actuators;
    private static List<World> overworlds;

    public static Bloodmoon GetInstance ()
    {
        return instance;
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

    public ConfigReader getConfigReader ()
    {
        if (configReader == null)
        {
            File configFile = new File (getDataFolder () + SLASH + CONFIG_FILE);

            try
            {
                configFile.createNewFile();
            }
            catch (IOException e) {}

            configReader = new ConfigReader (configFile);
        }

        return configReader;
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

    @Override
    public void onEnable()
    {
        CreateFolder();

        getSqlAccess();
        getConfigReader();

        instance = this;
        nightChecks = new ArrayList<>();
        actuators = new ArrayList<>();

        for (World world : GetOverworlds())
        {
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
        getCommand("startbloodmoon").setExecutor(new BloodMoonStartExecutor());
        getCommand("endbloodmoon").setExecutor(new BloodMoonEndExecutor());

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
        if (configReader != null)
        {
            try
            {
                configReader.close();
            }
            catch (IOException ignored) {}
        }
    }
}
