package mekanism.common.attachments.containers.type;

import com.google.common.primitives.Ints;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.attachments.containers.resource.AttachedResources;
import mekanism.common.attachments.containers.resource.ComponentBackedResourceHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.inventory.slot.CraftingWindowOutputInventorySlot;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.Mth;
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public class ResourceContainerType<RESOURCE extends @NonNull Resource, CONTAINER extends IResourceContainer<RESOURCE>>
      extends CapableContainerType<CONTAINER, AttachedResources<RESOURCE>, ResourceHandler<RESOURCE>> implements IListContainerType<LargeResourceStack<RESOURCE>, CONTAINER, AttachedResources<RESOURCE>> {

    private final Function<TileEntityMekanism, List<CONTAINER>> containersFromTile;
    private final LargeResourceStack.StackHelper<RESOURCE> stackHelper;
    private final Predicate<TileEntityMekanism> canHandle;
    private final Predicate<Resource> isResourceType;

    protected ResourceContainerType(DeferredHolder<DataComponentType<?>, DataComponentType<AttachedResources<RESOURCE>>> component, String containerTag,
          MultiTypeCapability<ResourceHandler<RESOURCE>> capability, Function<TileEntityMekanism, List<CONTAINER>> containersFromTile, Predicate<TileEntityMekanism> canHandle,
          LargeResourceStack.StackHelper<RESOURCE> stackHelper, Predicate<Resource> isResourceType) {
        super(component, containerTag, capability, AttachedResources.empty());
        this.containersFromTile = containersFromTile;
        this.isResourceType = isResourceType;
        this.stackHelper = stackHelper;
        this.canHandle = canHandle;
    }

    public RESOURCE emptyResource() {
        return stackHelper.empty().resource();
    }

    public LargeResourceStack.StackHelper<RESOURCE> stackHelper() {
        return stackHelper;
    }

    @Override
    public List<CONTAINER> getContainers(TileEntityMekanism tile) {
        return containersFromTile.apply(tile);
    }

    @Override
    public boolean canHandle(TileEntityMekanism tile) {
        return canHandle.test(tile);
    }

    @Override
    protected boolean shouldAddAttachment(AttachedResources<RESOURCE> attached) {
        return !attached.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public RESOURCE asResourceOrEmpty(Resource resource) {
        if (isResourceType.test(resource)) {
            return (RESOURCE) resource;
        }
        return emptyResource();
    }

    @Nullable
    @Override
    protected ResourceHandler<RESOURCE> createHandler(ItemAccess itemAccess) {
        ItemResource resource = itemAccess.getResource();
        int count;
        AttachedResources<RESOURCE> attached = getOrEmpty(resource);
        if (attached.isEmpty()) {
            //TODO: Are there any cases where the attached is empty, but we have containers?
            //TODO - 26.1: Re-evaluate this branch not just being zero and then returning null is the only difference for what getCapOrUnexposed
            // (which would use createHandlerIfData) previously did
            count = getContainerCount(resource.typeHolder().value());
        } else {
            //TODO - 1.21: Do we need to look it up in case the max size changed since we were last saved?
            count = attached.size();
        }
        if (count == 0) {
            return null;
        }
        return new ComponentBackedResourceHandler<>(this, itemAccess, count);
    }

    @Override
    public void copyToContainers(List<CONTAINER> containers, AttachedResources<RESOURCE> attached) {
        List<LargeResourceStack<RESOURCE>> attachedContainers = attached.containers();
        int size = attachedContainers.size();
        if (size == containers.size()) {
            for (int i = 0; i < size; i++) {
                CONTAINER container = containers.get(i);
                LargeResourceStack<RESOURCE> stack;
                if (container instanceof CraftingWindowOutputInventorySlot) {
                    //TODO: Can we do this handling for the crafting window output slot in a more generic way?
                    stack = stackHelper().empty();
                } else {
                    stack = attachedContainers.get(i);
                }
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
            LargeResourceStack<RESOURCE> stack = container instanceof CraftingWindowOutputInventorySlot ? stackHelper.empty() : container.asStack();
            stacks.add(stack);
            if (!stack.isEmpty()) {
                hasNonEmpty = true;
            }
        }
        return hasNonEmpty ? new AttachedResources<>(stacks) : null;
    }

    @Override
    public void copy(CONTAINER from, CONTAINER to) {
        to.copyContents(from);
    }

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

    public void tryDumpContents(Level level, BlockPos pos, ItemAccess itemAccess, @Nullable TransactionContext transaction) {
        if (capability.getCapability(itemAccess) instanceof IMekanismResourceHandler<RESOURCE, ?> handler) {
            for (IResourceContainer<RESOURCE> container : handler.getContainers()) {
                dumpContents(level, pos, container, transaction);
            }
        }
    }

    public void clearContents(IResourceContainer<RESOURCE> container, @Nullable TransactionContext transaction) {
        container.setContents(stackHelper.empty(), transaction);
    }

    public void dumpContents(Level level, BlockPos pos, IResourceContainer<RESOURCE> container, @Nullable TransactionContext transaction) {
        clearContents(container, transaction);
    }

    public void clampContents(IResourceContainer<RESOURCE> container, @Nullable TransactionContext transaction) {
        RESOURCE resource = container.resource();
        if (!resource.isEmpty()) {
            long capacity = container.capacityAsLong(resource);
            if (capacity == 0 && (container instanceof VariableCapacityFluidTank || container instanceof VariableCapacityChemicalTank)) {
                //TODO - 26.1: Re-evaluate this, and add comments
                //Our capacity should never actually be zero, and given we fake it being zero
                // until we finish building the network, we need to override this method to bypass the upper limit check
                // when our upper limit is zero
                return;
            }
            if (container.amountAsLong() > capacity) {
                container.setContents(resource, capacity, transaction);
            }
        }
    }

    //TODO - 26.1: validate and then add as docs that we don't need to also be modifying toAdd
    public void merge(List<CONTAINER> orig, List<CONTAINER> toAdd, Object2LongMap<RESOURCE> rejects, TransactionContext transaction) {
        StorageUtils.validateSizeMatches(orig, toAdd, "container");
        for (int container = 0, size = toAdd.size(); container < size; container++) {
            CONTAINER toAddContainer = toAdd.get(container);
            if (!toAddContainer.isEmpty()) {
                RESOURCE toAddResource = toAddContainer.resource();
                long toAddAmount = toAddContainer.amountAsLong();
                CONTAINER origContainer = orig.get(container);
                //TODO - 26.1: Validate all callers have this work with the given automation type
                // Also how much do we care about merging identical slots? Should we use the InventoryUtils#insertItem helper
                // to try inserting against all the slots of the other?
                //TODO - 26.1: Is  this how we want to handle trying to insert it, or would it be better to basically loop inserting multiple times as long
                // as we are inserting max int while we get closer to toAddAmount
                int added = origContainer.insert(toAddResource, Ints.saturatedCast(toAddAmount), transaction, AutomationType.INTERNAL);
                if (added < toAddAmount) {
                    //Add any remainder to the rejects
                    rejects.mergeLong(toAddResource, toAddAmount - added, Long::sum);
                }
            }
        }
    }

    /// Calculates the redstone level based on the percentage of amount stored.
    ///
    /// @return A redstone level based on the percentage of the amount stored.
    ///
    /// @see ResourceHandlerUtil#getRedstoneSignalFromResourceHandler(ResourceHandler)
    public int getRedstoneSignalFromContainers(List<CONTAINER> containers) {
        float proportion = 0.0F;
        int sampleCount = 0; // Number of samples in proportion
        for (CONTAINER container : containers) {
            long containerFill = container.amountAsLong();
            if (containerFill > 0) {
                long capacity = container.capacityAsLong(container.resource());
                if (capacity > 0) {
                    // Clamp to 1 to avoid overfilled slots increasing the signal strength beyond 15
                    proportion += Math.min(1.0f, (float) containerFill / capacity);
                    sampleCount++;
                }
            }
        }
        if (sampleCount == 0) {
            return Redstone.SIGNAL_NONE;
        }
        //TODO - 26.1: This ignores empty slots? I think that is wrong, even though it is like ResourceHandlerUtil...
        // Vanilla's getRedstoneSignalFromContainer takes the container size
        proportion /= sampleCount;
        return Mth.lerpDiscrete(proportion, Redstone.SIGNAL_NONE, Redstone.SIGNAL_MAX);
    }

    public int getRedstoneSignalFromContainer(CONTAINER container) {
        long containerFill = container.amountAsLong();
        if (containerFill > 0) {
            long capacity = container.capacityAsLong(container.resource());
            if (capacity > 0) {
                // Clamp to 1 to avoid overfilled slots increasing the signal strength beyond 15
                float proportion = Math.min(1.0f, (float) containerFill / capacity);
                return Mth.lerpDiscrete(proportion, Redstone.SIGNAL_NONE, Redstone.SIGNAL_MAX);
            }
        }
        return Redstone.SIGNAL_NONE;
    }

    //TODO - 26.1: Docs
    public boolean areContainersEmpty(List<CONTAINER> containers) {
        for (CONTAINER container : containers) {
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

    /// Helper to first try inserting ignoring empty containers, and then insert not ignoring empty containers
    ///
    /// @param containers     Containers to insert into
    /// @param resource       Type of resource to insert.
    /// @param amount         Amount of the resource to insert.
    /// @param transaction    The transaction that this operation is part of.
    /// @param automationType The method that this container is being interacted from.
    ///
    /// @return Amount inserted
    ///
    /// @see net.neoforged.neoforge.transfer.ResourceHandlerUtil#insertStacking(ResourceHandler, Resource, int, TransactionContext)
    public int insertInto(List<? extends IResourceContainer<RESOURCE>> containers, RESOURCE resource, final int amount, TransactionContext transaction,
          AutomationType automationType) {
        //TODO: Would it be simpler to just make an IMekanismResourceHandler that returns the containers and then call insert on it?
        // Or better yet if we have a handler anywhere this is called, then directly use it
        if (containers.isEmpty()) {
            return 0;
        } else if (containers.size() == 1) {
            return containers.getFirst().insert(resource, amount, transaction, automationType);
        }
        int inserted = 0;
        List<IResourceContainer<RESOURCE>> emptyContainers = new ArrayList<>(containers.size());
        for (IResourceContainer<RESOURCE> container : containers) {
            if (container.isEmpty()) {
                emptyContainers.add(container);
            } else {
                inserted += container.insert(resource, amount - inserted, transaction, automationType);
                if (inserted == amount) {
                    return inserted;
                }
            }
        }
        for (IResourceContainer<RESOURCE> container : emptyContainers) {
            inserted += container.insert(resource, amount - inserted, transaction, automationType);
            if (inserted == amount) {
                return inserted;
            }
        }
        return inserted;
    }

    public static class ChemicalContainerType extends ResourceContainerType<ChemicalResource, IChemicalTank> {

        ChemicalContainerType() {
            super(MekanismDataComponents.ATTACHED_CHEMICALS, SerializationConstants.CHEMICAL_TANKS, Capabilities.CHEMICAL,
                  TileEntityMekanism::getChemicalTanks, TileEntityMekanism::canHandleChemicals, LargeResourceStack.CHEMICAL_HELPER,
                  resource -> resource instanceof ChemicalResource);
        }

        public void dumpOrClearContents(@Nullable Level level, BlockPos pos, IResourceContainer<ChemicalResource> container, @Nullable TransactionContext transaction) {
            if (level == null) {
                clearContents(container, transaction);
            } else {
                dumpContents(level, pos, container, transaction);
            }
        }

        @Override
        public void dumpContents(Level level, BlockPos pos, IResourceContainer<ChemicalResource> container, @Nullable TransactionContext transaction) {
            LargeResourceStack<ChemicalResource> current = container.asStack();
            //Dump any radiation the current contents might contain
            IRadiationManager.INSTANCE.dumpRadiation(level, pos, current.resource(), current.amount());
            super.dumpContents(level, pos, container, transaction);
        }

        @Nullable
        @Override
        public AttachedResources<ChemicalResource> copyFromTile(TileEntityMekanism tile, List<IChemicalTank> containers) {
            boolean skipRadioactive = RadiationManager.isGlobalRadiationEnabled() && tile.shouldDumpRadiation();
            boolean hasNonEmpty = false;
            List<LargeResourceStack<ChemicalResource>> stacks = new ArrayList<>(containers.size());
            for (IChemicalTank container : containers) {
                LargeResourceStack<ChemicalResource> stack;
                if (skipRadioactive && container instanceof IChemicalTank tank && tank.resource().isRadioactive()) {
                    stack = stackHelper().empty();
                } else {
                    stack = container.asStack();
                }
                stacks.add(stack);
                if (!stack.isEmpty()) {
                    hasNonEmpty = true;
                }
            }
            return hasNonEmpty ? new AttachedResources<>(stacks) : null;
        }
    }
}