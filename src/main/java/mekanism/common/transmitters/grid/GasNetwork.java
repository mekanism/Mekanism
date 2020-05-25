package mekanism.common.transmitters.grid;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.text.TextComponentUtil;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.chemical.VariableCapacityGasTank;
import mekanism.common.distribution.target.GasHandlerTarget;
import mekanism.common.distribution.target.GasTransmitterSaveTarget;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
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

    private final List<IGasTank> gasTanks;
    public final VariableCapacityGasTank gasTank;

    @Nonnull
    public Gas lastGas = MekanismAPI.EMPTY_GAS;
    public float gasScale;
    private long prevTransferAmount;

    public GasNetwork() {
        gasTank = VariableCapacityGasTank.create(this::getCapacity, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, this);
        gasTanks = Collections.singletonList(gasTank);
    }

    public GasNetwork(UUID networkID) {
        super(networkID);
        gasTank = VariableCapacityGasTank.create(this::getCapacity, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, this);
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
            gasScale = (float) Math.min(1, gasTank.getStored() / (double) gasTank.getCapacity());
        } else {
            gasScale = 0;
        }
    }

    @Override
    public void adoptTransmittersAndAcceptorsFrom(GasNetwork net) {
        float oldScale = gasScale;
        long oldCapacity = getCapacity();
        super.adoptTransmittersAndAcceptorsFrom(net);
        //Merge the gas scales
        long capacity = getCapacity();
        gasScale = Math.min(1, capacity == 0 ? 0 : (gasScale * oldCapacity + net.gasScale * net.capacity) / capacity);
        if (isRemote()) {
            if (gasTank.isEmpty() && !net.gasTank.isEmpty()) {
                gasTank.setStack(net.getBuffer());
                net.gasTank.setEmpty();
            }
        } else {
            if (!net.gasTank.isEmpty()) {
                if (gasTank.isEmpty()) {
                    gasTank.setStack(net.getBuffer());
                } else if (gasTank.isTypeEqual(net.gasTank.getType())) {
                    long amount = net.gasTank.getStored();
                    MekanismUtils.logMismatchedStackSize(gasTank.growStack(amount, Action.EXECUTE), amount);
                }
                net.gasTank.setEmpty();
            }
            if (oldScale != gasScale) {
                //We want to make sure we update to the scale change
                needsUpdate = true;
            }
        }
    }

    @Nonnull
    @Override
    public GasStack getBuffer() {
        return gasTank.getStack().copy();
    }

    @Override
    public void absorbBuffer(IGridTransmitter<IGasHandler, GasNetwork, GasStack> transmitter) {
        GasStack gas = transmitter.releaseShare();
        if (gas.isEmpty()) {
            return;
        }
        if (gasTank.isEmpty()) {
            gasTank.setStack(gas.copy());
        } else if (gas.isTypeEqual(gasTank.getType())) {
            long amount = gas.getAmount();
            MekanismUtils.logMismatchedStackSize(gasTank.growStack(amount, Action.EXECUTE), amount);
        }
    }

    @Override
    public void clampBuffer() {
        if (!gasTank.isEmpty()) {
            long capacity = getCapacity();
            if (gasTank.getStored() > capacity) {
                MekanismUtils.logMismatchedStackSize(gasTank.setStackSize(capacity, Action.EXECUTE), capacity);
            }
        }
    }

    @Override
    protected void updateSaveShares(@Nullable IGridTransmitter<?, ?, ?> triggerTransmitter) {
        super.updateSaveShares(triggerTransmitter);
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
            long sent = EmitUtils.sendToAcceptors(saveTargets, size, gasType.getAmount(), gasType);
            if (sent < gasType.getAmount() && triggerTransmitter != null) {
                disperse(triggerTransmitter, new GasStack(gasType.getType(), gasType.getAmount() - sent));
            }
            for (GasTransmitterSaveTarget saveTarget : saveTargets) {
                saveTarget.saveShare(side);
            }
        }
    }

    @Override
    protected void onLastTransmitterRemoved(@Nullable IGridTransmitter<?, ?, ?> triggerTransmitter) {
        disperse(triggerTransmitter, gasTank.getStack());
    }

    private void disperse(@Nullable IGridTransmitter<?, ?, ?> triggerTransmitter, GasStack gasType) {
        if (gasType.has(GasAttributes.Radiation.class)) {
            // Handle radiation leakage
            double radioactivity = gasType.get(GasAttributes.Radiation.class).getRadioactivity();
            Mekanism.radiationManager.radiate(triggerTransmitter.coord(), gasType.getAmount() * radioactivity);
        }
    }

    private long tickEmit(@Nonnull GasStack stack) {
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
            float scale = computeContentScale();
            if (scale != gasScale) {
                gasScale = scale;
                needsUpdate = true;
            }
            if (needsUpdate) {
                MinecraftForge.EVENT_BUS.post(new GasTransferEvent(this, lastGas, gasScale));
                needsUpdate = false;
            }
            if (gasTank.isEmpty()) {
                prevTransferAmount = 0;
            } else {
                prevTransferAmount = tickEmit(gasTank.getStack());
                MekanismUtils.logMismatchedStackSize(gasTank.shrinkStack(prevTransferAmount, Action.EXECUTE), prevTransferAmount);
            }
        }
    }

    public float computeContentScale() {
        float scale = (float) (gasTank.getStored() / (double) gasTank.getCapacity());
        float ret = Math.max(gasScale, scale);
        if (prevTransferAmount > 0 && ret < 1) {
            ret = Math.min(1, ret + 0.02F);
        } else if (prevTransferAmount <= 0 && ret > 0) {
            ret = Math.max(scale, ret - 0.02F);
        }
        return ret;
    }

    public long getPrevTransferAmount() {
        return prevTransferAmount;
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
    public List<IGasTank> getChemicalTanks(@Nullable Direction side) {
        return gasTanks;
    }

    @Override
    public void onContentsChanged() {
        markDirty();
        Gas type = gasTank.getType();
        if (lastGas != type) {
            //If the gas type does not match update it, and mark that we need an update
            if (!type.isEmptyType()) {
                lastGas = type;
            }
            needsUpdate = true;
        }
    }

    public void setLastGas(@Nonnull Gas gas) {
        if (gas.isEmptyType()) {
            gasTank.setEmpty();
        } else {
            lastGas = gas;
            gasTank.setStack(lastGas.getGasStack(1));
        }
    }

    public static class GasTransferEvent extends Event {

        public final GasNetwork gasNetwork;
        public final Gas transferType;
        public final float gasScale;

        public GasTransferEvent(GasNetwork network, Gas type, float scale) {
            gasNetwork = network;
            transferType = type;
            gasScale = scale;
        }
    }
}