package mekanism.common.util;

import cofh.redstoneflux.api.IEnergyContainerItem;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import mekanism.api.energy.EnergizedItemManager;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public final class ChargeUtils {

    /**
     * Universally discharges an item, and updates the TileEntity's energy level.
     *
     * @param slotID - ID of the slot of which to charge
     * @param storer - TileEntity the item is being charged in
     */
    public static void discharge(int slotID, IStrictEnergyStorage storer) {
        IInventory inv = (TileEntityContainerBlock) storer;
        ItemStack stack = inv.getStackInSlot(slotID);

        if (!stack.isEmpty() && storer.getEnergy() < storer.getMaxEnergy()) {
            if (stack.getItem() instanceof IEnergizedItem) {
                storer.setEnergy(storer.getEnergy() + EnergizedItemManager
                      .discharge(stack, storer.getMaxEnergy() - storer.getEnergy()));
            } else if (MekanismUtils.useTesla() && stack.hasCapability(Capabilities.TESLA_PRODUCER_CAPABILITY, null)) {
                ITeslaProducer producer = stack.getCapability(Capabilities.TESLA_PRODUCER_CAPABILITY, null);

                long needed = Math.round((storer.getMaxEnergy() - storer.getEnergy()) * general.TO_TESLA);
                storer.setEnergy(storer.getEnergy() + producer.takePower(needed, false) * general.FROM_TESLA);
            } else if (MekanismUtils.useForge() && stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
                IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY, null);

                if (storage.canExtract()) {
                    int needed = (int) Math.round(
                          Math.min(Integer.MAX_VALUE, (storer.getMaxEnergy() - storer.getEnergy()) * general.TO_FORGE));
                    storer.setEnergy(storer.getEnergy() + storage.extractEnergy(needed, false) * general.FROM_FORGE);
                }
            } else if (MekanismUtils.useRF() && stack.getItem() instanceof IEnergyContainerItem) {
                IEnergyContainerItem item = (IEnergyContainerItem) stack.getItem();

                int needed = (int) Math
                      .round(Math.min(Integer.MAX_VALUE, (storer.getMaxEnergy() - storer.getEnergy()) * general.TO_RF));
                storer.setEnergy(storer.getEnergy() + (item.extractEnergy(stack, needed, false) * general.FROM_RF));
            } else if (MekanismUtils.useIC2() && stack.getItem() instanceof IElectricItem) {
                IElectricItem item = (IElectricItem) stack.getItem();

                if (item.canProvideEnergy(stack)) {
                    double gain = ElectricItem.manager
                          .discharge(stack, (storer.getMaxEnergy() - storer.getEnergy()) * general.TO_IC2, 4, true,
                                true, false) * general.FROM_IC2;
                    storer.setEnergy(storer.getEnergy() + gain);
                }
            } else if (stack.getItem() == Items.REDSTONE && storer.getEnergy() + general.ENERGY_PER_REDSTONE <= storer
                  .getMaxEnergy()) {
                storer.setEnergy(storer.getEnergy() + general.ENERGY_PER_REDSTONE);
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
        IInventory inv = (TileEntityContainerBlock) storer;
        ChargeUtils.charge(inv.getStackInSlot(slotID), storer);
    }

    /**
     * Universally charges an item, and updates the TileEntity's energy level.
     *
     * @param stack - ItemStack to charge
     * @param storer - TileEntity the item is being discharged in
     */
    public static void charge(ItemStack stack, IStrictEnergyStorage storer) {
        if (!stack.isEmpty() && storer.getEnergy() > 0) {
            if (stack.getItem() instanceof IEnergizedItem) {
                storer.setEnergy(storer.getEnergy() - EnergizedItemManager.charge(stack, storer.getEnergy()));
            } else if (MekanismUtils.useTesla() && stack.hasCapability(Capabilities.TESLA_CONSUMER_CAPABILITY, null)) {
                ITeslaConsumer consumer = stack.getCapability(Capabilities.TESLA_CONSUMER_CAPABILITY, null);

                long stored = Math.round(storer.getEnergy() * general.TO_TESLA);
                storer.setEnergy(storer.getEnergy() - consumer.givePower(stored, false) * general.FROM_TESLA);
            } else if (MekanismUtils.useForge() && stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
                IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY, null);

                if (storage.canReceive()) {
                    int stored = (int) Math.round(Math.min(Integer.MAX_VALUE, storer.getEnergy() * general.TO_FORGE));
                    storer.setEnergy(storer.getEnergy() - storage.receiveEnergy(stored, false) * general.FROM_FORGE);
                }
            } else if (MekanismUtils.useRF() && stack.getItem() instanceof IEnergyContainerItem) {
                IEnergyContainerItem item = (IEnergyContainerItem) stack.getItem();

                int toTransfer = (int) Math.round(storer.getEnergy() * general.TO_RF);
                storer.setEnergy(storer.getEnergy() - (item.receiveEnergy(stack, toTransfer, false) * general.FROM_RF));
            } else if (MekanismUtils.useIC2() && ElectricItem.manager.getTier(stack) > 0) {
                double sent = ElectricItem.manager.charge(stack, storer.getEnergy() * general.TO_IC2, 4, true, false)
                      * general.FROM_IC2;
                storer.setEnergy(storer.getEnergy() - sent);
            }
        }
    }

    /**
     * Whether or not a defined ItemStack can be discharged for energy in some way.
     *
     * @param itemstack - ItemStack to check
     * @return if the ItemStack can be discharged
     */
    public static boolean canBeDischarged(ItemStack itemstack) {
        return MekanismUtils.useIC2() && ElectricItem.manager.discharge(itemstack, 1, 0, true, true, true) > 0
                || (itemstack.getItem() instanceof IEnergizedItem
                        && ((IEnergizedItem) itemstack.getItem()).canSend(itemstack)
                        && ((IEnergizedItem) itemstack.getItem()).getEnergy(itemstack) > 0)
                || (MekanismUtils.useRF() && itemstack.getItem() instanceof IEnergyContainerItem
                        && ((IEnergyContainerItem) itemstack.getItem()).extractEnergy(itemstack, 1, true) != 0)
                || (MekanismUtils.useTesla() && itemstack.hasCapability(Capabilities.TESLA_PRODUCER_CAPABILITY, null)
                        && itemstack.getCapability(Capabilities.TESLA_PRODUCER_CAPABILITY, null).takePower(1, true) > 0)
                || (MekanismUtils.useForge() && itemstack.hasCapability(CapabilityEnergy.ENERGY, null)
                        && itemstack.getCapability(CapabilityEnergy.ENERGY, null).extractEnergy(1, true) > 0)
                || itemstack.getItem() == Items.REDSTONE;
    }

    /**
     * Whether or not a defined ItemStack can be charged with energy in some way.
     *
     * @param itemstack - ItemStack to check
     * @return if the ItemStack can be discharged
     */
    public static boolean canBeCharged(ItemStack itemstack) {
        return (MekanismUtils.useIC2() && ElectricItem.manager.charge(itemstack, 1, 0, true, true) > 0)
                || (itemstack.getItem() instanceof IEnergizedItem
                        && ((IEnergizedItem) itemstack.getItem()).canReceive(itemstack)
                        && ((IEnergizedItem) itemstack.getItem())
                                .getMaxEnergy(itemstack) < ((IEnergizedItem) itemstack.getItem()).getEnergy(itemstack))
                || (MekanismUtils.useRF() && itemstack.getItem() instanceof IEnergyContainerItem
                        && ((IEnergyContainerItem) itemstack.getItem()).receiveEnergy(itemstack, 1, true) > 0)
                || (MekanismUtils.useTesla() && itemstack.hasCapability(Capabilities.TESLA_CONSUMER_CAPABILITY, null)
                        && itemstack.getCapability(Capabilities.TESLA_CONSUMER_CAPABILITY, null).givePower(1, true) > 0)
                || (MekanismUtils.useForge() && itemstack.hasCapability(CapabilityEnergy.ENERGY, null)
                        && itemstack.getCapability(CapabilityEnergy.ENERGY, null).receiveEnergy(1, true) > 0);
    }

    /**
     * Whether or not a defined deemed-electrical ItemStack can be outputted out of a slot. This puts into account
     * whether or not that slot is used for charging or discharging.
     *
     * @param itemstack - ItemStack to perform the check on
     * @param chargeSlot - whether or not the outputting slot is for charging or discharging
     * @return if the ItemStack can be outputted
     */
    public static boolean canBeOutputted(ItemStack itemstack, boolean chargeSlot) {
        if (itemstack.getItem() instanceof IEnergizedItem) {
            IEnergizedItem energized = (IEnergizedItem) itemstack.getItem();

            if (chargeSlot) {
                return energized.getEnergy(itemstack) == energized.getMaxEnergy(itemstack);
            } else {
                return energized.getEnergy(itemstack) == 0;
            }
        } else if (MekanismUtils.useRF() && itemstack.getItem() instanceof IEnergyContainerItem) {
            IEnergyContainerItem energyContainer = (IEnergyContainerItem) itemstack.getItem();

            if (chargeSlot) {
                return energyContainer.receiveEnergy(itemstack, 1, true) == 0;
            } else {
                return energyContainer.extractEnergy(itemstack, 1, true) == 0;
            }
        } else if (MekanismUtils.useTesla()) {
            if (chargeSlot && itemstack.hasCapability(Capabilities.TESLA_CONSUMER_CAPABILITY, null)) {
                ITeslaConsumer consumer = itemstack.getCapability(Capabilities.TESLA_CONSUMER_CAPABILITY, null);
                return consumer.givePower(1, true) == 0;
            } else if (!chargeSlot && itemstack.hasCapability(Capabilities.TESLA_PRODUCER_CAPABILITY, null)) {
                ITeslaProducer producer = itemstack.getCapability(Capabilities.TESLA_PRODUCER_CAPABILITY, null);
                return producer.takePower(1, true) == 0;
            }
        } else if (MekanismUtils.useForge() && itemstack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage storage = itemstack.getCapability(CapabilityEnergy.ENERGY, null);

            if (chargeSlot) {
                return !storage.canReceive() || storage.receiveEnergy(1, true) == 0;
            } else {
                return !storage.canExtract() || storage.extractEnergy(1, true) == 0;
            }
        } else if (MekanismUtils.useIC2() && itemstack.getItem() instanceof ISpecialElectricItem) {
            IElectricItemManager manager = ((ISpecialElectricItem) itemstack.getItem()).getManager(itemstack);

            if (manager != null) {
                if (chargeSlot) {
                    return manager.charge(itemstack, 1, 3, true, true) == 0;
                } else {
                    return manager.discharge(itemstack, 1, 3, true, true, true) == 0;
                }
            }
        }

        return true;
    }
}
