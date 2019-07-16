package mekanism.common.util;

import buildcraft.api.mj.IMjPassiveProvider;
import buildcraft.api.mj.IMjReceiver;
import cofh.redstoneflux.api.IEnergyContainerItem;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItemManager;
import mekanism.api.energy.EnergizedItemManager;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.buildcraft.MjIntegration;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.integration.tesla.TeslaIntegration;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

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
        IInventory inv = (TileEntityContainerBlock) storer;
        ItemStack stack = inv.getStackInSlot(slotID);
        if (!stack.isEmpty() && storer.getEnergy() < storer.getMaxEnergy()) {
            if (stack.getItem() instanceof IEnergizedItem) {
                storer.setEnergy(storer.getEnergy() + EnergizedItemManager.discharge(stack, storer.getMaxEnergy() - storer.getEnergy()));
            } else if (MekanismUtils.useTesla() && stack.hasCapability(Capabilities.TESLA_PRODUCER_CAPABILITY, null)) {
                ITeslaProducer producer = stack.getCapability(Capabilities.TESLA_PRODUCER_CAPABILITY, null);
                long needed = TeslaIntegration.toTesla(storer.getMaxEnergy() - storer.getEnergy());
                storer.setEnergy(storer.getEnergy() + TeslaIntegration.fromTesla(producer.takePower(needed, false)));
            } else if (MekanismUtils.useMj() && stack.hasCapability(Capabilities.MJ_PROVIDER_CAPABILITY, null)) {
                IMjPassiveProvider provider = stack.getCapability(Capabilities.MJ_PROVIDER_CAPABILITY, null);
                long needed = MjIntegration.toMj(storer.getMaxEnergy() - storer.getEnergy());
                storer.setEnergy(storer.getEnergy() + MjIntegration.fromMj(provider.extractPower(0, needed, false)));
            } else if (MekanismUtils.useForge() && stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
                IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY, null);
                if (storage.canExtract()) {
                    int needed = ForgeEnergyIntegration.toForge(storer.getMaxEnergy() - storer.getEnergy());
                    storer.setEnergy(storer.getEnergy() + ForgeEnergyIntegration.fromForge(storage.extractEnergy(needed, false)));
                }
            } else if (MekanismUtils.useRF() && stack.getItem() instanceof IEnergyContainerItem) {
                IEnergyContainerItem item = (IEnergyContainerItem) stack.getItem();
                int needed = MekanismUtils.clampToInt((storer.getMaxEnergy() - storer.getEnergy()) * MekanismConfig.current().general.TO_RF.val());
                storer.setEnergy(storer.getEnergy() + (item.extractEnergy(stack, needed, false) * MekanismConfig.current().general.FROM_RF.val()));
            } else if (MekanismUtils.useIC2() && isIC2Dischargeable(stack)) {
                double gain = ElectricItem.manager.discharge(stack, (storer.getMaxEnergy() - storer.getEnergy()) * MekanismConfig.current().general.TO_IC2.val(),
                      4, true, true, false) * MekanismConfig.current().general.FROM_IC2.val();
                storer.setEnergy(storer.getEnergy() + gain);
            } else if (stack.getItem() == Items.REDSTONE && storer.getEnergy() + MekanismConfig.current().general.ENERGY_PER_REDSTONE.val() <= storer.getMaxEnergy()) {
                storer.setEnergy(storer.getEnergy() + MekanismConfig.current().general.ENERGY_PER_REDSTONE.val());
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
            } else if (MekanismUtils.useTesla() && stack.hasCapability(Capabilities.TESLA_CONSUMER_CAPABILITY, null)) {
                ITeslaConsumer consumer = stack.getCapability(Capabilities.TESLA_CONSUMER_CAPABILITY, null);
                long stored = TeslaIntegration.toTesla(storer.getEnergy());
                storer.setEnergy(storer.getEnergy() - TeslaIntegration.fromTesla(consumer.givePower(stored, false)));
            } else if (MekanismUtils.useMj() && stack.hasCapability(Capabilities.MJ_RECEIVER_CAPABILITY, null)) {
                IMjReceiver receiver = stack.getCapability(Capabilities.MJ_RECEIVER_CAPABILITY, null);
                long stored = MjIntegration.toMj(storer.getEnergy());
                storer.setEnergy(storer.getEnergy() - MjIntegration.fromMj(receiver.receivePower(stored, false)));
            } else if (MekanismUtils.useForge() && stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
                IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY, null);
                if (storage.canReceive()) {
                    int stored = ForgeEnergyIntegration.toForge(storer.getEnergy());
                    storer.setEnergy(storer.getEnergy() - ForgeEnergyIntegration.fromForge(storage.receiveEnergy(stored, false)));
                }
            } else if (MekanismUtils.useRF() && stack.getItem() instanceof IEnergyContainerItem) {
                IEnergyContainerItem item = (IEnergyContainerItem) stack.getItem();
                int toTransfer = MekanismUtils.clampToInt(storer.getEnergy() * MekanismConfig.current().general.TO_RF.val());
                storer.setEnergy(storer.getEnergy() - (item.receiveEnergy(stack, toTransfer, false) * MekanismConfig.current().general.FROM_RF.val()));
            } else if (MekanismUtils.useIC2() && isIC2Chargeable(stack)) {
                double sent = ElectricItem.manager.charge(stack, storer.getEnergy() * MekanismConfig.current().general.TO_IC2.val(), 4, true, false)
                              * MekanismConfig.current().general.FROM_IC2.val();
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
            if (((IEnergizedItem) itemstack.getItem()).canSend(itemstack)) {
                if (((IEnergizedItem) itemstack.getItem()).getEnergy(itemstack) > 0) {
                    return true;
                }
            }
        }
        if (MekanismUtils.useTesla()) {
            if (itemstack.hasCapability(Capabilities.TESLA_PRODUCER_CAPABILITY, null)) {
                if (itemstack.getCapability(Capabilities.TESLA_PRODUCER_CAPABILITY, null).takePower(1, true) > 0) {
                    return true;
                }
            }
        }
        if (MekanismUtils.useMj()) {
            if (itemstack.hasCapability(Capabilities.MJ_PROVIDER_CAPABILITY, null)) {
                if (itemstack.getCapability(Capabilities.MJ_PROVIDER_CAPABILITY, null).extractPower(1, 1, true) > 0) {
                    return true;
                }
            }
        }
        if (MekanismUtils.useForge()) {
            if (itemstack.hasCapability(CapabilityEnergy.ENERGY, null)) {
                if (itemstack.getCapability(CapabilityEnergy.ENERGY, null).extractEnergy(1, true) > 0) {
                    return true;
                }
            }
        }
        if (MekanismUtils.useRF()) {
            if (itemstack.getItem() instanceof IEnergyContainerItem) {
                if (((IEnergyContainerItem) itemstack.getItem()).extractEnergy(itemstack, 1, true) != 0) {
                    return true;
                }
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
        if (MekanismUtils.useTesla()) {
            if (itemstack.hasCapability(Capabilities.TESLA_CONSUMER_CAPABILITY, null)) {
                if (itemstack.getCapability(Capabilities.TESLA_CONSUMER_CAPABILITY, null).givePower(1, true) > 0) {
                    return true;
                }
            }
        }
        if (MekanismUtils.useMj()) {
            if (itemstack.hasCapability(Capabilities.MJ_RECEIVER_CAPABILITY, null)) {
                if (itemstack.getCapability(Capabilities.MJ_RECEIVER_CAPABILITY, null).receivePower(1, true) > 0) {
                    return true;
                }
            }
        }
        if (MekanismUtils.useForge()) {
            if (itemstack.hasCapability(CapabilityEnergy.ENERGY, null)) {
                if (itemstack.getCapability(CapabilityEnergy.ENERGY, null).receiveEnergy(1, true) > 0) {
                    return true;
                }
            }
        }
        if (MekanismUtils.useRF()) {
            if (itemstack.getItem() instanceof IEnergyContainerItem) {
                if (((IEnergyContainerItem) itemstack.getItem()).receiveEnergy(itemstack, 1, true) > 0) {
                    return true;
                }
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
        if (MekanismUtils.useTesla()) {
            if (chargeSlot && itemstack.hasCapability(Capabilities.TESLA_CONSUMER_CAPABILITY, null)) {
                ITeslaConsumer consumer = itemstack.getCapability(Capabilities.TESLA_CONSUMER_CAPABILITY, null);
                return consumer.givePower(1, true) == 0;
            } else if (!chargeSlot && itemstack.hasCapability(Capabilities.TESLA_PRODUCER_CAPABILITY, null)) {
                ITeslaProducer producer = itemstack.getCapability(Capabilities.TESLA_PRODUCER_CAPABILITY, null);
                return producer.takePower(1, true) == 0;
            }
        }
        if (MekanismUtils.useMj()) {
            if (chargeSlot && itemstack.hasCapability(Capabilities.MJ_RECEIVER_CAPABILITY, null)) {
                IMjReceiver receiver = itemstack.getCapability(Capabilities.MJ_RECEIVER_CAPABILITY, null);
                return receiver.receivePower(1, true) == 0;
            } else if (!chargeSlot && itemstack.hasCapability(Capabilities.MJ_PROVIDER_CAPABILITY, null)) {
                IMjPassiveProvider provider = itemstack.getCapability(Capabilities.MJ_PROVIDER_CAPABILITY, null);
                return provider.extractPower(1, 1, true) == 0;
            }
        }
        if (MekanismUtils.useForge() && itemstack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage storage = itemstack.getCapability(CapabilityEnergy.ENERGY, null);
            if (chargeSlot) {
                return !storage.canReceive() || storage.receiveEnergy(1, true) == 0;
            }
            return !storage.canExtract() || storage.extractEnergy(1, true) == 0;
        }
        if (MekanismUtils.useRF() && itemstack.getItem() instanceof IEnergyContainerItem) {
            IEnergyContainerItem energyContainer = (IEnergyContainerItem) itemstack.getItem();
            if (chargeSlot) {
                return energyContainer.receiveEnergy(itemstack, 1, true) == 0;
            }
            return energyContainer.extractEnergy(itemstack, 1, true) == 0;
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
        return true;
    }
}