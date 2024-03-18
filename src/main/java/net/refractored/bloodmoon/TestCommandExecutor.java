package net.refractored.bloodmoon;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.spectralmemories.sqlaccess.SQLAccess;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestCommandExecutor implements CommandExecutor
{
    Player player;
    World world;
    ConfigReader configReader;
    LocaleReader localeReader;
    Bloodmoon bloodmoon;
    FileWriter writer;
    SQLAccess access;

    int totalTests;
    int passedTests;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        CommandSender target = null;
        if (sender instanceof Player)
        {
            target = sender;
        }
        else if (sender instanceof ConsoleCommandSender)
        {
            if (args.length != 1)
            {
                sender.sendMessage("Please specify a player name to use for tests");
                return false;
            }
            else
            {
                Player playerFound = Bukkit.getPlayer(args[0]);
                if (playerFound != null)
                {
                    target = playerFound;
                }
                else
                {
                    sender.sendMessage("[Error] Could not find player " + args[0]);
                    return false;
                }
            }
        }
        else
        {
            return false;
        }

        //This command will run a test suite to
        Setup (target);

        StartReport();

        Method[] tests = getClass().getDeclaredMethods();

        for (Method test : tests)
        {
            if (test.getName().startsWith("Test"))
            {
                totalTests++;
                try
                {
                    boolean result = (boolean) test.invoke(this);
                    Report(test.getName(), result);
                    if (result) passedTests++;
                } catch (Exception e){
                    try
                    {
                        writer.write("There was an error running the test " + test.getName() + "\n");
                        writer.write(e.getMessage());
                        break;
                    } catch (Exception ignored) {}
                }
            }
        }

        Summarize();

        Finalize (sender);
        return true;
    }

    private void StartReport ()
    {
        File reportFile = new File (bloodmoon.getDataFolder() + Bloodmoon.SLASH + "TestSuiteReport.log");
        if (! reportFile.exists())
        {
            try
            {
                reportFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            writer = new FileWriter(reportFile, true);
            writer.write("Test Report generated at ");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            writer.write(dtf.format(LocalDateTime.now()));
            writer.write("\n============================\n\n");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void Report (String methodName, boolean passed)
    {
        try
        {
            writer.write(methodName + (passed ? " Passed!" : " Failed!") + "\n");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void Summarize ()
    {
        float ratio = (float) passedTests / (float) totalTests;
        try
        {
            writer.write("\nTest Suite Completed!\n");
            writer.write("Results: " + passedTests + " / " + totalTests + " (" + ratio * 100f + "%)\n\n\n");

            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void CustomLog (String log, boolean newLine)
    {
        try
        {
            writer.write(log + (newLine ? "\n" : ""));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void CustomLog (String log)
    {
        CustomLog(log, true);
    }

    private void Setup (CommandSender sender)
    {
        player = (Player) sender;
        world = player.getWorld();
        bloodmoon = Bloodmoon.GetInstance();
        configReader = bloodmoon.getConfigReader(world);
        localeReader = bloodmoon.getLocaleReader();
        access = bloodmoon.getSqlAccess();
        player.sendMessage("Starting tests!");
        totalTests = 0;
        passedTests = 0;
    }

    private void Finalize (CommandSender sender)
    {
        sender.sendMessage("Tests completed! " + ((totalTests == passedTests) ? "All tests passed!" : "Some tests failed!"));
    }

    //Tests here. They will NOT be run in order
    //===================================================================================
    private boolean TestTest ()
    {
        //Test the testing framework itself
        return true;
    }

    //Core functions to test: Settings, locale reading, timing
    //Player & mob damage & death

    private boolean TestStartBloodMoon ()
    {
        BloodmoonActuator actuator = BloodmoonActuator.GetActuator(world);
        if (actuator == null)
        {
            return false;
        }
        actuator.StartBloodMoon();
        return actuator.isInProgress();
    }


    private boolean TestEndBloodMoon ()
    {
        BloodmoonActuator actuator = BloodmoonActuator.GetActuator(world);
        if (actuator == null)
        {
            return false;
        }
        actuator.StopBloodMoon();
        return (! actuator.isInProgress());
    }

    private boolean TestDefaultSettings ()
    {

        if (configReader == null) return false;
        if (configReader.GetIntervalConfig() != 5) return false;
        if (! (configReader.GetInventoryLossConfig())) return false;
        if (! (configReader.GetExperienceLossConfig())) return false;
        if (configReader.GetMinItemsDropConfig() != 0) return false;
        if (configReader.GetMaxItemsDropConfig() != 4) return false;
        if (configReader.GetExpMultConfig() != 4) return false;
        if (configReader.GetMobDamageMultConfig() != 2) return false;
        if (configReader.GetMobHealthMultConfig() != 3) return false;
        if (! (configReader.GetLightningEffectConfig())) return false;
        if (! (configReader.GetMobDeathThunderConfig())) return false;
        if (! (configReader.GetBloodMoonEndSoundConfig())) return false;
        if (! (configReader.GetBloodMoonPeriodicSoundConfig())) return false;
        if (! (configReader.GetPlayerDamageSoundConfig())) return false;
        if (! (configReader.GetDarkenSkyConfig())) return false;
        if (! (configReader.GetThunderingConfig())) return false;


        String[] zombieEff = configReader.GetMobEffectConfig("ZOMBIE");
        if (zombieEff == null || zombieEff.length != 1 || ! zombieEff[0].equals("WITHER,7,1")) return false;

        if (configReader.GetIsBlacklistedConfig()) return false;

        return true;
    }


    private boolean TestSQLAccess ()
    {
        try
        {
            ResultSet set = access.ExecuteSQLQuery("SELECT * FROM lastBloodMoon WHERE world = '" + world.getUID().toString() + "';");

            boolean response = set.next();

            set.close();
            return response;
        }
        catch (SQLException e)
        {
            return false;
        }
    }

    private boolean TestReloadPlugin ()
    {
        try
        {
            player.performCommand("bloodmoon reload");
            return true;
        }
        catch (Exception ignored) {}
        return false;
    }
    //===================================================================================
}
