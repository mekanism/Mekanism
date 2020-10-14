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
import mekanism.api.Coord4D;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.merged.BoxedChemical;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.merged.MergedChemicalTank.Current;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.chemical.BoxedChemicalHandler;
import mekanism.common.capabilities.chemical.dynamic.IGasTracker;
import mekanism.common.capabilities.chemical.dynamic.IInfusionTracker;
import mekanism.common.capabilities.chemical.dynamic.IPigmentTracker;
import mekanism.common.capabilities.chemical.dynamic.ISlurryTracker;
import mekanism.common.capabilities.chemical.variable.VariableCapacityChemicalTankBuilder;
import mekanism.common.content.network.distribution.BoxedChemicalTransmitterSaveTarget;
import mekanism.common.content.network.distribution.ChemicalHandlerTarget;
import mekanism.common.content.network.transmitter.BoxedPressurizedTube;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;

/**
 * A DynamicNetwork extension created specifically for the transfer of Chemicals.
 */
public class BoxedChemicalNetwork extends DynamicBufferedNetwork<BoxedChemicalHandler, BoxedChemicalNetwork, BoxedChemicalStack, BoxedPressurizedTube>
      implements IGasTracker, IInfusionTracker, IPigmentTracker, ISlurryTracker {

    public final MergedChemicalTank chemicalTank;
    private final List<IGasTank> gasTanks;
    private final List<IInfusionTank> infusionTanks;
    private final List<IPigmentTank> pigmentTanks;
    private final List<ISlurryTank> slurryTanks;
    @Nonnull
    public BoxedChemical lastChemical = BoxedChemical.EMPTY;
    private long prevTransferAmount;

    public BoxedChemicalNetwork() {
        this(UUID.randomUUID());
    }

    public BoxedChemicalNetwork(UUID networkID) {
        super(networkID);
        chemicalTank = MergedChemicalTank.create(
              VariableCapacityChemicalTankBuilder.GAS.createAllValid(this::getCapacity, this),
              VariableCapacityChemicalTankBuilder.INFUSION.createAllValid(this::getCapacity, this),
              VariableCapacityChemicalTankBuilder.PIGMENT.createAllValid(this::getCapacity, this),
              VariableCapacityChemicalTankBuilder.SLURRY.createAllValid(this::getCapacity, this)
        );
        gasTanks = Collections.singletonList(chemicalTank.getGasTank());
        infusionTanks = Collections.singletonList(chemicalTank.getInfusionTank());
        pigmentTanks = Collections.singletonList(chemicalTank.getPigmentTank());
        slurryTanks = Collections.singletonList(chemicalTank.getSlurryTank());
    }

    public BoxedChemicalNetwork(Collection<BoxedChemicalNetwork> networks) {
        this();
        adoptAllAndRegister(networks);
    }

    public boolean isTankEmpty() {
        return chemicalTank.getCurrent() == Current.EMPTY;
    }

    public IGasTank getGasTank() {
        return chemicalTank.getGasTank();
    }

    public IInfusionTank getInfusionTank() {
        return chemicalTank.getInfusionTank();
    }

    public IPigmentTank getPigmentTank() {
        return chemicalTank.getPigmentTank();
    }

    public ISlurryTank getSlurryTank() {
        return chemicalTank.getSlurryTank();
    }

    /**
     * @implNote Falls back to the gas tank if empty
     */
    private IChemicalTank<?, ?> getCurrentTankWithFallback() {
        Current current = chemicalTank.getCurrent();
        return current == Current.EMPTY ? getGasTank() : chemicalTank.getTankFromCurrent(current);
    }

    @Override
    protected void forceScaleUpdate() {
        if (!isTankEmpty() && getCapacity() > 0) {
            currentScale = (float) Math.min(1, getCurrentTankWithFallback().getStored() / (double) getCapacity());
        } else {
            currentScale = 0;
        }
    }

    @Override
    public List<BoxedPressurizedTube> adoptTransmittersAndAcceptorsFrom(BoxedChemicalNetwork net) {
        float oldScale = currentScale;
        long oldCapacity = getCapacity();
        List<BoxedPressurizedTube> transmittersToUpdate = super.adoptTransmittersAndAcceptorsFrom(net);
        //Merge the chemical scales
        long capacity = getCapacity();
        currentScale = Math.min(1, capacity == 0 ? 0 : (currentScale * oldCapacity + net.currentScale * net.capacity) / capacity);
        if (isRemote()) {
            if (isTankEmpty()) {
                adoptBuffer(net);
            }
        } else {
            if (!net.isTankEmpty()) {
                if (isTankEmpty()) {
                    adoptBuffer(net);
                } else {
                    Current current = chemicalTank.getCurrent();
                    Current netCurrent = net.chemicalTank.getCurrent();
                    if (current == netCurrent) {
                        //If the chemical types match (then compare the chemicals themselves)
                        IChemicalTank<?, ?> tank = chemicalTank.getTankFromCurrent(current);
                        IChemicalTank<?, ?> netTank = net.chemicalTank.getTankFromCurrent(current);
                        if (tank.getType() == netTank.getType()) {
                            long amount = netTank.getStored();
                            MekanismUtils.logMismatchedStackSize(tank.growStack(amount, Action.EXECUTE), amount);
                        }
                        netTank.setEmpty();
                    } else {
                        Mekanism.logger.error("Incompatible chemical networks merged: {}, {}.", current, netCurrent);
                    }
                }
            }
            if (oldScale != currentScale) {
                //We want to make sure we update to the scale change
                needsUpdate = true;
            }
        }
        return transmittersToUpdate;
    }

    private void adoptBuffer(BoxedChemicalNetwork net) {
        Current netCurrent = net.chemicalTank.getCurrent();
        if (netCurrent == Current.GAS) {
            moveBuffer(getGasTank(), net.getGasTank());
        } else if (netCurrent == Current.INFUSION) {
            moveBuffer(getInfusionTank(), net.getInfusionTank());
        } else if (netCurrent == Current.PIGMENT) {
            moveBuffer(getPigmentTank(), net.getPigmentTank());
        } else if (netCurrent == Current.SLURRY) {
            moveBuffer(getSlurryTank(), net.getSlurryTank());
        }
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> void moveBuffer(TANK tank, TANK other) {
        tank.setStack(ChemicalUtil.copy(other.getStack()));
        other.setEmpty();
    }

    @Nonnull
    @Override
    public BoxedChemicalStack getBuffer() {
        Current current = chemicalTank.getCurrent();
        if (current == Current.EMPTY) {
            return BoxedChemicalStack.EMPTY;
        }
        return BoxedChemicalStack.box(chemicalTank.getTankFromCurrent(current).getStack().copy());
    }

    @Override
    public void absorbBuffer(BoxedPressurizedTube transmitter) {
        BoxedChemicalStack chemical = transmitter.releaseShare();
        if (!chemical.isEmpty()) {
            Current current = chemicalTank.getCurrent();
            ChemicalStack<?> chemicalStack = chemical.getChemicalStack();
            if (current == Current.EMPTY) {
                setStack(chemicalStack.copy(), chemicalTank.getTankForType(chemical.getChemicalType()));
            } else if (ChemicalUtil.compareTypes(chemical.getChemicalType(), current)) {
                IChemicalTank<?, ?> tank = chemicalTank.getTankFromCurrent(current);
                if (chemicalStack.getType() == tank.getType()) {
                    long amount = chemicalStack.getAmount();
                    MekanismUtils.logMismatchedStackSize(tank.growStack(amount, Action.EXECUTE), amount);
                }
            }
        }
    }

    @Override
    public void clampBuffer() {
        Current current = chemicalTank.getCurrent();
        if (current != Current.EMPTY) {
            long capacity = getCapacity();
            IChemicalTank<?, ?> tank = chemicalTank.getTankFromCurrent(current);
            if (tank.getStored() > capacity) {
                MekanismUtils.logMismatchedStackSize(tank.setStackSize(capacity, Action.EXECUTE), capacity);
            }
        }
    }

    @Override
    protected void updateSaveShares(@Nullable BoxedPressurizedTube triggerTransmitter) {
        super.updateSaveShares(triggerTransmitter);
        int size = transmittersSize();
        if (size > 0) {
            updateSaveShares(triggerTransmitter, size, getCurrentTankWithFallback().getStack());
        }
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void updateSaveShares(@Nullable BoxedPressurizedTube triggerTransmitter, int size,
          STACK chemical) {
        STACK empty = ChemicalUtil.getEmptyStack(chemical);
        Direction side = Direction.NORTH;
        Set<BoxedChemicalTransmitterSaveTarget<CHEMICAL, STACK>> saveTargets = new ObjectOpenHashSet<>(size);
        for (BoxedPressurizedTube transmitter : transmitters) {
            BoxedChemicalTransmitterSaveTarget<CHEMICAL, STACK> saveTarget = new BoxedChemicalTransmitterSaveTarget<>(empty, chemical);
            saveTarget.addHandler(side, transmitter);
            saveTargets.add(saveTarget);
        }
        long sent = EmitUtils.sendToAcceptors(saveTargets, size, chemical.getAmount(), chemical);
        if (triggerTransmitter != null && sent < chemical.getAmount()) {
            disperse(triggerTransmitter, ChemicalUtil.copyWithAmount(chemical, chemical.getAmount() - sent));
        }
        for (BoxedChemicalTransmitterSaveTarget<CHEMICAL, STACK> saveTarget : saveTargets) {
            saveTarget.saveShare(side);
        }
    }

    @Override
    protected void onLastTransmitterRemoved(@Nonnull BoxedPressurizedTube triggerTransmitter) {
        Current current = chemicalTank.getCurrent();
        if (current != Current.EMPTY) {
            disperse(triggerTransmitter, chemicalTank.getTankFromCurrent(current).getStack());
        }
    }

    protected <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void disperse(@Nonnull BoxedPressurizedTube triggerTransmitter, STACK chemical) {
        if (chemical instanceof GasStack) {
            if (chemical.has(GasAttributes.Radiation.class)) {
                // Handle radiation leakage
                double radioactivity = chemical.get(GasAttributes.Radiation.class).getRadioactivity();
                Mekanism.radiationManager.radiate(new Coord4D(triggerTransmitter.getTilePos(), triggerTransmitter.getTileWorld()), chemical.getAmount() * radioactivity);
            }
        }
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> long tickEmit(@Nonnull STACK stack) {
        ChemicalType chemicalType = ChemicalType.getTypeFor(stack);
        Set<ChemicalHandlerTarget<CHEMICAL, STACK, IChemicalHandler<CHEMICAL, STACK>>> availableAcceptors = new ObjectOpenHashSet<>();
        int totalHandlers = 0;
        for (Entry<BlockPos, Map<Direction, LazyOptional<BoxedChemicalHandler>>> entry : acceptorCache.getAcceptorEntrySet()) {
            ChemicalHandlerTarget<CHEMICAL, STACK, IChemicalHandler<CHEMICAL, STACK>> target = new ChemicalHandlerTarget<>(stack);
            entry.getValue().forEach((side, lazyAcceptor) -> lazyAcceptor.ifPresent(acceptor -> {
                IChemicalHandler<CHEMICAL, STACK> handler = acceptor.getHandlerFor(chemicalType);
                if (handler != null && ChemicalUtil.canInsert(handler, stack)) {
                    target.addHandler(side, handler);
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
            MinecraftForge.EVENT_BUS.post(new ChemicalTransferEvent(this, lastChemical));
            needsUpdate = false;
        }
        Current current = chemicalTank.getCurrent();
        if (current == Current.EMPTY) {
            prevTransferAmount = 0;
        } else {
            IChemicalTank<?, ?> tank = chemicalTank.getTankFromCurrent(current);
            prevTransferAmount = tickEmit(tank.getStack());
            MekanismUtils.logMismatchedStackSize(tank.shrinkStack(prevTransferAmount, Action.EXECUTE), prevTransferAmount);
        }
    }

    @Override
    protected float computeContentScale() {
        float scale = (float) (getCurrentTankWithFallback().getStored() / (double) getCapacity());
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
    public ITextComponent getNeededInfo() {
        return TextComponentUtil.build(getCurrentTankWithFallback().getNeeded());
    }

    @Override
    public ITextComponent getStoredInfo() {
        if (isTankEmpty()) {
            return MekanismLang.NONE.translate();
        }
        IChemicalTank<?, ?> tank = getCurrentTankWithFallback();
        return MekanismLang.NETWORK_MB_STORED.translate(tank.getStack(), tank.getStored());
    }

    @Override
    public ITextComponent getFlowInfo() {
        return MekanismLang.NETWORK_MB_PER_TICK.translate(prevTransferAmount);
    }

    @Override
    public boolean isCompatibleWith(BoxedChemicalNetwork other) {
        if (super.isCompatibleWith(other)) {
            Current current = chemicalTank.getCurrent();
            if (current == Current.EMPTY) {
                return true;
            }
            Current otherCurrent = other.chemicalTank.getCurrent();
            return otherCurrent == Current.EMPTY || current == otherCurrent && chemicalTank.getTankFromCurrent(current).getType() ==
                                                                               other.chemicalTank.getTankFromCurrent(otherCurrent).getType();
        }
        return false;
    }

    @Override
    public boolean compatibleWithBuffer(@Nonnull BoxedChemicalStack buffer) {
        if (super.compatibleWithBuffer(buffer)) {
            Current current = chemicalTank.getCurrent();
            if (current == Current.EMPTY || buffer.isEmpty()) {
                return true;
            }
            return ChemicalUtil.compareTypes(buffer.getChemicalType(), current) && chemicalTank.getTankFromCurrent(current).getType() == buffer.getChemicalStack().getType();
        }
        return false;
    }

    @Override
    public ITextComponent getTextComponent() {
        return MekanismLang.NETWORK_DESCRIPTION.translate(MekanismLang.CHEMICAL_NETWORK, transmitters.size(), getAcceptorCount());
    }

    @Override
    public String toString() {
        return "[ChemicalNetwork] " + transmitters.size() + " transmitters, " + getAcceptorCount() + " acceptors.";
    }

    @Override
    public void onContentsChanged() {
        markDirty();
        Current current = chemicalTank.getCurrent();
        BoxedChemical type = current == Current.EMPTY ? BoxedChemical.EMPTY : BoxedChemical.box(chemicalTank.getTankFromCurrent(current).getType());
        if (!lastChemical.equals(type)) {
            //If the chemical type does not match update it, and mark that we need an update
            if (!type.isEmpty()) {
                lastChemical = type;
            }
            needsUpdate = true;
        }
    }

    public void setLastChemical(@Nonnull BoxedChemical chemical) {
        if (chemical.isEmpty()) {
            Current current = chemicalTank.getCurrent();
            if (current != Current.EMPTY) {
                chemicalTank.getTankFromCurrent(current).setEmpty();
            }
        } else {
            lastChemical = chemical;
            setStackClearOthers(lastChemical.getChemical().getStack(1), chemicalTank.getTankForType(lastChemical.getChemicalType()));
        }
    }

    @Nonnull
    @Override
    public List<IGasTank> getGasTanks(@Nullable Direction side) {
        return gasTanks;
    }

    @Nonnull
    @Override
    public List<IInfusionTank> getInfusionTanks(@Nullable Direction side) {
        return infusionTanks;
    }

    @Nonnull
    @Override
    public List<IPigmentTank> getPigmentTanks(@Nullable Direction side) {
        return pigmentTanks;
    }

    @Nonnull
    @Override
    public List<ISlurryTank> getSlurryTanks(@Nullable Direction side) {
        return slurryTanks;
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void setStack(STACK stack, IChemicalTank<?, ?> tank) {
        ((IChemicalTank<CHEMICAL, STACK>) tank).setStack(stack);
    }

    private void setStackClearOthers(ChemicalStack<?> stack, IChemicalTank<?, ?> tank) {
        setStack(stack, tank);
        for (IChemicalTank<?, ?> tankToClear : chemicalTank.getAllTanks()) {
            if (tank != tankToClear) {
                tankToClear.setEmpty();
            }
        }
    }

    public static class ChemicalTransferEvent extends TransferEvent<BoxedChemicalNetwork> {

        public final BoxedChemical transferType;

        public ChemicalTransferEvent(BoxedChemicalNetwork network, BoxedChemical type) {
            super(network);
            transferType = type;
        }
    }
}