package mekanism.common.lib.transmitter;

import mekanism.api.chemical.ChemicalResource;
import mekanism.common.content.network.ChemicalNetwork;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.content.network.transmitter.PressurizedTube;
import mekanism.common.content.network.transmitter.Transmitter;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

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

    public static class CompatibleChemicalTransmitterValidator extends CompatibleTransmitterValidator<ResourceHandler<ChemicalResource>, ChemicalNetwork, PressurizedTube> {

        private ChemicalResource buffer;

        public CompatibleChemicalTransmitterValidator(PressurizedTube transmitter) {
            buffer = transmitter.getBufferWithFallback().resource();
        }

        private boolean compareBuffers(ChemicalResource otherBuffer) {
            if (buffer.isEmpty()) {
                buffer = otherBuffer;
                return true;
            }
            return otherBuffer.isEmpty() || buffer.equals(otherBuffer);
        }

        @Override
        public boolean isNetworkCompatible(ChemicalNetwork network) {
            if (super.isNetworkCompatible(network)) {
                ChemicalResource otherBuffer;
                if (network.getTransmitterValidator() instanceof CompatibleChemicalTransmitterValidator validator) {
                    //Null check it, but use instanceof to double-check it is actually the expected type
                    otherBuffer = validator.buffer;
                } else {
                    otherBuffer = network.getBuffer().resource();
                    if (otherBuffer.isEmpty() && network.getPrevTransferAmount() > 0) {
                        otherBuffer = network.getLastType();
                    }
                }
                return compareBuffers(otherBuffer);
            }
            return false;
        }

        @Override
        public boolean isTransmitterCompatible(Transmitter<?, ?, ?> transmitter) {
            return super.isTransmitterCompatible(transmitter) && transmitter instanceof PressurizedTube tube && compareBuffers(tube.getBufferWithFallback().resource());
        }
    }

    public static class CompatibleFluidTransmitterValidator extends CompatibleTransmitterValidator<ResourceHandler<FluidResource>, FluidNetwork, MechanicalPipe> {

        private FluidResource buffer;

        public CompatibleFluidTransmitterValidator(MechanicalPipe transmitter) {
            buffer = transmitter.getBufferWithFallback().resource();
        }

        private boolean compareBuffers(FluidResource otherBuffer) {
            if (buffer.isEmpty()) {
                buffer = otherBuffer;
                return true;
            }
            return otherBuffer.isEmpty() || buffer.equals(otherBuffer);
        }

        @Override
        public boolean isNetworkCompatible(FluidNetwork network) {
            if (super.isNetworkCompatible(network)) {
                FluidResource otherBuffer;
                if (network.getTransmitterValidator() instanceof CompatibleFluidTransmitterValidator validator) {
                    //Null check it, but use instanceof to double-check it is actually the expected type
                    otherBuffer = validator.buffer;
                } else {
                    otherBuffer = network.getBuffer().resource();
                    if (otherBuffer.isEmpty() && network.getPrevTransferAmount() > 0) {
                        otherBuffer = network.getLastType();
                    }
                }
                return compareBuffers(otherBuffer);
            }
            return false;
        }

        @Override
        public boolean isTransmitterCompatible(Transmitter<?, ?, ?> transmitter) {
            return super.isTransmitterCompatible(transmitter) && transmitter instanceof MechanicalPipe pipe && compareBuffers(pipe.getBufferWithFallback().resource());
        }
    }
}