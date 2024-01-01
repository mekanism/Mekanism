package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.basic.BasicCombinerRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
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
                  SerializerHelper.ITEMSTACK_CODEC.fieldOf(JsonConstants.OUTPUT).forGetter(BasicCombinerRecipe::getOutputRaw)
            ).apply(instance, factory::create));
        }
        return codec;
    }

    @Override
    public BasicCombinerRecipe fromNetwork(@NotNull FriendlyByteBuf buffer) {
        try {
            ItemStackIngredient mainInput = IngredientCreatorAccess.item().read(buffer);
            ItemStackIngredient extraInput = IngredientCreatorAccess.item().read(buffer);
            ItemStack output = buffer.readItem();
            return this.factory.create(mainInput, extraInput, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading combiner recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull BasicCombinerRecipe recipe) {
        try {
            recipe.getMainInput().write(buffer);
            recipe.getExtraInput().write(buffer);
            buffer.writeItem(recipe.getOutputRaw());
        } catch (Exception e) {
            Mekanism.logger.error("Error writing combiner recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends BasicCombinerRecipe> {

        RECIPE create(ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output);
    }
}