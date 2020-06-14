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
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.inventory.AutomationType;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.prefab.TileEntityMultiblock;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockPigmentTank<MULTIBLOCK extends MultiblockData> extends MultiblockChemicalTank<Pigment, PigmentStack, MULTIBLOCK> implements IPigmentHandler,
      IPigmentTank {

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
          Predicate<@NonNull Pigment> validator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(tile, "Tile cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Pigment validity check cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new MultiblockPigmentTank<>(multiblock, tile, capacity, canExtract, canInsert, validator, listener);
    }

    protected MultiblockPigmentTank(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity, Predicate<@NonNull Pigment> validator) {
        super(multiblock, tile, capacity, validator);
    }

    protected MultiblockPigmentTank(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity,
          BiPredicate<@NonNull Pigment, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Pigment> validator, @Nullable IContentsListener listener) {
        super(multiblock, tile, capacity, canExtract, canInsert, validator, null, listener);
    }
}