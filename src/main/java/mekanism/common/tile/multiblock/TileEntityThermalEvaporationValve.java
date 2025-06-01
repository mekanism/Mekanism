package mekanism.common.tile.multiblock;

import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class TileEntityThermalEvaporationValve extends TileEntityThermalEvaporationBlock {

    public TileEntityThermalEvaporationValve(BlockPos pos, BlockState state) {
        super(MekanismBlocks.THERMAL_EVAPORATION_VALVE, pos, state);
    }

    @NotNull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        return side -> getMultiblock().getFluidTanks(side);
    }

    @NotNull
    @Override
    public FluidStack insertFluid(int tank, @NotNull FluidStack stack, Direction side, @NotNull Action action) {
        return handleValves(stack, action, super.insertFluid(tank, stack, side, action));
    }

    @NotNull
    @Override
    public FluidStack insertFluid(@NotNull FluidStack stack, Direction side, @NotNull Action action) {
        return handleValves(stack, action, super.insertFluid(stack, side, action));
    }

    private FluidStack handleValves(@NotNull FluidStack stack, @NotNull Action action, @NotNull FluidStack remainder) {
        if (action.execute() && remainder.getAmount() < stack.getAmount()) {
            getMultiblock().triggerValveTransfer(this);
        }
        return remainder;
    }

    @Override
    public int getRedstoneLevel() {
        return getMultiblock().getCurrentRedstoneLevel();
    }
}