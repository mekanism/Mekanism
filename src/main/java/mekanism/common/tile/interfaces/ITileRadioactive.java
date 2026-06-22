package mekanism.common.tile.interfaces;

import java.util.List;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.math.MathUtils;
import mekanism.common.lib.radiation.RadiationManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.LevelReader;

public interface ITileRadioactive {

    static float calculateRadiationScale(LevelReader level, List<IChemicalTank> tanks) {
        if (RadiationManager.isGlobalRadiationEnabled() && !tanks.isEmpty()) {
            float summedScale = 0;
            RegistryAccess registryAccess = level.registryAccess();
            for (IChemicalTank tank : tanks) {
                ChemicalResource resource = tank.resource();
                if (!resource.isEmpty() && resource.isRadioactive(registryAccess)) {
                    //TODO: Eventually we may want to debate doing this based on the radioactivity
                    // but for now this will work well
                    summedScale += tank.amountAsLong() / (float) tank.capacityAsLong(resource);
                }
            }
            return summedScale / tanks.size();
        }
        return 0;
    }

    float getRadiationScale(LevelReader level);

    default int getRadiationParticleCount(LevelReader level) {
        return MathUtils.clampToInt(10 * getRadiationScale(level));
    }
}