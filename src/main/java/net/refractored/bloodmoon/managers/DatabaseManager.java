package net.refractored.bloodmoon.managers;


import net.refractored.bloodmoon.Bloodmoon;
import net.refractored.bloodmoon.commands.RegisterCommands;
import net.refractored.bloodmoon.listeners.*;
import net.refractored.bloodmoon.readers.ConfigReader;
import net.refractored.bloodmoon.readers.LocaleReader;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.spectralmemories.sqlaccess.FieldType;
import org.spectralmemories.sqlaccess.SQLAccess;
import org.spectralmemories.sqlaccess.SQLField;
import org.spectralmemories.sqlaccess.SQLTable;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static net.refractored.bloodmoon.Bloodmoon.*;


public class DatabaseManager {
    private static void InitializeSQLAccess()
    {
        boolean mustCreateDb = ! (new File(Bloodmoon.GetInstance().getDataFolder().getAbsolutePath() + SLASH + CACHE_DB).exists());
        try
        {
            DriverManager.getConnection(SQLAccess.JDBC_SQLITE + Bloodmoon.GetInstance().getDataFolder().getAbsolutePath() + SLASH + CACHE_DB); //create db if it does not exist
            File db = new File(Bloodmoon.GetInstance().getDataFolder().getAbsoluteFile() + SLASH + CACHE_DB);

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

    /**
     * Returns the SQLAccess object initialized at startup
     * @return the SQLAccess instance
     */
    public static SQLAccess getSqlAccess()
    {
        if (sqlAccess == null) InitializeSQLAccess();
        return sqlAccess;
    }
}
