package mekanism.api.providers;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

@MethodsReturnNonnullByDefault
@Deprecated(forRemoval = true, since = "10.7.11")
@SuppressWarnings("removal")
public interface IItemProvider extends IBaseProvider, ItemLike {

    /**
     * Creates an item stack of size one using the item this provider represents.
     */
    default ItemStack getItemStack() {
        return getItemStack(1);
    }

    /**
     * Creates an item stack of the given size using the item this provider represents.
     *
     * @param size Size of the stack.
     */
    default ItemStack getItemStack(int size) {
        return new ItemStack(asItem(), size);
    }

    @Override
    default ResourceLocation getRegistryName() {
        return BuiltInRegistries.ITEM.getKey(asItem());
    }

    @Override
    default String getTranslationKey() {
        return asItem().getDescriptionId();
    }

    @Override
    default Component getTextComponent() {
        return asItem().getDescription();
    }
}