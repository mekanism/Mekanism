package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.component.containers.type.ResourceContainerType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ResourceRecipeData<RESOURCE extends Resource> implements RecipeUpgradeData<ResourceRecipeData<RESOURCE>> {

    protected final ResourceContainerType<RESOURCE, ?> containerType;
    protected final List<LargeResourceStack<RESOURCE>> contents;

    ResourceRecipeData(ResourceContainerType<RESOURCE, ?> containerType, List<LargeResourceStack<RESOURCE>> contents) {
        this.containerType = containerType;
        this.contents = contents;
    }

    @Nullable
    @Override
    public ResourceRecipeData<RESOURCE> merge(ResourceRecipeData<RESOURCE> other) {
        List<LargeResourceStack<RESOURCE>> allContainers = new ArrayList<>(this.contents);
        allContainers.addAll(other.contents);
        return createFromMerge(allContainers);
    }

    protected ResourceRecipeData<RESOURCE> createFromMerge(List<LargeResourceStack<RESOURCE>> containers) {
        return new ResourceRecipeData<>(this.containerType, containers);
    }

    @Override
    public boolean applyToStack(ItemAccess itemAccess, TransactionContext transaction) {
        if (this.contents.isEmpty()) {
            return true;
        }
        ResourceHandler<RESOURCE> outputHandler = containerType.getCapOrUnexposed(itemAccess);
        if (outputHandler == null) {
            //Something went wrong, fail
            return false;
        }
        for (LargeResourceStack<RESOURCE> content : this.contents) {
            if (!content.isEmpty() && insertInto(outputHandler, content.resource(), content.amount(), transaction) < content.amount()) {
                //If we have a remainder something failed so bail
                return false;
            }
        }
        return true;
    }

    private long insertInto(ResourceHandler<RESOURCE> handler, RESOURCE resource, final long amount, TransactionContext transaction) {
        if (handler instanceof IMekanismResourceHandler<RESOURCE, ?> mekHandler) {
            return insertInto(mekHandler.getContainers(), resource, amount, transaction);
        } else if (amount > Integer.MAX_VALUE) {
            //We don't know how to force insert into non mekanism handlers, so if we end up with trying to, just return that we can't
            return 0;
        }
        return ResourceHandlerUtil.insertStacking(handler, resource, (int) amount, transaction);
    }

    protected long insertInto(List<? extends IResourceContainer<RESOURCE>> containers, RESOURCE resource, final long amount, TransactionContext transaction) {
        if (containers.isEmpty()) {
            return 0;
        } else if (containers.size() == 1) {
            return insertInto(containers.getFirst(), resource, amount, transaction);
        }
        long inserted = 0;
        List<IResourceContainer<RESOURCE>> emptyContainers = new ArrayList<>();
        for (IResourceContainer<RESOURCE> container : containers) {
            if (container.isEmpty()) {
                //If the container is empty, add it to a list of containers that we will check afterward
                emptyContainers.add(container);
            } else {
                inserted += insertInto(container, resource, amount - inserted, transaction);
                if (inserted == amount) {
                    break;
                }
            }
        }
        for (IResourceContainer<RESOURCE> container : emptyContainers) {
            inserted += insertInto(container, resource, amount - inserted, transaction);
            if (inserted == amount) {
                return inserted;
            }
        }
        return inserted;
    }

    /**
     * Similar to {@link IResourceContainer#insert(Resource, int, TransactionContext, AutomationType)} except directly sets the contents ignoring any rate limits, and
     * supporting if the amount is greater than max long.
     */
    private long insertInto(IResourceContainer<RESOURCE> container, RESOURCE resource, final long amount, TransactionContext transaction) {
        //TODO - 26.1: Evaluate if any containers ever get passed to this that override insert that we potentially need bonus logic? In general as it is
        // just component backed slots, most likely the answer is no. Maybe if someone added a recipe to make creative bins would be the only case
        if (container.isEmpty() || container.resource().equals(resource)) {
            long capacity = container.capacityAsLong(resource);
            long stored = container.amountAsLong();
            long needed = capacity - stored;
            if (needed > 0 && container.isValidForInsertion(resource, AutomationType.MANUAL)) {
                long toAdd = Math.min(needed, amount);
                if (toAdd > 0) {
                    container.setContents(resource, stored + toAdd, transaction);
                    return toAdd;
                }
            }
        }
        return 0;
    }
}