package mekanism.common.integration.forgeenergy;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.integration.EnergyCompatUtils.EnergyType;
import net.minecraftforge.energy.IEnergyStorage;

public class ForgeStrictEnergyHandler implements IStrictEnergyHandler {

    private final IEnergyStorage storage;

    public ForgeStrictEnergyHandler(IEnergyStorage storage) {
        this.storage = storage;
    }

    @Override
    public int getEnergyContainerCount() {
        return 1;
    }

    @Override
    public double getEnergy(int container) {
        return container == 0 ? EnergyType.FORGE.convertFrom(storage.getEnergyStored()) : 0;
    }

    @Override
    public void setEnergy(int container, double energy) {
        //Not implemented or directly needed
    }

    @Override
    public double getMaxEnergy(int container) {
        return container == 0 ? EnergyType.FORGE.convertFrom(storage.getMaxEnergyStored()) : 0;
    }

    @Override
    public double getNeededEnergy(int container) {
        return container == 0 ? EnergyType.FORGE.convertFrom(Math.max(0, storage.getMaxEnergyStored() - storage.getEnergyStored())) : 0;
    }

    @Override
    public double insertEnergy(int container, double amount, @Nonnull Action action) {
        if (container == 0 && storage.canReceive()) {
            int toInsert = EnergyType.FORGE.convertToAsInt(amount);
            return EnergyType.FORGE.convertFrom(toInsert - storage.receiveEnergy(toInsert, action.simulate()));
        }
        return amount;
    }

    @Override
    public double extractEnergy(int container, double amount, @Nonnull Action action) {
        if (container == 0 && storage.canExtract()) {
            return EnergyType.FORGE.convertFrom(storage.extractEnergy(EnergyType.FORGE.convertToAsInt(amount), action.simulate()));
        }
        return 0;
    }
}