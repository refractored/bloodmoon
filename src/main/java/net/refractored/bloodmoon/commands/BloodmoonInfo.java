package net.refractored.bloodmoon.commands;

import com.willfp.eco.core.Eco;
import com.willfp.eco.core.config.base.LangYml;
import net.refractored.bloodmoon.Bloodmoon;
import net.refractored.bloodmoon.managers.BloodmoonManager;
import net.refractored.bloodmoon.PeriodicNightCheck;
import net.refractored.bloodmoon.readers.ConfigReader;
import org.bukkit.World;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.logging.Logger;

public class BloodmoonInfo {
    @CommandPermission("bloodmoon.info")
    @Description("Check when the next bloodmoon is")
    @Command("bloodmoon info")
    public void bloodmoonInfo(BukkitCommandActor actor) {
        World world = actor.getAsPlayer().getWorld();
        LangYml LangReader = Bloodmoon.GetInstance().getLangYml();
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
        if (world.getEnvironment() != World.Environment.NORMAL) {
            actor.error(Bloodmoon.GetInstance().getLangYml().get("WorldIsNotOverworld").toString());

            return;
        }

        if (configReader.GetPermanentBloodMoonConfig())
        {

            actor.error(LangReader.get("WorldIsPermanentBloodMoon").toString());

            return;
        }

        BloodmoonManager worldActuator = BloodmoonManager.GetActuator(world);

        if (worldActuator == null)
        {
            actor.reply(String.format(LangReader.get("NoBloodMoonInWorld").toString(), world.getName()));
            return;
        }

        if (BloodmoonManager.GetActuator(world).isInProgress())
        {
            actor.error(String.format(LangReader.get("BloodMoonRightNow").toString(), world.getName()));
            return;
        }

        int remainingDays = (BloodmoonManager.GetActuator(world).getBloodMoonDays() + 1);

        if (remainingDays < 0) {
            Bloodmoon.GetInstance().getLogger().warning("remainingDays is lower than 0. Please regenerate both the cache and the config for world" + world.getName());
            actor.error(LangReader.get("GeneralError").toString());
            return;
        }
        actor.reply(String.format(LangReader.get("DaysRemaning").toString(), remainingDays, world.getName()));
        actor.reply(String.format(LangReader.get("BloodmoonLevel").toString(), worldActuator.getBloodMoonLevel()));
    }
}
