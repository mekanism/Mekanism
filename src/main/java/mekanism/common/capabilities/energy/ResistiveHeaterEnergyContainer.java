package mekanism.common.capabilities.energy;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.math.FloatingLong;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import mekanism.common.util.NBTUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ResistiveHeaterEnergyContainer extends MachineEnergyContainer<TileEntityResistiveHeater> {

    public static ResistiveHeaterEnergyContainer input(TileEntityResistiveHeater tile, @Nullable IContentsListener listener) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new ResistiveHeaterEnergyContainer(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, alwaysTrue, tile, listener);
    }

    private ResistiveHeaterEnergyContainer(FloatingLong maxEnergy, FloatingLong energyPerTick, Predicate<@NonNull AutomationType> canExtract,
          Predicate<@NonNull AutomationType> canInsert, TileEntityResistiveHeater tile, @Nullable IContentsListener listener) {
        super(maxEnergy, energyPerTick, canExtract, canInsert, tile, listener);
    }

    @Override
    public boolean adjustableRates() {
        return true;
    }

    public void updateEnergyUsage(FloatingLong energyUsage) {
        currentEnergyPerTick = energyUsage;
        setMaxEnergy(energyUsage.multiply(400));
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = super.serializeNBT();
        nbt.putString(NBTConstants.ENERGY_USAGE, getEnergyPerTick().toString());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        NBTUtils.setFloatingLongIfPresent(nbt, NBTConstants.ENERGY_USAGE, this::updateEnergyUsage);
    }
}