package mekanism.common.tile.interfaces;

import java.util.List;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.MathUtils;
import mekanism.api.radiation.IRadiationManager;

public interface ITileRadioactive {

    static float calculateRadiationScale(List<IGasTank> tanks) {
        if (IRadiationManager.INSTANCE.isRadiationEnabled() && !tanks.isEmpty()) {
            if (tanks.size() == 1) {
                IGasTank tank = tanks.get(0);
                if (!tank.isEmpty() && tank.getStack().isRadioactive()) {
                    return tank.getStored() / (float) tank.getCapacity();
                }
                return 0F;
            }
            float summedScale = 0;
            for (IGasTank tank : tanks) {
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