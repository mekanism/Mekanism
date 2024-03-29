package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.basic.BasicCombinerRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class CombinerRecipeSerializer implements RecipeSerializer<BasicCombinerRecipe> {

    private final IFactory<BasicCombinerRecipe> factory;
    private Codec<BasicCombinerRecipe> codec;

    public CombinerRecipeSerializer(IFactory<BasicCombinerRecipe> factory) {
        this.factory = factory;
    }

    @NotNull
    @Override
    public Codec<BasicCombinerRecipe> codec() {
        if (codec == null) {
            codec = RecordCodecBuilder.create(instance -> instance.group(
                  IngredientCreatorAccess.item().codec().fieldOf(JsonConstants.MAIN_INPUT).forGetter(CombinerRecipe::getMainInput),
                  IngredientCreatorAccess.item().codec().fieldOf(JsonConstants.EXTRA_INPUT).forGetter(CombinerRecipe::getExtraInput),
                  ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf(JsonConstants.OUTPUT).forGetter(BasicCombinerRecipe::getOutputRaw)
            ).apply(instance, factory::create));
        }
        return codec;
    }

    @NotNull
    @Override
    public BasicCombinerRecipe fromNetwork(@NotNull FriendlyByteBuf buffer) {
        ItemStackIngredient mainInput = IngredientCreatorAccess.item().read(buffer);
        ItemStackIngredient extraInput = IngredientCreatorAccess.item().read(buffer);
        ItemStack output = buffer.readItem();
        return this.factory.create(mainInput, extraInput, output);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull BasicCombinerRecipe recipe) {
        recipe.getMainInput().write(buffer);
        recipe.getExtraInput().write(buffer);
        buffer.writeItem(recipe.getOutputRaw());
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends BasicCombinerRecipe> {

        RECIPE create(ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output);
    }
}