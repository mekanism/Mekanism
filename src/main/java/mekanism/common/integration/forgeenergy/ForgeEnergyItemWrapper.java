package mekanism.common.integration.forgeenergy;

import mekanism.api.energy.IEnergizedItem;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

//TODO: Re-evaluate this
@Deprecated
public class ForgeEnergyItemWrapper extends ItemCapability implements IEnergyStorage {

    @Override
    public boolean canProcess(Capability<?> capability) {
        return capability == CapabilityEnergy.ENERGY;
    }

    public IEnergizedItem getItem() {
        return (IEnergizedItem) getStack().getItem();
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (getItem().canReceive(getStack())) {
            int energyNeeded = getMaxEnergyStored() - getEnergyStored();
            int toReceive = Math.min(maxReceive, energyNeeded);
            if (!simulate) {
                getItem().setEnergy(getStack(), getItem().getEnergy(getStack()) + ForgeEnergyIntegration.fromForge(toReceive));
            }
            return toReceive;
        }
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (getItem().canSend(getStack())) {
            int energyRemaining = getEnergyStored();
            int toSend = Math.min(maxExtract, energyRemaining);
            if (!simulate) {
                getItem().setEnergy(getStack(), getItem().getEnergy(getStack()) - ForgeEnergyIntegration.fromForge(toSend));
            }
            return toSend;
        }
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return ForgeEnergyIntegration.toForge(getItem().getEnergy(getStack()));
    }

    @Override
    public int getMaxEnergyStored() {
        return ForgeEnergyIntegration.toForge(getItem().getMaxEnergy(getStack()));
    }

    @Override
    public boolean canExtract() {
        return getItem().canSend(getStack());
    }

    @Override
    public boolean canReceive() {
        return getItem().canReceive(getStack());
    }
}