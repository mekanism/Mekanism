package mekanism.common.util;

import mekanism.api.energy.EnergizedItemManager;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public final class ChargeUtils {

    /**
     * Universally charges an item, and updates the TileEntity's energy level.
     *
     * @param stack  - ItemStack to charge
     * @param storer - TileEntity the item is being discharged in
     */
    public static void charge(ItemStack stack, IStrictEnergyStorage storer) {
        //TODO: This is now only used by the Chargepad the rest of it (currently nearly duplicate code), is in the EnergyInventorySlot
        if (!stack.isEmpty() && storer.getEnergy() > 0) {
            if (stack.getItem() instanceof IEnergizedItem) {
                storer.setEnergy(storer.getEnergy() - EnergizedItemManager.charge(stack, storer.getEnergy()));
                return;
            }
            if (MekanismUtils.useForge()) {
                LazyOptionalHelper<IEnergyStorage> forgeCapability = new LazyOptionalHelper<>(stack.getCapability(CapabilityEnergy.ENERGY));
                boolean charged = forgeCapability.getIfPresentElse(storage -> {
                    if (storage.canReceive()) {
                        int stored = ForgeEnergyIntegration.toForge(storer.getEnergy());
                        storer.setEnergy(storer.getEnergy() - ForgeEnergyIntegration.fromForge(storage.receiveEnergy(stored, false)));
                        return true;
                    }
                    return false;
                }, false);
                //TODO: IC2 or other energy integrations, uncomment this so that we can add another if block below the useForge if block
                /*if (charged) {
                    return;
                }*/
            }
        }
    }
}