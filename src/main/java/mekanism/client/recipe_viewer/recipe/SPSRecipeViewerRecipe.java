package mekanism.client.recipe_viewer.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.INamedRVRecipe;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismChemicals;
import net.minecraft.resources.ResourceLocation;

//TODO - V11: Make the SPS have a proper recipe type to allow for custom recipes
public record SPSRecipeViewerRecipe(ResourceLocation id, ChemicalStackIngredient input, ChemicalStack output) implements INamedRVRecipe {

    public static final Codec<SPSRecipeViewerRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          ResourceLocation.CODEC.fieldOf(SerializationConstants.ID).forGetter(SPSRecipeViewerRecipe::id),
          ChemicalStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(SPSRecipeViewerRecipe::input),
          ChemicalStack.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(SPSRecipeViewerRecipe::output)
    ).apply(instance, SPSRecipeViewerRecipe::new));

    public static List<SPSRecipeViewerRecipe> getSPSRecipes() {
        return Collections.singletonList(new SPSRecipeViewerRecipe(
              RecipeViewerUtils.synthetic(Mekanism.rl("antimatter"), "sps"),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.POLONIUM, MekanismConfig.general.spsInputPerAntimatter.get()),
              MekanismChemicals.ANTIMATTER.asStack(1)
        ));
    }
}