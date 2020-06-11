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
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.content.transmitter.distribution.ChemicalHandlerTarget;
import mekanism.common.content.transmitter.distribution.ChemicalTransmitterSaveTarget;
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
 * A DynamicNetwork extension created specifically for the transfer of Chemicals.
 */
public abstract class ChemicalNetwork<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>,
      TANK extends IChemicalTank<CHEMICAL, STACK>, NETWORK extends ChemicalNetwork<CHEMICAL, STACK, HANDLER, TANK, NETWORK>>
      extends DynamicBufferedNetwork<HANDLER, NETWORK, STACK, TileEntityPressurizedTube> implements IMekanismChemicalHandler<CHEMICAL, STACK, TANK> {

    private final List<TANK> tanks;
    public final TANK tank;
    @Nonnull
    public CHEMICAL lastChemical = getEmptyStack().getType();
    private long prevTransferAmount;

    protected ChemicalNetwork() {
        tanks = Collections.singletonList(this.tank = createTank());
    }

    protected ChemicalNetwork(UUID networkID) {
        super(networkID);
        tanks = Collections.singletonList(this.tank = createTank());
    }

    protected ChemicalNetwork(Collection<NETWORK> networks) {
        this();
        for (NETWORK net : networks) {
            if (net != null) {
                adoptTransmittersAndAcceptorsFrom(net);
                net.deregister();
            }
        }
        register();
    }

    protected abstract TANK createTank();

    @Override
    protected void forceScaleUpdate() {
        if (!tank.isEmpty() && tank.getCapacity() > 0) {
            currentScale = (float) Math.min(1, tank.getStored() / (double) tank.getCapacity());
        } else {
            currentScale = 0;
        }
    }

    @Override
    public void adoptTransmittersAndAcceptorsFrom(NETWORK net) {
        float oldScale = currentScale;
        long oldCapacity = getCapacity();
        super.adoptTransmittersAndAcceptorsFrom(net);
        //Merge the chemical scales
        long capacity = getCapacity();
        currentScale = Math.min(1, capacity == 0 ? 0 : (currentScale * oldCapacity + net.currentScale * net.capacity) / capacity);
        if (isRemote()) {
            if (tank.isEmpty() && !net.tank.isEmpty()) {
                tank.setStack(net.getBuffer());
                net.tank.setEmpty();
            }
        } else {
            if (!net.tank.isEmpty()) {
                if (tank.isEmpty()) {
                    tank.setStack(net.getBuffer());
                } else if (tank.isTypeEqual(net.tank.getType())) {
                    long amount = net.tank.getStored();
                    MekanismUtils.logMismatchedStackSize(tank.growStack(amount, Action.EXECUTE), amount);
                }
                net.tank.setEmpty();
            }
            if (oldScale != currentScale) {
                //We want to make sure we update to the scale change
                needsUpdate = true;
            }
        }
    }

    @Nonnull
    @Override
    public STACK getBuffer() {
        return ChemicalUtil.copy(tank.getStack());
    }

    @Override
    public void absorbBuffer(TileEntityPressurizedTube transmitter) {
        STACK chemical = transmitter.releaseShare();
        if (!chemical.isEmpty()) {
            if (tank.isEmpty()) {
                tank.setStack(ChemicalUtil.copy(chemical));
            } else if (chemical.isTypeEqual(tank.getType())) {
                long amount = chemical.getAmount();
                MekanismUtils.logMismatchedStackSize(tank.growStack(amount, Action.EXECUTE), amount);
            }
        }
    }

    @Override
    public void clampBuffer() {
        if (!tank.isEmpty()) {
            long capacity = getCapacity();
            if (tank.getStored() > capacity) {
                MekanismUtils.logMismatchedStackSize(tank.setStackSize(capacity, Action.EXECUTE), capacity);
            }
        }
    }

    @Override
    protected void updateSaveShares(@Nullable TileEntityPressurizedTube triggerTransmitter) {
        super.updateSaveShares(triggerTransmitter);
        int size = transmittersSize();
        if (size > 0) {
            STACK chemical = tank.getStack();
            //Just pretend we are always accessing it from the north
            Direction side = Direction.NORTH;
            Set<ChemicalTransmitterSaveTarget<CHEMICAL, STACK>> saveTargets = new ObjectOpenHashSet<>(size);
            for (TileEntityPressurizedTube transmitter : transmitters) {
                ChemicalTransmitterSaveTarget<CHEMICAL, STACK> saveTarget = new ChemicalTransmitterSaveTarget<>(getEmptyStack(), chemical);
                saveTarget.addHandler(side, transmitter);
                saveTargets.add(saveTarget);
            }
            long sent = EmitUtils.sendToAcceptors(saveTargets, size, chemical.getAmount(), chemical);
            if (triggerTransmitter != null && sent < chemical.getAmount()) {
                disperse(triggerTransmitter, ChemicalUtil.copyWithAmount(chemical, chemical.getAmount() - sent));
            }
            for (ChemicalTransmitterSaveTarget<CHEMICAL, STACK> saveTarget : saveTargets) {
                saveTarget.saveShare(side);
            }
        }
    }

    @Override
    protected void onLastTransmitterRemoved(@Nonnull TileEntityPressurizedTube triggerTransmitter) {
        disperse(triggerTransmitter, tank.getStack());
    }

    protected void disperse(@Nonnull TileEntityPressurizedTube triggerTransmitter, STACK chemical) {
    }

    private long tickEmit(@Nonnull STACK stack) {
        Set<ChemicalHandlerTarget<CHEMICAL, STACK, HANDLER>> availableAcceptors = new ObjectOpenHashSet<>();
        int totalHandlers = 0;
        for (Entry<BlockPos, Map<Direction, LazyOptional<HANDLER>>> entry : acceptorCache.getAcceptorEntrySet()) {
            ChemicalHandlerTarget<CHEMICAL, STACK, HANDLER> target = new ChemicalHandlerTarget<>(stack);
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
            MinecraftForge.EVENT_BUS.post(getTransferEvent());
            needsUpdate = false;
        }
        if (tank.isEmpty()) {
            prevTransferAmount = 0;
        } else {
            prevTransferAmount = tickEmit(tank.getStack());
            MekanismUtils.logMismatchedStackSize(tank.shrinkStack(prevTransferAmount, Action.EXECUTE), prevTransferAmount);
        }
    }

    @Override
    protected float computeContentScale() {
        float scale = (float) (tank.getStored() / (double) tank.getCapacity());
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
        return TextComponentUtil.build(tank.getNeeded());
    }

    @Override
    public ITextComponent getStoredInfo() {
        if (tank.isEmpty()) {
            return MekanismLang.NONE.translate();
        }
        return MekanismLang.NETWORK_MB_STORED.translate(tank.getStack(), tank.getStored());
    }

    @Override
    public ITextComponent getFlowInfo() {
        return MekanismLang.NETWORK_MB_PER_TICK.translate(prevTransferAmount);
    }

    @Override
    public boolean isCompatibleWith(NETWORK other) {
        return super.isCompatibleWith(other) && (tank.isEmpty() || other.tank.isEmpty() || tank.isTypeEqual(other.tank.getType()));
    }

    @Override
    public boolean compatibleWithBuffer(@Nonnull STACK buffer) {
        return super.compatibleWithBuffer(buffer) && (tank.isEmpty() || buffer.isEmpty() || tank.isTypeEqual(buffer));
    }

    protected abstract ILangEntry getNetworkName();

    @Override
    public ITextComponent getTextComponent() {
        return MekanismLang.NETWORK_DESCRIPTION.translate(getNetworkName(), transmitters.size(), getAcceptorCount());
    }

    //TODO: Replace this with querying the transmission type string?
    protected abstract String getNetworkNameRaw();

    @Override
    public String toString() {
        return "[" + getNetworkNameRaw() + "] " + transmitters.size() + " transmitters, " + getAcceptorCount() + " acceptors.";
    }

    @Nonnull
    @Override
    public List<TANK> getChemicalTanks(@Nullable Direction side) {
        return tanks;
    }

    @Override
    public void onContentsChanged() {
        markDirty();
        CHEMICAL type = tank.getType();
        if (lastChemical != type) {
            //If the chemical type does not match update it, and mark that we need an update
            if (!type.isEmptyType()) {
                lastChemical = type;
            }
            needsUpdate = true;
        }
    }

    public void setLastChemical(@Nonnull CHEMICAL chemical) {
        if (chemical.isEmptyType()) {
            tank.setEmpty();
        } else {
            lastChemical = chemical;
            tank.setStack((STACK) lastChemical.getStack(1));
        }
    }

    protected abstract ChemicalTransferEvent<CHEMICAL, NETWORK> getTransferEvent();

    public static class ChemicalTransferEvent<CHEMICAL extends Chemical<CHEMICAL>, NETWORK extends ChemicalNetwork<CHEMICAL, ?, ?, ?, NETWORK>>
          extends TransferEvent<NETWORK> {

        public final CHEMICAL transferType;

        public ChemicalTransferEvent(NETWORK network, CHEMICAL type) {
            super(network);
            transferType = type;
        }
    }
}