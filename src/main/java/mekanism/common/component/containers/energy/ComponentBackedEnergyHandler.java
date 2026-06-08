package mekanism.common.component.containers.energy;

import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.MathUtils;
import mekanism.common.component.containers.ComponentBackedHandler;
import mekanism.common.component.containers.type.EnergyContainerType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

///Similar to [net.neoforged.neoforge.transfer.energy.ItemAccessEnergyHandler] in that it scales the results based on the amount of items in the backing attached access.
@NothingNullByDefault
public class ComponentBackedEnergyHandler extends ComponentBackedHandler<IEnergyContainer, Long, EnergyContainerType> implements EnergyHandler {

    @Nullable
    private IEnergyContainer container;

    public ComponentBackedEnergyHandler(EnergyContainerType containerType, ItemAccess attachedAccess, boolean validateItemType) {
        super(containerType, attachedAccess, validateItemType);
    }

    private int getPerItem(int amount) {
        return isAccessInvalid() ? 0 : amount / attachedAccess.getAmount();
    }

    public IEnergyContainer getEnergyContainer() {
        if (container == null) {
            //Lazily initialize the energy container
            container = containerType().createContainer(attachedAccess, 0);
        }
        return container;
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
        return MathUtils.multiplyClamped(attachedAccess.getAmount(), getAttached());
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getCapacityAsLong() {
        if (isAccessInvalid()) {
            return 0;
        }
        //Scale the total capacity by how many items are in the backing access
        return MathUtils.multiplyClamped(attachedAccess.getAmount(), getEnergyContainer().getCapacityAsLong());
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        int amountPerItem = getPerItem(amount);
        if (amountPerItem == 0) {
            return 0;
        }
        //Our component backed containers act on the full access, but without regard for the access' size. To get around that we wrap calls that would go
        // through it by only attempting to insert how much we can insert per item, and then multiplying our result back by how many items there are
        return attachedAccess.getAmount() * getEnergyContainer().insert(amountPerItem, transaction, automationType);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
        return insert(amount, transaction, defaultAutomationType());
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int extract(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        int amountPerItem = getPerItem(amount);
        if (amountPerItem == 0) {
            return 0;
        }
        //Our component backed containers act on the full access, but without regard for the access' size. To get around that we wrap calls that would go
        // through it by only attempting to extract how much we can extract per item, and then multiplying our result back by how many items there are
        return attachedAccess.getAmount() * getEnergyContainer().extract(amountPerItem, transaction, automationType);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int extract(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
        return extract(amount, transaction, defaultAutomationType());
    }

    private AutomationType defaultAutomationType() {
        return AutomationType.EXTERNAL;
    }
}