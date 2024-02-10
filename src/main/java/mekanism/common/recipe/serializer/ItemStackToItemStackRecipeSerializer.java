package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.recipes.basic.BasicItemStackToItemStackRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ItemStackToItemStackRecipeSerializer<RECIPE extends BasicItemStackToItemStackRecipe> implements RecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;
    private Codec<RECIPE> codec;

    public ItemStackToItemStackRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @NotNull
    @Override
    public Codec<RECIPE> codec() {
        if (codec == null) {
            codec = RecordCodecBuilder.create(instance -> instance.group(
                  IngredientCreatorAccess.item().codec().fieldOf(JsonConstants.INPUT).forGetter(BasicItemStackToItemStackRecipe::getInput),
                  ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf(JsonConstants.OUTPUT).forGetter(BasicItemStackToItemStackRecipe::getOutputRaw)
            ).apply(instance, factory::create));
        }
        return codec;
    }

    @NotNull
    @Override
    public RECIPE fromNetwork(@NotNull FriendlyByteBuf buffer) {
        ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().read(buffer);
        ItemStack output = buffer.readItem();
        return this.factory.create(inputIngredient, output);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        recipe.getInput().write(buffer);
        buffer.writeItem(recipe.getOutputRaw());
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends BasicItemStackToItemStackRecipe> {

        RECIPE create(ItemStackIngredient input, ItemStack output);
    }
}