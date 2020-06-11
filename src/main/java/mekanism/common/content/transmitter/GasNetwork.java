package mekanism.common.content.transmitter;

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
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasHandler.IMekanismGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.chemical.variable.VariableCapacityGasTank;
import mekanism.common.content.transmitter.distribution.ChemicalHandlerTarget;
import mekanism.common.content.transmitter.distribution.GasTransmitterSaveTarget;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;

/**
 * A DynamicNetwork extension created specifically for the transfer of Gases. By default this is server-only, but if ticked on the client side and if it's posted events
 * are handled properly, it has the capability to visually display gases network-wide.
 *
 * @author aidancbrady
 */
public class GasNetwork extends DynamicBufferedNetwork<IGasHandler, GasNetwork, GasStack, TileEntityPressurizedTube> implements IMekanismGasHandler {

    private final List<IGasTank> gasTanks;
    public final VariableCapacityGasTank gasTank;
    @Nonnull
    public Gas lastGas = MekanismAPI.EMPTY_GAS;
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
            currentScale = (float) Math.min(1, gasTank.getStored() / (double) gasTank.getCapacity());
        } else {
            currentScale = 0;
        }
    }

    @Override
    public void adoptTransmittersAndAcceptorsFrom(GasNetwork net) {
        float oldScale = currentScale;
        long oldCapacity = getCapacity();
        super.adoptTransmittersAndAcceptorsFrom(net);
        //Merge the gas scales
        long capacity = getCapacity();
        currentScale = Math.min(1, capacity == 0 ? 0 : (currentScale * oldCapacity + net.currentScale * net.capacity) / capacity);
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
            if (oldScale != currentScale) {
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
    public void absorbBuffer(TileEntityPressurizedTube transmitter) {
        GasStack gas = transmitter.releaseShare();
        if (!gas.isEmpty()) {
            if (gasTank.isEmpty()) {
                gasTank.setStack(gas.copy());
            } else if (gas.isTypeEqual(gasTank.getType())) {
                long amount = gas.getAmount();
                MekanismUtils.logMismatchedStackSize(gasTank.growStack(amount, Action.EXECUTE), amount);
            }
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
    protected void updateSaveShares(@Nullable TileEntityPressurizedTube triggerTransmitter) {
        super.updateSaveShares(triggerTransmitter);
        int size = transmittersSize();
        if (size > 0) {
            GasStack gasType = gasTank.getStack();
            //Just pretend we are always accessing it from the north
            Direction side = Direction.NORTH;
            Set<GasTransmitterSaveTarget> saveTargets = new ObjectOpenHashSet<>(size);
            for (TileEntityPressurizedTube transmitter : transmitters) {
                GasTransmitterSaveTarget saveTarget = new GasTransmitterSaveTarget(gasType);
                saveTarget.addHandler(side, transmitter);
                saveTargets.add(saveTarget);
            }
            long sent = EmitUtils.sendToAcceptors(saveTargets, size, gasType.getAmount(), gasType);
            if (triggerTransmitter != null && sent < gasType.getAmount()) {
                disperse(triggerTransmitter, new GasStack(gasType, gasType.getAmount() - sent));
            }
            for (GasTransmitterSaveTarget saveTarget : saveTargets) {
                saveTarget.saveShare(side);
            }
        }
    }

    @Override
    protected void onLastTransmitterRemoved(@Nonnull TileEntityPressurizedTube triggerTransmitter) {
        disperse(triggerTransmitter, gasTank.getStack());
    }

    private void disperse(@Nonnull TileEntityPressurizedTube triggerTransmitter, GasStack gasType) {
        if (gasType.has(GasAttributes.Radiation.class)) {
            // Handle radiation leakage
            double radioactivity = gasType.get(GasAttributes.Radiation.class).getRadioactivity();
            Mekanism.radiationManager.radiate(Coord4D.get(triggerTransmitter), gasType.getAmount() * radioactivity);
        }
    }

    private long tickEmit(@Nonnull GasStack stack) {
        Set<ChemicalHandlerTarget<Gas, GasStack, IGasHandler>> availableAcceptors = new ObjectOpenHashSet<>();
        int totalHandlers = 0;
        for (Entry<BlockPos, Map<Direction, LazyOptional<IGasHandler>>> entry : acceptorCache.getAcceptorEntrySet()) {
            ChemicalHandlerTarget<Gas, GasStack, IGasHandler> target = new ChemicalHandlerTarget<>(stack);
            entry.getValue().forEach((side, lazyAcceptor) -> lazyAcceptor.ifPresent(acceptor -> {
                if (ChemicalUtil.canInsert(acceptor, stack)) {
                    target.addHandler(side, acceptor);
                }
            }));
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
        if (needsUpdate) {
            MinecraftForge.EVENT_BUS.post(new GasTransferEvent(this, lastGas));
            needsUpdate = false;
        }
        if (gasTank.isEmpty()) {
            prevTransferAmount = 0;
        } else {
            prevTransferAmount = tickEmit(gasTank.getStack());
            MekanismUtils.logMismatchedStackSize(gasTank.shrinkStack(prevTransferAmount, Action.EXECUTE), prevTransferAmount);
        }
    }

    @Override
    protected float computeContentScale() {
        float scale = (float) (gasTank.getStored() / (double) gasTank.getCapacity());
        float ret = Math.max(currentScale, scale);
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
        return "[GasNetwork] " + transmitters.size() + " transmitters, " + getAcceptorCount() + " acceptors.";
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
        return MekanismLang.NETWORK_DESCRIPTION.translate(MekanismLang.GAS_NETWORK, transmitters.size(), getAcceptorCount());
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
            gasTank.setStack(lastGas.getStack(1));
        }
    }

    public static class GasTransferEvent extends TransferEvent<GasNetwork> {

        public final Gas transferType;

        public GasTransferEvent(GasNetwork network, Gas type) {
            super(network);
            transferType = type;
        }
    }
}