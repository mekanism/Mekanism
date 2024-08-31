package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.math.MathUtils;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

//TODO: Improve handling for fluid storage as longs
public class FluidTransmitterSaveTarget extends Target<FluidTransmitterSaveTarget.SaveHandler, @NotNull FluidStack> {

    public FluidTransmitterSaveTarget(@NotNull FluidStack type, Collection<MechanicalPipe> transmitters) {
        super(transmitters.size());
        this.extra = type;
        for (MechanicalPipe transmitter : transmitters) {
            addHandler(new SaveHandler(transmitter));
        }
    }

    @Override
    protected void acceptAmount(FluidTransmitterSaveTarget.SaveHandler handler, SplitInfo splitInfo, long amount) {
        handler.acceptAmount(splitInfo, MathUtils.clampToInt(amount));
    }

    @Override
    protected long simulate(FluidTransmitterSaveTarget.SaveHandler handler, @NotNull FluidStack fluidStack) {
        return handler.simulate(fluidStack);
    }

    public void saveShare() {
        for (FluidTransmitterSaveTarget.SaveHandler handler : handlers) {
            handler.saveShare();
        }
    }

    public class SaveHandler {

        private FluidStack currentStored = FluidStack.EMPTY;
        private final MechanicalPipe transmitter;

        public SaveHandler(MechanicalPipe transmitter) {
            this.transmitter = transmitter;
        }

        protected void acceptAmount(SplitInfo splitInfo, Integer amount) {
            amount = Math.min(amount, MathUtils.clampToInt(transmitter.getCapacity() - currentStored.getAmount()));
            if (currentStored.isEmpty()) {
                currentStored = extra.copyWithAmount(amount);
            } else {
                currentStored.grow(amount);
            }
            splitInfo.send(amount);
        }

        protected Integer simulate(@NotNull FluidStack fluidStack) {
            if (!currentStored.isEmpty() && !FluidStack.isSameFluidSameComponents(currentStored, fluidStack)) {
                return 0;
            }
            return Math.min(fluidStack.getAmount(), MathUtils.clampToInt(transmitter.getCapacity() - currentStored.getAmount()));
        }

        protected void saveShare() {
            if (currentStored.isEmpty() != transmitter.saveShare.isEmpty() || (!currentStored.isEmpty() && !FluidStack.matches(currentStored, transmitter.saveShare))) {
                transmitter.saveShare = currentStored;
                transmitter.getTransmitterTile().markForSave();
            }
        }
    }
}