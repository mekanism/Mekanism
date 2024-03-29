package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.basic.BasicChemicalDissolutionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ChemicalDissolutionRecipeSerializer implements RecipeSerializer<BasicChemicalDissolutionRecipe> {

    private final IFactory<BasicChemicalDissolutionRecipe> factory;
    private Codec<BasicChemicalDissolutionRecipe> codec;

    public ChemicalDissolutionRecipeSerializer(IFactory<BasicChemicalDissolutionRecipe> factory) {
        this.factory = factory;
    }

    @NotNull
    @Override
    public Codec<BasicChemicalDissolutionRecipe> codec() {
        if (codec == null) {
            codec = RecordCodecBuilder.create(instance -> instance.group(
                  IngredientCreatorAccess.item().codec().fieldOf(JsonConstants.ITEM_INPUT).forGetter(ChemicalDissolutionRecipe::getItemInput),
                  IngredientCreatorAccess.gas().codec().fieldOf(JsonConstants.GAS_INPUT).forGetter(ChemicalDissolutionRecipe::getGasInput),
                  SerializerHelper.BOXED_CHEMICALSTACK_CODEC.fieldOf(JsonConstants.OUTPUT).forGetter(r -> r.getOutputRaw().getChemicalStack())
            ).apply(instance, factory::create));
        }
        return codec;
    }

    @NotNull
    @Override
    public BasicChemicalDissolutionRecipe fromNetwork(@NotNull FriendlyByteBuf buffer) {
        ItemStackIngredient itemInput = IngredientCreatorAccess.item().read(buffer);
        GasStackIngredient gasInput = IngredientCreatorAccess.gas().read(buffer);
        BoxedChemicalStack boxedChemicalStack = BoxedChemicalStack.read(buffer);
        return this.factory.create(itemInput, gasInput, boxedChemicalStack.getChemicalStack());
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull BasicChemicalDissolutionRecipe recipe) {
        recipe.getItemInput().write(buffer);
        recipe.getGasInput().write(buffer);
        recipe.getOutputRaw().write(buffer);
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends BasicChemicalDissolutionRecipe> {

        RECIPE create(ItemStackIngredient itemInput, GasStackIngredient gasInput, ChemicalStack<?> output);
    }
}