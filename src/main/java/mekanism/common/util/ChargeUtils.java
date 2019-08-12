package mekanism.common.util;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItemManager;
import mekanism.api.energy.EnergizedItemManager;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.config_old.MekanismConfigOld;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.integration.ic2.IC2Integration;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.energy.CapabilityEnergy;

public final class ChargeUtils {

    public static boolean isIC2Chargeable(ItemStack itemStack) {
        return ElectricItem.manager.charge(itemStack, Integer.MAX_VALUE, 4, true, true) > 0;
    }

    public static boolean isIC2Dischargeable(ItemStack itemStack) {
        return ElectricItem.manager.discharge(itemStack, Integer.MAX_VALUE, 4, true, true, true) > 0;
    }

    /**
     * Universally discharges an item, and updates the TileEntity's energy level.
     *
     * @param slotID - ID of the slot of which to charge
     * @param storer - TileEntity the item is being charged in
     */
    public static void discharge(int slotID, IStrictEnergyStorage storer) {
        //TODO: Switch to item handler
        IInventory inv = (TileEntityMekanism) storer;
        ItemStack stack = inv.getStackInSlot(slotID);
        if (!stack.isEmpty() && storer.getEnergy() < storer.getMaxEnergy()) {
            if (stack.getItem() instanceof IEnergizedItem) {
                storer.setEnergy(storer.getEnergy() + EnergizedItemManager.discharge(stack, storer.getMaxEnergy() - storer.getEnergy()));
            } else if (MekanismUtils.useForge() && stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
                stack.getCapability(CapabilityEnergy.ENERGY).ifPresent(storage -> {
                    if (storage.canExtract()) {
                        int needed = ForgeEnergyIntegration.toForge(storer.getMaxEnergy() - storer.getEnergy());
                        storer.setEnergy(storer.getEnergy() + ForgeEnergyIntegration.fromForge(storage.extractEnergy(needed, false)));
                    }
                });
            } else if (MekanismUtils.useIC2() && isIC2Dischargeable(stack)) {
                double gain = IC2Integration.fromEU(ElectricItem.manager.discharge(stack, IC2Integration.toEU(storer.getMaxEnergy() - storer.getEnergy()), 4, true, true, false));
                storer.setEnergy(storer.getEnergy() + gain);
            } else if (stack.getItem() == Items.REDSTONE && storer.getEnergy() + MekanismConfigOld.current().general.ENERGY_PER_REDSTONE.get() <= storer.getMaxEnergy()) {
                storer.setEnergy(storer.getEnergy() + MekanismConfigOld.current().general.ENERGY_PER_REDSTONE.get());
                stack.shrink(1);
            }
        }
    }

    /**
     * Universally charges an item, and updates the TileEntity's energy level.
     *
     * @param slotID - ID of the slot of which to discharge
     * @param storer - TileEntity the item is being discharged in
     */
    public static void charge(int slotID, IStrictEnergyStorage storer) {
        //TODO: Switch to item handler
        IInventory inv = (TileEntityMekanism) storer;
        charge(inv.getStackInSlot(slotID), storer);
    }

    /**
     * Universally charges an item, and updates the TileEntity's energy level.
     *
     * @param stack  - ItemStack to charge
     * @param storer - TileEntity the item is being discharged in
     */
    public static void charge(ItemStack stack, IStrictEnergyStorage storer) {
        if (!stack.isEmpty() && storer.getEnergy() > 0) {
            if (stack.getItem() instanceof IEnergizedItem) {
                storer.setEnergy(storer.getEnergy() - EnergizedItemManager.charge(stack, storer.getEnergy()));
            } else if (MekanismUtils.useForge() && stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
                stack.getCapability(CapabilityEnergy.ENERGY).ifPresent(storage -> {
                    if (storage.canReceive()) {
                        int stored = ForgeEnergyIntegration.toForge(storer.getEnergy());
                        storer.setEnergy(storer.getEnergy() - ForgeEnergyIntegration.fromForge(storage.receiveEnergy(stored, false)));
                    }
                });
            } else if (MekanismUtils.useIC2() && isIC2Chargeable(stack)) {
                double sent = IC2Integration.fromEU(ElectricItem.manager.charge(stack, IC2Integration.toEU(storer.getEnergy()), 4, true, false));
                storer.setEnergy(storer.getEnergy() - sent);
            }
        }
    }

    /**
     * Whether or not a defined ItemStack can be discharged for energy in some way. Note: The ItemStack must also have energy to discharge.
     *
     * @param itemstack - ItemStack to check
     *
     * @return if the ItemStack can be discharged
     */
    public static boolean canBeDischarged(ItemStack itemstack) {
        if (itemstack.getItem() instanceof IEnergizedItem) {
            IEnergizedItem energizedItem = (IEnergizedItem) itemstack.getItem();
            if (energizedItem.canSend(itemstack) && energizedItem.getEnergy(itemstack) > 0) {
                return true;
            }
        }
        if (MekanismUtils.useForge()) {
            if (new LazyOptionalHelper<>(itemstack.getCapability(CapabilityEnergy.ENERGY)).matches(capability -> capability.extractEnergy(1, true) > 0)) {
                return true;
            }
        }
        if (MekanismUtils.useIC2()) {
            if (ElectricItem.manager.discharge(itemstack, 1, 0, true, true, true) > 0) {
                return true;
            }
        }
        return itemstack.getItem() == Items.REDSTONE;
    }

    /**
     * Whether or not a defined ItemStack can be charged with energy in some way. Note: The ItemStack must also have room for more energy.
     *
     * @param itemstack - ItemStack to check
     *
     * @return if the ItemStack can be discharged
     */
    public static boolean canBeCharged(ItemStack itemstack) {
        if (itemstack.getItem() instanceof IEnergizedItem) {
            IEnergizedItem energizedItem = (IEnergizedItem) itemstack.getItem();
            if (energizedItem.canReceive(itemstack)) {
                if (energizedItem.getMaxEnergy(itemstack) < energizedItem.getEnergy(itemstack)) {
                    return true;
                }
            }
        }
        if (MekanismUtils.useForge()) {
            if (new LazyOptionalHelper<>(itemstack.getCapability(CapabilityEnergy.ENERGY)).matches(capability -> capability.receiveEnergy(1, true) > 0)) {
                return true;
            }
        }
        if (MekanismUtils.useIC2()) {
            if (isIC2Chargeable(itemstack)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether or not a defined deemed-electrical ItemStack can be outputted out of a slot. This puts into account whether or not that slot is used for charging or
     * discharging.
     *
     * @param itemstack  - ItemStack to perform the check on
     * @param chargeSlot - whether or not the outputting slot is for charging or discharging
     *
     * @return if the ItemStack can be outputted
     */
    public static boolean canBeOutputted(ItemStack itemstack, boolean chargeSlot) {
        if (itemstack.getItem() instanceof IEnergizedItem) {
            IEnergizedItem energized = (IEnergizedItem) itemstack.getItem();
            if (chargeSlot) {
                return energized.getEnergy(itemstack) == energized.getMaxEnergy(itemstack);
            }
            return energized.getEnergy(itemstack) == 0;
        }
        if (MekanismUtils.useForge() && itemstack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            return new LazyOptionalHelper<>(itemstack.getCapability(CapabilityEnergy.ENERGY)).matches(storage -> {
                if (chargeSlot) {
                    return !storage.canReceive() || storage.receiveEnergy(1, true) == 0;
                }
                return !storage.canExtract() || storage.extractEnergy(1, true) == 0;
            });
        }
        if (MekanismUtils.useIC2() && (isIC2Chargeable(itemstack) || isIC2Dischargeable(itemstack))) {
            IElectricItemManager manager = ElectricItem.manager;
            if (manager != null) {
                if (chargeSlot) {
                    return manager.charge(itemstack, 1, 3, true, true) == 0;
                }
                return manager.discharge(itemstack, 1, 3, true, true, true) == 0;
            }
        }
        //TODO: Evaluate, the default used to be true but I think that is wrong
        return true;
    }
}