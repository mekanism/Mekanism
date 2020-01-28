package mekanism.common.transmitters.grid;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.MekanismLang;
import mekanism.common.base.target.FluidHandlerTarget;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class FluidNetwork extends DynamicNetwork<IFluidHandler, FluidNetwork, FluidStack> {

    public int transferDelay = 0;

    public boolean didTransfer;
    public boolean prevTransfer;

    public float fluidScale;

    @Nonnull
    public FluidStack buffer = FluidStack.EMPTY;
    public int prevStored;

    public int prevTransferAmount = 0;

    public FluidNetwork() {
    }

    public FluidNetwork(Collection<FluidNetwork> networks) {
        for (FluidNetwork net : networks) {
            if (net != null) {
                adoptTransmittersAndAcceptorsFrom(net);
                net.deregister();
            }
        }
        fluidScale = getScale();
        register();
    }

    @Override
    public void adoptTransmittersAndAcceptorsFrom(FluidNetwork net) {
        if (!net.buffer.isEmpty()) {
            if (buffer.isEmpty()) {
                buffer = net.buffer.copy();
            } else if (buffer.getFluid() == net.buffer.getFluid()) {
                buffer.setAmount(buffer.getAmount() + net.buffer.getAmount());
            } else if (net.buffer.getAmount() > buffer.getAmount()) {
                buffer = net.buffer.copy();
            }
            net.buffer = FluidStack.EMPTY;
        }
        super.adoptTransmittersAndAcceptorsFrom(net);
    }

    @Nonnull
    @Override
    public FluidStack getBuffer() {
        return buffer;
    }

    @Override
    public void absorbBuffer(IGridTransmitter<IFluidHandler, FluidNetwork, FluidStack> transmitter) {
        FluidStack fluid = transmitter.getBuffer();
        if (fluid.isEmpty()) {
            return;
        }
        if (buffer.isEmpty()) {
            buffer = fluid.copy();
            fluid.setAmount(0);
            return;
        }

        //TODO better multiple buffer impl
        if (buffer.isFluidEqual(fluid)) {
            buffer.setAmount(buffer.getAmount() + fluid.getAmount());
        }
        fluid.setAmount(0);
    }

    @Override
    public void clampBuffer() {
        if (buffer.getAmount() > getCapacity()) {
            buffer.setAmount(getCapacity());
        }
    }

    public int getFluidNeeded() {
        return getCapacity() - buffer.getAmount();
    }

    private int tickEmit(@Nonnull FluidStack fluidToSend) {
        Set<FluidHandlerTarget> availableAcceptors = new HashSet<>();
        int totalHandlers = 0;
        for (Coord4D coord : possibleAcceptors) {
            EnumSet<Direction> sides = acceptorDirections.get(coord);
            if (sides == null || sides.isEmpty()) {
                continue;
            }
            TileEntity tile = MekanismUtils.getTileEntity(getWorld(), coord.getPos());
            if (tile == null) {
                continue;
            }
            FluidHandlerTarget target = new FluidHandlerTarget(fluidToSend);
            for (Direction side : sides) {
                CapabilityUtils.getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side).ifPresent(acceptor -> {
                    if (PipeUtils.canFill(acceptor, fluidToSend)) {
                        target.addHandler(side, acceptor);
                    }
                });
            }
            int curHandlers = target.getHandlers().size();
            if (curHandlers > 0) {
                availableAcceptors.add(target);
                totalHandlers += curHandlers;
            }
        }
        return EmitUtils.sendToAcceptors(availableAcceptors, totalHandlers, fluidToSend.getAmount(), fluidToSend);
    }

    public int emit(@Nonnull FluidStack fluidToSend, FluidAction fluidAction) {
        if (fluidToSend.isEmpty() || (!buffer.isEmpty() && !buffer.isFluidEqual(fluidToSend))) {
            return 0;
        }
        int toUse = Math.min(getFluidNeeded(), fluidToSend.getAmount());
        if (fluidAction.execute()) {
            if (buffer.isEmpty()) {
                buffer = fluidToSend.copy();
                buffer.setAmount(toUse);
            } else {
                buffer.grow(toUse);
            }
        }
        return toUse;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isRemote()) {
            prevTransferAmount = 0;
            if (transferDelay == 0) {
                didTransfer = false;
            } else {
                transferDelay--;
            }
            int stored = buffer.getAmount();
            if (stored != prevStored) {
                needsUpdate = true;
            }
            prevStored = stored;
            if (didTransfer != prevTransfer || needsUpdate) {
                MinecraftForge.EVENT_BUS.post(new FluidTransferEvent(this, buffer, didTransfer));
                needsUpdate = false;
            }
            prevTransfer = didTransfer;
            if (!buffer.isEmpty()) {
                prevTransferAmount = tickEmit(buffer);
                if (prevTransferAmount > 0) {
                    didTransfer = true;
                    transferDelay = 2;
                }
                if (!buffer.isEmpty()) {
                    buffer.setAmount(buffer.getAmount() - prevTransferAmount);
                    if (buffer.getAmount() <= 0) {
                        buffer = FluidStack.EMPTY;
                    }
                }
            }
        }
    }

    @Override
    public void clientTick() {
        super.clientTick();
        fluidScale = Math.max(fluidScale, getScale());
        if (didTransfer && fluidScale < 1) {
            fluidScale = Math.max(getScale(), Math.min(1, fluidScale + 0.02F));
        } else if (!didTransfer && fluidScale > 0) {
            fluidScale = getScale();
            if (fluidScale == 0) {
                buffer = FluidStack.EMPTY;
            }
        }
    }

    public float getScale() {
        return Math.min(1, buffer.isEmpty() || getCapacity() == 0 ? 0 : (float) buffer.getAmount() / getCapacity());
    }

    @Override
    public String toString() {
        return "[FluidNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
    }

    @Override
    public ITextComponent getNeededInfo() {
        return MekanismLang.FLUID_NETWORK_NEEDED.translate((float) getFluidNeeded() / 1_000F);
    }

    @Override
    public ITextComponent getStoredInfo() {
        if (buffer.isEmpty()) {
            return MekanismLang.NONE.translate();
        }
        return MekanismLang.NETWORK_MB_STORED.translate(buffer, buffer.getAmount());
    }

    @Override
    public ITextComponent getFlowInfo() {
        return MekanismLang.NETWORK_MB_PER_TICK.translate(prevTransferAmount);
    }

    @Override
    public boolean isCompatibleWith(FluidNetwork other) {
        return super.isCompatibleWith(other) && (this.buffer.isEmpty() || other.buffer.isEmpty() || this.buffer.isFluidEqual(other.buffer));
    }

    @Override
    public boolean compatibleWithBuffer(@Nonnull FluidStack buffer) {
        return super.compatibleWithBuffer(buffer) && (this.buffer.isEmpty() || buffer.isEmpty() || this.buffer.isFluidEqual(buffer));
    }

    @Override
    public ITextComponent getTextComponent() {
        return MekanismLang.NETWORK_DESCRIPTION.translate(MekanismLang.FLUID_NETWORK, transmitters.size(), possibleAcceptors.size());
    }

    public static class FluidTransferEvent extends Event {

        public final FluidNetwork fluidNetwork;

        public final FluidStack fluidType;
        public final boolean didTransfer;

        public FluidTransferEvent(FluidNetwork network, @Nonnull FluidStack type, boolean did) {
            fluidNetwork = network;
            fluidType = type;
            didTransfer = did;
        }
    }
}