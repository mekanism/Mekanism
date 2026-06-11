package mekanism.common.capabilities.energy;

import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.component.containers.energy.ComponentBackedResistiveEnergyContainer;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class ResistiveHeaterEnergyContainer extends MachineEnergyContainer<TileEntityResistiveHeater> {

    public static ResistiveHeaterEnergyContainer input(TileEntityResistiveHeater tile, @Nullable IContentsListener listener) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new ResistiveHeaterEnergyContainer(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, ConstantPredicates.alwaysTrue(), tile, listener);
    }

    private ResistiveHeaterEnergyContainer(long maxEnergy, int energyPerTick, Predicate<AutomationType> canExtract,
          Predicate<AutomationType> canInsert, TileEntityResistiveHeater tile, @Nullable IContentsListener listener) {
        super(maxEnergy, energyPerTick, canExtract, canInsert, tile, listener);
    }

    @Override
    public boolean adjustableRates() {
        return true;
    }

    public void updateEnergyUsage(int energyUsage) {
        //TODO: Do we want to make this support transactions?
        currentEnergyPerTick = energyUsage;
        setMaxEnergy(AttributeEnergy.STORAGE_MULTIPLIER * energyUsage);
    }

    @Override
    public void copyContents(IEnergyContainer other, @Nullable TransactionContext transaction) {
        if (other instanceof ResistiveHeaterEnergyContainer otherContainer) {
            updateEnergyUsage(otherContainer.getEnergyPerTick());
        } else if (other instanceof ComponentBackedResistiveEnergyContainer otherContainer) {
            updateEnergyUsage(otherContainer.getEnergyPerTick());
        }
        super.copyContents(other, transaction);
    }

    @Override
    public void serialize(ValueOutput output) {
        super.serialize(output);
        output.putInt(SerializationConstants.ENERGY_USAGE, getEnergyPerTick());
    }

    @Override
    public void deserialize(ValueInput input) {
        input.getInt(SerializationConstants.ENERGY_USAGE).ifPresent(this::updateEnergyUsage);
        super.deserialize(input);
    }
}