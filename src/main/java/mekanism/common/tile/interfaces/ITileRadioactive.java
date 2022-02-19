package mekanism.common.tile.interfaces;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes.Radiation;
import mekanism.api.math.MathUtils;

public interface ITileRadioactive {

    static float calculateRadiationScale(List<IGasTank> tanks) {
        if (MekanismAPI.getRadiationManager().isRadiationEnabled() && !tanks.isEmpty()) {
            float summedScale = 0;
            for (IGasTank tank : tanks) {
                if (!tank.isEmpty() && tank.getStack().has(Radiation.class)) {
                    //TODO: Eventually we may want to debate doing this based on the radioactivity
                    // but for now this will work well
                    summedScale += tank.getStored() / (float) tank.getCapacity();
                }
            }
            return summedScale / tanks.size();
        }
        return 0;
    }

    float getRadiationScale();

    default int getRadiationParticleCount() {
        return MathUtils.clampToInt(10 * getRadiationScale());
    }
}