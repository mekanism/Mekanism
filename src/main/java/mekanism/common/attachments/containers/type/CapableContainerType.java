package mekanism.common.attachments.containers.type;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.containers.IAttachedContainers;
import mekanism.common.attachments.containers.creator.IContainerCreator;
import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public abstract class CapableContainerType<CONTAINER extends ValueIOSerializable, ATTACHED extends IAttachedContainers<?, ATTACHED>, HANDLER>
      extends AbstractContainerType<CONTAINER, ATTACHED> {

    protected final MultiTypeCapability<HANDLER> capability;

    protected CapableContainerType(DeferredHolder<DataComponentType<?>, DataComponentType<ATTACHED>> component, String containerTag, String containerKey,
          MultiTypeCapability<HANDLER> capability, ATTACHED emptyAttachment, Function<TileEntityMekanism, List<CONTAINER>> containersFromTile,
          Predicate<TileEntityMekanism> canHandle) {
        super(component, containerTag, containerKey, emptyAttachment, containersFromTile, canHandle);
        this.capability = capability;
    }

    public MultiTypeCapability<HANDLER> capability() {
        return this.capability;
    }

    @Override
    public void addDefaultCreators(@Nullable IEventBus eventBus, Item item, Supplier<? extends IContainerCreator<CONTAINER, ATTACHED>> defaultCreator,
          IMekanismConfig... requiredConfigs) {
        super.addDefaultCreators(eventBus, item, defaultCreator, requiredConfigs);
        if (eventBus != null) {
            eventBus.addListener(RegisterCapabilitiesEvent.class, event -> registerItemCapabilities(event, item, requiredConfigs));
        }
    }

    public void registerItemCapabilities(RegisterCapabilitiesEvent event, Item item, IMekanismConfig... requiredConfigs) {
        event.registerItem(capability.item(), getCapabilityProvider(requiredConfigs), item);
    }

    protected ICapabilityProvider<ItemStack, @NonNull ItemAccess, HANDLER> getCapabilityProvider(IMekanismConfig... requiredConfigs) {
        if (requiredConfigs.length == 0) {
            return (_, itemAccess) -> createHandler(itemAccess);
        }
        //Only expose the capabilities if the required configs are loaded
        return (_, itemAccess) -> {
            for (IMekanismConfig requiredConfig : requiredConfigs) {
                if (!requiredConfig.isLoaded()) {
                    return null;
                }
            }
            return createHandler(itemAccess);
        };
    }

    @Nullable
    private HANDLER createHandler(ItemAccess itemAccess) {
        ItemResource resource = itemAccess.getResource();
        int count;
        ATTACHED attached = getOrEmpty(resource);
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
        return createHandler(itemAccess, count);
    }

    protected abstract HANDLER createHandler(ItemAccess attachedAccess, int totalContainers);

    @Nullable
    public HANDLER getCapOrUnexposed(ItemAccess itemAccess) {
        HANDLER handler = capability.getCapability(itemAccess);
        //Fall back to the raw unexposed handler if it isn't exposed as a capability
        return handler == null ? createHandler(itemAccess) : handler;
    }
}