package mekanism.common.content.network.distribution;

import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.math.MathUtils;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;
import java.util.stream.Collectors;

//TODO: Improve handling for fluid storage as longs
public class FluidTransmitterSaveTarget extends Target<FluidTransmitterSaveTarget.SaveHandler, Integer, @NonNull FluidStack> {

    public FluidTransmitterSaveTarget(@Nonnull FluidStack type) {
        this.extra = type;
    }

    public FluidTransmitterSaveTarget(@Nonnull FluidStack type, int expectedSize) {
        super(expectedSize);
        this.extra = type;
    }

    @Override
    protected void acceptAmount(FluidTransmitterSaveTarget.SaveHandler handler, SplitInfo<Integer> splitInfo, Integer amount) {
        handler.acceptAmount(splitInfo, amount);
    }

    @Override
    protected Integer simulate(FluidTransmitterSaveTarget.SaveHandler handler, @Nonnull FluidStack fluidStack) {
        return handler.simulate(fluidStack);
    }

    public void saveShare() {
        for (FluidTransmitterSaveTarget.SaveHandler handler : handlers) {
            handler.saveShare();
        }
    }

    public void addDelegate(MechanicalPipe pipe) {
        this.addHandler(new SaveHandler(pipe));
    }

    public class SaveHandler {
        private FluidStack currentStored = FluidStack.EMPTY;
        private final MechanicalPipe transmitter;

        public SaveHandler(MechanicalPipe transmitter) {
            this.transmitter = transmitter;
        }

        protected void acceptAmount(SplitInfo<Integer> splitInfo, Integer amount) {
            amount = Math.min(amount, MathUtils.clampToInt(transmitter.getCapacity() - currentStored.getAmount()));
            FluidStack newFluid = new FluidStack(extra, amount);
            if (currentStored.isEmpty()) {
                currentStored = newFluid;
            } else {
                currentStored.grow(amount);
            }
            splitInfo.send(amount);
        }

        protected Integer simulate(@Nonnull FluidStack fluidStack) {
            if (!currentStored.isEmpty() && !currentStored.isFluidEqual(fluidStack)) {
                return 0;
            }
            return Math.min(fluidStack.getAmount(), MathUtils.clampToInt(transmitter.getCapacity() - currentStored.getAmount()));
        }

        protected void saveShare() {
            if (currentStored.isEmpty() != transmitter.saveShare.isEmpty() || (!currentStored.isEmpty() && !currentStored.isFluidStackIdentical(transmitter.saveShare))) {
                transmitter.saveShare = currentStored;
                transmitter.getTransmitterTile().markDirty(false);
            }
        }
    }
}