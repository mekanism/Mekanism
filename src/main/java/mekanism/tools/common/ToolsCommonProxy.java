package mekanism.tools.common;

import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig.tools;

public class ToolsCommonProxy {

    /**
     * Set and load the mod's common configuration properties.
     */
    public void loadConfiguration() {
        tools.armorSpawnRate = Mekanism.configuration
              .get("tools.general", "MobArmorSpawnRate", 0.03, "The chance that Mekanica Armor can spawn on mobs.",
                    0.00, 1.00).getDouble(0.03);

        if (Mekanism.configuration.hasChanged()) {
            Mekanism.configuration.save();
        }
    }

    public void registerItemRenders() {
    }
}
