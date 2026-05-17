package mekanism.common.tile.multiblock;

import mekanism.api.IContentsListener;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.IContainerHolder;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class TileEntityThermalEvaporationValve extends TileEntityThermalEvaporationBlock {

    public TileEntityThermalEvaporationValve(BlockPos pos, BlockState state) {
        super(MekanismBlocks.THERMAL_EVAPORATION_VALVE, pos, state);
    }

    @NotNull
    @Override
    protected IContainerHolder<IFluidTank> getInitialFluidTanks(IContentsListener listener) {
        return _ -> getMultiblock().getFluidTanks();
    }

    @NotNull
    @Override
    protected IContainerHolder<IHeatCapacitor> getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return _ -> getMultiblock().getHeatCapacitors();
    }

    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
        //But that we do not handle fluid when it comes to syncing it/saving this tile to disk
        if (type == ContainerType.FLUID || type == ContainerType.HEAT) {
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