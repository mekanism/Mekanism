package mekanism.common.capabilities.fluid;

import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraftforge.fluids.FluidStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockFluidTank<MULTIBLOCK extends TileEntityMultiblock<?>> extends VariableCapacityFluidTank {

    protected final MULTIBLOCK multiblock;

    public static <MULTIBLOCK extends TileEntityMultiblock<?>> MultiblockFluidTank<MULTIBLOCK> create(MULTIBLOCK multiblock,
          IntSupplier capacity, Predicate<@NonNull FluidStack> validator) {
        return create(multiblock, capacity, (stack, automationType) -> automationType != AutomationType.EXTERNAL || multiblock.structure != null,
              (stack, automationType) -> automationType != AutomationType.EXTERNAL || multiblock.structure != null, validator, null);
    }

    public static <MULTIBLOCK extends TileEntityMultiblock<?>> MultiblockFluidTank<MULTIBLOCK> input(MULTIBLOCK multiblock,
          IntSupplier capacity, Predicate<@NonNull FluidStack> validator) {
        return create(multiblock, capacity, (stack, automationType) -> automationType != AutomationType.EXTERNAL && multiblock.structure != null,
              (stack, automationType) -> multiblock.structure != null, validator, null);
    }

    public static <MULTIBLOCK extends TileEntityMultiblock<?>> MultiblockFluidTank<MULTIBLOCK> output(MULTIBLOCK multiblock,
          IntSupplier capacity, Predicate<@NonNull FluidStack> validator) {
        return create(multiblock, capacity, (stack, automationType) -> multiblock.structure != null,
              (stack, automationType) -> automationType != AutomationType.EXTERNAL && multiblock.structure != null, validator, null);
    }

    public static <MULTIBLOCK extends TileEntityMultiblock<?>> MultiblockFluidTank<MULTIBLOCK> create(MULTIBLOCK multiblock, IntSupplier capacity, BiPredicate<@NonNull FluidStack, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull FluidStack, @NonNull AutomationType> canInsert, Predicate<@NonNull FluidStack> validator, @Nullable IMekanismFluidHandler fluidHandler) {
        return new MultiblockFluidTank<>(multiblock, capacity, canExtract, canInsert, validator, fluidHandler);
    }

    protected MultiblockFluidTank(MULTIBLOCK multiblock, IntSupplier capacity, Predicate<@NonNull FluidStack> validator) {
        this(multiblock, capacity, (stack, automationType) -> automationType != AutomationType.EXTERNAL || multiblock.structure != null,
            (stack, automationType) -> automationType != AutomationType.EXTERNAL || multiblock.structure != null, validator, null);
    }

    protected MultiblockFluidTank(MULTIBLOCK multiblock, IntSupplier capacity, BiPredicate<@NonNull FluidStack, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull FluidStack, @NonNull AutomationType> canInsert, Predicate<@NonNull FluidStack> validator, @Nullable IMekanismFluidHandler fluidHandler) {
        super(capacity, canExtract, canInsert, validator, fluidHandler);
        this.multiblock = multiblock;
    }

    protected void updateValveData() {
    }

    @Override
    public void onContentsChanged() {
        super.onContentsChanged();
        if (multiblock.hasWorld() && !multiblock.isRemote()) {
            updateValveData();
            multiblock.markDirty(false);
        }
    }
}
