package mekanism.common.capabilities.energy;

import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.AutomationType;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.tile.TileEntityResistiveHeater;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ResistiveHeaterEnergyContainer extends MachineEnergyContainer<TileEntityResistiveHeater> {

    public static ResistiveHeaterEnergyContainer input(TileEntityResistiveHeater tile) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new ResistiveHeaterEnergyContainer(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, alwaysTrue, tile);
    }

    private ResistiveHeaterEnergyContainer(double maxEnergy, double energyPerTick, Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert,
          TileEntityResistiveHeater tile) {
        super(maxEnergy, energyPerTick, canExtract, canInsert, tile);
    }

    @Override
    public boolean adjustableRates() {
        return true;
    }

    public void updateEnergyUsage(double energyUsage) {
        if (energyUsage >= 0) {
            currentEnergyPerTick = energyUsage;
            setMaxEnergy(energyUsage * 400);
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putDouble(NBTConstants.ENERGY_USAGE, getEnergyPerTick());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        NBTUtils.setDoubleIfPresent(nbt, NBTConstants.ENERGY_USAGE, this::updateEnergyUsage);
    }
}