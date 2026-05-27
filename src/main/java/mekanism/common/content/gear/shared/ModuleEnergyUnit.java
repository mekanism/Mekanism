package mekanism.common.content.gear.shared;

import java.util.function.LongSupplier;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismModules;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;

@ParametersAreNotNullByDefault
public class ModuleEnergyUnit implements ICustomModule<ModuleEnergyUnit> {

    public static long getEnergyCapacity(ItemAccess attachedAccess, LongSupplier base) {
        return getEnergyValue(attachedAccess, base);
    }

    public static long getChargeRate(ItemAccess attachedAccess, LongSupplier base) {
        return getEnergyValue(attachedAccess, base);
    }

    private static long getEnergyValue(ItemAccess attachedAccess, LongSupplier base) {
        IModule<ModuleEnergyUnit> module = IModuleHelper.INSTANCE.getModule(attachedAccess.getResource(), MekanismModules.ENERGY_UNIT);
        if (module == null) {
            return base.getAsLong();
        }
        return MathUtils.clampToLong(base.getAsLong() * Math.pow(2, module.getInstalledCount()));
    }

    @Override
    public void onRemoved(IModule<ModuleEnergyUnit> module, IModuleContainer moduleContainer, ItemStack stack, boolean wasLast) {
        //Note: While technically we could use IModule#getEnergyContainer as it is just a helper,
        // we choose not to so that the behavior is clearer when the module was the last module
        // and technically is no longer installed in the module container
        //TODO - 26.1: Test that this still works
        IStrictEnergyHandler energyHandlerItem = Capabilities.STRICT_ENERGY.getCapability(ItemAccess.forStack(stack));
        if (energyHandlerItem instanceof IMekanismStrictEnergyHandler energyHandler) {
            //Note: Just directly interact with the containers as we want to change the entire access and don't care about
            // splitting between multiple items if for some reason the player has an oversized stack of the MekaSuit
            for (IEnergyContainer energyContainer : energyHandler.getContainers()) {
                long capacity = energyContainer.capacity();
                if (energyContainer.energy() > capacity) {
                    energyContainer.setEnergy(capacity, null);
                }
            }
        }
    }
}