package mekanism.common.content.network;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.lib.transmitter.DynamicBufferedResourceNetwork;
import mekanism.common.util.MekanismUtils;
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
    public List<MechanicalPipe> adoptTransmittersAndAcceptorsFrom(FluidNetwork net) {
        float oldScale = currentScale;
        long oldCapacity = getCapacity();
        List<MechanicalPipe> transmittersToUpdate = super.adoptTransmittersAndAcceptorsFrom(net);
        //Merge the fluid scales
        long capacity = getCapacity();
        currentScale = Math.min(1, capacity == 0 ? 0 : (currentScale * oldCapacity + net.currentScale * net.capacity) / capacity);
        if (isRemote()) {
            if (container.isEmpty() && !net.container.isEmpty()) {
                container.setContents(net.container.getResource(), net.container.amountAsLong());
                net.container.setEmpty();
            }
        } else {
            if (!net.container.isEmpty()) {
                if (container.isEmpty()) {
                    container.setContents(net.container.getResource(), net.container.amountAsLong());
                } else if (container.getResource().equals(net.container.getResource())) {
                    int amount = net.container.amount();
                    MekanismUtils.logMismatchedStackSize(container.growStack(amount, Action.EXECUTE), amount);
                } else {
                    Mekanism.logger.error("Incompatible fluid networks merged.");
                }
                net.container.setEmpty();
            }
            if (oldScale != currentScale) {
                //We want to make sure we update to the scale change
                needsUpdate = true;
            }
        }
        return transmittersToUpdate;
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
        return MekanismLang.FLUID_NETWORK_NEEDED.translate(container.getNeeded() / (float) FluidType.BUCKET_VOLUME);
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
