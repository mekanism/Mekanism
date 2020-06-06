package mekanism.common.distribution.target;

import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.math.MathUtils;
import mekanism.common.lib.transmitter.IGridTransmitter;
import mekanism.common.distribution.SplitInfo;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.transmitters.TransmitterImpl;
import mekanism.common.transmitters.grid.FluidNetwork;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

//TODO: Improve handling for fluid storage as longs
public class FluidTransmitterSaveTarget extends Target<IGridTransmitter<IFluidHandler, FluidNetwork, FluidStack>, Integer, @NonNull FluidStack> {

    private FluidStack currentStored = FluidStack.EMPTY;

    public FluidTransmitterSaveTarget(@Nonnull FluidStack type) {
        this.extra = type;
    }

    @Override
    protected void acceptAmount(IGridTransmitter<IFluidHandler, FluidNetwork, FluidStack> transmitter, SplitInfo<Integer> splitInfo, Integer amount) {
        amount = Math.min(amount, MathUtils.clampToInt(transmitter.getCapacity() - currentStored.getAmount()));
        FluidStack newFluid = new FluidStack(extra, amount);
        if (currentStored.isEmpty()) {
            currentStored = newFluid;
        } else {
            currentStored.grow(amount);
        }
        splitInfo.send(amount);
    }

    @Override
    protected Integer simulate(IGridTransmitter<IFluidHandler, FluidNetwork, FluidStack> transmitter, @Nonnull FluidStack fluidStack) {
        if (!currentStored.isEmpty() && !currentStored.isFluidEqual(fluidStack)) {
            return 0;
        }
        return Math.min(fluidStack.getAmount(), MathUtils.clampToInt(transmitter.getCapacity() - currentStored.getAmount()));
    }

    public void saveShare(Direction handlerDirection) {
        IGridTransmitter<IFluidHandler, FluidNetwork, FluidStack> transmitter = handlers.get(handlerDirection);
        if (transmitter instanceof TransmitterImpl<?, ?, ?>) {
            TileEntity tile = ((TransmitterImpl<?, ?, ?>) transmitter).getTileEntity();
            if (tile instanceof TileEntityMechanicalPipe) {
                TileEntityMechanicalPipe pipe = (TileEntityMechanicalPipe) tile;
                if (currentStored.isEmpty() != pipe.saveShare.isEmpty() || (!currentStored.isEmpty() && !currentStored.isFluidStackIdentical(pipe.saveShare))) {
                    pipe.saveShare = currentStored;
                    pipe.markDirty(false);
                }
            }
        }
    }
}