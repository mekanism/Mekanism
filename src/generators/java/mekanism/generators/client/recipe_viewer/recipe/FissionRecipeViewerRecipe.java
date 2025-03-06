package mekanism.generators.client.recipe_viewer.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.attribute.ChemicalAttributes;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.chemical.attribute.CooledCoolant;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.INamedRVRecipe;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.util.HeatUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import org.jetbrains.annotations.Nullable;

//If null -> coolant is water
public record FissionRecipeViewerRecipe(ResourceLocation id, @Nullable ChemicalStackIngredient inputCoolant, ChemicalStackIngredient fuel, ChemicalStack outputCoolant,
                                        ChemicalStack waste)
      implements INamedRVRecipe {

    public static final Codec<FissionRecipeViewerRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          ResourceLocation.CODEC.fieldOf(SerializationConstants.ID).forGetter(FissionRecipeViewerRecipe::id),
          ChemicalStackIngredient.CODEC.optionalFieldOf(SerializationConstants.EXTRA_INPUT).forGetter(recipe -> Optional.ofNullable(recipe.inputCoolant())),
          ChemicalStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(FissionRecipeViewerRecipe::fuel),
          ChemicalStack.CODEC.fieldOf(SerializationConstants.SECONDARY_OUTPUT).forGetter(FissionRecipeViewerRecipe::outputCoolant),
          ChemicalStack.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(FissionRecipeViewerRecipe::waste)
    ).apply(instance, (id, inputCoolant, fuel, outputCoolant, waste) ->
          new FissionRecipeViewerRecipe(id, inputCoolant.orElse(null), fuel, outputCoolant, waste)));

    public FluidStackIngredient waterInput() {
        return IngredientCreatorAccess.fluid().from(FluidTags.WATER, MathUtils.clampToInt(outputCoolant().getAmount()));
    }

    @SuppressWarnings("removal")
    public static List<FissionRecipeViewerRecipe> getFissionRecipes() {
        //Note: The recipes below ignore thermal conductivity and just take enthalpy into account and it rounds the amount of coolant
        //TODO: Eventually we may want to try and improve on that but for now this should be fine
        List<FissionRecipeViewerRecipe> recipes = new ArrayList<>();
        long energyPerFuel = MekanismGeneratorsConfig.generators.energyPerFissionFuel.get();
        //Special case water recipe
        long coolantAmount = Math.round(energyPerFuel * HeatUtils.getSteamEnergyEfficiency() / HeatUtils.getWaterThermalEnthalpy());
        recipes.add(new FissionRecipeViewerRecipe(
              RecipeViewerUtils.synthetic(MekanismGenerators.rl("water"), "fission"),
              null, IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.FISSILE_FUEL, 1),
              MekanismChemicals.STEAM.asStack(coolantAmount), MekanismChemicals.NUCLEAR_WASTE.asStack(1)
        ));
        //Add recipes for all cooled coolants
        for (Map.Entry<ResourceKey<Chemical>, CooledCoolant> entry : MekanismAPI.CHEMICAL_REGISTRY.getDataMap(IMekanismDataMapTypes.INSTANCE.cooledChemicalCoolant()).entrySet()) {
            ResourceKey<Chemical> key = entry.getKey();
            CooledCoolant coolant = entry.getValue();
            long amount = Math.round(energyPerFuel / coolant.thermalEnthalpy());
            recipes.add(new FissionRecipeViewerRecipe(
                  RecipeViewerUtils.synthetic(key.location(), "fission", MekanismGenerators.MODID),
                  IngredientCreatorAccess.chemicalStack().fromHolder(MekanismAPI.CHEMICAL_REGISTRY.getHolderOrThrow(key), amount),
                  IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.FISSILE_FUEL, 1),
                  coolant.heat(amount), MekanismChemicals.NUCLEAR_WASTE.asStack(1)
            ));
        }
        //TODO - 1.22: Remove this handling of legacy attributes
        //Go through all gases and add each legacy coolant
        for (Chemical chemical : MekanismAPI.CHEMICAL_REGISTRY) {
            ChemicalAttributes.CooledCoolant cooledCoolant = chemical.getLegacy(ChemicalAttributes.CooledCoolant.class);
            if (cooledCoolant != null) {
                //If it is a cooled coolant add a recipe for it
                Chemical heatedCoolant = cooledCoolant.getHeatedChemical();
                long amount = Math.round(energyPerFuel / cooledCoolant.getThermalEnthalpy());
                recipes.add(new FissionRecipeViewerRecipe(
                      RecipeViewerUtils.synthetic(chemical.toString(), "fission", MekanismGenerators.MODID),
                      IngredientCreatorAccess.chemicalStack().from(chemical, amount),
                      IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.FISSILE_FUEL, 1),
                      heatedCoolant.getStack(amount), MekanismChemicals.NUCLEAR_WASTE.asStack(1)
                ));
            }
        }
        return recipes;
    }
}