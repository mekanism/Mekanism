package mekanism.api.providers;

import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public interface IItemProvider extends IBaseProvider, ItemLike {

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
        return new ItemStack(asItem(), size);
    }

    @Override
    default ResourceLocation getRegistryName() {
        return asItem().getRegistryName();
    }

    @Override
    default String getTranslationKey() {
        return asItem().getDescriptionId();
    }
}