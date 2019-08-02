package mekanism.common.base;

import javax.annotation.Nonnull;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public interface IItemProvider {

    @Nonnull
    Item getItem();

    @Nonnull
    default ItemStack getItemStack() {
        return getItemStack(1);
    }

    @Nonnull
    default ItemStack getItemStack(int size) {
        return new ItemStack(getItem(), size);
    }

    default boolean itemMatches(ItemStack otherStack) {
        return itemMatches(otherStack.getItem());
    }

    default boolean itemMatches(Item other) {
        return getItem() == other;
    }

    default ResourceLocation getRegistryName() {
        return getItem().getRegistryName();
    }

    default String getName() {
        return getRegistryName().getPath();
    }

    default String getTranslationKey() {
        return getItem().getTranslationKey();
    }
}