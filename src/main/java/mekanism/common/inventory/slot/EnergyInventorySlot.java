package mekanism.common.inventory.slot;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.annotations.NonNull;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

//TODO: IC2 add back in support to the different predicates
public class EnergyInventorySlot extends BasicInventorySlot {

    //Cache the predicates as we only really need one instance of them
    private static final BiPredicate<@NonNull ItemStack, @NonNull AutomationType> dischargeExtractPredicate = (stack, automationType) -> {
        if (automationType == AutomationType.MANUAL) {
            //Always allow extracting manually
            return true;
        }
        //Used to be ChargeUtils#canBeOutputted(stack, false)
        if (stack.getItem() instanceof IEnergizedItem) {
            return ((IEnergizedItem) stack.getItem()).getEnergy(stack) == 0;
        }
        if (MekanismUtils.useForge()) {
            LazyOptionalHelper<IEnergyStorage> forgeCapability = new LazyOptionalHelper<>(stack.getCapability(CapabilityEnergy.ENERGY));
            if (forgeCapability.isPresent()) {
                return forgeCapability.matches(storage -> !storage.canExtract() || storage.extractEnergy(1, true) == 0);
            }
        }
        return true;
    };
    private static final BiPredicate<@NonNull ItemStack, @NonNull AutomationType> dischargeInsertPredicate = (stack, automationType) -> {
        //Used to be contained in ChargeUtils#canBeDischarged
        if (stack.getItem() instanceof IEnergizedItem) {
            IEnergizedItem energizedItem = (IEnergizedItem) stack.getItem();
            if (energizedItem.canSend(stack) && energizedItem.getEnergy(stack) > 0) {
                return true;
            }
        }
        if (MekanismUtils.useForge()) {
            if (new LazyOptionalHelper<>(stack.getCapability(CapabilityEnergy.ENERGY)).matches(capability -> capability.extractEnergy(1, true) > 0)) {
                return true;
            }
        }
        return stack.getItem() == Items.REDSTONE;
    };
    private static final BiPredicate<@NonNull ItemStack, @NonNull AutomationType> chargeExtractPredicate = (stack, automationType) ->{
        if (automationType == AutomationType.MANUAL) {
            //Always allow extracting manually
            return true;
        }
        //Used to be ChargeUtils#canBeOutputted(stack, true)
        if (stack.getItem() instanceof IEnergizedItem) {
            IEnergizedItem energized = (IEnergizedItem) stack.getItem();
            return energized.getEnergy(stack) == energized.getMaxEnergy(stack);
        }
        if (MekanismUtils.useForge()) {
            LazyOptionalHelper<IEnergyStorage> forgeCapability = new LazyOptionalHelper<>(stack.getCapability(CapabilityEnergy.ENERGY));
            if (forgeCapability.isPresent()) {
                return forgeCapability.matches(storage -> !storage.canReceive() || storage.receiveEnergy(1, true) == 0);
            }
        }
        return true;
    };
    private static final BiPredicate<@NonNull ItemStack, @NonNull AutomationType> chargeInsertPredicate = (stack, automationType) -> {
        //Used to be ChargeUtils#canBeCharged
        if (stack.getItem() instanceof IEnergizedItem) {
            IEnergizedItem energizedItem = (IEnergizedItem) stack.getItem();
            if (energizedItem.canReceive(stack)) {
                //TODO: FIX THIS IN 1.12 as well, it can only be charged if we have less energy than the max energy we can store
                if (energizedItem.getEnergy(stack) < energizedItem.getMaxEnergy(stack)) {
                    return true;
                }
            }
        }
        return MekanismUtils.useForge() && new LazyOptionalHelper<>(stack.getCapability(CapabilityEnergy.ENERGY))
              .matches(capability -> capability.receiveEnergy(1, true) > 0);
    };
    private static final Predicate<@NonNull ItemStack> validPredicate = stack -> {
        //Used to be ChargeUtils#isEnergyItem
        if (stack.getItem() instanceof IEnergizedItem) {
            IEnergizedItem energizedItem = (IEnergizedItem) stack.getItem();
            //TODO: Should this just always return true??
            if (energizedItem.canSend(stack) || energizedItem.canReceive(stack)) {
                return true;
            }
        }
        if (MekanismUtils.useForge()) {
            if (new LazyOptionalHelper<>(stack.getCapability(CapabilityEnergy.ENERGY)).matches(IEnergyStorage::canExtract)) {
                return true;
            }
        }
        return stack.getItem() == Items.REDSTONE;
    };

    /**
     * Takes energy from the item
     */
    public static EnergyInventorySlot discharge(IMekanismInventory inventory, int x, int y) {
        return new EnergyInventorySlot(dischargeExtractPredicate, dischargeInsertPredicate, inventory, x, y);
    }

    /**
     * Gives energy to the item
     */
    public static EnergyInventorySlot charge(IMekanismInventory inventory, int x, int y) {
        return new EnergyInventorySlot(chargeExtractPredicate, chargeInsertPredicate, inventory, x, y);
    }

    private EnergyInventorySlot(BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canExtract, BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canInsert,
          IMekanismInventory inventory, int x, int y) {
        super(canExtract, canInsert, validPredicate, inventory, x, y);
    }

    @Override
    protected ContainerSlotType getSlotType() {
        return ContainerSlotType.POWER;
    }

    //TODO: Should we make this slot keep track of an IStrictEnergyStorage AND also then make some sort of "ITickableSlot" or something that lets us tick a bunch
    // of slots at once instead of having to manually call the relevant methods
    public void discharge(IStrictEnergyStorage storer) {
        if (!current.isEmpty() && storer.getEnergy() < storer.getMaxEnergy()) {
            if (current.getItem() instanceof IEnergizedItem) {
                IEnergizedItem energizedItem = (IEnergizedItem) current.getItem();
                if (energizedItem.canSend(current)) {
                    double currentStoredEnergy = energizedItem.getEnergy(current);
                    double energyToTransfer = Math.min(energizedItem.getMaxTransfer(current), Math.min(currentStoredEnergy, storer.getMaxEnergy() - storer.getEnergy()));
                    if (energyToTransfer > 0) {
                        //Update the energy in the item
                        energizedItem.setEnergy(current, currentStoredEnergy - energyToTransfer);
                        //Update the energy in the IStrictEnergyStorage
                        storer.setEnergy(storer.getEnergy() + energyToTransfer);
                        onContentsChanged();
                        return;
                    }
                }
            }
            if (MekanismUtils.useForge()) {
                LazyOptionalHelper<IEnergyStorage> forgeCapability = new LazyOptionalHelper<>(current.getCapability(CapabilityEnergy.ENERGY));
                boolean discharged = forgeCapability.getIfPresentElse(storage -> {
                    if (storage.canExtract()) {
                        int needed = ForgeEnergyIntegration.toForge(storer.getMaxEnergy() - storer.getEnergy());
                        storer.setEnergy(storer.getEnergy() + ForgeEnergyIntegration.fromForge(storage.extractEnergy(needed, false)));
                        return true;
                    }
                    return false;
                }, false);
                if (discharged) {
                    onContentsChanged();
                    //Exit early as we successfully discharged
                    return;
                }
            }
            if (current.getItem() == Items.REDSTONE && storer.getEnergy() + MekanismConfig.general.ENERGY_PER_REDSTONE.get() <= storer.getMaxEnergy()) {
                storer.setEnergy(storer.getEnergy() + MekanismConfig.general.ENERGY_PER_REDSTONE.get());
                current.shrink(1);
                onContentsChanged();
            }
        }
    }

    public void charge(IStrictEnergyStorage storer) {
        if (!isEmpty() && storer.getEnergy() > 0) {
            if (current.getItem() instanceof IEnergizedItem) {
                IEnergizedItem energizedItem = (IEnergizedItem) current.getItem();
                if (energizedItem.canReceive(current)) {
                    double storedEnergy = storer.getEnergy();
                    double itemStoredEnergy = energizedItem.getEnergy(current);
                    double energyToTransfer = Math.min(energizedItem.getMaxTransfer(current), Math.min(energizedItem.getMaxEnergy(current) - itemStoredEnergy, storedEnergy));
                    if (energyToTransfer > 0) {
                        //Update the energy in the item
                        energizedItem.setEnergy(current, itemStoredEnergy + energyToTransfer);
                        //Update the energy in the IStrictEnergyStorage
                        storer.setEnergy(storedEnergy - energyToTransfer);
                        onContentsChanged();
                        return;
                    }
                }
            }
            if (MekanismUtils.useForge()) {
                LazyOptionalHelper<IEnergyStorage> forgeCapability = new LazyOptionalHelper<>(current.getCapability(CapabilityEnergy.ENERGY));
                boolean charged = forgeCapability.getIfPresentElse(storage -> {
                    if (storage.canReceive()) {
                        int stored = ForgeEnergyIntegration.toForge(storer.getEnergy());
                        storer.setEnergy(storer.getEnergy() - ForgeEnergyIntegration.fromForge(storage.receiveEnergy(stored, false)));
                        return true;
                    }
                    return false;
                }, false);
                if (charged) {
                    onContentsChanged();
                    //TODO: IC2 or other energy integrations, uncomment this so that we can add another if block below the useForge if block
                    //return;
                }
            }
        }
    }
}