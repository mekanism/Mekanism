package mekanism.common.content.gear.shared;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.EnergyUtils;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

@ParametersAreNotNullByDefault
public class ModuleEnergyUnit implements ICustomModule<ModuleEnergyUnit> {

    public static long getEnergyCapacity(ItemAccess attachedAccess, LongSupplier base) {
        IModule<ModuleEnergyUnit> module = IModuleHelper.INSTANCE.getModule(attachedAccess.getResource(), MekanismModules.ENERGY_UNIT);
        if (module == null) {
            return base.getAsLong();
        }
        return MathUtils.clampToLong(base.getAsLong() * Math.pow(2, module.getInstalledCount()));
    }

    public static int getChargeRate(ItemAccess attachedAccess, IntSupplier base) {
        IModule<ModuleEnergyUnit> module = IModuleHelper.INSTANCE.getModule(attachedAccess.getResource(), MekanismModules.ENERGY_UNIT);
        if (module == null) {
            return base.getAsInt();
        }
        return MathUtils.clampToInt(base.getAsInt() * Math.pow(2, module.getInstalledCount()));
    }

    @Override
    public void onRemoved(IModule<ModuleEnergyUnit> module, ItemAccess itemAccess, boolean wasLast, TransactionContext transaction) {
        IEnergyContainer energyContainer = EnergyUtils.getEnergyContainer(Capabilities.ENERGY.getCapability(itemAccess));
        if (energyContainer != null) {
            //Note: Just directly interact with the containers as we want to change the entire access and don't care about
            // splitting between multiple items if for some reason the player has an oversized stack of the MekaSuit
            long capacity = energyContainer.getCapacityAsLong();
            if (energyContainer.getAmountAsLong() > capacity) {
                energyContainer.setEnergy(capacity, transaction);
            }
        }
    }
}