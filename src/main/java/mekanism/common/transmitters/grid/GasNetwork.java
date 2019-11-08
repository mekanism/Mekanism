package mekanism.common.transmitters.grid;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.base.target.GasHandlerTarget;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

/**
 * A DynamicNetwork extension created specifically for the transfer of Gases. By default this is server-only, but if ticked on the client side and if it's posted events
 * are handled properly, it has the capability to visually display gasses network-wide.
 *
 * @author aidancbrady
 */
//TODO: Should GasStack have @NonNull in the params
public class GasNetwork extends DynamicNetwork<IGasHandler, GasNetwork, GasStack> {

    public int transferDelay = 0;

    public boolean didTransfer;
    public boolean prevTransfer;

    public float gasScale;

    @Nonnull
    public GasStack buffer = GasStack.EMPTY;
    public int prevStored;

    public int prevTransferAmount = 0;

    public GasNetwork() {
    }

    public GasNetwork(Collection<GasNetwork> networks) {
        for (GasNetwork net : networks) {
            if (net != null) {
                adoptTransmittersAndAcceptorsFrom(net);
                net.deregister();
            }
        }
        gasScale = getScale();
        register();
    }

    @Override
    public void adoptTransmittersAndAcceptorsFrom(GasNetwork net) {
        if (isRemote()) {
            if (!net.buffer.isEmpty() && net.gasScale > gasScale) {
                gasScale = net.gasScale;
                buffer = net.buffer;
                net.gasScale = 0;
                net.buffer = GasStack.EMPTY;
            }
        } else {
            if (!net.buffer.isEmpty()) {
                if (buffer.isEmpty()) {
                    buffer = net.buffer.copy();
                } else if (buffer.isTypeEqual(net.buffer)) {
                    buffer.grow(net.buffer.getAmount());
                } else if (net.buffer.getAmount() > buffer.getAmount()) {
                    buffer = net.buffer.copy();
                }
                net.buffer = GasStack.EMPTY;
            }
        }
        super.adoptTransmittersAndAcceptorsFrom(net);
    }

    @Nonnull
    @Override
    public GasStack getBuffer() {
        return buffer;
    }

    @Override
    public void absorbBuffer(IGridTransmitter<IGasHandler, GasNetwork, GasStack> transmitter) {
        GasStack gas = transmitter.getBuffer();
        if (gas.isEmpty()) {
            return;
        }
        if (buffer.isEmpty()) {
            buffer = gas.copy();
            gas.setAmount(0);
            return;
        }

        //TODO better multiple buffer impl
        if (buffer.isTypeEqual(gas)) {
            buffer.grow(gas.getAmount());
        }
        gas.setAmount(0);
    }

    @Override
    public void clampBuffer() {
        if (!buffer.isEmpty() && buffer.getAmount() > getCapacity()) {
            buffer.setAmount(getCapacity());
        }
    }

    public int getGasNeeded() {
        return getCapacity() - buffer.getAmount();
    }

    private int tickEmit(@Nonnull GasStack stack) {
        Set<GasHandlerTarget> availableAcceptors = new HashSet<>();
        int totalHandlers = 0;
        Gas type = stack.getType();
        for (Coord4D coord : possibleAcceptors) {
            EnumSet<Direction> sides = acceptorDirections.get(coord);
            if (sides == null || sides.isEmpty()) {
                continue;
            }
            TileEntity tile = MekanismUtils.getTileEntity(getWorld(), coord.getPos());
            if (tile == null) {
                continue;
            }
            GasHandlerTarget target = new GasHandlerTarget(stack);
            for (Direction side : sides) {
                CapabilityUtils.getCapabilityHelper(tile, Capabilities.GAS_HANDLER_CAPABILITY, side).ifPresent(acceptor -> {
                    if (acceptor.canReceiveGas(side, type)) {
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
        return EmitUtils.sendToAcceptors(availableAcceptors, totalHandlers, stack.getAmount(), stack);
    }

    public int emit(@Nonnull GasStack stack, Action action) {
        if (!buffer.isEmpty() && !buffer.isTypeEqual(stack)) {
            return 0;
        }
        int toUse = Math.min(getGasNeeded(), stack.getAmount());
        if (action.execute()) {
            if (buffer.isEmpty()) {
                buffer = stack.copy();
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
                MinecraftForge.EVENT_BUS.post(new GasTransferEvent(this, buffer, didTransfer));
                needsUpdate = false;
            }

            prevTransfer = didTransfer;
            if (!buffer.isEmpty()) {
                prevTransferAmount = tickEmit(buffer);
                if (prevTransferAmount > 0) {
                    didTransfer = true;
                    transferDelay = 2;
                }
                buffer.shrink(prevTransferAmount);
            }
        }
    }

    @Override
    public void clientTick() {
        super.clientTick();
        gasScale = Math.max(gasScale, getScale());
        if (didTransfer && gasScale < 1) {
            gasScale = Math.max(getScale(), Math.min(1, gasScale + 0.02F));
        } else if (!didTransfer && gasScale > 0) {
            gasScale = Math.max(getScale(), Math.max(0, gasScale - 0.02F));
            if (gasScale == 0) {
                buffer = GasStack.EMPTY;
            }
        }
    }

    public float getScale() {
        return Math.min(1, buffer.isEmpty() || getCapacity() == 0 ? 0 : (float) buffer.getAmount() / getCapacity());
    }

    @Override
    public String toString() {
        return "[GasNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
    }

    @Override
    public ITextComponent getNeededInfo() {
        return TextComponentUtil.build(getGasNeeded());
    }

    @Override
    public ITextComponent getStoredInfo() {
        if (buffer.isEmpty()) {
            return TextComponentUtil.translate("gui.mekanism.none");
        }
        return TextComponentUtil.build(buffer, " (" + buffer.getAmount() + ")");
    }

    @Override
    public ITextComponent getFlowInfo() {
        return TextComponentUtil.getString(prevTransferAmount + "/t");
    }

    @Override
    public boolean isCompatibleWith(GasNetwork other) {
        return super.isCompatibleWith(other) && (this.buffer.isEmpty() || other.buffer.isEmpty() || this.buffer.isTypeEqual(other.buffer));
    }

    @Override
    public boolean compatibleWithBuffer(@Nonnull GasStack buffer) {
        return super.compatibleWithBuffer(buffer) && (this.buffer.isEmpty() || buffer.isEmpty() || this.buffer.isTypeEqual(buffer));
    }

    public static class GasTransferEvent extends Event {

        public final GasNetwork gasNetwork;

        public final GasStack transferType;
        public final boolean didTransfer;

        public GasTransferEvent(GasNetwork network, GasStack type, boolean did) {
            gasNetwork = network;
            transferType = type;
            didTransfer = did;
        }
    }
}