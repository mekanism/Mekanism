package mekanism.common.capabilities.energy;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.math.FloatingLong;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.tile.machine.TileEntityElectrolyticSeparator;
import net.minecraft.MethodsReturnNonnullByDefault;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectrolyticSeparatorEnergyContainer extends MachineEnergyContainer<TileEntityElectrolyticSeparator> {

    public static ElectrolyticSeparatorEnergyContainer input(TileEntityElectrolyticSeparator tile, @Nullable IContentsListener listener) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new ElectrolyticSeparatorEnergyContainer(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, alwaysTrue, tile, listener);
    }

    private ElectrolyticSeparatorEnergyContainer(FloatingLong maxEnergy, FloatingLong energyPerTick, Predicate<@NonNull AutomationType> canExtract,
          Predicate<@NonNull AutomationType> canInsert, TileEntityElectrolyticSeparator tile, @Nullable IContentsListener listener) {
        super(maxEnergy, energyPerTick, canExtract, canInsert, tile, listener);
    }

    @Override
    public FloatingLong getBaseEnergyPerTick() {
        return super.getBaseEnergyPerTick().multiply(tile.getRecipeEnergyMultiplier());
    }

    @Override
    public void updateEnergyPerTick() {
        //Update our energy per tick to be based off of our recipe
        this.currentEnergyPerTick = getBaseEnergyPerTick();
    }
}