package mekanism.common.content.network;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.math.MathUtils;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.content.network.distribution.FluidHandlerTarget;
import mekanism.common.content.network.distribution.FluidTransmitterSaveTarget;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidNetwork extends DynamicBufferedNetwork<IFluidHandler, FluidNetwork, FluidStack, MechanicalPipe> implements IMekanismFluidHandler {

    private final List<IExtendedFluidTank> fluidTanks;
    public final VariableCapacityFluidTank fluidTank;
    @Nonnull
    public FluidStack lastFluid = FluidStack.EMPTY;
    private int prevTransferAmount;

    //TODO: Make fluid storage support storing as longs?
    private int intCapacity;

    public FluidNetwork() {
        fluidTank = VariableCapacityFluidTank.create(this::getCapacityAsInt, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue, this);
        fluidTanks = Collections.singletonList(fluidTank);
    }

    public FluidNetwork(UUID networkID) {
        super(networkID);
        fluidTank = VariableCapacityFluidTank.create(this::getCapacityAsInt, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue, this);
        fluidTanks = Collections.singletonList(fluidTank);
    }

    public FluidNetwork(Collection<FluidNetwork> networks) {
        this();
        adoptAllAndRegister(networks);
    }

    @Override
    protected void forceScaleUpdate() {
        if (!fluidTank.isEmpty() && fluidTank.getCapacity() > 0) {
            currentScale = Math.min(1, (float) fluidTank.getFluidAmount() / fluidTank.getCapacity());
        } else {
            currentScale = 0;
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
                } else if (fluidTank.isFluidEqual(net.fluidTank.getFluid())) {
                    int amount = net.fluidTank.getFluidAmount();
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

    @Nonnull
    @Override
    public FluidStack getBuffer() {
        return fluidTank.getFluid().copy();
    }

    @Override
    public void absorbBuffer(MechanicalPipe transmitter) {
        FluidStack fluid = transmitter.releaseShare();
        if (!fluid.isEmpty()) {
            if (fluidTank.isEmpty()) {
                fluidTank.setStack(fluid.copy());
            } else if (fluidTank.isFluidEqual(fluid)) {
                int amount = fluid.getAmount();
                MekanismUtils.logMismatchedStackSize(fluidTank.growStack(amount, Action.EXECUTE), amount);
            }
        }
    }

    @Override
    public void clampBuffer() {
        if (!fluidTank.isEmpty()) {
            int capacity = getCapacityAsInt();
            if (fluidTank.getFluidAmount() > capacity) {
                MekanismUtils.logMismatchedStackSize(fluidTank.setStackSize(capacity, Action.EXECUTE), capacity);
            }
        }
    }

    @Override
    protected synchronized void updateCapacity(MechanicalPipe transmitter) {
        super.updateCapacity(transmitter);
        intCapacity = MathUtils.clampToInt(getCapacity());
    }

    @Override
    public synchronized void updateCapacity() {
        super.updateCapacity();
        intCapacity = MathUtils.clampToInt(getCapacity());
    }

    public int getCapacityAsInt() {
        return intCapacity;
    }

    @Override
    protected void updateSaveShares(@Nullable MechanicalPipe triggerTransmitter) {
        super.updateSaveShares(triggerTransmitter);
        int size = transmittersSize();
        if (size > 0) {
            FluidStack fluidType = fluidTank.getFluid();
            //Just pretend we are always accessing it from the north
            Direction side = Direction.NORTH;
            Set<FluidTransmitterSaveTarget> saveTargets = new ObjectOpenHashSet<>(size);
            for (MechanicalPipe transmitter : transmitters) {
                FluidTransmitterSaveTarget saveTarget = new FluidTransmitterSaveTarget(fluidType);
                saveTarget.addHandler(side, transmitter);
                saveTargets.add(saveTarget);
            }
            EmitUtils.sendToAcceptors(saveTargets, size, fluidType.getAmount(), fluidType);
            for (FluidTransmitterSaveTarget saveTarget : saveTargets) {
                saveTarget.saveShare(side);
            }
        }
    }

    private int tickEmit(@Nonnull FluidStack fluidToSend) {
        Set<FluidHandlerTarget> availableAcceptors = new ObjectOpenHashSet<>();
        int totalHandlers = 0;
        for (Entry<BlockPos, Map<Direction, LazyOptional<IFluidHandler>>> entry : acceptorCache.getAcceptorEntrySet()) {
            FluidHandlerTarget target = new FluidHandlerTarget(fluidToSend);
            entry.getValue().forEach((side, lazyAcceptor) -> lazyAcceptor.ifPresent(acceptor -> {
                if (FluidUtils.canFill(acceptor, fluidToSend)) {
                    target.addHandler(side, acceptor);
                }
            }));
            int curHandlers = target.getHandlers().size();
            if (curHandlers > 0) {
                availableAcceptors.add(target);
                totalHandlers += curHandlers;
            }
        }
        return EmitUtils.sendToAcceptors(availableAcceptors, totalHandlers, fluidToSend.getAmount(), fluidToSend);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (needsUpdate) {
            MinecraftForge.EVENT_BUS.post(new FluidTransferEvent(this, lastFluid));
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
        float scale = fluidTank.getFluidAmount() / (float) fluidTank.getCapacity();
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
        return "[FluidNetwork] " + transmitters.size() + " transmitters, " + getAcceptorCount() + " acceptors.";
    }

    @Override
    public ITextComponent getNeededInfo() {
        return MekanismLang.FLUID_NETWORK_NEEDED.translate(fluidTank.getNeeded() / 1_000F);
    }

    @Override
    public ITextComponent getStoredInfo() {
        if (fluidTank.isEmpty()) {
            return MekanismLang.NONE.translate();
        }
        return MekanismLang.NETWORK_MB_STORED.translate(fluidTank.getFluid(), fluidTank.getFluidAmount());
    }

    @Override
    public ITextComponent getFlowInfo() {
        return MekanismLang.NETWORK_MB_PER_TICK.translate(prevTransferAmount);
    }

    @Override
    public boolean isCompatibleWith(FluidNetwork other) {
        return super.isCompatibleWith(other) && (this.fluidTank.isEmpty() || other.fluidTank.isEmpty() || this.fluidTank.isFluidEqual(other.fluidTank.getFluid()));
    }

    @Override
    public boolean compatibleWithBuffer(@Nonnull FluidStack buffer) {
        return super.compatibleWithBuffer(buffer) && (this.fluidTank.isEmpty() || buffer.isEmpty() || this.fluidTank.isFluidEqual(buffer));
    }

    @Override
    public ITextComponent getTextComponent() {
        return MekanismLang.NETWORK_DESCRIPTION.translate(MekanismLang.FLUID_NETWORK, transmitters.size(), getAcceptorCount());
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }

    @Override
    public void onContentsChanged() {
        markDirty();
        FluidStack type = fluidTank.getFluid();
        if (!lastFluid.isFluidEqual(type)) {
            //If the fluid type does not match update it, and mark that we need an update
            if (!type.isEmpty()) {
                lastFluid = new FluidStack(type, 1);
            }
            needsUpdate = true;
        }
    }

    public void setLastFluid(@Nonnull FluidStack fluid) {
        if (fluid.isEmpty()) {
            fluidTank.setEmpty();
        } else {
            lastFluid = fluid;
            fluidTank.setStack(new FluidStack(fluid, 1));
        }
    }

    public static class FluidTransferEvent extends TransferEvent<FluidNetwork> {

        public final FluidStack fluidType;

        public FluidTransferEvent(FluidNetwork network, @Nonnull FluidStack type) {
            super(network);
            fluidType = type;
        }
    }
}
