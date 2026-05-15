package mekanism.common.content.network;

import java.util.Collection;
import java.util.UUID;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.lib.transmitter.DynamicBufferedResourceNetwork;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.NotNull;

public class FluidNetwork extends DynamicBufferedResourceNetwork<FluidResource, IFluidTank, FluidNetwork, MechanicalPipe> {

    public FluidNetwork(UUID networkID) {
        super(networkID, (capacity, listener) -> VariableCapacityFluidTank.create(capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(), listener));
    }

    public FluidNetwork(Collection<FluidNetwork> networks) {
        this(UUID.randomUUID());
        adoptAllAndRegister(networks);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (needsUpdate) {
            NeoForge.EVENT_BUS.post(new FluidTransferEvent(this, getLastType()));
            needsUpdate = false;
        }
        tickEmit();
    }

    @Override
    public String toString() {
        return "[FluidNetwork] " + transmittersSize() + " transmitters, " + getAcceptorCount() + " acceptors.";
    }

    @Override
    public Component getNeededInfo() {
        return MekanismLang.FLUID_NETWORK_NEEDED.translate(container.getNeededAsInt() / (float) FluidType.BUCKET_VOLUME);
    }

    @NotNull
    @Override
    public Component getTextComponent() {
        return MekanismLang.NETWORK_DESCRIPTION.translate(MekanismLang.FLUID_NETWORK, transmittersSize(), getAcceptorCount());
    }

    @Override
    protected FluidResource getEmptyType() {
        return FluidResource.EMPTY;
    }

    public static class FluidTransferEvent extends TransferEvent<FluidNetwork> {

        public final FluidResource fluidType;

        public FluidTransferEvent(FluidNetwork network, @NotNull FluidResource type) {
            super(network);
            fluidType = type;
        }
    }
}
