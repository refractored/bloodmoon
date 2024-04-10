package net.refractored.bloodmoon.commands;

import com.willfp.eco.core.config.base.LangYml;
import net.refractored.bloodmoon.Bloodmoon;
import net.refractored.bloodmoon.PeriodicNightCheck;
import net.refractored.bloodmoon.managers.BloodmoonManager;
import org.bukkit.World;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class BloodmoonLevelRemove {
    @CommandPermission("bloodmoon.admin.level.remove")
    @Description("Removes bloodmoon levels")
    @Command("bloodmoon level remove")
    public void BloodmoonLevelRemove(BukkitCommandActor actor, @Default("1") @Optional @Range(min=1, max=2) int targetlevel, @Optional World targetworld) {
        LangYml LangReader = Bloodmoon.GetInstance().getLangYml();
        World world;
        if (targetworld == null) {
            if (actor.isConsole()) {
                actor.error(LangReader.get("SpecifyWorld").toString());

                return;
            }
            world = actor.getAsPlayer().getWorld();
        } else {
            world = targetworld;
        }
        if (world.getEnvironment() != World.Environment.NORMAL) {
            actor.error(String.format(LangReader.get("WorldIsNotOverworldAdmin").toString(), world.getName()));
            return;
        }

        if (!Bloodmoon.GetInstance().getConfigReader(world).GetBloodMoonLevelsEnabledConfig()) {
            actor.error(String.format(LangReader.get("LevelsDisabled").toString(), world.getName()));
            return;
        }

        if (Bloodmoon.GetInstance().getConfigReader(world).GetPermanentBloodMoonConfig()) {
            actor.error(LangReader.get("WorldIsPermanentBloodMoon").toString());
            return;
        }

        PeriodicNightCheck nightCheck = PeriodicNightCheck.GetPeriodicNightCheck(world);
        if (nightCheck == null) {
            actor.error(String.format(LangReader.get("NoBloodMoonInWorld").toString(), world.getName()));
            return;
        }

        if (BloodmoonManager.GetActuator(world).isInProgress()) {
            actor.error(String.format(LangReader.get("BloodMoonRightNow").toString(), world.getName()));
            return;
        }
        int finalLevel = BloodmoonManager.GetActuator(world).getBloodMoonLevel() - targetlevel;
        if (finalLevel < 1) {
            actor.error(String.format(LangReader.get("CantBeBelowOne").toString(), world.getName()));
            return;
        }
        BloodmoonManager.GetActuator(world).setBloodMoonLevel(finalLevel);
        actor.reply(String.format("&aRemoved %d level(s) from the bloodmoom level. Bloodmoon level is now %d in world \"%s\"", targetlevel, finalLevel, world.getName()));
        actor.reply(String.format("", targetlevel, finalLevel, world.getName()));
    }
}