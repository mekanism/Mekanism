package mekanism.common.content.network.distribution;

import com.google.common.primitives.Ints;
import java.util.Collection;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

//TODO: Improve handling for fluid storage as longs
public class FluidTransmitterSaveTarget extends Target<FluidTransmitterSaveTarget.SaveHandler, @NotNull FluidStack> {

    public FluidTransmitterSaveTarget(Collection<MechanicalPipe> transmitters) {
        super(transmitters.size());
        for (MechanicalPipe transmitter : transmitters) {
            addHandler(new SaveHandler(transmitter));
        }
    }

    @Override
    protected void acceptAmount(SaveHandler handler, SplitInfo splitInfo, @NotNull FluidStack resource, long amount) {
        handler.acceptAmount(splitInfo, resource, Ints.saturatedCast(amount));
    }

    @Override
    protected long simulate(SaveHandler handler, @NotNull FluidStack resource, long amount) {
        return handler.simulate(resource.copyWithAmount(Ints.saturatedCast(amount)));
    }

    public void saveShare() {
        for (FluidTransmitterSaveTarget.SaveHandler handler : handlers) {
            handler.saveShare();
        }
    }

    public static class SaveHandler {

        private FluidStack currentStored = FluidStack.EMPTY;
        private final MechanicalPipe transmitter;

        public SaveHandler(MechanicalPipe transmitter) {
            this.transmitter = transmitter;
        }

        protected void acceptAmount(SplitInfo splitInfo, @NotNull FluidStack resource, int amount) {
            amount = Math.min(amount, Ints.saturatedCast(transmitter.getCapacity() - currentStored.amount()));
            if (currentStored.isEmpty()) {
                currentStored = resource.copyWithAmount(amount);
            } else {
                currentStored.grow(amount);
            }
            splitInfo.send(amount);
        }

        protected Integer simulate(@NotNull FluidStack fluidStack) {
            if (!currentStored.isEmpty() && !FluidStack.isSameFluidSameComponents(currentStored, fluidStack)) {
                return 0;
            }
            return Math.min(fluidStack.amount(), Ints.saturatedCast(transmitter.getCapacity() - currentStored.amount()));
        }

        protected void saveShare() {
            if (currentStored.isEmpty() != transmitter.saveShare.isEmpty() || (!currentStored.isEmpty() && !FluidStack.matches(currentStored, transmitter.saveShare))) {
                transmitter.saveShare = currentStored;
                transmitter.getTransmitterTile().markForSave();
            }
        }
    }
}