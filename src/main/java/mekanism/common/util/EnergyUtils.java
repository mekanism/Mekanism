package mekanism.common.util;

import com.google.common.primitives.Ints;
import java.util.Collection;
import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.component.containers.energy.ComponentBackedEnergyHandler;
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
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

public final class EnergyUtils {

    private EnergyUtils() {
    }

    @Nullable
    public static IEnergyContainer getEnergyContainer(@Nullable EnergyHandler handler) {
        if (handler instanceof IEnergyContainer container) {
            return container;
        } else if (handler instanceof ComponentBackedEnergyHandler energyHandler) {
            return energyHandler.getEnergyContainer();
        }
        //TODO - 26.2: Do we want a way to wrap energy handlers into an energy container for purposes of things like extractManual?
        //TODO - 26.2: We might want to change how we expose item caps in general so that they go via manual by default. Is there any reason we wouldn't want to?
        // It should be trivial to make the interface method for default type not private, and then override it on our items
        return null;
    }

    /// Emits energy from the given container split among the given collection of targets.
    ///
    /// @param targets     Capability caches to output to.
    /// @param container   Container to transfer energy out of.
    /// @param transaction The transaction that this operation is part of. This method will always use a nested transaction that will be committed. `null` can be passed
    /// to conveniently have this method open its own root transaction and perform the sending.
    ///
    /// @return the amount of energy transferred out of the container and emitted among the given targets.
    @Range(from = 0, to = Integer.MAX_VALUE)
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
    @Range(from = 0, to = Integer.MAX_VALUE)
    public static int emit(Collection<BlockCapabilityCache<EnergyHandler, @Nullable Direction>> targets, IEnergyContainer container,
          @Range(from = 0, to = Integer.MAX_VALUE) int maxOutput, @Nullable TransactionContext transaction) {
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
    @Range(from = 0, to = Long.MAX_VALUE)
    public static long emit(Collection<BlockCapabilityCache<EnergyHandler, @Nullable Direction>> targets, @Range(from = 0, to = Long.MAX_VALUE) long energyToSend,
          @Nullable TransactionContext transaction) {
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
    @Range(from = 0, to = Long.MAX_VALUE)
    public static long emit(List<EnergyHandler> targets, @Range(from = 0, to = Long.MAX_VALUE) long energyToSend, @Nullable TransactionContext transaction) {
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

    /// @param chargeFrom  Handler to take energy from to charge energy capabilities in `handler`.
    /// @param handler     Item handler to charge any contents that expose energy caps.
    /// @param amount      Amount of energy to use during charging.
    /// @param transaction The transaction that this operation is part of.
    ///
    /// @return amount transferred
    @Range(from = 0, to = Integer.MAX_VALUE)
    public static int chargeContents(EnergyHandler chargeFrom, ResourceHandler<ItemResource> handler, @Range(from = 0, to = Integer.MAX_VALUE) int amount,
          TransactionContext transaction) {
        return chargeContents(chargeFrom, handler, amount, transaction, -1);
    }

    /// @param chargeFrom  Handler to take energy from to charge energy capabilities in `handler`.
    /// @param handler     Item handler to charge any contents that expose energy caps.
    /// @param amount      Amount of energy to use during charging.
    /// @param transaction The transaction that this operation is part of.
    /// @param slotToSkip  Slot index that should be skipped, or `-1` to not skip any slots.
    ///
    /// @return amount transferred
    @Range(from = 0, to = Integer.MAX_VALUE)
    public static int chargeContents(EnergyHandler chargeFrom, ResourceHandler<ItemResource> handler, @Range(from = 0, to = Integer.MAX_VALUE) int amount,
          TransactionContext transaction, int slotToSkip) {
        int charged = 0;
        for (int slot = 0, slots = handler.size(); slot < slots; slot++) {
            if (slot == slotToSkip) {
                continue;
            }
            //Note: We don't use strict here as we want to allow the item to move to an empty slot if it has to in order to be charged
            charged += charge(chargeFrom, ItemAccess.forHandlerIndex(handler, slot), amount - charged, transaction);
            if (charged == amount) {
                break;
            }
        }
        return charged;
    }

    /// @param chargeFrom  Handler to take energy from to charge the energy capability exposed by the `itemAccess`.
    /// @param itemAccess  Item Access to query for an energy capability to charge.
    /// @param amount      Amount of energy to use during charging.
    /// @param transaction The transaction that this operation is part of.
    ///
    /// @return amount transferred
    @Range(from = 0, to = Integer.MAX_VALUE)
    public static int charge(EnergyHandler chargeFrom, ItemAccess itemAccess, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
        return amount == 0 ? 0 : charge(chargeFrom, Capabilities.ENERGY.getCapability(itemAccess), amount, transaction);
    }

    /// @param chargeFrom      Handler to take energy from to charge `handlerToCharge`.
    /// @param handlerToCharge Handler to charge.
    /// @param amount          Amount of energy to use during charging.
    /// @param transaction     The transaction that this operation is part of.
    ///
    /// @return amount transferred
    @Range(from = 0, to = Integer.MAX_VALUE)
    public static int charge(EnergyHandler chargeFrom, @Nullable EnergyHandler handlerToCharge, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
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
            int extracted = chargeFrom.extract(toTransfer, subTransaction);
            if (extracted > 0 && handlerToCharge.insert(extracted, subTransaction) == extracted) {
                subTransaction.commit();
                return extracted;
            }
            return 0;
        }
    }
}