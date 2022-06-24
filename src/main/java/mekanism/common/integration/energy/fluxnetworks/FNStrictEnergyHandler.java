package mekanism.common.integration.energy.fluxnetworks;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import net.minecraft.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FNStrictEnergyHandler implements IStrictEnergyHandler {

    private final IFNEnergyStorage storage;

    public FNStrictEnergyHandler(IFNEnergyStorage storage) {
        this.storage = storage;
    }

    @Override
    public int getEnergyContainerCount() {
        return 1;
    }

    @Override
    public FloatingLong getEnergy(int container) {
        return container == 0 ? EnergyUnit.FORGE_ENERGY.convertFrom(storage.getEnergyStoredL()) : FloatingLong.ZERO;
    }

    @Override
    public void setEnergy(int container, FloatingLong energy) {
        //Not implemented or directly needed
    }

    @Override
    public FloatingLong getMaxEnergy(int container) {
        return container == 0 ? EnergyUnit.FORGE_ENERGY.convertFrom(storage.getMaxEnergyStoredL()) : FloatingLong.ZERO;
    }

    @Override
    public FloatingLong getNeededEnergy(int container) {
        return container == 0 ? EnergyUnit.FORGE_ENERGY.convertFrom(Math.max(0, storage.getMaxEnergyStoredL() - storage.getEnergyStoredL())) : FloatingLong.ZERO;
    }

    @Override
    public FloatingLong insertEnergy(int container, FloatingLong amount, @Nonnull Action action) {
        if (container == 0 && storage.canReceive()) {
            long inserted = storage.receiveEnergyL(EnergyUnit.FORGE_ENERGY.convertToAsLong(amount), action.simulate());
            if (inserted > 0) {
                //Only bother converting back if any was able to be inserted
                return amount.subtract(EnergyUnit.FORGE_ENERGY.convertFrom(inserted));
            }
        }
        return amount;
    }

    @Override
    public FloatingLong extractEnergy(int container, FloatingLong amount, @Nonnull Action action) {
        if (container == 0 && storage.canExtract()) {
            return EnergyUnit.FORGE_ENERGY.convertFrom(storage.extractEnergyL(EnergyUnit.FORGE_ENERGY.convertToAsLong(amount), action.simulate()));
        }
        return FloatingLong.ZERO;
    }
}