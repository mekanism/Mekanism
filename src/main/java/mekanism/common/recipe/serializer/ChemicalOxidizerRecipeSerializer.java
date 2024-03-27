package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalOxidizerRecipe;
import mekanism.api.recipes.basic.BasicChemicalOxidizerRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ChemicalOxidizerRecipeSerializer implements RecipeSerializer<BasicChemicalOxidizerRecipe> {

    private final IFactory<BasicChemicalOxidizerRecipe> factory;
    private Codec<BasicChemicalOxidizerRecipe> codec;

    public ChemicalOxidizerRecipeSerializer(IFactory<BasicChemicalOxidizerRecipe> factory) {
        this.factory = factory;
    }

    @NotNull
    @Override
    public Codec<BasicChemicalOxidizerRecipe> codec() {
        if (codec == null) {
            codec = RecordCodecBuilder.create(instance -> instance.group(
                  IngredientCreatorAccess.item().codec().fieldOf(JsonConstants.ITEM_INPUT).forGetter(ChemicalOxidizerRecipe::getInput),
                  SerializerHelper.BOXED_CHEMICALSTACK_CODEC.fieldOf(JsonConstants.OUTPUT).forGetter(r -> r.getOutputRaw().getChemicalStack())
            ).apply(instance, factory::create));
        }
        return codec;
    }

    @NotNull
    @Override
    public BasicChemicalOxidizerRecipe fromNetwork(@NotNull FriendlyByteBuf buffer) {
        ItemStackIngredient input = IngredientCreatorAccess.item().read(buffer);
        BoxedChemicalStack boxedChemicalStack = BoxedChemicalStack.read(buffer);
        return this.factory.create(input, boxedChemicalStack.getChemicalStack());
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull BasicChemicalOxidizerRecipe recipe) {
        recipe.getInput().write(buffer);
        recipe.getOutputRaw().write(buffer);
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends BasicChemicalOxidizerRecipe> {

        RECIPE create(ItemStackIngredient input, ChemicalStack<?> output);
    }
}