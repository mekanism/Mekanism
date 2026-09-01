package mekanism.common.recipe.ingredients.creator;

import com.mojang.serialization.Codec;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IItemStackIngredientCreator;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory.ForStacks;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class ItemStackIngredientCreator implements IItemStackIngredientCreator {

    public static final ItemStackIngredientCreator INSTANCE = new ItemStackIngredientCreator();
    public static final ForStacks<ItemResource> RESOURCE_DISPLAY_RESOLVER = new ForStacks<>() {
        @Override
        public ItemResource forStack(Holder<Item> item) {
            return ItemResource.of(item);
        }

        @Override
        public ItemResource forStack(Item item) {
            return ItemResource.of(item);
        }

        @Override
        public ItemResource forStack(ItemStack stack) {
            return ItemResource.of(stack);
        }
    };

    private ItemStackIngredientCreator() {
    }

    @Override
    public Codec<ItemStackIngredient> codec() {
        return ItemStackIngredient.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ItemStackIngredient> streamCodec() {
        return ItemStackIngredient.STREAM_CODEC;
    }
}