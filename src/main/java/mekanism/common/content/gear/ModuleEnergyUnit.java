package mekanism.common.content.gear;

import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.StorageUtils;

public class ModuleEnergyUnit extends Module {

    public FloatingLong getEnergyCapacity() {
        return MekanismConfig.general.mekaToolBaseEnergyCapacity.get().multiply(Math.pow(2, getInstalledCount()));
    }

    @Override
    public void onRemoved(boolean last) {
        super.onRemoved(last);

        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(getContainer(), 0);
        if (energyContainer != null) {
            energyContainer.setEnergy(energyContainer.getEnergy().min(energyContainer.getMaxEnergy()));
        }
    }
}
