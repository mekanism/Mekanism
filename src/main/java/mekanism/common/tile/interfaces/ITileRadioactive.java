package mekanism.common.tile.interfaces;

import java.util.List;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.math.MathUtils;
import mekanism.common.lib.radiation.RadiationManager;

public interface ITileRadioactive {

    static float calculateRadiationScale(List<IChemicalTank> tanks) {
        if (RadiationManager.isGlobalRadiationEnabled() && !tanks.isEmpty()) {
            float summedScale = 0;
            for (IChemicalTank tank : tanks) {
                ChemicalResource resource = tank.getResource();
                if (!resource.isEmpty() && resource.isRadioactive()) {
                    //TODO: Eventually we may want to debate doing this based on the radioactivity
                    // but for now this will work well
                    summedScale += tank.amountAsLong() / (float) tank.getLimitAsLong(resource);
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