package mekanism.common.content.gear.shared;

import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.Module;
import mekanism.common.item.gear.ItemMekaSuitArmor;

public class ModuleEnergyUnit extends Module {

    public FloatingLong getEnergyCapacity() {
        FloatingLong base = getContainer().getItem() instanceof ItemMekaSuitArmor ? MekanismConfig.gear.mekaSuitBaseEnergyCapacity.get() : MekanismConfig.gear.mekaToolBaseEnergyCapacity.get();
        return base.multiply(Math.pow(2, getInstalledCount()));
    }

    public FloatingLong getChargeRate() {
        FloatingLong base = getContainer().getItem() instanceof ItemMekaSuitArmor ? MekanismConfig.gear.mekaSuitBaseChargeRate.get() : MekanismConfig.gear.mekaToolBaseChargeRate.get();
        return base.multiply(Math.pow(2, getInstalledCount()));
    }

    @Override
    public void onRemoved(boolean last) {
        super.onRemoved(last);
        IEnergyContainer energyContainer = getEnergyContainer();
        if (energyContainer != null) {
            energyContainer.setEnergy(energyContainer.getEnergy().min(energyContainer.getMaxEnergy()));
        }
    }
}
