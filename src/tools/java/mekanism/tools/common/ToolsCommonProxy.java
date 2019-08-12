package mekanism.tools.common;

import mekanism.common.Mekanism;
import mekanism.common.config_old.MekanismConfigOld;

public class ToolsCommonProxy {

    /**
     * Set and load the mod's common configuration properties.
     */
    public void loadConfiguration() {
        MekanismConfigOld.current().tools.load(Mekanism.configuration);
        if (Mekanism.configuration.hasChanged()) {
            Mekanism.configuration.save();
        }
    }

    public void registerItemRenders() {
    }
}