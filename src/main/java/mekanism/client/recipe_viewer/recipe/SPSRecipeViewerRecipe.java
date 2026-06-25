package mekanism.client.recipe_viewer.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mekanism.api.MekanismRegistries;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.recipe_viewer.INamedRVRecipe;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

//TODO - V11: Make the SPS have a proper recipe type to allow for custom recipes
public record SPSRecipeViewerRecipe(Identifier id, ChemicalStackIngredient input, ChemicalStackTemplate output) implements INamedRVRecipe {

    public static final Codec<SPSRecipeViewerRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Identifier.CODEC.fieldOf(SerializationConstants.ID).forGetter(SPSRecipeViewerRecipe::id),
          ChemicalStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(SPSRecipeViewerRecipe::input),
          ChemicalStackTemplate.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(SPSRecipeViewerRecipe::output)
    ).apply(instance, SPSRecipeViewerRecipe::new));

    public static List<SPSRecipeViewerRecipe> getSPSRecipes() {
        Optional<Registry<Chemical>> optionalRegistry = RecipeViewerUtils.getRegistry(MekanismRegistries.Keys.CHEMICAL);
        if (optionalRegistry.isEmpty()) {
            //Something went horribly wrong, bail
            Mekanism.logger.warn("Failed to find chemical registry when generating sps recipes");
            return Collections.emptyList();
        }
        Registry<Chemical> chemicals = optionalRegistry.get();
        Optional<Reference<Chemical>> poloniumReference = chemicals.get(MekanismChemicals.POLONIUM);
        Optional<Reference<Chemical>> antimatterReference = chemicals.get(MekanismChemicals.ANTIMATTER);
        if (poloniumReference.isEmpty() || antimatterReference.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new SPSRecipeViewerRecipe(
              RegistryUtils.synthetic(MekanismChemicals.ANTIMATTER.identifier(), "sps"),
              IngredientCreatorAccess.chemicalStack().fromHolder(poloniumReference.get(), MekanismConfig.general.spsInputPerAntimatter.get()),
              new ChemicalStackTemplate(antimatterReference.get(), 1)
        ));
    }
}