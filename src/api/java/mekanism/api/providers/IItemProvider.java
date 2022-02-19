package mekanism.api.providers;

import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public interface IItemProvider extends IBaseProvider, net.minecraft.util.IItemProvider {

    /**
     * Gets the item this provider represents.
     */
    @Nonnull
    Item getItem();//TODO - 1.18: Replace this with just using vanilla's asItem?

    @Nonnull
    @Override
    default Item asItem() {
        return getItem();
    }

    /**
     * Creates an item stack of size one using the item this provider represents.
     */
    @Nonnull
    default ItemStack getItemStack() {
        return getItemStack(1);
    }

    /**
     * Creates an item stack of the given size using the item this provider represents.
     *
     * @param size Size of the stack.
     */
    @Nonnull
    default ItemStack getItemStack(int size) {
        return new ItemStack(getItem(), size);
    }

    @Deprecated//TODO - 1.18: Remove this as we don't actually use this
    default boolean itemMatches(ItemStack otherStack) {
        return itemMatches(otherStack.getItem());
    }

    @Deprecated//TODO - 1.18: Remove this as we don't actually use this
    default boolean itemMatches(Item other) {
        return getItem() == other;
    }

    @Nonnull
    @Override
    default ResourceLocation getRegistryName() {
        return Objects.requireNonNull(getItem().getRegistryName(), "Unregistered Item");
    }

    @Nonnull
    @Override
    default String getTranslationKey() {
        return getItem().getDescriptionId();
    }
}