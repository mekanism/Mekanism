package mekanism.common.content.gear.shared;

import java.util.Optional;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.FloatingLong;
import mekanism.common.registries.MekanismModules;
import net.minecraft.world.item.ItemStack;

@ParametersAreNotNullByDefault
public class ModuleEnergyUnit implements ICustomModule<ModuleEnergyUnit> {

    public static FloatingLong getEnergyCapacity(ItemStack stack, FloatingLong base) {
        return getEnergyValue(stack, base);
    }

    public static FloatingLong getChargeRate(ItemStack stack, FloatingLong base) {
        return getEnergyValue(stack, base);
    }

    private static FloatingLong getEnergyValue(ItemStack stack, FloatingLong base) {
        IModule<ModuleEnergyUnit> module = IModuleHelper.INSTANCE.getModule(stack, MekanismModules.ENERGY_UNIT);
        if (module == null) {
            return base;
        }
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