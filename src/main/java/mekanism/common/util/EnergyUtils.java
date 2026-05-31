package mekanism.common.util;

import com.google.common.primitives.Ints;
import java.util.Collection;
import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.attachments.containers.energy.ComponentBackedEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.distribution.EnergyHandlerTarget;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class EnergyUtils {//TODO - 26.1: Update docs

    private EnergyUtils() {
    }

    @Nullable
    public static IEnergyContainer getEnergyContainer(@Nullable EnergyHandler handler) {
        if (handler instanceof IEnergyContainer container) {
            return container;
        } else if (handler instanceof ComponentBackedEnergyHandler energyHandler) {
            return energyHandler.getEnergyContainer();
        }
        //TODO - 26.1: Do we want a way to wrap energy handlers into an energy container for purposes of things like extractManual?
        return null;
    }

    /// Extracts up to the given amount of energy from the handler, using the [`manual automation type`][AutomationType#MANUAL] if it is a Mekanism handler.
    ///
    /// @param handler     to extract from.
    /// @param amount      The maximum amount of energy to extract. **Must be non-negative.**
    /// @param transaction The transaction that this operation is part of.
    ///
    /// @return The amount that was extracted. Between `0` (inclusive, nothing was extracted) and `amount` (inclusive, everything was extracted).
    ///
    /// @throws IllegalArgumentException If the amount is negative.
    public static int extractManual(EnergyHandler handler, int amount, TransactionContext transaction) {
        IEnergyContainer energyContainer = getEnergyContainer(handler);
        if (energyContainer != null) {
            return energyContainer.extract(amount, transaction, AutomationType.MANUAL);
        }
        return handler.extract(amount, transaction);
    }

    /// Inserts up to the given amount of energy into the handler, using the [`manual automation type`][AutomationType#MANUAL] if it is a Mekanism handler.
    ///
    /// @param handler     to insert into.
    /// @param amount      The maximum amount of energy to insert. **Must be non-negative.**
    /// @param transaction The transaction that this operation is part of.
    ///
    /// @return The amount that was inserted. Between `0` (inclusive, nothing was inserted) and `amount` (inclusive, everything was inserted).
    ///
    /// @throws IllegalArgumentException If the amount is negative.
    public static int insertManual(EnergyHandler handler, int amount, TransactionContext transaction) {
        IEnergyContainer energyContainer = getEnergyContainer(handler);
        if (energyContainer != null) {
            return energyContainer.insert(amount, transaction, AutomationType.MANUAL);
        }
        return handler.insert(amount, transaction);
    }

    /// Emits energy from the given container split among the given collection of targets.
    ///
    /// @param targets     Capability caches to output to.
    /// @param container   Container to transfer energy out of.
    /// @param transaction The transaction that this operation is part of. This method will always use a nested transaction that will be committed. `null` can be passed
    /// to conveniently have this method open its own root transaction and perform the sending.
    ///
    /// @return the amount of energy transferred out of the container and emitted among the given targets.
    public static int emit(Collection<BlockCapabilityCache<EnergyHandler, @Nullable Direction>> targets, IEnergyContainer container, @Nullable TransactionContext transaction) {
        return emit(targets, container, container.getAmountAsInt(), transaction);
    }

    /// Emits energy from the given container at the specified maximum transfer rate split among the given collection of targets.
    ///
    /// @param targets     Capability caches to output to.
    /// @param container   Container to transfer energy out of.
    /// @param maxOutput   Maximum transfer rate to transfer out of the container.
    /// @param transaction The transaction that this operation is part of. This method will always use a nested transaction that will be committed. `null` can be passed
    /// to conveniently have this method open its own root transaction and perform the sending.
    ///
    /// @return the amount of energy transferred out of the container and emitted among the given targets.
    public static int emit(Collection<BlockCapabilityCache<EnergyHandler, @Nullable Direction>> targets, IEnergyContainer container, int maxOutput, @Nullable TransactionContext transaction) {
        if (!container.isEmpty() && maxOutput > 0) {
            int energyToSend;
            try (Transaction simulation = Transaction.open(transaction)) {
                energyToSend = container.extract(maxOutput, simulation, AutomationType.INTERNAL);
                if (energyToSend == 0) {
                    //If we failed to extract from it, just exit early
                    return 0;
                }
            }
            try (Transaction subTransaction = Transaction.open(transaction)) {
                int sent = Ints.saturatedCast(emit(targets, energyToSend, subTransaction));
                if (sent > 0 && container.extract(sent, subTransaction, AutomationType.INTERNAL) == sent) {
                    //Validate that we were able to extract the amount we sent. In theory this should always be true
                    subTransaction.commit();
                    return sent;
                }
            }
        }
        return 0;
    }


    /// Emits and splits energy to a given collection of targets.
    ///
    /// @param targets      Targets to output to.
    /// @param energyToSend Amount of energy to split between the various targets.
    /// @param transaction  The transaction that this operation is part of. This method will always use a nested transaction that will be committed. `null` can be passed
    /// to conveniently have this method open its own root transaction and perform the sending.
    ///
    /// @return the amount of energy emitted
    public static long emit(Collection<BlockCapabilityCache<EnergyHandler, @Nullable Direction>> targets, long energyToSend, @Nullable TransactionContext transaction) {
        if (energyToSend <= 0 || targets.isEmpty()) {
            return 0;
        }
        return emit(EmitUtils.getHandlersFromCaches(targets), energyToSend, transaction);
    }

    /// Emits and splits energy to a given collection of targets.
    ///
    /// @param targets      Targets to output to.
    /// @param energyToSend Amount of energy to split between the various targets.
    /// @param transaction  The transaction that this operation is part of. This method will always use a nested transaction that will be committed. `null` can be passed
    /// to conveniently have this method open its own root transaction and perform the sending.
    ///
    /// @return the amount of energy emitted
    public static long emit(List<EnergyHandler> targets, long energyToSend, @Nullable TransactionContext transaction) {
        if (targets.isEmpty() || energyToSend <= 0) {
            return 0;
        } else if (targets.size() == 1) {
            //If we only have a single target, optimize out wrapping it in a resource handler target
            try (Transaction subTransaction = Transaction.open(transaction)) {
                int sent = targets.getFirst().insert(Ints.saturatedCast(energyToSend), subTransaction);
                subTransaction.commit();
                return sent;
            }
        }
        return EmitUtils.sendToAcceptors(new EnergyHandlerTarget(targets), energyToSend, EnergyNetwork.ENERGY, transaction);
    }

    /// @return amount transferred
    public static int chargeContents(IEnergyContainer energyContainer, ResourceHandler<ItemResource> handler, int amount, TransactionContext transaction) {
        //TODO - 26.1: Docs
        int charged = 0;
        for (int slot = 0, slots = handler.size(); slot < slots; slot++) {
            charged += charge(energyContainer, ItemAccess.forHandlerIndexStrict(handler, slot), amount - charged, transaction);
            if (charged == amount) {
                break;
            }
        }
        return charged;
    }

    /// @return amount transferred
    public static int charge(IEnergyContainer chargeFrom, ItemAccess itemAccess, int amount, TransactionContext transaction) {
        return amount == 0 ? 0 : charge(chargeFrom, Capabilities.ENERGY.getCapability(itemAccess), amount, transaction);
    }

    /// @return amount transferred
    public static int charge(IEnergyContainer chargeFrom, @Nullable EnergyHandler handlerToCharge, int amount, TransactionContext transaction) {
        return charge(chargeFrom, handlerToCharge, amount, transaction, (container, toExtract, tx) -> container.extract(toExtract, tx, AutomationType.MANUAL));
    }

    /// @return amount transferred
    public static int chargeContents(EnergyHandler chargeFrom, ResourceHandler<ItemResource> handler, int amount, TransactionContext transaction) {
        int charged = 0;
        for (int slot = 0, slots = handler.size(); slot < slots; slot++) {
            charged += charge(chargeFrom, ItemAccess.forHandlerIndexStrict(handler, slot), amount - charged, transaction);
            if (charged == amount) {
                break;
            }
        }
        return charged;
    }

    /// @return amount transferred
    public static int charge(EnergyHandler chargeFrom, ItemAccess itemAccess, int amount, TransactionContext transaction) {
        return amount == 0 ? 0 : charge(chargeFrom, Capabilities.ENERGY.getCapability(itemAccess), amount, transaction);
    }

    /// @return amount transferred
    public static int charge(EnergyHandler chargeFrom, @Nullable EnergyHandler handlerToCharge, int amount, TransactionContext transaction) {
        return charge(chargeFrom, handlerToCharge, amount, transaction, EnergyUtils::extractManual);
    }

    /// @return amount transferred
    private static <CONTAINER> int charge(CONTAINER chargeFrom, @Nullable EnergyHandler handlerToCharge, int amount, TransactionContext transaction,
          EnergyExtractor<CONTAINER> extractor) {
        if (amount == 0 || handlerToCharge == null) {
            return 0;
        }
        int toTransfer;
        try (Transaction simulation = Transaction.open(transaction)) {
            toTransfer = handlerToCharge.insert(amount, simulation);
            if (toTransfer == 0) {
                //Validate we can actually insert anything into the handler we are trying to charge
                return 0;
            }
        }
        try (Transaction subTransaction = Transaction.open(transaction)) {
            int extracted = extractor.extract(chargeFrom, toTransfer, subTransaction);
            int inserted = handlerToCharge.insert(extracted, subTransaction);
            if (inserted == extracted) {
                subTransaction.commit();
                return inserted;
            }
            return 0;
        }
    }

    @FunctionalInterface
    private interface EnergyExtractor<CONTAINER> {

        int extract(CONTAINER container, int amount, TransactionContext transaction);
    }
}