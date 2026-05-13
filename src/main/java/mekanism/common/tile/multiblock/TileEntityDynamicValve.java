package mekanism.common.tile.multiblock;

import mekanism.api.IContentsListener;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class TileEntityDynamicValve extends TileEntityDynamicTank {

    public TileEntityDynamicValve(BlockPos pos, BlockState state) {
        super(MekanismBlocks.DYNAMIC_VALVE, pos, state);
    }

    @NotNull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        return _ -> getMultiblock().getFluidTanks();
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        return _ -> getMultiblock().getChemicalTanks();
    }
    
    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
        //Do not handle fluid when it comes to syncing it/saving this tile to disk
        if (type == ContainerType.FLUID || type == ContainerType.CHEMICAL) {
            return false;
        }
        return super.persists(type);
    }

    /*@NotNull
    @Override
    public FluidStack insertFluid(int tank, @NotNull FluidStack stack, Direction side, @NotNull Action action) {
        return handleValves(stack, action, super.insertFluid(tank, stack, side, action));
    }

    @NotNull
    @Override
    public FluidStack insertFluid(@NotNull FluidStack stack, Direction side, @NotNull Action action) {
        return handleValves(stack, action, super.insertFluid(stack, side, action));
    }*/

    //TODO - 26.1: Hook valve transferring back up
    private FluidStack handleValves(@NotNull FluidStack stack, boolean execute, @NotNull FluidStack remainder) {
        if (execute && remainder.amount() < stack.amount()) {
            getMultiblock().triggerValveTransfer(this);
        }
        return remainder;
    }

    @Override
    public int getRedstoneLevel() {
        return getMultiblock().getCurrentRedstoneLevel();
    }
}