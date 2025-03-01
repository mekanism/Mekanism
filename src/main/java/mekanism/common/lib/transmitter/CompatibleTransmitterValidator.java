package mekanism.common.lib.transmitter;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.content.network.ChemicalNetwork;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.content.network.transmitter.PressurizedTube;
import mekanism.common.content.network.transmitter.Transmitter;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

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

    public static class CompatibleChemicalTransmitterValidator extends CompatibleTransmitterValidator<IChemicalHandler, ChemicalNetwork, PressurizedTube> {

        private Holder<Chemical> buffer;

        public CompatibleChemicalTransmitterValidator(PressurizedTube transmitter) {
            buffer = transmitter.getBufferWithFallback().getChemicalHolder();
        }

        private boolean compareBuffers(Holder<Chemical> otherBuffer) {
            if (buffer.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
                buffer = otherBuffer;
                return true;
            }
            return otherBuffer.is(MekanismAPI.EMPTY_CHEMICAL_KEY) || buffer == otherBuffer;
        }

        @Override
        public boolean isNetworkCompatible(ChemicalNetwork network) {
            if (super.isNetworkCompatible(network)) {
                Holder<Chemical> otherBuffer;
                if (network.getTransmitterValidator() instanceof CompatibleChemicalTransmitterValidator validator) {
                    //Null check it, but use instanceof to double-check it is actually the expected type
                    otherBuffer = validator.buffer;
                } else {
                    otherBuffer = network.getBuffer().getChemicalHolder();
                    if (otherBuffer.is(MekanismAPI.EMPTY_CHEMICAL_KEY) && network.getPrevTransferAmount() > 0) {
                        otherBuffer = network.lastChemical;
                    }
                }
                return compareBuffers(otherBuffer);
            }
            return false;
        }

        @Override
        public boolean isTransmitterCompatible(Transmitter<?, ?, ?> transmitter) {
            return super.isTransmitterCompatible(transmitter) && transmitter instanceof PressurizedTube tube && compareBuffers(tube.getBufferWithFallback().getChemicalHolder());
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
            return otherBuffer.isEmpty() || FluidStack.isSameFluidSameComponents(buffer, otherBuffer);
        }

        @Override
        public boolean isNetworkCompatible(FluidNetwork network) {
            if (super.isNetworkCompatible(network)) {
                FluidStack otherBuffer;
                if (network.getTransmitterValidator() instanceof CompatibleFluidTransmitterValidator validator) {
                    //Null check it, but use instanceof to double-check it is actually the expected type
                    otherBuffer = validator.buffer;
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
            return super.isTransmitterCompatible(transmitter) && transmitter instanceof MechanicalPipe pipe && compareBuffers(pipe.getBufferWithFallback());
        }
    }
}