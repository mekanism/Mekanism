package mekanism.common.capabilities.fluid;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class MultiblockFluidTank<MULTIBLOCK extends MultiblockData> extends VariableCapacityFluidTank {

    protected final MULTIBLOCK multiblock;
    protected final TileEntityMultiblock<MULTIBLOCK> tile;

    public static <MULTIBLOCK extends MultiblockData> MultiblockFluidTank<MULTIBLOCK> create(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
          IntSupplier capacity, Predicate<@NotNull FluidStack> validator) {
        Objects.requireNonNull(tile, "Tile cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new MultiblockFluidTank<>(multiblock, tile, capacity, validator);
    }

    public static <MULTIBLOCK extends MultiblockData> MultiblockFluidTank<MULTIBLOCK> input(MULTIBLOCK multiblock,
          TileEntityMultiblock<MULTIBLOCK> tile, IntSupplier capacity, Predicate<@NotNull FluidStack> validator) {
        return input(multiblock, tile, capacity, validator, null);
    }

    public static <MULTIBLOCK extends MultiblockData> MultiblockFluidTank<MULTIBLOCK> input(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
          IntSupplier capacity, Predicate<@NotNull FluidStack> validator, @Nullable IContentsListener listener) {
        return create(multiblock, tile, capacity, (stack, automationType) -> automationType != AutomationType.EXTERNAL && multiblock.isFormed(),
              (stack, automationType) -> multiblock.isFormed(), validator, listener);
    }

    public static <MULTIBLOCK extends MultiblockData> MultiblockFluidTank<MULTIBLOCK> output(MULTIBLOCK multiblock,
          TileEntityMultiblock<MULTIBLOCK> tile, IntSupplier capacity, Predicate<@NotNull FluidStack> validator) {
        return create(multiblock, tile, capacity, (stack, automationType) -> multiblock.isFormed(),
              (stack, automationType) -> automationType != AutomationType.EXTERNAL && multiblock.isFormed(), validator, null);
    }

    public static <MULTIBLOCK extends MultiblockData> MultiblockFluidTank<MULTIBLOCK> create(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
          IntSupplier capacity, BiPredicate<@NotNull FluidStack, @NotNull AutomationType> canExtract, BiPredicate<@NotNull FluidStack, @NotNull AutomationType> canInsert,
          Predicate<@NotNull FluidStack> validator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(tile, "Tile cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new MultiblockFluidTank<>(multiblock, tile, capacity, canExtract, canInsert, validator, listener);
    }

    protected MultiblockFluidTank(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, IntSupplier capacity, Predicate<@NotNull FluidStack> validator) {
        this(multiblock, tile, capacity, (stack, automationType) -> automationType != AutomationType.EXTERNAL || multiblock.isFormed(),
              (stack, automationType) -> automationType != AutomationType.EXTERNAL || multiblock.isFormed(), validator, null);
    }

    protected MultiblockFluidTank(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, IntSupplier capacity,
          BiPredicate<@NotNull FluidStack, @NotNull AutomationType> canExtract, BiPredicate<@NotNull FluidStack, @NotNull AutomationType> canInsert,
          Predicate<@NotNull FluidStack> validator, @Nullable IContentsListener listener) {
        super(capacity, canExtract, canInsert, validator, listener);
        this.multiblock = multiblock;
        this.tile = tile;
    }

    @Override
    public void onContentsChanged() {
        super.onContentsChanged();
        if (tile.hasLevel() && !tile.getLevel().isClientSide()) {
            tile.markForSave();
            multiblock.markDirtyComparator(tile.getLevel());
        }
    }
}
