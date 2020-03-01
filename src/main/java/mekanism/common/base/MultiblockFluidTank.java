package mekanism.common.base;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.MekanismUtils;
import net.minecraftforge.fluids.FluidStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockFluidTank<MULTIBLOCK extends TileEntityMultiblock<?>> extends VariableCapacityFluidTank {

    public static <MULTIBLOCK extends TileEntityMultiblock<?>> MultiblockFluidTank<MULTIBLOCK> create(MULTIBLOCK tile, IntSupplier capacity, Predicate<@NonNull FluidStack> validator) {
        //TODO: Validate capacity is positive
        Objects.requireNonNull(tile, "Tile cannot be null");
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new MultiblockFluidTank<>(tile, capacity, validator);
    }

    protected final MULTIBLOCK multiblock;

    protected MultiblockFluidTank(MULTIBLOCK multiblock, IntSupplier capacity, Predicate<@NonNull FluidStack> validator) {
        super(capacity, (stack, automationType) -> automationType != AutomationType.EXTERNAL || multiblock.structure != null,
              (stack, automationType) -> automationType != AutomationType.EXTERNAL || multiblock.structure != null, validator, null);
        this.multiblock = multiblock;
    }

    protected void updateValveData() {
    }

    @Override
    public void onContentsChanged() {
        super.onContentsChanged();
        if (multiblock.hasWorld() && !multiblock.getWorld().isRemote()) {
            MekanismUtils.saveChunk(multiblock);
            updateValveData();
            multiblock.sendPacketToRenderer();
        }
    }
}