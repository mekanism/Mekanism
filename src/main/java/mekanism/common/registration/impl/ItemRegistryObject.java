package mekanism.common.registration.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.providers.IItemProvider;
import mekanism.common.attachments.IAttachmentAware;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemRegistryObject<ITEM extends Item> extends MekanismDeferredHolder<Item, ITEM> implements IItemProvider {

    @Nullable
    private Map<ContainerType<?, ?, ?>, Function<ItemStack, ? extends List<?>>> defaultContainers;
    @Nullable
    private List<Consumer<RegisterCapabilitiesEvent>> containerCapabilities;

    public ItemRegistryObject(ResourceKey<Item> key) {
        super(key);
    }

    @NotNull
    @Override
    public ITEM asItem() {
        return value();
    }

    @Internal
    public <CONTAINER extends INBTSerializable<CompoundTag>> ItemRegistryObject<ITEM> addAttachmentOnlyContainer(ContainerType<CONTAINER, ?, ?> containerType,
          Function<ItemStack, CONTAINER> defaultCreator) {
        return addAttachmentOnlyContainers(containerType, defaultCreator.andThen(List::of));
    }

    @Internal
    public <CONTAINER extends INBTSerializable<CompoundTag>> ItemRegistryObject<ITEM> addAttachmentOnlyContainers(ContainerType<CONTAINER, ?, ?> containerType,
          Function<ItemStack, List<CONTAINER>> defaultCreators) {
        if (defaultContainers == null) {
            //In case any containers have deps on others make this linked even though it really shouldn't matter
            // as nothing should be trying to construct the containers between register calls
            defaultContainers = new LinkedHashMap<>();
        }
        if (defaultContainers.put(containerType, defaultCreators) != null) {
            throw new IllegalStateException("Duplicate attachments added for container type: " + containerType.getAttachmentName());
        }
        return this;
    }

    @Internal
    public <CONTAINER extends INBTSerializable<CompoundTag>> ItemRegistryObject<ITEM> addAttachedContainerCapability(ContainerType<CONTAINER, ?, ?> containerType,
          Function<ItemStack, CONTAINER> defaultCreator, IMekanismConfig... requiredConfigs) {
        return addAttachedContainerCapabilities(containerType, defaultCreator.andThen(List::of), requiredConfigs);
    }

    @Internal
    public <CONTAINER extends INBTSerializable<CompoundTag>> ItemRegistryObject<ITEM> addAttachedContainerCapabilities(ContainerType<CONTAINER, ?, ?> containerType,
          Function<ItemStack, List<CONTAINER>> defaultCreators, IMekanismConfig... requiredConfigs) {
        addAttachmentOnlyContainers(containerType, defaultCreators);
        return addContainerCapability(containerType, requiredConfigs);
    }

    @Internal
    private ItemRegistryObject<ITEM> addContainerCapability(ContainerType<?, ?, ?> containerType, IMekanismConfig... requiredConfigs) {
        if (containerCapabilities == null) {
            containerCapabilities = new ArrayList<>();
        }
        containerCapabilities.add(event -> containerType.registerItemCapabilities(event, asItem(), false, requiredConfigs));
        return this;
    }

    @Internal
    public <TANK extends MergedChemicalTank> ItemRegistryObject<ITEM> addMissingMergedTanks(Supplier<AttachmentType<TANK>> backingAttachment, boolean supportsFluid,
          boolean exposeCapability) {
        int added = addMissingTankType(ContainerType.GAS, exposeCapability, stack -> stack.getData(backingAttachment).getGasTank());
        added += addMissingTankType(ContainerType.INFUSION, exposeCapability, stack -> stack.getData(backingAttachment).getInfusionTank());
        added += addMissingTankType(ContainerType.PIGMENT, exposeCapability, stack -> stack.getData(backingAttachment).getPigmentTank());
        added += addMissingTankType(ContainerType.SLURRY, exposeCapability, stack -> stack.getData(backingAttachment).getSlurryTank());
        if (supportsFluid) {
            Supplier<AttachmentType<MergedTank>> attachment = (Supplier) backingAttachment;
            added += addMissingTankType(ContainerType.FLUID, exposeCapability, stack -> stack.getData(attachment).getFluidTank());
        }
        if (added == 0) {
            throw new IllegalStateException("Unnecessary addMissingMergedTanks call");
        }
        return this;
    }

    private <CONTAINER extends INBTSerializable<CompoundTag>> int addMissingTankType(ContainerType<CONTAINER, ?, ?> containerType, boolean exposeCapability,
          Function<ItemStack, CONTAINER> defaultCreator) {
        if (defaultContainers != null && defaultContainers.containsKey(containerType)) {
            return 0;
        }
        addAttachmentOnlyContainer(containerType, defaultCreator);
        if (exposeCapability) {
            addContainerCapability(containerType);
        }
        return 1;
    }

    @Internal
    void registerCapabilities(RegisterCapabilitiesEvent event) {
        if (asItem() instanceof ICapabilityAware capabilityAware) {
            capabilityAware.attachCapabilities(event);
        }
        if (containerCapabilities != null) {
            for (Consumer<RegisterCapabilitiesEvent> consumer : containerCapabilities) {
                consumer.accept(event);
            }
            //We only allow registering once, and then we allow the memory to be freed up
            containerCapabilities = null;
        }
    }

    @Internal
    @SuppressWarnings({"unchecked", "rawtypes"})
    void attachDefaultContainers(IEventBus eventBus) {
        ITEM item = asItem();
        if (item instanceof IAttachmentAware attachmentAware) {
            attachmentAware.attachAttachments(eventBus);
        }
        if (defaultContainers != null) {
            for (Map.Entry<ContainerType<?, ?, ?>, Function<ItemStack, ? extends List<?>>> entry : defaultContainers.entrySet()) {
                //Note: We pass null for the event bus to not expose this attachment as a capability
                entry.getKey().addDefaultContainers(null, item, (Function) entry.getValue());
            }
            //We only allow them being attached once
            defaultContainers = null;
        }
    }
}