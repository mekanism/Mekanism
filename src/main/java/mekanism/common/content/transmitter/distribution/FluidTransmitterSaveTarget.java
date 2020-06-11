package mekanism.common.content.transmitter.distribution;

import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.math.MathUtils;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

//TODO: Improve handling for fluid storage as longs
public class FluidTransmitterSaveTarget extends Target<TileEntityMechanicalPipe, Integer, @NonNull FluidStack> {

    private FluidStack currentStored = FluidStack.EMPTY;

    public FluidTransmitterSaveTarget(@Nonnull FluidStack type) {
        this.extra = type;
    }

    @Override
    protected void acceptAmount(TileEntityMechanicalPipe transmitter, SplitInfo<Integer> splitInfo, Integer amount) {
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
    protected Integer simulate(TileEntityMechanicalPipe transmitter, @Nonnull FluidStack fluidStack) {
        if (!currentStored.isEmpty() && !currentStored.isFluidEqual(fluidStack)) {
            return 0;
        }
        return Math.min(fluidStack.getAmount(), MathUtils.clampToInt(transmitter.getCapacity() - currentStored.getAmount()));
    }

    public void saveShare(Direction handlerDirection) {
        TileEntityMechanicalPipe pipe = handlers.get(handlerDirection);
        if (currentStored.isEmpty() != pipe.saveShare.isEmpty() || (!currentStored.isEmpty() && !currentStored.isFluidStackIdentical(pipe.saveShare))) {
            pipe.saveShare = currentStored;
            pipe.markDirty(false);
        }
    }
}