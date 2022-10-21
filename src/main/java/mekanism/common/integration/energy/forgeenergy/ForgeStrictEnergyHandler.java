package mekanism.common.integration.energy.forgeenergy;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

//Note: When wrapping joules to a whole number based energy type we don't need to add any extra simulation steps
// for insert or extract when executing as we will always round down the number and just act upon a lower max requested amount
@NothingNullByDefault
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
    public FloatingLong getEnergy(int container) {
        return container == 0 ? EnergyUnit.FORGE_ENERGY.convertFrom(storage.getEnergyStored()) : FloatingLong.ZERO;
    }

    @Override
    public void setEnergy(int container, FloatingLong energy) {
        //Not implemented or directly needed
    }

    @Override
    public FloatingLong getMaxEnergy(int container) {
        return container == 0 ? EnergyUnit.FORGE_ENERGY.convertFrom(storage.getMaxEnergyStored()) : FloatingLong.ZERO;
    }

    @Override
    public FloatingLong getNeededEnergy(int container) {
        return container == 0 ? EnergyUnit.FORGE_ENERGY.convertFrom(Math.max(0, storage.getMaxEnergyStored() - storage.getEnergyStored())) : FloatingLong.ZERO;
    }

    @Override
    public FloatingLong insertEnergy(int container, FloatingLong amount, @NotNull Action action) {
        if (container == 0 && storage.canReceive()) {
            int toInsert = EnergyUnit.FORGE_ENERGY.convertToAsInt(amount);
            if (toInsert > 0) {
                int inserted = storage.receiveEnergy(toInsert, action.simulate());
                if (inserted > 0) {
                    //Only bother converting back if any was inserted
                    return amount.subtract(EnergyUnit.FORGE_ENERGY.convertFrom(inserted));
                }
            }
        }
        return amount;
    }

    @Override
    public FloatingLong extractEnergy(int container, FloatingLong amount, @NotNull Action action) {
        if (container == 0 && storage.canExtract()) {
            int toExtract = EnergyUnit.FORGE_ENERGY.convertToAsInt(amount);
            if (toExtract > 0) {
                int extracted = storage.extractEnergy(toExtract, action.simulate());
                return EnergyUnit.FORGE_ENERGY.convertFrom(extracted);
            }
        }
        return FloatingLong.ZERO;
    }
}