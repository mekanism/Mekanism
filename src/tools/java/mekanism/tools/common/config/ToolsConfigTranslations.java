package mekanism.tools.common.config;

import mekanism.common.config.IConfigTranslation;
import mekanism.tools.common.MekanismTools;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;

public enum ToolsConfigTranslations implements IConfigTranslation {
    //CLIENT CONFIG FILE
    CLIENT_DURABILITY_TOOLTIPS("client.durability_tooltips", "Durability Tooltips", "Enable durability tooltips for Mekanism: Tools gear."),

    //SERVER CONFIG FILE
    SERVER_GEAR_SPAWN_CHANCE("server.gear_spawn_chance", "Mob Gear Spawn Chance", "Settings for configuring the spawn chance of Mekanism: Tools gear on mobs"),
    SERVER_GEAR_SPAWN_CHANCE_ARMOR("server.gear_spawn_chance.armor", "Armor Chance",
          "The chance that Mekanism Armor can spawn on mobs. This is multiplied modified by the chunk's difficulty modifier. "
          + "Vanilla uses 0.15 for its armor spawns, we use 0.1 as default to lower chances of mobs getting some vanilla and some mek armor."),
    SERVER_GEAR_SPAWN_CHANCE_WEAPON("server.gear_spawn_chance.weapon", "Weapon Chance", "The chance that Mekanism Weapons can spawn in a zombie's hand."),
    SERVER_GEAR_SPAWN_CHANCE_WEAPON_HARD("server.gear_spawn_chance.weapon.hard", "Weapon Chance, Hard",
          "The chance that Mekanism Weapons can spawn in a zombie's hand when on hard difficulty."),
    ;

    private final String key;
    private final String title;
    private final String tooltip;

    ToolsConfigTranslations(String path, String title, String tooltip) {
        this.key = Util.makeDescriptionId("configuration", MekanismTools.rl(path));
        this.title = title;
        this.tooltip = tooltip;
    }

    @NotNull
    @Override
    public String getTranslationKey() {
        return key;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String tooltip() {
        return tooltip;
    }


}