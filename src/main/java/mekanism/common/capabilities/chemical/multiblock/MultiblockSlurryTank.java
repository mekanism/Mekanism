package mekanism.common.capabilities.chemical.multiblock;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.inventory.AutomationType;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.prefab.TileEntityMultiblock;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockSlurryTank<MULTIBLOCK extends MultiblockData> extends MultiblockChemicalTank<Slurry, SlurryStack, MULTIBLOCK> implements ISlurryHandler,
      ISlurryTank {

    public static <MULTIBLOCK extends MultiblockData> MultiblockSlurryTank<MULTIBLOCK> create(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
          LongSupplier capacity, Predicate<@NonNull Slurry> validator) {
        Objects.requireNonNull(tile, "Tile cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Slurry validity check cannot be null");
        return new MultiblockSlurryTank<>(multiblock, tile, capacity, validator);
    }

    public static <MULTIBLOCK extends MultiblockData> MultiblockSlurryTank<MULTIBLOCK> create(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
          LongSupplier capacity, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Slurry> validator) {
        return create(multiblock, tile, capacity, canExtract, canInsert, validator, null);
    }

    public static <MULTIBLOCK extends MultiblockData> MultiblockSlurryTank<MULTIBLOCK> input(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
          LongSupplier capacity, Predicate<@NonNull Slurry> validator) {
      return create(multiblock, tile, capacity, (stack, automationType) -> automationType != AutomationType.EXTERNAL && multiblock.isFormed(),
            (stack, automationType) -> multiblock.isFormed(), validator, null);
  }

  public static <MULTIBLOCK extends MultiblockData> MultiblockSlurryTank<MULTIBLOCK> output(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
        LongSupplier capacity, Predicate<@NonNull Slurry> validator) {
      return create(multiblock, tile, capacity, (stack, automationType) -> multiblock.isFormed(),
            (stack, automationType) -> automationType != AutomationType.EXTERNAL && multiblock.isFormed(), validator, null);
  }

    public static <MULTIBLOCK extends MultiblockData> MultiblockSlurryTank<MULTIBLOCK> create(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
          LongSupplier capacity, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Slurry> validator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(tile, "Tile cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Slurry validity check cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new MultiblockSlurryTank<>(multiblock, tile, capacity, canExtract, canInsert, validator, listener);
    }

    protected MultiblockSlurryTank(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity, Predicate<@NonNull Slurry> validator) {
        super(multiblock, tile, capacity, validator);
    }

    protected MultiblockSlurryTank(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity,
          BiPredicate<@NonNull Slurry, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Slurry> validator, @Nullable IContentsListener listener) {
        super(multiblock, tile, capacity, canExtract, canInsert, validator, null, listener);
    }
}