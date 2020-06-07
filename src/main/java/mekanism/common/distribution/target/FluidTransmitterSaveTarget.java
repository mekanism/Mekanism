package mekanism.common.distribution.target;

import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.math.MathUtils;
import mekanism.common.content.transmitter.FluidNetwork;
import mekanism.common.distribution.SplitInfo;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

//TODO: Improve handling for fluid storage as longs
public class FluidTransmitterSaveTarget extends Target<TileEntityTransmitter<IFluidHandler, FluidNetwork, FluidStack>, Integer, @NonNull FluidStack> {

    private FluidStack currentStored = FluidStack.EMPTY;

    public FluidTransmitterSaveTarget(@Nonnull FluidStack type) {
        this.extra = type;
    }

    @Override
    protected void acceptAmount(TileEntityTransmitter<IFluidHandler, FluidNetwork, FluidStack> transmitter, SplitInfo<Integer> splitInfo, Integer amount) {
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
    protected Integer simulate(TileEntityTransmitter<IFluidHandler, FluidNetwork, FluidStack> transmitter, @Nonnull FluidStack fluidStack) {
        if (!currentStored.isEmpty() && !currentStored.isFluidEqual(fluidStack)) {
            return 0;
        }
        return Math.min(fluidStack.getAmount(), MathUtils.clampToInt(transmitter.getCapacity() - currentStored.getAmount()));
    }

    public void saveShare(Direction handlerDirection) {
        TileEntityTransmitter<IFluidHandler, FluidNetwork, FluidStack> transmitter = handlers.get(handlerDirection);
        if (transmitter instanceof TileEntityMechanicalPipe) {
            TileEntityMechanicalPipe pipe = (TileEntityMechanicalPipe) transmitter;
            if (currentStored.isEmpty() != pipe.saveShare.isEmpty() || (!currentStored.isEmpty() && !currentStored.isFluidStackIdentical(pipe.saveShare))) {
                pipe.saveShare = currentStored;
                pipe.markDirty(false);
            }
        }
    }
}