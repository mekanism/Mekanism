package mekanism.common.util;

import java.util.Collection;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.attachments.containers.energy.ComponentBackedEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.distribution.EnergyAcceptorTarget;
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
        return null;
    }

    public static int extractManual(EnergyHandler handler, int amount, TransactionContext transaction) {
        IEnergyContainer energyContainer = getEnergyContainer(handler);
        if (energyContainer != null) {
            return energyContainer.extract(amount, transaction, AutomationType.MANUAL);
        }
        return handler.extract(amount, transaction);
    }

    public static int insertManual(EnergyHandler handler, int amount, TransactionContext transaction) {
        IEnergyContainer energyContainer = getEnergyContainer(handler);
        if (energyContainer != null) {
            return energyContainer.insert(amount, transaction, AutomationType.MANUAL);
        }
        return handler.insert(amount, transaction);
    }

    public static int emit(Collection<BlockCapabilityCache<EnergyHandler, @Nullable Direction>> targets, IEnergyContainer energyContainer, @Nullable TransactionContext transaction) {
        return emit(targets, energyContainer, energyContainer.getAmountAsInt(), transaction);
    }

    public static int emit(Collection<BlockCapabilityCache<EnergyHandler, @Nullable Direction>> targets, IEnergyContainer energyContainer, int maxOutput, @Nullable TransactionContext transaction) {
        if (!energyContainer.isEmpty() && maxOutput > 0) {
            int energyToSend;
            try (Transaction simulation = Transaction.open(transaction)) {
                energyToSend = energyContainer.extract(maxOutput, simulation, AutomationType.INTERNAL);
                if (energyToSend == 0) {
                    //If we failed to extract from it, just exit early
                    return 0;
                }
            }
            try (Transaction subTransaction = Transaction.open(transaction)) {
                //We won't be able to extract the resource, just fail early
                int sent = emit(targets, energyToSend, subTransaction);
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
    public static int emit(Collection<BlockCapabilityCache<EnergyHandler, @Nullable Direction>> targets, int energyToSend, @Nullable TransactionContext transaction) {
        if (energyToSend <= 0 || targets.isEmpty()) {
            return 0;
        }
        EnergyAcceptorTarget target = null;
        for (BlockCapabilityCache<EnergyHandler, @Nullable Direction> capability : targets) {
            EnergyHandler handler = capability.getCapability();
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