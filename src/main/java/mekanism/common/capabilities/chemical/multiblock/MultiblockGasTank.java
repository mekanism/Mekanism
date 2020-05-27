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
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.inventory.AutomationType;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.prefab.TileEntityMultiblock;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockGasTank<MULTIBLOCK extends MultiblockData> extends MultiblockChemicalTank<Gas, GasStack, MULTIBLOCK> implements IGasHandler, IGasTank {

    public static <MULTIBLOCK extends MultiblockData> MultiblockGasTank<MULTIBLOCK> create(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
          LongSupplier capacity, Predicate<@NonNull Gas> validator) {
        Objects.requireNonNull(tile, "Tile cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Gas validity check cannot be null");
        return new MultiblockGasTank<>(multiblock, tile, capacity, validator);
    }

    public static <MULTIBLOCK extends MultiblockData> MultiblockGasTank<MULTIBLOCK> create(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
          LongSupplier capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Gas> validator) {
        return create(multiblock, tile, capacity, canExtract, canInsert, validator, null, null);
    }

    public static <MULTIBLOCK extends MultiblockData> MultiblockGasTank<MULTIBLOCK> input(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
          LongSupplier capacity, Predicate<@NonNull Gas> validator) {
      return create(multiblock, tile, capacity, (stack, automationType) -> automationType != AutomationType.EXTERNAL && multiblock.isFormed(),
            (stack, automationType) -> multiblock.isFormed(), validator, null, null);
  }

  public static <MULTIBLOCK extends MultiblockData> MultiblockGasTank<MULTIBLOCK> output(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
        LongSupplier capacity, Predicate<@NonNull Gas> validator) {
      return create(multiblock, tile, capacity, (stack, automationType) -> multiblock.isFormed(),
            (stack, automationType) -> automationType != AutomationType.EXTERNAL && multiblock.isFormed(), validator, null, null);
  }

    public static <MULTIBLOCK extends MultiblockData> MultiblockGasTank<MULTIBLOCK> create(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
          LongSupplier capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Gas> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(tile, "Tile cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Gas validity check cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new MultiblockGasTank<>(multiblock, tile, capacity, canExtract, canInsert, validator, attributeValidator, listener);
    }

    protected MultiblockGasTank(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity, Predicate<@NonNull Gas> validator) {
        super(multiblock, tile, capacity, validator);
    }

    protected MultiblockGasTank(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity,
          BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert, Predicate<@NonNull Gas> validator,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        super(multiblock, tile, capacity, canExtract, canInsert, validator, attributeValidator, listener);
    }
}