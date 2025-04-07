package mekanism.client.recipe_viewer.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.attribute.ChemicalAttributes;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.chemical.attribute.HeatedCoolant;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.INamedRVRecipe;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import org.jetbrains.annotations.Nullable;

public record BoilerRecipeViewerRecipe(
      ResourceLocation id,
      @Nullable InputIngredient<?> heatedCoolant, //Either FluidStackIngredient | ChemicalStackIngredient
      FluidStackIngredient water,
      ChemicalStack steam,
      Object cooledCoolant, //Either FluidStack | ChemicalStack
      double temperature) implements INamedRVRecipe {

    public static final MapCodec<InputIngredient<?>> FLUID_OR_CHEMICAL_INGREDIENT = MekCodecs.alternativeElement(
          FluidStackIngredient.CODEC.fieldOf(SerializationConstants.FLUID_INPUT),
          ChemicalStackIngredient.CODEC.optionalFieldOf(SerializationConstants.CHEMICAL_INPUT, null),
          ingredient -> switch (ingredient) {
              case FluidStackIngredient fluid -> DataResult.success(Either.left(fluid));
              case ChemicalStackIngredient chemical -> DataResult.success(Either.right(chemical));
              case null -> DataResult.success(Either.right(null)); //Relay null to the codec with .optionalFieldOf()
              default -> DataResult.error(() -> "Bad ingredient: expected fluid or chemical, got " + ingredient);
          }
    );

    private static final int WATER_AMOUNT = 1;
    public static final Codec<BoilerRecipeViewerRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          ResourceLocation.CODEC.fieldOf(SerializationConstants.ID).forGetter(BoilerRecipeViewerRecipe::id),
          FLUID_OR_CHEMICAL_INGREDIENT.forGetter(BoilerRecipeViewerRecipe::heatedCoolant),
          FluidStackIngredient.CODEC.optionalFieldOf(SerializationConstants.FLUID_INPUT, IngredientCreatorAccess.fluid().from(FluidTags.WATER, WATER_AMOUNT)).forGetter(BoilerRecipeViewerRecipe::water),
          ChemicalStack.CODEC.optionalFieldOf(SerializationConstants.MAIN_OUTPUT, MekanismChemicals.STEAM.asStack(WATER_AMOUNT)).forGetter(BoilerRecipeViewerRecipe::steam),
          MekCodecs.FLUID_OR_CHEMICAL_STACK_LEGACY.optionalFieldOf(SerializationConstants.SECONDARY_OUTPUT, ChemicalStack.EMPTY).forGetter(BoilerRecipeViewerRecipe::cooledCoolant),
          Codec.DOUBLE.optionalFieldOf(SerializationConstants.TEMPERATURE, HeatUtils.BASE_BOIL_TEMP).forGetter(BoilerRecipeViewerRecipe::temperature)
    ).apply(instance, BoilerRecipeViewerRecipe::new));

    @SuppressWarnings("removal")
    public static List<BoilerRecipeViewerRecipe> getBoilerRecipes() {
        //Note: The recipes below ignore thermal conductivity and temperature and rounds the amount of coolant
        double waterToSteamHeatNecessary = WATER_AMOUNT * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency();
        List<BoilerRecipeViewerRecipe> recipes = new ArrayList<>();
        //Special case heat only recipe
        FluidStackIngredient water = IngredientCreatorAccess.fluid().from(FluidTags.WATER, WATER_AMOUNT);
        ChemicalStack steam = MekanismChemicals.STEAM.asStack(WATER_AMOUNT);
        recipes.add(new BoilerRecipeViewerRecipe(
              RecipeViewerUtils.synthetic(Mekanism.rl("water"), "boiler"),
              null, water,
              steam, ChemicalStack.EMPTY,
              HeatUtils.BASE_BOIL_TEMP + waterToSteamHeatNecessary / (BoilerMultiblockData.CASING_HEAT_CAPACITY * MekanismConfig.general.boilerWaterConductivity.get())
        ));
        //Add recipes for all heated coolants
        for (Entry<ResourceKey<Chemical>, HeatedCoolant> entry : MekanismAPI.CHEMICAL_REGISTRY.getDataMap(IMekanismDataMapTypes.INSTANCE.heatedChemicalCoolant()).entrySet()) {
            ResourceKey<Chemical> key = entry.getKey();
            HeatedCoolant coolant = entry.getValue();
            //Amount of coolant that is actually used to
            long coolantAmount = Math.round(waterToSteamHeatNecessary / coolant.thermalEnthalpy());
            recipes.add(new BoilerRecipeViewerRecipe(
                  RecipeViewerUtils.synthetic(key.location(), "boiler", Mekanism.MODID),
                  IngredientCreatorAccess.chemicalStack().fromHolder(MekanismAPI.CHEMICAL_REGISTRY.getHolderOrThrow(key), coolantAmount), water,
                  steam, new ChemicalStack(coolant.otherChemical(), coolantAmount),
                  HeatUtils.BASE_BOIL_TEMP
            ));
        }
        //TODO - 1.22: Remove this handling of legacy attributes
        //Go through all gases and add each legacy coolant
        for (Chemical gas : MekanismAPI.CHEMICAL_REGISTRY) {
            ChemicalAttributes.HeatedCoolant heatedCoolant = gas.getLegacy(ChemicalAttributes.HeatedCoolant.class);
            if (heatedCoolant != null) {
                //If it is a cooled coolant add a recipe for it
                long coolantAmount = Math.round(waterToSteamHeatNecessary / heatedCoolant.getThermalEnthalpy());
                recipes.add(new BoilerRecipeViewerRecipe(
                      RecipeViewerUtils.synthetic(gas.toString(), "boiler", Mekanism.MODID),
                      IngredientCreatorAccess.chemicalStack().from(gas, coolantAmount), water,
                      steam, heatedCoolant.getCooledChemical().getStack(coolantAmount),
                      HeatUtils.BASE_BOIL_TEMP
                ));
            }
        }
        return recipes;
    }
}