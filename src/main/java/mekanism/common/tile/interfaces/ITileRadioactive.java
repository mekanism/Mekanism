package mekanism.common.tile.interfaces;

import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.math.MathUtils;
import mekanism.common.lib.radiation.RadiationManager;

public interface ITileRadioactive {

    static float calculateRadiationScale(List<IChemicalTank> tanks) {
        if (RadiationManager.isGlobalRadiationEnabled() && !tanks.isEmpty()) {
            if (tanks.size() == 1) {
                IChemicalTank tank = tanks.getFirst();
                if (!tank.isEmpty() && tank.getStack().isRadioactive()) {
                    return tank.getStored() / (float) tank.getCapacity();
                }
                return 0F;
            }
            float summedScale = 0;
            for (IChemicalTank tank : tanks) {
                if (!tank.isEmpty() && tank.getStack().isRadioactive()) {
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