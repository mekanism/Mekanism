package mekanism.api.providers;

import javax.annotation.Nonnull;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public interface IItemProvider extends IBaseProvider, net.minecraft.util.IItemProvider {

    @Nonnull
    Item getItem();

    @Nonnull
    @Override
    default Item asItem() {
        return getItem();
    }

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

    @Override
    default ResourceLocation getRegistryName() {
        return getItem().getRegistryName();
    }

    @Override
    default ITextComponent getTextComponent() {
        return getItem().getName();
    }

    @Override
    default String getTranslationKey() {
        return getItem().getTranslationKey();
    }
}