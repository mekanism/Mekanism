package mekanism.common.content.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.MathUtils;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.content.network.distribution.EnergyTransmitterSaveTarget;
import mekanism.common.content.network.transmitter.UniversalCable;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.EnergyUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyNetwork extends DynamicBufferedNetwork<EnergyHandler, EnergyNetwork, Long, UniversalCable> {

    //for emit utils
    public static final Void ENERGY = null;

    public final VariableCapacityEnergyContainer energyContainer;
    private long prevTransferAmount = 0L;

    public EnergyNetwork(UUID networkID) {
        super(networkID);
        energyContainer = VariableCapacityEnergyContainer.create(this::getCapacity, ConstantPredicates.alwaysTrue(), ConstantPredicates.alwaysTrue(), this);
    }

    public EnergyNetwork(Collection<EnergyNetwork> networks) {
        this(UUID.randomUUID());
        adoptAllAndRegister(networks);
    }

    @Override
    protected void forceScaleUpdate() {
        if (!energyContainer.isEmpty() && getCapacity() > 0) {
            currentScale = (float) Math.min(1, energyContainer.getAmountAsLong() / (double) getCapacity());
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
            energyContainer.setEnergy(MathUtils.addClamped(energyContainer.getAmountAsLong(), net.getBuffer()), null);
            net.energyContainer.setEnergy(0, null);
        }
        return transmittersToUpdate;
    }

    @NotNull
    @Override
    public Long getBuffer() {
        return energyContainer.getAmountAsLong();
    }

    @Override
    public void absorbBuffer(UniversalCable transmitter) {
        long energy = transmitter.releaseShare();
        if (energy > 0) {
            energyContainer.setEnergy(energyContainer.getAmountAsLong() + energy, null);
        }
    }

    @Override
    public void clampBuffer() {
        ContainerType.ENERGY.clampContents(energyContainer, null);
    }

    @Override
    protected void updateSaveShares(@Nullable UniversalCable triggerTransmitter, TransactionContext transaction) {
        super.updateSaveShares(triggerTransmitter, transaction);
        if (!isEmpty()) {
            Collection<UniversalCable> transmitters = getTransmitters();
            EnergyTransmitterSaveTarget saveTarget = new EnergyTransmitterSaveTarget(transmitters.size());
            for (UniversalCable transmitter : transmitters) {
                saveTarget.addHandler(transmitter.startNewSaveShare(transaction));
            }
            EmitUtils.sendToAcceptors(saveTarget, energyContainer.getAmountAsLong(), ENERGY, transaction);
        }
    }

    protected void tickEmit() {
        if (energyContainer.isEmpty()) {
            prevTransferAmount = 0;
        } else {
            try (Transaction transaction = Transaction.openRoot()) {
                long current = energyContainer.getAmountAsLong();
                prevTransferAmount = tickEmit(current, transaction);
                energyContainer.setEnergy(current - prevTransferAmount, transaction);
                transaction.commit();
            }
        }
    }

    private long tickEmit(long amountToSend, TransactionContext transaction) {
        List<EnergyHandler> targets = null;
        for (Map<Direction, EnergyHandler> acceptors : acceptorCache.getAcceptorValues()) {
            for (EnergyHandler acceptor : acceptors.values()) {
                if (targets == null) {
                    //Lazily initialize the list of targets, which allows us to also skip attempting to start emitting
                    targets = new ArrayList<>();
                }
                //Note: We add the target regardless of if we can insert into it, as it skips the extra check,
                // and sendToAcceptors needs to calculate if the target can accept anyway
                targets.add(acceptor);
            }
        }
        return targets == null ? 0 : EnergyUtils.emit(targets, amountToSend, transaction);
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
        float scale = (float) MathUtils.divideToLevel(energyContainer.getAmountAsLong(), energyContainer.getCapacityAsLong());
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
        return EnergyDisplay.of(energyContainer.getNeededAsLong()).getTextComponent();
    }

    @Override
    public Component getStoredInfo() {
        return EnergyDisplay.of(energyContainer.getAmountAsLong()).getTextComponent();
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

    public static class EnergyTransferEvent extends TransferEvent<EnergyNetwork> {

        public EnergyTransferEvent(EnergyNetwork network) {
            super(network);
        }
    }
}