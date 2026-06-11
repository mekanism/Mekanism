package mekanism.common.component.containers.type;

import java.util.function.Supplier;
import mekanism.common.component.containers.creator.IContainerCreator;
import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.config.IMekanismConfig;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jspecify.annotations.Nullable;

public abstract class CapableContainerType<CONTAINER extends ValueIOSerializable, ATTACHED, HANDLER> extends AbstractContainerType<CONTAINER, ATTACHED> {

    protected final MultiTypeCapability<HANDLER> capability;

    protected CapableContainerType(DeferredHolder<DataComponentType<?>, DataComponentType<ATTACHED>> component, String containerTag, MultiTypeCapability<HANDLER> capability,
          ATTACHED emptyAttachment) {
        super(component, containerTag, emptyAttachment);
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

    protected ICapabilityProvider<ItemStack, ItemAccess, HANDLER> getCapabilityProvider(IMekanismConfig... requiredConfigs) {
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
    protected abstract HANDLER createHandler(ItemAccess itemAccess);

    @Nullable
    public HANDLER getCapOrUnexposed(ItemAccess itemAccess) {//TODO - 26.1: Re-evaluate this
        HANDLER handler = capability.getCapability(itemAccess);
        //Fall back to the raw unexposed handler if it isn't exposed as a capability
        return handler == null ? createHandler(itemAccess) : handler;
    }
}