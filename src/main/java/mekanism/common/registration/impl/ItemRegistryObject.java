package mekanism.common.registration.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.providers.IItemProvider;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemRegistryObject<ITEM extends Item> extends MekanismDeferredHolder<Item, ITEM> implements IItemProvider {

    @Nullable
    private Map<ContainerType<?, ?, ?>, Function<ItemStack, ? extends List<?>>> defaultContainers;

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
            defaultContainers = new HashMap<>();
        }
        if (defaultContainers.put(containerType, defaultCreators) != null) {
            throw new IllegalStateException("Duplicate attachments added for container type: " + containerType.getAttachmentName());
        }
        return this;
    }

    @Internal
    public <TANK extends MergedChemicalTank> ItemRegistryObject<ITEM> addMissingMergedAttachments(Supplier<AttachmentType<TANK>> backingAttachment) {
        int added = 0;
        if (defaultContainers == null || !defaultContainers.containsKey(ContainerType.GAS)) {
            addAttachmentOnlyContainer(ContainerType.GAS, stack -> stack.getData(backingAttachment).getGasTank());
            added++;
        }
        //Note: If default default containers is null it won't have gas, so it will be created and not be null by the time we get here
        if (!defaultContainers.containsKey(ContainerType.INFUSION)) {
            addAttachmentOnlyContainer(ContainerType.INFUSION, stack -> stack.getData(backingAttachment).getInfusionTank());
            added++;
        }
        if (!defaultContainers.containsKey(ContainerType.PIGMENT)) {
            addAttachmentOnlyContainer(ContainerType.PIGMENT, stack -> stack.getData(backingAttachment).getPigmentTank());
            added++;
        }
        if (!defaultContainers.containsKey(ContainerType.SLURRY)) {
            addAttachmentOnlyContainer(ContainerType.SLURRY, stack -> stack.getData(backingAttachment).getSlurryTank());
            added++;
        }
        if (added == 0) {
            throw new IllegalStateException("Unnecessary addMissingMergedAttachments call");
        }
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    void attachDefaultContainers() {
        if (defaultContainers != null) {
            ITEM item = asItem();
            //TODO - 1.20.4: Put this comment somewhere
            //Note: We pass null for the event bus to not expose this attachment as a capability
            defaultContainers.forEach(((containerType, defaultCreators) -> containerType.addDefaultContainers(null, item, (Function) defaultCreators)));
            //We only allow them being attached once
            defaultContainers = null;
        }
    }
}