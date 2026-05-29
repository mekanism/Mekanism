package mekanism.common.attachments.containers.energy;

import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismEnergyHandler;
import mekanism.api.math.MathUtils;
import mekanism.common.attachments.containers.ComponentBackedHandler;
import mekanism.common.attachments.containers.type.EnergyContainerType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

///Similar to [net.neoforged.neoforge.transfer.energy.ItemAccessEnergyHandler] in that it scales the results based on the amount of items in the backing attached access.
@NothingNullByDefault
public class ComponentBackedEnergyHandler extends ComponentBackedHandler<Long, IEnergyContainer, AttachedEnergy, EnergyContainerType> implements IMekanismEnergyHandler {

    public ComponentBackedEnergyHandler(EnergyContainerType containerType, ItemAccess attachedAccess,
          int totalContainers) {
        super(containerType, attachedAccess, totalContainers);
    }

    private int getPerItem(int amount) {
        return isAccessInvalid() ? 0 : amount / attachedAccess.getAmount();
    }

    @Override
    public IEnergyContainer getEnergyContainer() {
        return getContainer(0);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getAmountAsLong() {
        if (isAccessInvalid()) {
            //If the backing item access is not valid, return that there is nothing stored
            return 0;
        }
        //Scale the stored amount by how many items are in the backing access
        //Note: We get the contents directly rather than via super.getAmountAsLong to avoid skipping looking up the backing containers
        return MathUtils.multiplyClamped(attachedAccess.getAmount(), getContents(0));
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getCapacityAsLong() {
        if (isAccessInvalid()) {
            return 0;
        }
        //Scale the total capacity by how many items are in the backing access
        return MathUtils.multiplyClamped(attachedAccess.getAmount(), IMekanismEnergyHandler.super.getCapacityAsLong());
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        int amountPerItem = getPerItem(amount);
        if (amountPerItem == 0) {
            return 0;
        }
        //Our component backed containers act on the full access, but without regard for the access' size. To get around that we wrap calls that would go
        // through it by only attempting to insert how much we can insert per item, and then multiplying our result back by how many items there are
        return attachedAccess.getAmount() * IMekanismEnergyHandler.super.insert(amountPerItem, transaction, automationType);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int extract(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        int amountPerItem = getPerItem(amount);
        if (amountPerItem == 0) {
            return 0;
        }
        //Our component backed containers act on the full access, but without regard for the access' size. To get around that we wrap calls that would go
        // through it by only attempting to extract how much we can extract per item, and then multiplying our result back by how many items there are
        return attachedAccess.getAmount() * IMekanismEnergyHandler.super.extract(amountPerItem, transaction, automationType);
    }
}