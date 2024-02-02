package mekanism.common.registration.impl;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.registration.DoubleWrappedRegistryObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

public class BlockRegistryObject<BLOCK extends Block, ITEM extends Item> extends DoubleWrappedRegistryObject<Block, BLOCK, Item, ITEM> implements IBlockProvider {

    public BlockRegistryObject(DeferredHolder<Block, BLOCK> blockRegistryObject, DeferredHolder<Item, ITEM> itemRegistryObject) {
        super(blockRegistryObject, itemRegistryObject);
    }

    @NotNull
    @Override
    public BLOCK getBlock() {
        return getPrimary();
    }

    @NotNull
    @Override
    public ITEM asItem() {
        return getSecondary();
    }

    private ItemRegistryObject<ITEM> getItemHolder() {
        if (secondaryRO instanceof ItemRegistryObject<ITEM> itemHolder) {
            return itemHolder;
        }
        throw new IllegalStateException("Called method requires an ItemRegistryObject");
    }

    @Internal
    public <CONTAINER extends INBTSerializable<CompoundTag>> BlockRegistryObject<BLOCK, ITEM> addAttachmentOnlyContainer(ContainerType<CONTAINER, ?, ?> containerType,
          Function<ItemStack, CONTAINER> defaultCreator) {
        return addAttachmentOnlyContainers(containerType, defaultCreator.andThen(List::of));
    }

    @Internal
    public <CONTAINER extends INBTSerializable<CompoundTag>> BlockRegistryObject<BLOCK, ITEM> addAttachmentOnlyContainers(ContainerType<CONTAINER, ?, ?> containerType,
          Function<ItemStack, List<CONTAINER>> defaultCreators) {
        getItemHolder().addAttachmentOnlyContainers(containerType, defaultCreators);
        return this;
    }

    @Internal
    public <TANK extends MergedChemicalTank> BlockRegistryObject<BLOCK, ITEM> addMissingMergedAttachments(Supplier<AttachmentType<TANK>> backingAttachment) {
        getItemHolder().addMissingMergedAttachments(backingAttachment);
        return this;
    }
}