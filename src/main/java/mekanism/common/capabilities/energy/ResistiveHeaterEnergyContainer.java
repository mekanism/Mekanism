package mekanism.common.capabilities.energy;

import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.MathUtils;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ResistiveHeaterEnergyContainer extends MachineEnergyContainer<TileEntityResistiveHeater> {

    public static final long USAGE_MULTIPLIER = 4;

    public static ResistiveHeaterEnergyContainer input(TileEntityResistiveHeater tile, @Nullable IContentsListener listener) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new ResistiveHeaterEnergyContainer(electricBlock.getUsage() * USAGE_MULTIPLIER, electricBlock.getUsage(), notExternal, ConstantPredicates.alwaysTrue(), tile, listener);
    }

    private ResistiveHeaterEnergyContainer(long maxEnergy, long energyPerTick, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert, TileEntityResistiveHeater tile, @Nullable IContentsListener listener) {
        super(maxEnergy, energyPerTick, canExtract, canInsert, tile, listener);
    }

    @Override
    public boolean adjustableRates() {
        return true;
    }

    public void updateEnergyUsage(long energyUsage) {
        currentEnergyPerTick = energyUsage;
        setMaxEnergy(MathUtils.multiplyClamped(energyUsage, USAGE_MULTIPLIER));
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putLong(SerializationConstants.ENERGY_USAGE, getEnergyPerTick());
    }

    @Override
    public void deserialize(ValueInput input) {
        input.getInt(SerializationConstants.ENERGY_USAGE).ifPresent(this::updateEnergyUsage);
        super.deserialize(input);
    }
}