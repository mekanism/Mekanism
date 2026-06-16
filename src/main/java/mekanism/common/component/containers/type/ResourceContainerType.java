package mekanism.common.component.containers.type;

import com.google.common.primitives.Ints;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.MathUtils;
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.component.containers.resource.AttachedResources;
import mekanism.common.component.containers.resource.ComponentBackedResourceHandler;
import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.redstone.Redstone;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public abstract class ResourceContainerType<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>>
      extends CapableContainerType<CONTAINER, AttachedResources<RESOURCE>, ResourceHandler<RESOURCE>> implements IListContainerType<LargeResourceStack<RESOURCE>, CONTAINER, AttachedResources<RESOURCE>> {

    private final LargeResourceStack.StackHelper<RESOURCE> stackHelper;

    protected ResourceContainerType(DeferredHolder<DataComponentType<?>, DataComponentType<AttachedResources<RESOURCE>>> component, String containerTag,
          MultiTypeCapability<ResourceHandler<RESOURCE>> capability, LargeResourceStack.StackHelper<RESOURCE> stackHelper) {
        super(component, containerTag, capability);
        this.stackHelper = stackHelper;
    }

    public RESOURCE emptyResource() {
        return stackHelper.empty().resource();
    }

    public LargeResourceStack.StackHelper<RESOURCE> stackHelper() {
        return stackHelper;
    }

    @Override
    protected boolean shouldAddAttachment(AttachedResources<RESOURCE> attached) {
        return !attached.isEmpty();
    }

    public abstract RESOURCE asResourceOrEmpty(Resource resource);

    @Nullable
    @Override
    protected ResourceHandler<RESOURCE> createHandler(ItemAccess itemAccess) {
        ItemResource resource = itemAccess.getResource();
        int count;
        AttachedResources<RESOURCE> attached = get(resource);
        if (attached == null || attached.isEmpty()) {
            //TODO: Are there any cases where the attached is empty, but we have containers?
            //TODO - 26.2: Re-evaluate this branch not just being zero and then returning null is the only difference for what getCapOrUnexposed
            // (which would use createHandlerIfData) previously did
            count = getContainerCount(resource.typeHolder().value());
        } else {
            //TODO - 1.21: Do we need to look it up in case the max size changed since we were last saved?
            count = attached.size();
        }
        if (count == 0) {
            return null;
        }
        //Note: All our resource handlers that we expose on items, currently validate the backing item type just like Neo's ItemAccessEnergyHandler does.
        // If it is desired to skip that check similar to ItemAccessResourceHandler, such as because we have a handler that changes between item instances
        // similar to a bucket, then we just need to adjust this to pass false in those cases to the handler.
        return new ComponentBackedResourceHandler<>(this, itemAccess, count, true);
    }

    @Override
    public void copyToContainers(List<CONTAINER> containers, AttachedResources<RESOURCE> attached) {
        List<LargeResourceStack<RESOURCE>> attachedContainers = attached.containers();
        int size = attachedContainers.size();
        if (size == containers.size()) {
            for (int i = 0; i < size; i++) {
                CONTAINER container = containers.get(i);
                LargeResourceStack<RESOURCE> stack = isFakeOutput(container) ? stackHelper().empty() : attachedContainers.get(i);
                container.setContents(stack, null);
            }
        }
    }

    @Nullable
    @Override
    public AttachedResources<RESOURCE> attachedCopyOf(List<CONTAINER> containers) {
        boolean hasNonEmpty = false;
        List<LargeResourceStack<RESOURCE>> stacks = new ArrayList<>(containers.size());
        for (CONTAINER container : containers) {
            LargeResourceStack<RESOURCE> stack = isFakeOutput(container) ? stackHelper.empty() : container.asStack();
            stacks.add(stack);
            if (!stack.isEmpty()) {
                hasNonEmpty = true;
            }
        }
        return hasNonEmpty ? new AttachedResources<>(stacks) : null;
    }

    protected boolean isVariableSize(IResourceContainer<RESOURCE> container) {
        return false;
    }

    protected boolean isFakeOutput(CONTAINER container) {
        return false;
    }

    @Override
    public void copy(CONTAINER from, CONTAINER to, @Nullable TransactionContext transaction) {
        to.copyContents(from, transaction);
    }

    /// @param toFill      Item type to try and fill.
    /// @param resource    Resource to fill the item with.
    /// @param transaction The transaction that this operation is part of. May be `null`.
    ///
    /// @return Stack representation of the item access once it has been filled with the given resource.
    public ItemStack getFilledVariant(Holder<Item> toFill, RESOURCE resource, @Nullable TransactionContext transaction) {
        ItemAccess itemAccess = ItemAccessUtils.sideEffectFreeAccess(ItemResource.of(toFill));
        return getFilledVariant(itemAccess, resource, transaction);
    }

    /// @param itemAccess  Item access to try and fill the represented item.
    /// @param resource    Resource to fill the item with.
    /// @param transaction The transaction that this operation is part of. May be `null`.
    ///
    /// @return Stack representation of the item access once it has been filled with the given resource.
    public ItemStack getFilledVariant(ItemAccess itemAccess, RESOURCE resource, @Nullable TransactionContext transaction) {
        if (capability.getCapability(itemAccess) instanceof IMekanismResourceHandler<RESOURCE, ?> handler) {
            //Note: Just directly interact with the containers as we want to change the entire access and don't care about splitting between multiple items
            for (IResourceContainer<RESOURCE> container : handler.getContainers()) {
                container.setContents(resource, container.capacityAsLong(resource), transaction);
            }
        }
        //The item is now filled return it for convenience
        return ItemAccessUtils.asStack(itemAccess);
    }

    /// Gets the resource stored in an item's container by checking the attachment. This is for cases when we may not actually have a resource handler provided as a
    /// capability from our item, but it may have stored data in its container from when it was a block
    ///
    /// @implNote The returned stack is not scaled by the size of the passed item access.
    public LargeResourceStack<RESOURCE> getStoredContentsFromAttachment(ItemAccess itemAccess) {
        List<LargeResourceStack<RESOURCE>> containers = getAttachedContents(itemAccess.getResource());
        return switch (containers.size()) {
            case 0 -> stackHelper().empty();
            case 1 -> containers.getFirst();
            default -> {
                LargeResourceStack<RESOURCE> stored = stackHelper().empty();
                for (LargeResourceStack<RESOURCE> container : containers) {
                    if (container.isEmpty()) {
                        continue;
                    }
                    if (stored.isEmpty()) {
                        stored = container;
                    } else if (stored.matches(container.resource())) {
                        stored = stored.grow(container.amount(), true);
                        if (stored.amount() == Long.MAX_VALUE) {
                            break;
                        }
                    }
                    //Note: If we have multiple tanks that have different types stored we only return the first type
                }
                yield stored;
            }
        };
    }

    /// Gets the FIRST resource stored in an item's container by checking the attachment. This is for cases when we may not actually have a resource handler provided as a
    /// capability from our item, but it may have stored data in its container from when it was a block. Do NOT modify the result
    ///
    /// @return the first found resource FOR DISPLAY
    public RESOURCE getFirstResourceFromAttachment(ItemAccess itemAccess) {
        for (LargeResourceStack<RESOURCE> container : getAttachedContents(itemAccess.getResource())) {
            if (!container.isEmpty()) {
                return container.resource();
            }
        }
        return emptyResource();
    }

    /// Dumps the contents of a container into the level, and then clears the container.
    ///
    /// @param level       The level on which to act.
    /// @param pos         Location in the level that the container was dumped.
    /// @param itemAccess  The item access that may expose a capability of this container's type that then will have the contents dumped.
    /// @param transaction The transaction that this operation is part of. May be `null`.
    public void tryDumpContents(Level level, BlockPos pos, ItemAccess itemAccess, @Nullable TransactionContext transaction) {
        if (capability.getCapability(itemAccess) instanceof IMekanismResourceHandler<RESOURCE, ?> handler) {
            for (IResourceContainer<RESOURCE> container : handler.getContainers()) {
                dumpContents(level, pos, container, transaction);
            }
        }
    }

    /// Helper to clear the contents of a container.
    ///
    /// @param container   The container to clear
    /// @param transaction The transaction that this operation is part of. May be `null`.
    public void clearContents(IResourceContainer<RESOURCE> container, @Nullable TransactionContext transaction) {
        container.setContents(stackHelper.empty(), transaction);
    }

    /// Dumps the contents of a container into the level, and then clears the container.
    ///
    /// @param level       The level on which to act.
    /// @param pos         Location in the level that the container was dumped.
    /// @param container   The container to dump the contents of. This is effectively just clears it if there are no side effects for dumping the stored resource.
    /// @param transaction The transaction that this operation is part of. May be `null`.
    public void dumpContents(Level level, BlockPos pos, IResourceContainer<RESOURCE> container, @Nullable TransactionContext transaction) {
        clearContents(container, transaction);
    }

    /// Clamps the contents of the container to its capacity.
    ///
    /// @param container   Container to clamp.
    /// @param transaction The transaction that this operation is part of. May be `null`.
    ///
    /// @implNote If the capacity is zero, and the container is of variable size, this will skip clamping the container.
    public void clampContents(IResourceContainer<RESOURCE> container, @Nullable TransactionContext transaction) {
        RESOURCE resource = container.resource();
        if (!resource.isEmpty()) {
            long capacity = container.capacityAsLong(resource);
            if (capacity == 0 && isVariableSize(container)) {
                //Our capacity should never actually be zero, and given we fake it being zero until we finish building the network,
                // we need to override this method to bypass the upper limit check when our upper limit is zero
                return;
            }
            if (container.amountAsLong() > capacity) {
                container.setContents(resource, capacity, transaction);
            }
        }
    }

    /// Merges the contents from two sets of containers into the first set, and keeps track of any rejected contents.
    ///
    /// @param orig        List of containers to merge `toAdd`'s contents into
    /// @param toAdd       List of containers to merge with the corresponding original container.
    /// @param rejects     Map to add rejected resources to, and how much of each resource was rejected.
    /// @param transaction The transaction that this operation is part of. The changes to `rejects` will not be rolled back if this transaction is not committed.
    ///
    /// @implNote `toAdd` does not get modified by this method.
    public void merge(List<CONTAINER> orig, List<CONTAINER> toAdd, Object2LongMap<RESOURCE> rejects, TransactionContext transaction) {
        StorageUtils.validateSizeMatches(orig, toAdd, "container");
        for (int container = 0, size = toAdd.size(); container < size; container++) {
            CONTAINER toAddContainer = toAdd.get(container);
            if (!toAddContainer.isEmpty()) {
                RESOURCE toAddResource = toAddContainer.resource();
                long toAddAmount = toAddContainer.amountAsLong();
                CONTAINER origContainer = orig.get(container);
                //TODO - 26.2: Validate all callers have this work with the given automation type
                //TODO - 26.2: Should we change this to bypass all rate limits in case we add a multiblock that has them at a later date.
                // That might be problematic to do, as the container merging uses dummy containers, that have no limits
                //TODO - 26.2: If toAddAmount is greater than max int how do we want to handle it
                int added = origContainer.insert(toAddResource, Ints.saturatedCast(toAddAmount), transaction, AutomationType.INTERNAL);
                if (added < toAddAmount) {
                    //Add any remainder to the rejects
                    rejects.mergeLong(toAddResource, toAddAmount - added, Long::sum);
                }
            }
        }
    }

    /// Calculates the redstone signal strength based on the given containers' content. This value is between 0 and 15.
    ///
    /// This method is based on [AbstractContainerMenu#getRedstoneSignalFromContainer(Container)].
    ///
    /// @param containers the containers to calculate the signal from
    ///
    /// @return the redstone signal strength
    ///
    /// @implNote Unlike the method in [ResourceHandlerUtil], this method follows how [AbstractContainerMenu] does it, and does not ignore empty containers, it only
    /// ignores containers that have a zero capacity.
    /// @see ResourceHandlerUtil#getRedstoneSignalFromResourceHandler(ResourceHandler)
    public int getRedstoneSignalFromContainers(List<? extends IResourceContainer<RESOURCE>> containers) {
        float proportion = 0.0F;
        int sampleCount = 0; // Number of samples in proportion
        for (IResourceContainer<RESOURCE> container : containers) {
            long capacity = container.capacityAsLong(container.resource());
            if (capacity > 0) {
                long containerFill = container.amountAsLong();
                if (containerFill > 0) {
                    //Clamp to 1 to avoid overfilled slots increasing the signal strength beyond 15
                    proportion += Math.min(1, (float) containerFill / capacity);
                }
                sampleCount++;
            }
        }
        if (sampleCount == 0) {
            return Redstone.SIGNAL_NONE;
        }
        proportion /= sampleCount;
        return Mth.lerpDiscrete(proportion, Redstone.SIGNAL_NONE, Redstone.SIGNAL_MAX);
    }

    /// Calculates the redstone signal strength based on the given resource container's content. This value is between 0 and 15.
    ///
    /// This method is based on [AbstractContainerMenu#getRedstoneSignalFromContainer(Container)].
    ///
    /// @param container Container to calculate the signal from
    ///
    /// @return the redstone signal strength
    ///
    /// @see ResourceHandlerUtil#getRedstoneSignalFromResourceHandler(ResourceHandler)
    public int getRedstoneSignalFromContainer(IResourceContainer<RESOURCE> container) {
        long containerFill = container.amountAsLong();
        if (containerFill > 0) {
            long capacity = container.capacityAsLong(container.resource());
            if (capacity > 0) {
                //Clamp to 1 to avoid overfilled slots increasing the signal strength beyond 15
                float proportion = Math.min(1, (float) containerFill / capacity);
                return Mth.lerpDiscrete(proportion, Redstone.SIGNAL_NONE, Redstone.SIGNAL_MAX);
            }
        }
        return Redstone.SIGNAL_NONE;
    }

    /// Divides amount stored in the container by the capacity of the container and returns the result as a double.
    ///
    /// @param container The container to calculate the level of.
    ///
    /// @return A double representing the value of dividing the amount stored by the capacity, or `1` if the capacity is `0`, or the stored amount is larger than the
    /// capacity.
    ///
    /// @implNote This caps the returned value at `1`
    public double divideToLevel(IResourceContainer<RESOURCE> container) {
        return MathUtils.divideToLevel(container.amountAsLong(), container.capacityAsLong(container.resource()));
    }

    /// Checks if all the given containers are currently empty.
    ///
    /// @param containers Containers to check.
    ///
    /// @return `true` if all the containers are empty.
    ///
    /// @see ResourceHandlerUtil#isEmpty(ResourceHandler)
    public boolean areContainersEmpty(List<? extends IResourceContainer<RESOURCE>> containers) {
        for (IResourceContainer<RESOURCE> container : containers) {
            if (!container.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /// Helper to first try inserting ignoring empty containers, and then insert not ignoring empty containers
    ///
    /// @param handler        Containers to insert into
    /// @param resource       Type of resource to insert.
    /// @param amount         Amount of the resource to insert.
    /// @param transaction    The transaction that this operation is part of.
    /// @param automationType The method to interact with containers using if a Mekanism handler is found
    ///
    /// @return Amount inserted
    ///
    /// @see net.neoforged.neoforge.transfer.ResourceHandlerUtil#insertStacking(ResourceHandler, Resource, int, TransactionContext)
    public int insertInto(ResourceHandler<RESOURCE> handler, RESOURCE resource, final int amount, TransactionContext transaction, AutomationType automationType) {
        if (handler instanceof IMekanismResourceHandler<RESOURCE, ?> mekHandler) {
            return mekHandler.insert(resource, amount, transaction, automationType);
        }
        return ResourceHandlerUtil.insertStacking(handler, resource, amount, transaction);
    }

    /// Used to handle the common case of a player holding a container item and right-clicking on a resource handler. First it tries to fill the item from the handler, if
    /// that action fails then it tries to drain the item into the handler. Automatically updates the item in the player's hand and stashes any extra items created.
    ///
    /// @param player      The player doing the interaction between the item and resource handler.
    /// @param hand        The player's hand that is holding an item that should interact with the resource handler.
    /// @param pos         The position at which to send game events and play sounds. If `null`, the player's position will be used.
    /// @param handler     The resource handler.
    /// @param transaction The transaction context for the operation. Passing in `null` will open a root transaction, whereas passing in a transaction will allow you to
    /// make the final decision to commit based on the results of this method.
    ///
    /// @return true if the interaction succeeded, false otherwise.
    ///
    /// @see net.neoforged.neoforge.transfer.fluid.FluidUtil#interactWithFluidHandler(Player, InteractionHand, BlockPos, ResourceHandler, TransactionContext)
    public boolean interactWithHandler(Player player, InteractionHand hand, @Nullable BlockPos pos, ResourceHandler<RESOURCE> handler, @Nullable TransactionContext transaction) {
        //TODO - 26.2: Should we add a variant of this that allows following a container edit mode?
        // That way it can be set to force fill/drain the item instead of doing its best guess? I suspect this would be a nice QoL change
        //TODO - 26.2: Do we want chemical handler interactions to fire game events or make sounds?
        ItemAccess itemAccess = ItemAccess.forPlayerInteraction(player, hand).oneByOne();
        ResourceHandler<RESOURCE> handHandler = capability().getCapability(itemAccess);
        if (handHandler == null) {
            return false;
        }
        return ResourceHandlerUtil.moveFirst(handler, handHandler, ConstantPredicates.alwaysTrue(), Integer.MAX_VALUE, transaction) != null ||
               ResourceHandlerUtil.moveFirst(handHandler, handler, ConstantPredicates.alwaysTrue(), Integer.MAX_VALUE, transaction) != null;
    }

    /// Gets the color that represents the first resource stored on the item instance for use in "durability" bars.
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> int getRGBDurabilityForDisplay(ITEM instance) {
        return getRGBDurabilityForDisplay(ItemAccessUtils.sideEffectFreeAccess(instance));
    }

    /// Gets the color that represents the first resource stored on the item access for use in "durability" bars.
    public int getRGBDurabilityForDisplay(ItemAccess itemAccess) {
        return getRGBDurabilityForDisplay(getFirstResourceFromAttachment(itemAccess));
    }

    /// Gets the color that represents the given resource for use in "durability" bars.
    public int getRGBDurabilityForDisplay(RESOURCE resource) {
        return CommonColors.WHITE;
    }
}