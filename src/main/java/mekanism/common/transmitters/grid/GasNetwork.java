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
import mekanism.common.base.target.GasTransmitterSaveTarget;
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
public class GasNetwork extends DynamicNetwork<IGasHandler, GasNetwork, GasStack> implements IMekanismGasHandler {

    private final List<? extends IChemicalTank<Gas, GasStack>> gasTanks;
    public final VariableCapacityGasTank gasTank;

    public float gasScale;
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
        register();
    }

    @Override
    protected void forceScaleUpdate() {
        if (!gasTank.isEmpty() && gasTank.getCapacity() > 0) {
            gasScale = Math.min(1, (float) gasTank.getStored() / gasTank.getCapacity());
        }
    }

    @Override
    public void adoptTransmittersAndAcceptorsFrom(GasNetwork net) {
        int oldCapacity = getCapacity();
        super.adoptTransmittersAndAcceptorsFrom(net);
        //Merge the gas scales
        gasScale = (gasScale * oldCapacity + net.gasScale * net.capacity) / getCapacity();
        if (isRemote()) {
            if (gasTank.isEmpty() && !net.gasTank.isEmpty()) {
                gasTank.setStack(net.getBuffer());
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
    }

    @Nonnull
    @Override
    public GasStack getBuffer() {
        return gasTank.getStack().copy();
    }

    @Override
    public void absorbBuffer(IGridTransmitter<IGasHandler, GasNetwork, GasStack> transmitter) {
        GasStack gas = transmitter.getBuffer();
        if (gas == null || gas.isEmpty()) {
            //Note: We support null given technically the API says it is nullable, so if someone makes a custom IGridTransmitter
            // with it being null would have issues
            return;
        }
        if (gasTank.isEmpty()) {
            gasTank.setStack(gas.copy());
        } else if (gas.isTypeEqual(gasTank.getType())) {
            //TODO: better multiple buffer impl
            int amount = gas.getAmount();
            if (gasTank.growStack(amount, Action.EXECUTE) != amount) {
                //TODO: Print warning/error
            }
        }
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

    @Override
    protected void updateSaveShares() {
        super.updateSaveShares();
        int size = transmittersSize();
        if (size > 0) {
            GasStack gasType = gasTank.getStack();
            //Just pretend we are always accessing it from the north
            Direction side = Direction.NORTH;
            Set<GasTransmitterSaveTarget> saveTargets = new ObjectOpenHashSet<>(size);
            for (IGridTransmitter<IGasHandler, GasNetwork, GasStack> transmitter : transmitters) {
                GasTransmitterSaveTarget saveTarget = new GasTransmitterSaveTarget(gasType);
                saveTarget.addHandler(side, transmitter);
                saveTargets.add(saveTarget);
            }
            EmitUtils.sendToAcceptors(saveTargets, size, gasType.getAmount(), gasType);
            for (GasTransmitterSaveTarget saveTarget : saveTargets) {
                saveTarget.saveShare(side);
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

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isRemote()) {
            float scale = MekanismUtils.getScale(gasScale, gasTank);
            if (scale != gasScale) {
                gasScale = scale;
                needsUpdate = true;
            }
            if (needsUpdate) {
                MinecraftForge.EVENT_BUS.post(new GasTransferEvent(this, getBuffer(), gasScale));
                needsUpdate = false;
            }
            if (gasTank.isEmpty()) {
                prevTransferAmount = 0;
            } else {
                prevTransferAmount = tickEmit(gasTank.getStack());
                if (gasTank.shrinkStack(prevTransferAmount, Action.EXECUTE) != prevTransferAmount) {
                    //TODO: Print warning/error
                }
            }
        }
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
        updateSaveShares = true;
    }

    public static class GasTransferEvent extends Event {

        public final GasNetwork gasNetwork;
        public final GasStack transferType;
        public final float gasScale;

        public GasTransferEvent(GasNetwork network, GasStack type, float scale) {
            gasNetwork = network;
            transferType = type;
            gasScale = scale;
        }
    }
}