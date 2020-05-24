package mekanism.common.capabilities.chemical;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.pigment.IMekanismPigmentHandler;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.inventory.AutomationType;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.prefab.TileEntityMultiblock;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockPigmentTank<MULTIBLOCK extends MultiblockData> extends VariableCapacityPigmentTank {

    public static <MULTIBLOCK extends MultiblockData> MultiblockPigmentTank<MULTIBLOCK> create(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
          LongSupplier capacity, Predicate<@NonNull Pigment> validator) {
        Objects.requireNonNull(tile, "Tile cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Pigment validity check cannot be null");
        return new MultiblockPigmentTank<>(multiblock, tile, capacity, validator);
    }

    public static <MULTIBLOCK extends MultiblockData> MultiblockPigmentTank<MULTIBLOCK> create(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
          LongSupplier capacity, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Pigment> validator) {
        return create(multiblock, tile, capacity, canExtract, canInsert, validator, null);
    }

    public static <MULTIBLOCK extends MultiblockData> MultiblockPigmentTank<MULTIBLOCK> input(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
          LongSupplier capacity, Predicate<@NonNull Pigment> validator) {
      return create(multiblock, tile, capacity, (stack, automationType) -> automationType != AutomationType.EXTERNAL && multiblock.isFormed(),
            (stack, automationType) -> multiblock.isFormed(), validator, null);
  }

  public static <MULTIBLOCK extends MultiblockData> MultiblockPigmentTank<MULTIBLOCK> output(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
        LongSupplier capacity, Predicate<@NonNull Pigment> validator) {
      return create(multiblock, tile, capacity, (stack, automationType) -> multiblock.isFormed(),
            (stack, automationType) -> automationType != AutomationType.EXTERNAL && multiblock.isFormed(), validator, null);
  }

    public static <MULTIBLOCK extends MultiblockData> MultiblockPigmentTank<MULTIBLOCK> create(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
          LongSupplier capacity, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Pigment> validator, @Nullable IMekanismPigmentHandler pigmentHandler) {
        Objects.requireNonNull(tile, "Tile cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Pigment validity check cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new MultiblockPigmentTank<>(multiblock, tile, capacity, canExtract, canInsert, validator, pigmentHandler);
    }

    protected final MULTIBLOCK multiblock;
    protected final TileEntityMultiblock<MULTIBLOCK> tile;

    protected MultiblockPigmentTank(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity, Predicate<@NonNull Pigment> validator) {
        this(multiblock, tile, capacity, (stack, automationType) -> automationType != AutomationType.EXTERNAL || multiblock.isFormed(),
              (stack, automationType) -> automationType != AutomationType.EXTERNAL || multiblock.isFormed(), validator, null);
    }

    protected MultiblockPigmentTank(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity,
          BiPredicate<@NonNull Pigment, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Pigment> validator, @Nullable IMekanismPigmentHandler pigmentHandler) {
        super(capacity, canExtract, canInsert, validator, pigmentHandler);
        this.multiblock = multiblock;
        this.tile = tile;
    }

    @Override
    public void onContentsChanged() {
        super.onContentsChanged();
        if (tile.hasWorld() && !tile.isRemote()) {
            tile.markDirty(false);
            multiblock.markDirtyComparator(tile.getWorld());
        }
    }
}