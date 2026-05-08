package mekanism.common.content.network;

import com.google.common.primitives.Ints;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.content.network.distribution.FluidHandlerTarget;
import mekanism.common.content.network.distribution.FluidTransmitterSaveTarget;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidNetwork extends DynamicBufferedNetwork<ResourceHandler<FluidResource>, FluidNetwork, FluidStack, MechanicalPipe> implements IContentsListener {

    private final List<IFluidTank> fluidTanks;
    public final VariableCapacityFluidTank fluidTank;
    @NotNull
    public FluidStack lastFluid = FluidStack.EMPTY;
    private int prevTransferAmount;

    //TODO - 26.1: Make fluid storage support storing as longs?
    private int intCapacity;

    public FluidNetwork(UUID networkID) {
        super(networkID);
        fluidTank = VariableCapacityFluidTank.create(this::getCapacityAsInt, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(), this);
        fluidTanks = Collections.singletonList(fluidTank);
    }

    public FluidNetwork(Collection<FluidNetwork> networks) {
        this(UUID.randomUUID());
        adoptAllAndRegister(networks);
    }

    @Override
    protected void forceScaleUpdate() {
        if (fluidTank.isEmpty()) {
            currentScale = 0;
        } else {
            int capacity = fluidTank.getCurrentLimit();
            if (capacity > 0) {
                currentScale = Math.min(1, (float) fluidTank.amount() / capacity);
            } else {
                currentScale = 0;
            }
        }
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
            if (fluidTank.isEmpty() && !net.fluidTank.isEmpty()) {
                fluidTank.setStack(net.getBuffer());
                net.fluidTank.setEmpty();
            }
        } else {
            if (!net.fluidTank.isEmpty()) {
                if (fluidTank.isEmpty()) {
                    fluidTank.setStack(net.getBuffer());
                } else if (fluidTank.getResource().equals(net.fluidTank.getResource())) {
                    int amount = net.fluidTank.amount();
                    MekanismUtils.logMismatchedStackSize(fluidTank.growStack(amount, Action.EXECUTE), amount);
                } else {
                    Mekanism.logger.error("Incompatible fluid networks merged.");
                }
                net.fluidTank.setEmpty();
            }
            if (oldScale != currentScale) {
                //We want to make sure we update to the scale change
                needsUpdate = true;
            }
        }
        return transmittersToUpdate;
    }

    @NotNull
    @Override
    public FluidStack getBuffer() {
        return fluidTank.getResource().toStack(fluidTank.amount());
    }

    @Override
    public void absorbBuffer(MechanicalPipe transmitter) {
        FluidStack fluid = transmitter.releaseShare();
        if (!fluid.isEmpty()) {
            if (fluidTank.isEmpty()) {
                fluidTank.setStack(fluid.copy());
            } else if (fluidTank.isFluidEqual(fluid)) {
                int amount = fluid.amount();
                MekanismUtils.logMismatchedStackSize(fluidTank.growStack(amount, Action.EXECUTE), amount);
            }
        }
    }

    @Override
    public void clampBuffer() {
        if (!fluidTank.isEmpty()) {
            int capacity = getCapacityAsInt();
            if (fluidTank.amount() > capacity) {
                MekanismUtils.logMismatchedStackSize(fluidTank.setStackSize(capacity, Action.EXECUTE), capacity);
            }
        }
    }

    @Override
    protected synchronized void updateCapacity(MechanicalPipe transmitter) {
        super.updateCapacity(transmitter);
        intCapacity = Ints.saturatedCast(getCapacity());
    }

    @Override
    public synchronized void updateCapacity() {
        super.updateCapacity();
        intCapacity = Ints.saturatedCast(getCapacity());
    }

    public int getCapacityAsInt() {
        return intCapacity;
    }

    @Override
    protected void updateSaveShares(@Nullable MechanicalPipe triggerTransmitter) {
        super.updateSaveShares(triggerTransmitter);
        if (!isEmpty()) {
            FluidStack fluidType = fluidTank.getFluid();
            FluidTransmitterSaveTarget saveTarget = new FluidTransmitterSaveTarget(getTransmitters());
            EmitUtils.sendToAcceptors(saveTarget, fluidType.amount(), fluidType);
            saveTarget.saveShare();
        }
    }

    private int tickEmit(@NotNull FluidStack fluidToSend) {
        Collection<Map<Direction, ResourceHandler<FluidResource>>> acceptorValues = acceptorCache.getAcceptorValues();
        FluidHandlerTarget target = null;
        for (Map<Direction, ResourceHandler<FluidResource>> acceptors : acceptorValues) {
            for (ResourceHandler<FluidResource> acceptor : acceptors.values()) {
                if (FluidUtils.canFill(acceptor, fluidToSend)) {
                    if (target == null) {
                        //Lazily initialize the target, which allows us to also skip attempting to start emitting
                        target = new FluidHandlerTarget(acceptorValues.size() * 2);
                    }
                    target.addHandler(acceptor);
                }
            }
        }
        return EmitUtils.sendToAcceptors(target, fluidToSend.amount(), fluidToSend);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (needsUpdate) {
            NeoForge.EVENT_BUS.post(new FluidTransferEvent(this, lastFluid));
            needsUpdate = false;
        }
        if (fluidTank.isEmpty()) {
            prevTransferAmount = 0;
        } else {
            prevTransferAmount = tickEmit(fluidTank.getFluid());
            MekanismUtils.logMismatchedStackSize(fluidTank.shrinkStack(prevTransferAmount, Action.EXECUTE), prevTransferAmount);
        }
    }

    @Override
    protected float computeContentScale() {
        float scale = fluidTank.amount() / (float) fluidTank.getCurrentLimit();
        float ret = Math.max(currentScale, scale);
        if (prevTransferAmount > 0 && ret < 1) {
            ret = Math.min(1, ret + 0.02F);
        } else if (prevTransferAmount <= 0 && ret > 0) {
            ret = Math.max(scale, ret - 0.02F);
        }
        return ret;
    }

    public int getPrevTransferAmount() {
        return prevTransferAmount;
    }

    @Override
    public String toString() {
        return "[FluidNetwork] " + transmittersSize() + " transmitters, " + getAcceptorCount() + " acceptors.";
    }

    @Override
    public Component getNeededInfo() {
        return MekanismLang.FLUID_NETWORK_NEEDED.translate(fluidTank.getNeeded() / (float) FluidType.BUCKET_VOLUME);
    }

    @Override
    public Component getStoredInfo() {
        if (fluidTank.isEmpty()) {
            return MekanismLang.NONE.translate();
        }
        return MekanismLang.NETWORK_MB_STORED.translate(fluidTank.getResource(), fluidTank.amount());
    }

    @Override
    public Component getFlowInfo() {
        return MekanismLang.NETWORK_MB_PER_TICK.translate(prevTransferAmount);
    }

    @Override
    public boolean isCompatibleWith(FluidNetwork other) {
        return super.isCompatibleWith(other) && (this.fluidTank.isEmpty() || other.fluidTank.isEmpty() || this.fluidTank.isFluidEqual(other.fluidTank.getFluid()));
    }

    @NotNull
    @Override
    public Component getTextComponent() {
        return MekanismLang.NETWORK_DESCRIPTION.translate(MekanismLang.FLUID_NETWORK, transmittersSize(), getAcceptorCount());
    }

    @NotNull
    public List<IFluidTank> getFluidTanks() {
        return fluidTanks;
    }

    @Override
    public void onContentsChanged() {
        markDirty();
        FluidStack type = fluidTank.getFluid();
        if (!FluidStack.isSameFluidSameComponents(lastFluid, type)) {
            //If the fluid type does not match update it, and mark that we need an update
            if (!type.isEmpty()) {
                lastFluid = type.copyWithAmount(1);
            }
            needsUpdate = true;
        }
    }

    public void setLastFluid(@NotNull FluidStack fluid) {
        if (fluid.isEmpty()) {
            fluidTank.setEmpty();
        } else {
            lastFluid = fluid;
            fluidTank.setStack(fluid.copyWithAmount(1));
        }
    }

    public static class FluidTransferEvent extends TransferEvent<FluidNetwork> {

        public final FluidStack fluidType;

        public FluidTransferEvent(FluidNetwork network, @NotNull FluidStack type) {
            super(network);
            fluidType = type;
        }
    }
}
