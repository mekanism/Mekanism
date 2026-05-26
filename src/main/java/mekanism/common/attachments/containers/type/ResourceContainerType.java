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
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.attachments.containers.AttachedResources;
import mekanism.common.attachments.containers.ComponentBackedResourceHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.inventory.slot.CraftingWindowOutputInventorySlot;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.redstone.Redstone;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public class ResourceContainerType<RESOURCE extends @NonNull Resource, CONTAINER extends IResourceContainer<RESOURCE>>
      extends CapableContainerType<CONTAINER, AttachedResources<RESOURCE>, ResourceHandler<RESOURCE>> {

    private final LargeResourceStack.StackHelper<RESOURCE> stackHelper;
    private final RESOURCE emptyInstance;

    protected ResourceContainerType(DeferredHolder<DataComponentType<?>, DataComponentType<AttachedResources<RESOURCE>>> component, String containerTag, String containerKey,
          MultiTypeCapability<ResourceHandler<RESOURCE>> capability, Function<TileEntityMekanism, List<CONTAINER>> containersFromTile, Predicate<TileEntityMekanism> canHandle,
          LargeResourceStack.StackHelper<RESOURCE> stackHelper, RESOURCE emptyInstance) {
        super(component, containerTag, containerKey, capability, AttachedResources.empty(), containersFromTile, canHandle);
        this.stackHelper = stackHelper;
        this.emptyInstance = emptyInstance;
    }

    public RESOURCE emptyResource() {
        return emptyInstance;
    }

    public LargeResourceStack.StackHelper<RESOURCE> stackHelper() {
        return stackHelper;
    }

    @Override
    protected ResourceHandler<RESOURCE> createHandler(ItemAccess attachedAccess, int totalContainers) {
        return new ComponentBackedResourceHandler<>(this, attachedAccess, totalContainers);
    }

    @Override
    public void copyToContainers(List<CONTAINER> containers, AttachedResources<RESOURCE> attached) {
        List<LargeResourceStack<RESOURCE>> attachedContainers = attached.containers();
        int size = attachedContainers.size();
        if (size == containers.size()) {
            for (int i = 0; i < size; i++) {
                CONTAINER container = containers.get(i);
                if (container instanceof CraftingWindowOutputInventorySlot) {
                    //TODO: Can we do this handling for the crafting window output slot in a more generic way?
                    container.setEmpty();
                } else {
                    container.setContents(attachedContainers.get(i), null);
                }
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

    public ItemStack getFilledVariant(ItemAccess itemAccess, RESOURCE resource) {
        if (capability.getCapability(itemAccess) instanceof IMekanismResourceHandler<RESOURCE, ?> handler) {
            for (IResourceContainer<RESOURCE> container : handler.getContainers()) {
                container.setContents(resource, container.capacityAsLong(resource), null);
            }
        }
        //The item is now filled return it for convenience
        return itemAccess.getResource().toStack(itemAccess.getAmount());
    }

    /// Gets the resource stored in an item's container by checking the attachment. This is for cases when we may not actually have a resource handler provided as a
    /// capability from our item, but it may have stored data in its container from when it was a block
    public LargeResourceStack<RESOURCE> getStoredContentsFromAttachment(ItemAccess itemAccess) {
        List<CONTAINER> containers = getAttachmentContainersIfPresent(itemAccess);
        return switch (containers.size()) {
            case 0 -> stackHelper().empty();
            case 1 -> containers.getFirst().asStack();
            default -> {
                RESOURCE type = stackHelper().empty().resource();
                long storedAmount = 0;
                for (CONTAINER container : containers) {
                    if (container.isEmpty()) {
                        continue;
                    }
                    RESOURCE tankType = container.resource();
                    long tankAmount = container.amountAsLong();
                    if (type.isEmpty()) {
                        type = tankType;
                        storedAmount = tankAmount;
                    } else if (tankType.equals(type)) {
                        if (storedAmount < Long.MAX_VALUE - tankAmount) {
                            storedAmount += tankAmount;
                        } else {
                            storedAmount = Long.MAX_VALUE;
                            break;
                        }
                    }
                    //Note: If we have multiple tanks that have different types stored we only return the first type
                }
                yield stackHelper().createStack(type, storedAmount);
            }
        };
    }

    /// Gets the FIRST resource stored in an item's container by checking the attachment. This is for cases when we may not actually have a resource handler provided as a
    /// capability from our item, but it may have stored data in its container from when it was a block. Do NOT modify the result
    ///
    /// @return the first found resource FOR DISPLAY
    public RESOURCE getFirstResourceFromAttachment(ItemAccess itemAccess) {
        List<CONTAINER> containers = getAttachmentContainersIfPresent(itemAccess);
        return switch (containers.size()) {
            case 0 -> emptyResource();
            case 1 -> containers.getFirst().resource();
            default -> {
                for (CONTAINER container : containers) {
                    if (!container.isEmpty()) {
                        yield container.resource();
                    }
                }
                yield emptyResource();
            }
        };
    }

    public void clampContents(CONTAINER container) {
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
                container.setContents(resource, capacity, null);
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

    static class ChemicalContainerType extends ResourceContainerType<ChemicalResource, IChemicalTank> {

        ChemicalContainerType() {
            super(MekanismDataComponents.ATTACHED_CHEMICALS, SerializationConstants.CHEMICAL_TANKS, SerializationConstants.TANK, Capabilities.CHEMICAL,
                  TileEntityMekanism::getChemicalTanks, TileEntityMekanism::canHandleChemicals, LargeResourceStack.CHEMICAL_HELPER, ChemicalResource.EMPTY);
        }

        @Nullable
        @Override
        protected AttachedResources<ChemicalResource> copyFromTile(TileEntityMekanism tile, List<IChemicalTank> containers) {
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