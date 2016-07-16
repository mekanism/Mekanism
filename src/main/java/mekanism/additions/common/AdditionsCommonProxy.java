package mekanism.additions.common;

import mekanism.api.MekanismConfig.usage;
import mekanism.common.Mekanism;

public class AdditionsCommonProxy {

    public void loadConfiguration() {
        usage.heavyWaterElectrolysisUsage = Mekanism.configuration.get("usage", "heavyWaterElectrolysisUsage", 800D).getDouble();

        if(Mekanism.configuration.hasChanged()) {
            Mekanism.configuration.save();
        }
    }
}
