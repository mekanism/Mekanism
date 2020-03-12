package mekanism.common.transmitters.grid;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.MekanismLang;
import mekanism.common.base.target.GasHandlerTarget;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.chemical.VariableCapacityGasTank;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

/**
 * A DynamicNetwork extension created specifically for the transfer of Gases. By default this is server-only, but if ticked on the client side and if it's posted events
 * are handled properly, it has the capability to visually display gases network-wide.
 *
 * @author aidancbrady
 */
//TODO: Should GasStack have @NonNull in the params
public class GasNetwork extends DynamicNetwork<IGasHandler, GasNetwork, GasStack> implements IMekanismGasHandler {

    private final List<? extends IChemicalTank<Gas, GasStack>> gasTanks;
    public final VariableCapacityGasTank gasTank;

    private int transferDelay = 0;

    public boolean didTransfer;
    private boolean prevTransfer;

    public float gasScale;
    private int prevStored;
    private int prevTransferAmount;

    public GasNetwork() {
        gasTank = VariableCapacityGasTank.create(this::getCapacity, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrue, this);
        gasTanks = Collections.singletonList(gasTank);
    }

    public GasNetwork(Collection<GasNetwork> networks) {
        this();
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
            if (!net.gasTank.isEmpty() && net.gasScale > gasScale) {
                gasScale = net.gasScale;
                gasTank.setStack(net.getBuffer());
                net.gasScale = 0;
                net.gasTank.setEmpty();
            }
        } else if (!net.gasTank.isEmpty()) {
            if (gasTank.isEmpty()) {
                gasTank.setStack(net.getBuffer());
            } else if (gasTank.isTypeEqual(net.gasTank.getType())) {
                int amount = net.gasTank.getStored();
                if (gasTank.growStack(amount, Action.EXECUTE) != amount) {
                    //TODO: Print warning/error
                }
            } else if (net.gasTank.getStored() > gasTank.getStored()) {
                gasTank.setStack(net.getBuffer());
            }
            net.gasTank.setEmpty();
        }
        super.adoptTransmittersAndAcceptorsFrom(net);
    }

    @Nonnull
    @Override
    public GasStack getBuffer() {
        return gasTank.getStack().copy();
    }

    @Override
    public void absorbBuffer(IGridTransmitter<IGasHandler, GasNetwork, GasStack> transmitter) {
        GasStack gas = transmitter.getBuffer();
        if (gas.isEmpty()) {
            return;
        }
        if (gasTank.isEmpty()) {
            gasTank.setStack(gas.copy());
            gas.setAmount(0);
            return;
        }

        //TODO: better multiple buffer impl
        if (gas.isTypeEqual(gasTank.getType())) {
            int amount = gas.getAmount();
            if (gasTank.growStack(amount, Action.EXECUTE) != amount) {
                //TODO: Print warning/error
            }
        }
        gas.setAmount(0);
    }

    @Override
    public void clampBuffer() {
        if (!gasTank.isEmpty()) {
            int capacity = getCapacity();
            if (gasTank.getStored() > capacity) {
                if (gasTank.setStackSize(capacity, Action.EXECUTE) != capacity) {
                    //TODO: Print warning/error
                }
            }
        }
    }

    private int tickEmit(@Nonnull GasStack stack) {
        Set<GasHandlerTarget> availableAcceptors = new ObjectOpenHashSet<>();
        int totalHandlers = 0;
        GasStack unitStack = new GasStack(stack, 1);
        Long2ObjectMap<IChunk> chunkMap = new Long2ObjectOpenHashMap<>();
        for (Coord4D coord : possibleAcceptors) {
            EnumSet<Direction> sides = acceptorDirections.get(coord);
            if (sides == null || sides.isEmpty()) {
                continue;
            }
            TileEntity tile = MekanismUtils.getTileEntity(getWorld(), chunkMap, coord);
            if (tile == null) {
                continue;
            }
            GasHandlerTarget target = new GasHandlerTarget(stack);
            for (Direction side : sides) {
                CapabilityUtils.getCapability(tile, Capabilities.GAS_HANDLER_CAPABILITY, side).ifPresent(acceptor -> {
                    if (GasUtils.canInsert(acceptor, unitStack)) {
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

    /**
     * @return remainder
     */
    @Nonnull
    public GasStack emit(@Nonnull GasStack stack, Action action) {
        if (stack.isEmpty() || (!gasTank.isEmpty() && !gasTank.isTypeEqual(stack))) {
            return stack;
        }
        int toAdd = Math.min(gasTank.getNeeded(), stack.getAmount());
        if (action.execute()) {
            if (gasTank.isEmpty()) {
                gasTank.setStack(new GasStack(stack, toAdd));
            } else {
                //Otherwise try to grow the stack
                if (gasTank.growStack(toAdd, Action.EXECUTE) != toAdd) {
                    //TODO: Print warning/error
                }
            }
        }
        return new GasStack(stack, stack.getAmount() - toAdd);
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

            int stored = gasTank.getStored();
            if (stored != prevStored) {
                needsUpdate = true;
            }

            prevStored = stored;
            if (didTransfer != prevTransfer || needsUpdate) {
                MinecraftForge.EVENT_BUS.post(new GasTransferEvent(this, getBuffer(), didTransfer));
                needsUpdate = false;
            }

            prevTransfer = didTransfer;
            if (!gasTank.isEmpty()) {
                prevTransferAmount = tickEmit(gasTank.getStack());
                if (prevTransferAmount > 0) {
                    didTransfer = true;
                    transferDelay = 2;
                }
                if (gasTank.shrinkStack(prevTransferAmount, Action.EXECUTE) != prevTransferAmount) {
                    //TODO: Print warning/error
                }
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
                gasTank.setEmpty();
            }
        }
    }

    public float getScale() {
        return Math.min(1, gasTank.isEmpty() || getCapacity() == 0 ? 0 : (float) gasTank.getStored() / getCapacity());
    }

    @Override
    public String toString() {
        return "[GasNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
    }

    @Override
    public ITextComponent getNeededInfo() {
        return TextComponentUtil.build(gasTank.getNeeded());
    }

    @Override
    public ITextComponent getStoredInfo() {
        if (gasTank.isEmpty()) {
            return MekanismLang.NONE.translate();
        }
        return MekanismLang.NETWORK_MB_STORED.translate(gasTank.getStack(), gasTank.getStored());
    }

    @Override
    public ITextComponent getFlowInfo() {
        return MekanismLang.NETWORK_MB_PER_TICK.translate(prevTransferAmount);
    }

    @Override
    public boolean isCompatibleWith(GasNetwork other) {
        return super.isCompatibleWith(other) && (gasTank.isEmpty() || other.gasTank.isEmpty() || gasTank.isTypeEqual(other.gasTank.getType()));
    }

    @Override
    public boolean compatibleWithBuffer(@Nonnull GasStack buffer) {
        return super.compatibleWithBuffer(buffer) && (gasTank.isEmpty() || buffer.isEmpty() || gasTank.isTypeEqual(buffer));
    }

    @Override
    public ITextComponent getTextComponent() {
        return MekanismLang.NETWORK_DESCRIPTION.translate(MekanismLang.GAS_NETWORK, transmitters.size(), possibleAcceptors.size());
    }

    @Nonnull
    @Override
    public List<? extends IChemicalTank<Gas, GasStack>> getGasTanks(@Nullable Direction side) {
        return gasTanks;
    }

    @Override
    public void onContentsChanged() {
        //TODO: Do we want to mark the network as dirty
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