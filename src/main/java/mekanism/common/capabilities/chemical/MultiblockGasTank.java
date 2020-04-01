package mekanism.common.capabilities.chemical;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.inventory.AutomationType;
import mekanism.common.tile.TileEntityMultiblock;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockGasTank<MULTIBLOCK extends TileEntityMultiblock<?>> extends VariableCapacityGasTank {

    public static <MULTIBLOCK extends TileEntityMultiblock<?>> MultiblockGasTank<MULTIBLOCK> create(MULTIBLOCK tile, IntSupplier capacity, Predicate<@NonNull Gas> validator) {
        Objects.requireNonNull(tile, "Tile cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Gas validity check cannot be null");
        return new MultiblockGasTank<>(tile, capacity, validator);
    }

    protected final MULTIBLOCK multiblock;

    protected MultiblockGasTank(MULTIBLOCK multiblock, IntSupplier capacity, Predicate<@NonNull Gas> validator) {
        super(capacity, (stack, automationType) -> automationType != AutomationType.EXTERNAL || multiblock.structure != null,
              (stack, automationType) -> automationType != AutomationType.EXTERNAL || multiblock.structure != null, validator, null);
        this.multiblock = multiblock;
    }

    @Override
    public void onContentsChanged() {
        super.onContentsChanged();
        if (multiblock.hasWorld() && !multiblock.isRemote() && multiblock.isRendering) {
            multiblock.markDirty(false);
        }
    }
}