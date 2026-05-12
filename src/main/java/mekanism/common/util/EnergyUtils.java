package mekanism.common.util;

import java.util.Collection;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.distribution.EnergyAcceptorTarget;
import mekanism.common.integration.energy.BlockEnergyCapabilityCache;
import mekanism.common.integration.energy.EnergyCompatUtils;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class EnergyUtils {//TODO - 26.1: Update docs

    private EnergyUtils() {
    }

    public static long emit(Collection<BlockEnergyCapabilityCache> targets, IEnergyContainer energyContainer, @Nullable TransactionContext transaction) {
        return emit(targets, energyContainer, energyContainer.getMaxEnergy(), transaction);
    }

    public static long emit(Collection<BlockEnergyCapabilityCache> targets, IEnergyContainer energyContainer, long maxOutput, @Nullable TransactionContext transaction) {
        if (!energyContainer.isEmpty() && maxOutput > 0L) {
            long energyToSend;
            try (Transaction simulation = Transaction.open(transaction)) {
                energyToSend = energyContainer.extract(maxOutput, simulation, AutomationType.INTERNAL);
                if (energyToSend == 0) {
                    //If we failed to extract from it, just exit early
                    return 0;
                }
            }
            try (Transaction subTransaction = Transaction.open(transaction)) {
                //We won't be able to extract the resource, just fail early
                long sent = emit(targets, energyToSend, subTransaction);
                if (energyContainer.extract(sent, subTransaction, AutomationType.INTERNAL) == sent) {
                    //Validate that we were able to extract the amount we sent. In theory this should always be true
                    subTransaction.commit();
                    return sent;
                }
            }
        }
        return 0;
    }

    /**
     * Emits energy from a central block by splitting the received stack among the sides given.
     *
     * @param targets      - the list of capabilities to output to
     * @param energyToSend - the energy to output
     *
     * @return the amount of energy emitted
     */
    public static long emit(Collection<BlockEnergyCapabilityCache> targets, long energyToSend, @Nullable TransactionContext transaction) {
        if (energyToSend <= 0 || targets.isEmpty()) {
            return 0;
        }
        EnergyAcceptorTarget target = null;
        for (BlockEnergyCapabilityCache capability : targets) {
            IStrictEnergyHandler handler = capability.getCapability();
            if (handler != null) {
                if (target == null) {
                    target = new EnergyAcceptorTarget(targets.size());
                }
                target.addHandler(handler);
            }
        }
        if (target == null) {
            return 0;
        }
        try (Transaction subTransaction = Transaction.open(transaction)) {
            long sent = EmitUtils.sendToAcceptors(target, energyToSend, EnergyNetwork.ENERGY, subTransaction);
            subTransaction.commit();
            return sent;
        }
    }

    /// @return amount transferred
    public static long chargeContents(IEnergyContainer energyContainer, ResourceHandler<ItemResource> handler, long amount, TransactionContext transaction) {
        //TODO - 26.1: Docs
        long charged = 0;
        for (int slot = 0, slots = handler.size(); slot < slots; slot++) {
            charged += charge(energyContainer, ItemAccess.forHandlerIndexStrict(handler, slot), amount - charged, transaction);
            if (charged == amount) {
                break;
            }
        }
        return charged;
    }

    /// @return amount transferred
    public static long charge(IEnergyContainer energyContainer, ItemAccess itemAccess, long amount, TransactionContext transaction) {//TODO - 26.1: Update docs
        return amount == 0 ? 0 : charge(energyContainer, EnergyCompatUtils.getStrictEnergyHandler(itemAccess), amount, transaction);
    }

    /// @return amount transferred
    public static long charge(IEnergyContainer energyContainer, @Nullable IStrictEnergyHandler handler, long amount, TransactionContext transaction) {//TODO - 26.1: Update docs
        if (amount == 0 || handler == null) {
            return 0;
        }
        long toExtract;
        try (Transaction simulation = Transaction.open(transaction)) {//TODO - 26.1: Re-evaluate this simulation
            toExtract = handler.extract(amount, simulation);
        }
        if (toExtract > 0) {
            //If we can actually insert any energy into the item
            try (Transaction subTransaction = Transaction.open(transaction)) {
                long extracted = energyContainer.extract(toExtract, subTransaction, AutomationType.MANUAL);
                long inserted = handler.insert(extracted, subTransaction);
                if (inserted == extracted) {
                    subTransaction.commit();
                    return inserted;
                }
            }
        }
        return 0;
    }
}