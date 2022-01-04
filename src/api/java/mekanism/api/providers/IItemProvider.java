package mekanism.api.providers;

import javax.annotation.Nonnull;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public interface IItemProvider extends IBaseProvider, net.minecraft.world.level.ItemLike {

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

    @Override
    default ResourceLocation getRegistryName() {
        return getItem().getRegistryName();
    }

    @Override
    default String getTranslationKey() {
        return getItem().getDescriptionId();
    }
}