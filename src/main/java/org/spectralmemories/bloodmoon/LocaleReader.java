package org.spectralmemories.bloodmoon;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
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
            "PluginReloaded"
    };
    public static final String[] DEFAULT_LOCALES = {
            "BloodMoon",
            "during a BloodMoon!",
            "There are $d days until the next BloodMoon",
            "a BloodMoon is taking place right now!",
            "a BloodMoon is on its way tomorrow",
            "The sky is darker than usual tonight",
            "The BloodMoon is upon us",
            "Experience is multiplied, and mobs have bonus drops$n" +
                    "Mobs are stronger and apply special debuffs",
            "The BloodMoon fades away... for now",
            "Dying during a BloodMoon results in complete deletion of inventory",
            "Dying during a BloodMoon resets your experience to zero",
            "Plugin has been reloaded successfully"
    };
    public static final String STRING_NOT_FOUND = "[String not found]";
    public static final String NULL_LOCALE = "null";
    private File localeFile;
    private Map<String, Object> cache;
    public LocaleReader (File file)
    {
        localeFile = file;
    }

    @Override
    public void close() throws IOException
    {

    }


    //===========================================================

    public String GetLocaleString (String id)
    {
        try
        {
            Object locale = GetLocale(id);

            String value = String.valueOf(locale);
            if (value.equals(NULL_LOCALE)) throw new Exception();
            return value.replace("$n", "\n");
            //Todo: handle colors
        }
        catch (Exception ignored) {}

        System.out.println("[BloodMoon] WARNING: There was a problem loading the locale '" + id + "'. Please add it to the locales.yml file");
        return STRING_NOT_FOUND;
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
            int i = 0;
            for (String str : LOCALES_IDS)
            {
                 String finalStr = str + ": " + DEFAULT_LOCALES[i++] + "\n";

                 writer.write(finalStr);
            }

            writer.close();
        } catch (IOException e)
        {
            System.out.println("Error: could not generate " + Bloodmoon.LOCALES_YML);
        }
    }
}
