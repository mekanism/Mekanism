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
import mekanism.common.tile.machine.TileEntityPressurizedReactionChamber;
import net.minecraft.MethodsReturnNonnullByDefault;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PRCEnergyContainer extends MachineEnergyContainer<TileEntityPressurizedReactionChamber> {

    public static PRCEnergyContainer input(TileEntityPressurizedReactionChamber tile, @Nullable IContentsListener listener) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new PRCEnergyContainer(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, alwaysTrue, tile, listener);
    }

    private PRCEnergyContainer(FloatingLong maxEnergy, FloatingLong energyPerTick, Predicate<@NonNull AutomationType> canExtract,
          Predicate<@NonNull AutomationType> canInsert, TileEntityPressurizedReactionChamber tile, @Nullable IContentsListener listener) {
        super(maxEnergy, energyPerTick, canExtract, canInsert, tile, listener);
    }

    @Override
    public FloatingLong getBaseEnergyPerTick() {
        return super.getBaseEnergyPerTick().add(tile.getRecipeEnergyRequired());
    }
}