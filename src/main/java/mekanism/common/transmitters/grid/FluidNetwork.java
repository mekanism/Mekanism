package mekanism.common.transmitters.grid;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.base.target.FluidHandlerTarget;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.eventbus.api.Event;

public class FluidNetwork extends DynamicNetwork<IFluidHandler, FluidNetwork, FluidStack> {

    public int transferDelay = 0;

    public boolean didTransfer;
    public boolean prevTransfer;

    public float fluidScale;

    public Fluid refFluid;

    public FluidStack buffer;
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
        if (net.buffer != null) {
            if (buffer == null) {
                buffer = net.buffer.copy();
            } else if (buffer.getFluid() == net.buffer.getFluid()) {
                buffer.amount += net.buffer.amount;
            } else if (net.buffer.amount > buffer.amount) {
                buffer = net.buffer.copy();
            }
            net.buffer = null;
        }
        super.adoptTransmittersAndAcceptorsFrom(net);
    }

    @Nullable
    @Override
    public FluidStack getBuffer() {
        return buffer;
    }

    @Override
    public void absorbBuffer(IGridTransmitter<IFluidHandler, FluidNetwork, FluidStack> transmitter) {
        FluidStack fluid = transmitter.getBuffer();
        if (fluid == null || fluid.amount == 0) {
            return;
        }
        if (buffer == null || buffer.amount == 0) {
            buffer = fluid.copy();
            fluid.amount = 0;
            return;
        }

        //TODO better multiple buffer impl
        if (buffer.isFluidEqual(fluid)) {
            buffer.amount += fluid.amount;
        }
        fluid.amount = 0;
    }

    @Override
    public void clampBuffer() {
        if (buffer != null && buffer.amount > getCapacity()) {
            buffer.amount = getCapacity();
        }
    }

    public int getFluidNeeded() {
        return getCapacity() - (buffer != null ? buffer.amount : 0);
    }

    private int tickEmit(FluidStack fluidToSend) {
        Set<FluidHandlerTarget> availableAcceptors = new HashSet<>();
        int totalHandlers = 0;
        for (Coord4D coord : possibleAcceptors) {
            EnumSet<Direction> sides = acceptorDirections.get(coord);
            if (sides == null || sides.isEmpty()) {
                continue;
            }
            TileEntity tile = coord.getTileEntity(getWorld());
            if (tile == null) {
                continue;
            }
            FluidHandlerTarget target = new FluidHandlerTarget(fluidToSend);
            for (Direction side : sides) {
                if (CapabilityUtils.hasCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)) {
                    IFluidHandler acceptor = CapabilityUtils.getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
                    if (acceptor != null && PipeUtils.canFill(acceptor, fluidToSend)) {
                        target.addHandler(side, acceptor);
                    }
                }
            }
            int curHandlers = target.getHandlers().size();
            if (curHandlers > 0) {
                availableAcceptors.add(target);
                totalHandlers += curHandlers;
            }
        }
        return EmitUtils.sendToAcceptors(availableAcceptors, totalHandlers, fluidToSend.amount, fluidToSend);
    }

    public int emit(FluidStack fluidToSend, boolean doTransfer) {
        if (fluidToSend == null || (buffer != null && buffer.getFluid() != fluidToSend.getFluid())) {
            return 0;
        }
        int toUse = Math.min(getFluidNeeded(), fluidToSend.amount);
        if (doTransfer) {
            if (buffer == null) {
                buffer = fluidToSend.copy();
                buffer.amount = toUse;
            } else {
                buffer.amount += toUse;
            }
        }
        return toUse;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            prevTransferAmount = 0;
            if (transferDelay == 0) {
                didTransfer = false;
            } else {
                transferDelay--;
            }
            int stored = buffer != null ? buffer.amount : 0;
            if (stored != prevStored) {
                needsUpdate = true;
            }
            prevStored = stored;
            if (didTransfer != prevTransfer || needsUpdate) {
                MinecraftForge.EVENT_BUS.post(new FluidTransferEvent(this, buffer, didTransfer));
                needsUpdate = false;
            }
            prevTransfer = didTransfer;
            if (buffer != null) {
                prevTransferAmount = tickEmit(buffer);
                if (prevTransferAmount > 0) {
                    didTransfer = true;
                    transferDelay = 2;
                }
                if (buffer != null) {
                    buffer.amount -= prevTransferAmount;
                    if (buffer.amount <= 0) {
                        buffer = null;
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
                buffer = null;
            }
        }
    }

    public float getScale() {
        return Math.min(1, buffer == null || getCapacity() == 0 ? 0 : (float) buffer.amount / getCapacity());
    }

    @Override
    public String toString() {
        return "[FluidNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
    }

    @Override
    public String getNeededInfo() {
        return (float) getFluidNeeded() / 1000F + " buckets";
    }

    @Override
    public String getStoredInfo() {
        return buffer != null ? LangUtils.localizeFluidStack(buffer) + " (" + buffer.amount + " mB)" : "None";
    }

    @Override
    public String getFlowInfo() {
        return prevTransferAmount + " mB/t";
    }

    @Override
    public boolean isCompatibleWith(FluidNetwork other) {
        return super.isCompatibleWith(other) && (this.buffer == null || other.buffer == null || this.buffer.isFluidEqual(other.buffer));
    }

    @Override
    public boolean compatibleWithBuffer(FluidStack buffer) {
        return super.compatibleWithBuffer(buffer) && (this.buffer == null || buffer == null || this.buffer.isFluidEqual(buffer));
    }

    public static class FluidTransferEvent extends Event {

        public final FluidNetwork fluidNetwork;

        public final FluidStack fluidType;
        public final boolean didTransfer;

        public FluidTransferEvent(FluidNetwork network, FluidStack type, boolean did) {
            fluidNetwork = network;
            fluidType = type;
            didTransfer = did;
        }
    }
}