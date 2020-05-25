package org.spectralmemories.bloodmoon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class LocaleReader implements Closeable
{
    public static final String[] LOCALES_IDS = {
            "BloodMoonTitleBar",
            "DeathSuffix",
            "DaysBeforeBloodMoon",
            "BloodMoonRightNow",
            "BloodMoonTomorrow",
            "BloodMoonTonight",
            "BloodMoonWarningTitle",
            "BloodMoonWarningBody",
            "BloodMoonEndingMessage",
            "DyingResultsInInventoryLoss",
            "DyingResultsInExperienceLoss",
            "PluginReloaded",
            "NoBloodMoonInWorld",
            "CommandNotFound",
            "NoPermission",
            "AllowedCommands",
            "BedNotAllowed",
            "WorldIsPermanentBloodMoon",
            "CannotStopBloodMoon",
            "GeneralError",
            "ZombieBossSpawned",
            "ZombieBossName",
            "BossSlain"
    };
    public static final String[] DEFAULT_LOCALES = {
            "BloodMoon",
            "during a BloodMoon!",
            "&aThere are&f $d &adays until the next BloodMoon",
            "&ca BloodMoon is taking place right now!",
            "&6a BloodMoon is on its way tomorrow",
            "&5The sky is darker than usual tonight",
            "&4&lThe BloodMoon is upon us",
            "&cExperience is multiplied, and mobs have bonus drops$n" +
                    "&cMobs are stronger and apply special debuffs",
            "&a&lThe BloodMoon fades away... for now",
            "&4&lDying during a BloodMoon results in complete deletion of inventory",
            "&4&lDying during a BloodMoon resets your experience to zero",
            "&aPlugin has been reloaded successfully",
            "&oThere are no BloodMoon in you current world",
            "&cCommand&o&f $d &r&cdoes not exist!",
            "&cYou do not have the permission to do that!",
            "&6Supported commands are: &f&o$d",
            "&cYou cannot sleep during a &4BloodMoon",
            "&cThis whole world is permanently in a BloodMoon!",
            "&cYou cannot stop the BloodMoon in this world!",
            "&c&oThere was an error processing your request",
            "&f&l$b &fhas arrived!",
            "The Tough One",
            "&l$p &2has slain &f$b!"
    };
    public static final String STRING_NOT_FOUND = "[String not found]";
    public static final String VERSION_CONFIG = "LocalesVersion";

    public static final String NULL_LOCALE = "null";
    public static final String VOID_STRING = "%void%";
    private File localeFile;
    private Map<String, Object> cache;
    private Map<String, String> specialCharacters;
    private String[] specialCharactersList;

    public LocaleReader (File file)
    {
        localeFile = file;
        Initialize();
    }

    private void Initialize ()
    {
        specialCharacters = new HashMap<>();
        specialCharacters.put("&b", ChatColor.AQUA.toString());
        specialCharacters.put("&0", ChatColor.BLACK.toString());
        specialCharacters.put("&9", ChatColor.BLUE.toString());
        specialCharacters.put("&3", ChatColor.DARK_AQUA.toString());
        specialCharacters.put("&1", ChatColor.DARK_BLUE.toString());
        specialCharacters.put("&8", ChatColor.DARK_GRAY.toString());
        specialCharacters.put("&2", ChatColor.DARK_GREEN.toString());
        specialCharacters.put("&5", ChatColor.DARK_PURPLE.toString());
        specialCharacters.put("&4", ChatColor.DARK_RED.toString());
        specialCharacters.put("&6", ChatColor.GOLD.toString());
        specialCharacters.put("&7", ChatColor.GRAY.toString());
        specialCharacters.put("&a", ChatColor.GREEN.toString());
        specialCharacters.put("&d", ChatColor.LIGHT_PURPLE.toString());
        specialCharacters.put("&c", ChatColor.RED.toString());
        specialCharacters.put("&f", ChatColor.WHITE.toString());
        specialCharacters.put("&e", ChatColor.YELLOW.toString());

        specialCharacters.put("&l", ChatColor.BOLD.toString());
        specialCharacters.put("&o", ChatColor.ITALIC.toString());
        specialCharacters.put("&n", ChatColor.UNDERLINE.toString());
        specialCharacters.put("&m", ChatColor.STRIKETHROUGH.toString());
        specialCharacters.put("&r", ChatColor.RESET.toString());

        specialCharactersList = new String[]{"&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7", "&8", "&9", "&a", "&b", "&c", "&d", "&e", "&f", "&l", "&o", "&n", "&m", "&r"};
    }

    @Override
    public void close() throws IOException
    {

    }


    //===========================================================

    public static void BroadcastLocale (String id, String[] args, String[] replacements)
    {
        String locale = Bloodmoon.GetInstance().getLocaleReader().GetLocaleString(id);
        if(locale.length() > 0)
        {
            if(args != null && replacements != null && args.length == replacements.length)
            {
                for (int i = 0; i < args.length; i++)
                {
                    locale = locale.replace(args[i], replacements[i]);
                }
            }
            Bukkit.broadcastMessage(locale);
        }
    }

    public static void MessageAllLocale (String id, String[] args, String[] replacements, World world)
    {
        String locale = Bloodmoon.GetInstance().getLocaleReader().GetLocaleString(id);
        if(locale.length() > 0)
        {
            if(args != null && replacements != null && args.length == replacements.length)
            {
                for (int i = 0; i < args.length; i++)
                {
                    locale = locale.replace(args[i], replacements[i]);
                }
            }
            for (Player player : world.getPlayers())
            {
                player.sendMessage(locale);
            }
        }
    }

    public static void MessageLocale (String id, String[] args, String[] replacements, Player player)
    {
        String locale = Bloodmoon.GetInstance().getLocaleReader().GetLocaleString(id);
        if(locale.length() > 0)
        {
            if(args != null && replacements != null && args.length == replacements.length)
            {
                for (int i = 0; i < args.length; i++)
                {
                    locale = locale.replace(args[i], replacements[i]);
                }
            }
            player.sendMessage(locale);
        }
    }

    public String GetLocaleString (String id)
    {
        try
        {
            Object locale = GetLocale(id);

            String value = String.valueOf(locale);
            if (value.equals(NULL_LOCALE)) throw new Exception();
            if (value.equals(VOID_STRING)) return "";
            value = value.replace("$n", "\n");

            for (String key : specialCharactersList)
            {
                value = value.replace(key, specialCharacters.get(key));
            }

            return value;
        }
        catch (Exception ignored) {}

        System.out.println("[BloodMoon] WARNING: There was a problem loading the locale '" + id + "'. Please add it to the " + Bloodmoon.LOCALES_YML + " file");
        return STRING_NOT_FOUND;
    }

    public String GetFileVersion ()
    {
        try
        {
            Object interval = GetLocale(VERSION_CONFIG);
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

    //===========================================================

    public void RefreshLocales ()
    {
        cache = null;
    }

    private Object GetLocale (String locale) throws FileNotFoundException
    {
        if (cache == null)
        {
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream (localeFile);

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

        return cache.get(locale);
    }



    public void ReadAllEntries()
    {
        for (String str : LOCALES_IDS)
        {
            try
            {
                GetLocale(str);
            }
            catch (FileNotFoundException ignored){}
        }
    }

    public void GenerateDefaultFile()
    {
        try
        {
            FileWriter writer = new FileWriter(localeFile, true);

            writer.write("#Plugin version. Please do not tamper\n");
            writer.write("LocalesVersion: " + Bloodmoon.GetInstance().getDescription().getVersion() + "\n\n");
            writer.write("#Locales file. use $n to create a new line\n");
            writer.write("#Some entries can accept a parameter, which will be $d\n");
            writer.write("#You can completely silence a line by assigning \"%void%\"");
            writer.write("#You can use color codes. Refer to https://dev.bukkit.org/projects/color-chat for more info\n\n");
            int i = 0;
            for (String str : LOCALES_IDS)
            {
                 String finalStr = str + ": " + "\"" + DEFAULT_LOCALES[i++] + "\"\n";

                 writer.write(finalStr);
            }

            writer.close();
        } catch (IOException e)
        {
            System.out.println("Error: could not generate " + Bloodmoon.LOCALES_YML);
        }
    }
}
