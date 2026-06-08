package mekanism.client.recipe_viewer.recipe;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.chemical.attribute.HeatedCoolant;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.recipe_viewer.INamedRVRecipe;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import org.jetbrains.annotations.Nullable;

public record BoilerRecipeViewerRecipe(Identifier id, @Nullable ChemicalStackIngredient superHeatedCoolant, FluidStackIngredient water, ChemicalStackTemplate steam,
                                       @Nullable ChemicalStackTemplate cooledCoolant, double temperature) implements INamedRVRecipe {

    private static final int WATER_AMOUNT = 1;
    public static final Codec<BoilerRecipeViewerRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Identifier.CODEC.fieldOf(SerializationConstants.ID).forGetter(BoilerRecipeViewerRecipe::id),
          ChemicalStackIngredient.CODEC.optionalFieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(recipe -> Optional.ofNullable(recipe.superHeatedCoolant())),
          FluidStackIngredient.CODEC.optionalFieldOf(SerializationConstants.FLUID_INPUT, IngredientCreatorAccess.fluid().from(BuiltInRegistries.FLUID, FluidTags.WATER, WATER_AMOUNT)).forGetter(BoilerRecipeViewerRecipe::water),
          ChemicalStackTemplate.CODEC.optionalFieldOf(SerializationConstants.MAIN_OUTPUT, MekanismChemicals.STEAM.asTemplate(WATER_AMOUNT)).forGetter(BoilerRecipeViewerRecipe::steam),
          ChemicalStackTemplate.CODEC.optionalFieldOf(SerializationConstants.SECONDARY_OUTPUT).forGetter(recipe -> Optional.ofNullable(recipe.cooledCoolant())),
          Codec.DOUBLE.optionalFieldOf(SerializationConstants.TEMPERATURE, HeatUtils.BASE_BOIL_TEMP).forGetter(BoilerRecipeViewerRecipe::temperature)
    ).apply(instance, (id, superHeatedCoolant, water, steam, cooledCoolant, temperature) ->
          new BoilerRecipeViewerRecipe(id, superHeatedCoolant.orElse(null), water, steam, cooledCoolant.orElse(null), temperature)));

    public static List<BoilerRecipeViewerRecipe> getBoilerRecipes() {
        //Note: The recipes below ignore thermal conductivity and temperature and rounds the amount of coolant
        double waterToSteamHeatNecessary = WATER_AMOUNT * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency();
        List<BoilerRecipeViewerRecipe> recipes = new ArrayList<>();
        //Special case heat only recipe
        FluidStackIngredient water = IngredientCreatorAccess.fluid().from(BuiltInRegistries.FLUID, FluidTags.WATER, WATER_AMOUNT);
        ChemicalStackTemplate steam = MekanismChemicals.STEAM.asTemplate(WATER_AMOUNT);
        recipes.add(new BoilerRecipeViewerRecipe(
              RegistryUtils.synthetic(Mekanism.rl("water"), "boiler"),
              null, water,
              steam, null,
              HeatUtils.BASE_BOIL_TEMP + waterToSteamHeatNecessary / (BoilerMultiblockData.CASING_HEAT_CAPACITY * MekanismConfig.general.boilerWaterConductivity.get())
        ));
        //Add recipes for all heated coolants
        for (Map.Entry<ResourceKey<Chemical>, HeatedCoolant> entry : MekanismAPI.CHEMICAL_REGISTRY.getDataMap(IMekanismDataMapTypes.INSTANCE.heatedChemicalCoolant()).entrySet()) {
            ResourceKey<Chemical> key = entry.getKey();
            HeatedCoolant coolant = entry.getValue();
            //Amount of coolant that is actually used to
            int coolantAmount = Ints.saturatedCast(Math.round(waterToSteamHeatNecessary / coolant.thermalEnthalpy()));
            recipes.add(new BoilerRecipeViewerRecipe(
                  RegistryUtils.synthetic(key.identifier(), "boiler", Mekanism.MODID),
                  IngredientCreatorAccess.chemicalStack().fromHolder(MekanismAPI.CHEMICAL_REGISTRY.getOrThrow(key), coolantAmount), water,
                  steam, ChemicalStackTemplate.fromNonEmptyStack(coolant.cool().toStack(coolantAmount)),
                  HeatUtils.BASE_BOIL_TEMP
            ));
        }
        return recipes;
    }
}