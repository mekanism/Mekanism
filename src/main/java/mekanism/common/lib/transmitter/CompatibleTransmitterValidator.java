package mekanism.common.lib.transmitter;

import mekanism.api.chemical.merged.BoxedChemical;
import mekanism.common.capabilities.chemical.BoxedChemicalHandler;
import mekanism.common.content.network.BoxedChemicalNetwork;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.content.network.transmitter.BoxedPressurizedTube;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.content.network.transmitter.Transmitter;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class CompatibleTransmitterValidator<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, TRANSMITTER>,
      TRANSMITTER extends Transmitter<ACCEPTOR, NETWORK, TRANSMITTER>> {

    public boolean isNetworkCompatible(NETWORK net) {
        return true;
    }

    /**
     * @param transmitter Orphan transmitter to check if it is valid against this validator.
     */
    public boolean isTransmitterCompatible(Transmitter<?, ?, ?> transmitter) {
        return true;
    }

    public static class CompatibleChemicalTransmitterValidator extends CompatibleTransmitterValidator<BoxedChemicalHandler, BoxedChemicalNetwork, BoxedPressurizedTube> {

        private BoxedChemical buffer;

        public CompatibleChemicalTransmitterValidator(BoxedPressurizedTube transmitter) {
            buffer = transmitter.getBufferWithFallback().getType();
        }

        private boolean compareBuffers(BoxedChemical otherBuffer) {
            if (buffer.isEmpty()) {
                buffer = otherBuffer;
                return true;
            }
            return otherBuffer.isEmpty() || buffer.equals(otherBuffer);
        }

        @Override
        public boolean isNetworkCompatible(BoxedChemicalNetwork network) {
            if (super.isNetworkCompatible(network)) {
                BoxedChemical otherBuffer;
                if (network.getTransmitterValidator() instanceof CompatibleChemicalTransmitterValidator) {
                    //Null check it, but use instanceof to double-check it is actually the expected type
                    otherBuffer = ((CompatibleChemicalTransmitterValidator) network.getTransmitterValidator()).buffer;
                } else {
                    otherBuffer = network.getBuffer().getType();
                    if (otherBuffer.isEmpty() && network.getPrevTransferAmount() > 0) {
                        otherBuffer = network.lastChemical;
                    }
                }
                return compareBuffers(otherBuffer);
            }
            return false;
        }

        @Override
        public boolean isTransmitterCompatible(Transmitter<?, ?, ?> transmitter) {
            return super.isTransmitterCompatible(transmitter) && transmitter instanceof BoxedPressurizedTube &&
                   compareBuffers(((BoxedPressurizedTube) transmitter).getBufferWithFallback().getType());
        }
    }

    public static class CompatibleFluidTransmitterValidator extends CompatibleTransmitterValidator<IFluidHandler, FluidNetwork, MechanicalPipe> {

        private FluidStack buffer;

        public CompatibleFluidTransmitterValidator(MechanicalPipe transmitter) {
            buffer = transmitter.getBufferWithFallback();
        }

        private boolean compareBuffers(FluidStack otherBuffer) {
            if (buffer.isEmpty()) {
                buffer = otherBuffer;
                return true;
            }
            return otherBuffer.isEmpty() || buffer.isFluidEqual(otherBuffer);
        }

        @Override
        public boolean isNetworkCompatible(FluidNetwork network) {
            if (super.isNetworkCompatible(network)) {
                FluidStack otherBuffer;
                if (network.getTransmitterValidator() instanceof CompatibleFluidTransmitterValidator) {
                    //Null check it, but use instanceof to double-check it is actually the expected type
                    otherBuffer = ((CompatibleFluidTransmitterValidator) network.getTransmitterValidator()).buffer;
                } else {
                    otherBuffer = network.getBuffer();
                    if (otherBuffer.isEmpty() && network.getPrevTransferAmount() > 0) {
                        otherBuffer = network.lastFluid;
                    }
                }
                return compareBuffers(otherBuffer);
            }
            return false;
        }

        @Override
        public boolean isTransmitterCompatible(Transmitter<?, ?, ?> transmitter) {
            return super.isTransmitterCompatible(transmitter) && transmitter instanceof MechanicalPipe &&
                   compareBuffers(((MechanicalPipe) transmitter).getBufferWithFallback());
        }
    }
}