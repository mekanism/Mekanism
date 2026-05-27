package mekanism.common.attachments.containers.energy;

import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.math.MathUtils;
import mekanism.common.attachments.containers.ComponentBackedHandler;
import mekanism.common.attachments.containers.type.EnergyContainerType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

///Similar to [net.neoforged.neoforge.transfer.energy.ItemAccessEnergyHandler] in that it scales the results based on the amount of items in the backing attached access.
@NothingNullByDefault
public class ComponentBackedEnergyHandler extends ComponentBackedHandler<Long, IEnergyContainer, AttachedEnergy, EnergyContainerType> implements IMekanismStrictEnergyHandler {

    public ComponentBackedEnergyHandler(EnergyContainerType containerType, ItemAccess attachedAccess,
          int totalContainers) {
        super(containerType, attachedAccess, totalContainers);
    }

    private long getPerItem(long amount) {
        return isAccessInvalid() ? 0 : amount / attachedAccess.getAmount();
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getAmountAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int container) {
        if (isAccessInvalid()) {
            //If the backing item access is not valid, return that there is nothing stored
            return 0;
        }
        //Scale the stored amount by how many items are in the backing access
        //Note: We get the contents directly rather than via super.getAmountAsLong to avoid skipping looking up the backing containers
        return MathUtils.multiplyClamped(attachedAccess.getAmount(), getContents(container));
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getCapacityAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        if (isAccessInvalid()) {
            return 0;
        }
        //Scale the total capacity by how many items are in the backing access
        return MathUtils.multiplyClamped(attachedAccess.getAmount(), IMekanismStrictEnergyHandler.super.getCapacityAsLong(index));
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long insert(@Range(from = 0, to = Integer.MAX_VALUE) int index, @Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction,
          AutomationType automationType) {
        long amountPerItem = getPerItem(amount);
        if (amountPerItem == 0) {
            return 0;
        }
        //Our component backed containers act on the full access, but without regard for the access' size. To get around that we wrap calls that would go
        // through it by only attempting to insert how much we can insert per item, and then multiplying our result back by how many items there are
        return attachedAccess.getAmount() * IMekanismStrictEnergyHandler.super.insert(index, amountPerItem, transaction, automationType);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long insert(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType) {
        long amountPerItem = getPerItem(amount);
        if (amountPerItem == 0) {
            return 0;
        }
        //Our component backed containers act on the full access, but without regard for the access' size. To get around that we wrap calls that would go
        // through it by only attempting to insert how much we can insert per item, and then multiplying our result back by how many items there are
        return attachedAccess.getAmount() * IMekanismStrictEnergyHandler.super.insert(amountPerItem, transaction, automationType);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long extract(int index, @Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType) {
        long amountPerItem = getPerItem(amount);
        if (amountPerItem == 0) {
            return 0;
        }
        //Our component backed containers act on the full access, but without regard for the access' size. To get around that we wrap calls that would go
        // through it by only attempting to extract how much we can extract per item, and then multiplying our result back by how many items there are
        return attachedAccess.getAmount() * IMekanismStrictEnergyHandler.super.extract(index, amountPerItem, transaction, automationType);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long extract(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType) {
        long amountPerItem = getPerItem(amount);
        if (amountPerItem == 0) {
            return 0;
        }
        //Our component backed containers act on the full access, but without regard for the access' size. To get around that we wrap calls that would go
        // through it by only attempting to extract how much we can extract per item, and then multiplying our result back by how many items there are
        return attachedAccess.getAmount() * IMekanismStrictEnergyHandler.super.extract(amountPerItem, transaction, automationType);
    }
}