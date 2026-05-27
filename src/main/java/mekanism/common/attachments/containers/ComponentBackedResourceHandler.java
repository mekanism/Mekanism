package mekanism.common.attachments.containers;

import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.attachments.containers.type.ResourceContainerType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

///Similar to [net.neoforged.neoforge.transfer.ItemAccessResourceHandler] in that it scales the results based on the amount of items in the backing attached access.
@NothingNullByDefault
public class ComponentBackedResourceHandler<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>>
      extends ComponentBackedHandler<LargeResourceStack<RESOURCE>, CONTAINER, AttachedResources<RESOURCE>, ResourceContainerType<RESOURCE, CONTAINER>>
      implements IMekanismResourceHandler<RESOURCE, CONTAINER> {

    public ComponentBackedResourceHandler(ResourceContainerType<RESOURCE, CONTAINER> containerType, ItemAccess attachedAccess, int totalSlots) {
        super(containerType, attachedAccess, totalSlots);
    }

    private int getPerItem(int amount) {
        return isAccessInvalid() ? 0 : amount / attachedAccess.getAmount();
    }

    @Override
    public RESOURCE getResource(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        if (isAccessInvalid()) {
            //If the backing item access is not valid, return that there is nothing stored
            return containerType().emptyResource();
        }
        return IMekanismResourceHandler.super.getResource(index);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getAmountAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        if (isAccessInvalid()) {
            //If the backing item access is not valid, return that there is nothing stored
            return 0;
        }
        //Scale the stored amount by how many items are in the backing access
        return MathUtils.multiplyClamped(attachedAccess.getAmount(), IMekanismResourceHandler.super.getAmountAsLong(index));
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(@Range(from = 0, to = Integer.MAX_VALUE) int index, RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount,
          TransactionContext transaction, AutomationType automationType) {
        int amountPerItem = getPerItem(amount);
        if (amountPerItem == 0) {
            return 0;
        }
        //Our component backed containers act on the full access, but without regard for the access' size. To get around that we wrap calls that would go
        // through it by only attempting to insert how much we can insert per item, and then multiplying our result back by how many items there are
        return attachedAccess.getAmount() * IMekanismResourceHandler.super.insert(index, resource, amountPerItem, transaction, automationType);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        int amountPerItem = getPerItem(amount);
        if (amountPerItem == 0) {
            return 0;
        }
        //Our component backed containers act on the full access, but without regard for the access' size. To get around that we wrap calls that would go
        // through it by only attempting to insert how much we can insert per item, and then multiplying our result back by how many items there are
        return attachedAccess.getAmount() * IMekanismResourceHandler.super.insert(resource, amountPerItem, transaction, automationType);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int extract(@Range(from = 0, to = Integer.MAX_VALUE) int index, RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount,
          TransactionContext transaction, AutomationType automationType) {
        int amountPerItem = getPerItem(amount);
        if (amountPerItem == 0) {
            return 0;
        }
        //Our component backed containers act on the full access, but without regard for the access' size. To get around that we wrap calls that would go
        // through it by only attempting to extract how much we can extract per item, and then multiplying our result back by how many items there are
        return attachedAccess.getAmount() * IMekanismResourceHandler.super.extract(index, resource, amountPerItem, transaction, automationType);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int extract(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        int amountPerItem = getPerItem(amount);
        if (amountPerItem == 0) {
            return 0;
        }
        //Our component backed containers act on the full access, but without regard for the access' size. To get around that we wrap calls that would go
        // through it by only attempting to extract how much we can extract per item, and then multiplying our result back by how many items there are
        return attachedAccess.getAmount() * IMekanismResourceHandler.super.extract(resource, amountPerItem, transaction, automationType);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public long getCapacityAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int index, RESOURCE resource) {
        if (isAccessInvalid()) {
            return 0;
        }
        //Scale the total capacity by how many items are in the backing access
        return MathUtils.multiplyClamped(attachedAccess.getAmount(), IMekanismResourceHandler.super.getCapacityAsLong(index, resource));
    }
}