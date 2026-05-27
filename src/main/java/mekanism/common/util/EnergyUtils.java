package mekanism.common.util;

import java.util.Collection;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
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

    public static long extractManual(IStrictEnergyHandler handler, long amount, TransactionContext transaction) {
        if (handler instanceof IMekanismStrictEnergyHandler mekHandler) {
            //Ensure droppers use the manual automation type
            return mekHandler.extract(amount, transaction, AutomationType.MANUAL);
        }
        return handler.extract(amount, transaction);
    }

    public static long insertManual(IStrictEnergyHandler handler, long amount, TransactionContext transaction) {
        if (handler instanceof IMekanismStrictEnergyHandler mekHandler) {
            //Ensure droppers use the manual automation type
            return mekHandler.insert(amount, transaction, AutomationType.MANUAL);
        }
        return handler.insert(amount, transaction);
    }

    public static long emit(Collection<BlockEnergyCapabilityCache> targets, IEnergyContainer energyContainer, @Nullable TransactionContext transaction) {
        return emit(targets, energyContainer, energyContainer.capacity(), transaction);
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
        return EmitUtils.sendToAcceptors(target, energyToSend, EnergyNetwork.ENERGY, transaction);
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
    public static long charge(IEnergyContainer chargeFrom, ItemAccess itemAccess, long amount, TransactionContext transaction) {
        return amount == 0 ? 0 : charge(chargeFrom, EnergyCompatUtils.getStrictEnergyHandler(itemAccess), amount, transaction);
    }

    /// @return amount transferred
    public static long charge(IEnergyContainer chargeFrom, @Nullable IStrictEnergyHandler handlerToCharge, long amount, TransactionContext transaction) {
        return charge(chargeFrom, handlerToCharge, amount, transaction, (container, toExtract, tx) -> container.extract(toExtract, tx, AutomationType.MANUAL));
    }

    /// @return amount transferred
    public static long chargeContents(IStrictEnergyHandler chargeFrom, ResourceHandler<ItemResource> handler, long amount, TransactionContext transaction) {
        long charged = 0;
        for (int slot = 0, slots = handler.size(); slot < slots; slot++) {
            charged += charge(chargeFrom, ItemAccess.forHandlerIndexStrict(handler, slot), amount - charged, transaction);
            if (charged == amount) {
                break;
            }
        }
        return charged;
    }

    /// @return amount transferred
    public static long charge(IStrictEnergyHandler chargeFrom, ItemAccess itemAccess, long amount, TransactionContext transaction) {
        return amount == 0 ? 0 : charge(chargeFrom, EnergyCompatUtils.getStrictEnergyHandler(itemAccess), amount, transaction);
    }

    /// @return amount transferred
    public static long charge(IStrictEnergyHandler chargeFrom, @Nullable IStrictEnergyHandler handlerToCharge, long amount, TransactionContext transaction) {
        return charge(chargeFrom, handlerToCharge, amount, transaction, EnergyUtils::extractManual);
    }

    /// @return amount transferred
    private static <CONTAINER> long charge(CONTAINER chargeFrom, @Nullable IStrictEnergyHandler handlerToCharge, long amount, TransactionContext transaction,
          EnergyExtractor<CONTAINER> extractor) {
        if (amount == 0 || handlerToCharge == null) {
            return 0;
        }
        long toTransfer;
        try (Transaction simulation = Transaction.open(transaction)) {
            toTransfer = handlerToCharge.insert(amount, simulation);
            if (toTransfer == 0) {
                //Validate we can actually insert anything into the handler we are trying to charge
                return 0;
            }
        }
        try (Transaction subTransaction = Transaction.open(transaction)) {
            long extracted = extractor.extract(chargeFrom, toTransfer, subTransaction);
            long inserted = handlerToCharge.insert(extracted, subTransaction);
            if (inserted == extracted) {
                subTransaction.commit();
                return inserted;
            }
            return 0;
        }
    }

    @FunctionalInterface
    private interface EnergyExtractor<CONTAINER> {

        long extract(CONTAINER container, long amount, TransactionContext transaction);
    }
}