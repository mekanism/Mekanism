package mekanism.common.integration.forgeenergy;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.EnergyCompatUtils.EnergyType;
import net.minecraftforge.energy.IEnergyStorage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
        return container == 0 ? EnergyType.FORGE.convertFrom(storage.getEnergyStored()) : FloatingLong.ZERO;
    }

    @Override
    public void setEnergy(int container, FloatingLong energy) {
        //Not implemented or directly needed
    }

    @Override
    public FloatingLong getMaxEnergy(int container) {
        return container == 0 ? EnergyType.FORGE.convertFrom(storage.getMaxEnergyStored()) : FloatingLong.ZERO;
    }

    @Override
    public FloatingLong getNeededEnergy(int container) {
        return container == 0 ? EnergyType.FORGE.convertFrom(Math.max(0, storage.getMaxEnergyStored() - storage.getEnergyStored())) : FloatingLong.ZERO;
    }

    @Override
    public FloatingLong insertEnergy(int container, FloatingLong amount, @Nonnull Action action) {
        if (container == 0 && storage.canReceive()) {
            int toInsert = EnergyType.FORGE.convertToAsInt(amount);
            return EnergyType.FORGE.convertFrom(toInsert - storage.receiveEnergy(toInsert, action.simulate()));
        }
        return amount;
    }

    @Override
    public FloatingLong extractEnergy(int container, FloatingLong amount, @Nonnull Action action) {
        if (container == 0 && storage.canExtract()) {
            return EnergyType.FORGE.convertFrom(storage.extractEnergy(EnergyType.FORGE.convertToAsInt(amount), action.simulate()));
        }
        return FloatingLong.ZERO;
    }
}