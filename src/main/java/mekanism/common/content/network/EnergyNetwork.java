package mekanism.common.content.network;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.api.IContentsListener;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.MathUtils;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.content.network.distribution.EnergyAcceptorTarget;
import mekanism.common.content.network.distribution.EnergyTransmitterSaveTarget;
import mekanism.common.content.network.transmitter.UniversalCable;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyNetwork extends DynamicBufferedNetwork<IStrictEnergyHandler, EnergyNetwork, Long, UniversalCable> implements IContentsListener {

    //for emit utils
    public static final Void ENERGY = null;

    private final List<IEnergyContainer> energyContainers;
    public final VariableCapacityEnergyContainer energyContainer;
    private long prevTransferAmount = 0L;

    public EnergyNetwork(UUID networkID) {
        super(networkID);
        energyContainer = VariableCapacityEnergyContainer.create(this::getCapacity, ConstantPredicates.alwaysTrue(), ConstantPredicates.alwaysTrue(), this);
        energyContainers = Collections.singletonList(energyContainer);
    }

    public EnergyNetwork(Collection<EnergyNetwork> networks) {
        this(UUID.randomUUID());
        adoptAllAndRegister(networks);
    }

    @Override
    protected void forceScaleUpdate() {
        if (!energyContainer.isEmpty() && energyContainer.capacity() != 0L) {
            currentScale = (float) Math.min(1, ((double) energyContainer.energy() / energyContainer.capacity()));
        } else {
            currentScale = 0;
        }
    }

    @Override
    public List<UniversalCable> adoptTransmittersAndAcceptorsFrom(EnergyNetwork net) {
        long oldCapacity = getCapacity();
        List<UniversalCable> transmittersToUpdate = super.adoptTransmittersAndAcceptorsFrom(net);
        //Merge the energy scales
        long ourScale = currentScale == 0 ? 0L : (long) (oldCapacity * currentScale);
        long theirScale = net.currentScale == 0 ? 0L : (long) (net.getCapacity() * net.currentScale);
        long capacity = getCapacity();
        currentScale = (float) Math.min(1, capacity == 0L ? 0D : (ourScale + theirScale) / (double) capacity);
        if (!isRemote() && !net.energyContainer.isEmpty()) {
            energyContainer.setEnergy(MathUtils.addClamped(energyContainer.energy(), net.getBuffer()));
            net.energyContainer.setEnergy(0);
        }
        return transmittersToUpdate;
    }

    @NotNull
    @Override
    public Long getBuffer() {
        return energyContainer.energy();
    }

    @Override
    public void absorbBuffer(UniversalCable transmitter) {
        long energy = transmitter.releaseShare();
        if (energy != 0L) {
            energyContainer.setEnergy(energyContainer.energy() + energy);
        }
    }

    @Override
    public void clampBuffer() {
        if (!energyContainer.isEmpty()) {
            long capacity = getCapacity();
            if (energyContainer.energy() > capacity) {
                energyContainer.setEnergy(capacity);
            }
        }
    }

    @Override
    protected void updateSaveShares(@Nullable UniversalCable triggerTransmitter, TransactionContext transaction) {
        super.updateSaveShares(triggerTransmitter, transaction);
        if (!isEmpty()) {
            EnergyTransmitterSaveTarget saveTarget = new EnergyTransmitterSaveTarget(getTransmitters());
            long energy = energyContainer.energy();
            EmitUtils.sendToAcceptors(saveTarget, energy, ENERGY, transaction);
            saveTarget.save();
        }
    }

    protected void tickEmit() {
        if (energyContainer.isEmpty()) {
            prevTransferAmount = 0;
        } else {
            try (Transaction transaction = Transaction.openRoot()) {
                long current = energyContainer.energy();
                prevTransferAmount = tickEmit(current, transaction);
                //TODO - 26.1: Evaluate this
                energyContainer.setEnergy(current - prevTransferAmount);
                transaction.commit();
            }
        }
    }

    private long tickEmit(long energyToSend, TransactionContext transaction) {
        Collection<Map<Direction, IStrictEnergyHandler>> acceptorValues = acceptorCache.getAcceptorValues();
        EnergyAcceptorTarget target = null;
        for (Map<Direction, IStrictEnergyHandler> acceptors : acceptorValues) {
            for (IStrictEnergyHandler acceptor : acceptors.values()) {
                try (Transaction simulation = Transaction.open(transaction)) {
                    if (acceptor.insert(energyToSend, simulation) > 0) {
                        if (target == null) {
                            //Lazily initialize the target, which allows us to also skip attempting to start emitting
                            target = new EnergyAcceptorTarget(acceptorValues.size() * 2);
                        }
                        target.addHandler(acceptor);
                    }
                }
            }
        }
        return EmitUtils.sendToAcceptors(target, energyToSend, ENERGY, transaction);
    }

    @Override
    public String toString() {
        return "[EnergyNetwork] " + transmittersSize() + " transmitters, " + getAcceptorCount() + " acceptors.";
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (needsUpdate) {
            NeoForge.EVENT_BUS.post(new EnergyTransferEvent(this));
            needsUpdate = false;
        }
        tickEmit();
    }

    @Override
    protected float computeContentScale() {
        float scale = (float) MathUtils.divideToLevel(energyContainer.energy(), energyContainer.capacity());
        float ret = Math.max(currentScale, scale);
        if (prevTransferAmount != 0 && ret < 1) {
            ret = Math.min(1, ret + 0.02F);
        } else if (prevTransferAmount == 0L && ret > 0) {
            ret = Math.max(scale, ret - 0.02F);
        }
        return ret;
    }

    @Override
    public Component getNeededInfo() {
        return EnergyDisplay.of(energyContainer.getNeeded()).getTextComponent();
    }

    @Override
    public Component getStoredInfo() {
        return EnergyDisplay.of(energyContainer.energy()).getTextComponent();
    }

    @Override
    public Component getFlowInfo() {
        return MekanismLang.GENERIC_PER_TICK.translate(EnergyDisplay.of(prevTransferAmount));
    }

    @NotNull
    @Override
    public Component getTextComponent() {
        return MekanismLang.NETWORK_DESCRIPTION.translate(MekanismLang.ENERGY_NETWORK, transmittersSize(), getAcceptorCount());
    }

    @NotNull
    public List<IEnergyContainer> getEnergyContainers() {
        return energyContainers;
    }

    @Override
    public void onContentsChanged() {
        markDirty();
    }

    public static class EnergyTransferEvent extends TransferEvent<EnergyNetwork> {

        public EnergyTransferEvent(EnergyNetwork network) {
            super(network);
        }
    }
}