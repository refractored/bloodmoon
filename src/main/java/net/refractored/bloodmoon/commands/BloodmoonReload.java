package net.refractored.bloodmoon.commands;

import net.refractored.bloodmoon.Bloodmoon;
import net.refractored.bloodmoon.PeriodicNightCheck;
import net.refractored.bloodmoon.readers.ConfigReader;
import net.refractored.bloodmoon.readers.LocaleReader;
import org.bukkit.World;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;


public class BloodmoonReload {
    @CommandPermission("bloodmoon.admin.reload")
    @Description("Reload the config for bloodmoons")
    @Command("bloodmoon reload")
    public void bloodmoonReload(BukkitCommandActor actor) {


        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        localeReader.RefreshLocales();

        ConfigReader[] configReaders = Bloodmoon.GetInstance().getAllConfigReaders();
        for (ConfigReader configReader : configReaders)
        {
            configReader.RefreshConfigs();
        }
        actor.reply("&aBloodmoon has been reloaded.");
    }
}
