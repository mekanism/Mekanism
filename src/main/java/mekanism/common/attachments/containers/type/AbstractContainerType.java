package mekanism.common.attachments.containers.type;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.containers.ComponentBackedHandler;
import mekanism.common.attachments.containers.IAttachedContainers;
import mekanism.common.attachments.containers.creator.IContainerCreator;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public abstract class AbstractContainerType<CONTAINER extends ValueIOSerializable, ATTACHED extends IAttachedContainers<?, ATTACHED>, HANDLER>
      implements IContainerType<CONTAINER, ATTACHED> {

    private final Map<Item, Lazy<? extends IContainerCreator<CONTAINER, ATTACHED>>> knownDefaultCreators = new Reference2ObjectOpenHashMap<>();
    private final Function<TileEntityMekanism, List<CONTAINER>> containersFromTile;
    private final DeferredHolder<DataComponentType<?>, DataComponentType<ATTACHED>> component;
    private final Predicate<TileEntityMekanism> canHandle;
    private final ATTACHED emptyAttachment;
    private final String containerTag;
    private final String containerKey;

    protected AbstractContainerType(DeferredHolder<DataComponentType<?>, DataComponentType<ATTACHED>> component, String containerTag, String containerKey,
          ATTACHED emptyAttachment, Function<TileEntityMekanism, List<CONTAINER>> containersFromTile, Predicate<TileEntityMekanism> canHandle) {
        ContainerType.TYPES_INTERNAL.add(this);
        this.component = component;
        this.containerTag = containerTag;
        this.containerKey = containerKey;
        this.emptyAttachment = emptyAttachment;
        this.containersFromTile = containersFromTile;
        this.canHandle = canHandle;
    }

    @Override
    public DeferredHolder<DataComponentType<?>, DataComponentType<ATTACHED>> getComponentType() {
        return component;
    }

    @Override
    public String getTag() {
        return containerTag;
    }

    @Override
    public String getKey() {
        return containerKey;
    }

    /**
     * Adds some containers as default and exposes it as a capability that requires the given configs if the specified bus is present.
     */
    @Override
    public void addDefaultCreators(@Nullable IEventBus eventBus, Item item, Supplier<? extends IContainerCreator<CONTAINER, ATTACHED>> defaultCreator,
          IMekanismConfig... requiredConfigs) {
        knownDefaultCreators.put(item, Lazy.of(defaultCreator));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CONTAINER> getAttachmentContainersIfPresent(ItemAccess itemAccess) {
        HANDLER handler = createHandlerIfData(itemAccess);
        if (handler instanceof ComponentBackedHandler) {
            return ((ComponentBackedHandler<?, CONTAINER, ?, ?>) handler).getContainers();
        }
        return Collections.emptyList();
    }

    @Nullable
    private IContainerCreator<CONTAINER, ATTACHED> getCreator(Item item) {
        Lazy<? extends IContainerCreator<CONTAINER, ATTACHED>> containerCreator = knownDefaultCreators.get(item);
        return containerCreator == null ? null : containerCreator.get();
    }

    @Override
    public int getContainerCount(Item item) {
        IContainerCreator<CONTAINER, ATTACHED> creator = getCreator(item);
        return creator == null ? 0 : creator.totalContainers();
    }

    @Nullable//TODO - 26.1: remove me, just use caps
    protected HANDLER createHandlerIfData(ItemAccess itemAccess) {
        ATTACHED attached = getOrEmpty(itemAccess);
        //TODO - 1.21: Do we need to look it up in case the max size changed since we were last saved?
        return attached.isEmpty() ? null : createHandler(itemAccess, attached.size());
    }

    protected abstract HANDLER createHandler(ItemAccess attachedAccess, int totalContainers);

    @Override
    public ATTACHED createNewAttachment(ItemResource itemType) {
        IContainerCreator<CONTAINER, ATTACHED> containerCreator = getCreator(itemType.getItem());
        return containerCreator == null ? emptyAttachment : containerCreator.initStorage();
    }

    @Override
    public ATTACHED getOrEmpty(DataComponentGetter stack) {
        return stack.getOrDefault(getComponentType(), emptyAttachment);
    }

    @Override
    public CONTAINER createContainer(ItemAccess attachedAccess, int containerIndex) {
        //TODO - 1.21: Re-evaluate usages and see if they should be going via capability instead?
        // Also I theoretically users of this bypass any checks for if the attached access is stacked, but I believe all uses are fine with directly acting on the stack (validate this)
        Item attachedTo = attachedAccess.getResource().getItem();
        IContainerCreator<CONTAINER, ATTACHED> containerCreator = getCreator(attachedTo);
        if (containerCreator == null) {
            throw new IllegalArgumentException("No known containers for item " + attachedTo);
        }
        return containerCreator.create(attachedAccess, containerIndex);
    }

    @Override
    public boolean supports(Holder<Item> item) {
        return knownDefaultCreators.containsKey(item.value());
    }

    @Override
    public void addDefault(Item item, DataComponentMap.Builder components) {
        IContainerCreator<CONTAINER, ATTACHED> containerCreator = getCreator(item);
        if (containerCreator != null) {
            //Supports the type
            ATTACHED attached = containerCreator.initStorage();
            if (!attached.isEmpty()) {
                components.set(getComponentType(), attached);
            }
        }
    }

    @Override
    public void copyFromTile(TileEntityMekanism tile, DataComponentMap.Builder builder) {
        List<CONTAINER> containers = getContainers(tile);
        if (!containers.isEmpty()) {
            ATTACHED attachedData = copyFromTile(tile, containers);
            if (attachedData != null) {
                builder.set(getComponentType(), attachedData);
            }
        }
    }

    @Nullable
    protected ATTACHED copyFromTile(TileEntityMekanism tile, List<CONTAINER> containers) {
        return attachedCopyOf(containers);
    }

    @Override
    public boolean canHandle(TileEntityMekanism tile) {
        return canHandle.test(tile);
    }

    @Override
    public List<CONTAINER> getContainers(TileEntityMekanism tile) {
        return containersFromTile.apply(tile);
    }
}