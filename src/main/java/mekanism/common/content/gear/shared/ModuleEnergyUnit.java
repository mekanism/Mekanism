package mekanism.common.content.gear.shared;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemMekaSuitArmor;

@ParametersAreNonnullByDefault
public class ModuleEnergyUnit implements ICustomModule<ModuleEnergyUnit> {

    public FloatingLong getEnergyCapacity(IModule<ModuleEnergyUnit> module) {
        FloatingLong base = module.getContainer().getItem() instanceof ItemMekaSuitArmor ? MekanismConfig.gear.mekaSuitBaseEnergyCapacity.get()
                                                                                         : MekanismConfig.gear.mekaToolBaseEnergyCapacity.get();
        return base.multiply(Math.pow(2, module.getInstalledCount()));
    }

    public FloatingLong getChargeRate(IModule<ModuleEnergyUnit> module) {
        FloatingLong base = module.getContainer().getItem() instanceof ItemMekaSuitArmor ? MekanismConfig.gear.mekaSuitBaseChargeRate.get()
                                                                                         : MekanismConfig.gear.mekaToolBaseChargeRate.get();
        return base.multiply(Math.pow(2, module.getInstalledCount()));
    }

    @Override
    public void onRemoved(IModule<ModuleEnergyUnit> module, boolean last) {
        IEnergyContainer energyContainer = module.getEnergyContainer();
        if (energyContainer != null) {
            energyContainer.setEnergy(energyContainer.getEnergy().min(energyContainer.getMaxEnergy()));
        }
    }
}