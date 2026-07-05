package mekanism.client.recipe_viewer.recipe;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mekanism.api.MekanismRegistries;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.chemical.attribute.HeatedCoolant;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.recipe_viewer.INamedRVRecipe;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.context.ContextMap;
import org.jspecify.annotations.Nullable;

public record BoilerRecipeViewerRecipe(Identifier id, @Nullable ChemicalStackIngredient superHeatedCoolant, FluidStackIngredient water, @Nullable ChemicalStackTemplate steam,
                                       @Nullable ChemicalStackTemplate cooledCoolant, double temperature) implements INamedRVRecipe {

    private static final int WATER_AMOUNT = 1;
    public static final Codec<BoilerRecipeViewerRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Identifier.CODEC.fieldOf(SerializationConstants.ID).forGetter(BoilerRecipeViewerRecipe::id),
          ChemicalStackIngredient.CODEC.optionalFieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(recipe -> Optional.ofNullable(recipe.superHeatedCoolant())),
          FluidStackIngredient.CODEC.optionalFieldOf(SerializationConstants.FLUID_INPUT, IngredientCreatorAccess.fluid().from(BuiltInRegistries.FLUID, FluidTags.WATER, WATER_AMOUNT)).forGetter(BoilerRecipeViewerRecipe::water),
          ChemicalStackTemplate.CODEC.optionalFieldOf(SerializationConstants.MAIN_OUTPUT).forGetter(recipe -> Optional.ofNullable(recipe.steam())),
          ChemicalStackTemplate.CODEC.optionalFieldOf(SerializationConstants.SECONDARY_OUTPUT).forGetter(recipe -> Optional.ofNullable(recipe.cooledCoolant())),
          Codec.DOUBLE.optionalFieldOf(SerializationConstants.TEMPERATURE, HeatUtils.BASE_BOIL_TEMP).forGetter(BoilerRecipeViewerRecipe::temperature)
    ).apply(instance, (id, superHeatedCoolant, water, steam, cooledCoolant, temperature) ->
          new BoilerRecipeViewerRecipe(id, superHeatedCoolant.orElse(null), water, steam.orElse(null), cooledCoolant.orElse(null), temperature)));

    public int steamAmount() {
        return steam == null ? WATER_AMOUNT : steam.amount();
    }

    public List<ChemicalStackTemplate> steamRepresentations(ContextMap contextMap) {
        return steam == null ? Collections.emptyList() : Collections.singletonList(steam);
    }

    public static List<BoilerRecipeViewerRecipe> getBoilerRecipes() {
        Optional<Registry<Chemical>> optionalRegistry = RecipeViewerUtils.getRegistry(MekanismRegistries.Keys.CHEMICAL);
        if (optionalRegistry.isEmpty()) {
            //Something went horribly wrong, bail
            Mekanism.logger.warn("Failed to find chemical registry when generating boiler recipes");
            return Collections.emptyList();
        }
        Registry<Chemical> chemicals = optionalRegistry.get();
        Optional<Reference<Chemical>> steamReference = chemicals.get(ChemicalIds.STEAM);
        if (steamReference.isEmpty()) {
            return Collections.emptyList();
        }
        //Note: The recipes below ignore thermal conductivity and temperature and rounds the amount of coolant
        double waterToSteamHeatNecessary = WATER_AMOUNT * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency();
        List<BoilerRecipeViewerRecipe> recipes = new ArrayList<>();
        //Special case heat only recipe
        FluidStackIngredient water = IngredientCreatorAccess.fluid().from(BuiltInRegistries.FLUID, FluidTags.WATER, WATER_AMOUNT);
        ChemicalStackTemplate steam = new ChemicalStackTemplate(steamReference.get(), WATER_AMOUNT);
        recipes.add(new BoilerRecipeViewerRecipe(
              RegistryUtils.synthetic(Mekanism.rl("water"), "boiler"),
              null, water,
              steam, null,
              HeatUtils.BASE_BOIL_TEMP + waterToSteamHeatNecessary / (BoilerMultiblockData.CASING_HEAT_CAPACITY * MekanismConfig.general.boilerWaterConductivity.get())
        ));
        //Add recipes for all heated coolants
        for (Map.Entry<ResourceKey<Chemical>, HeatedCoolant> entry : chemicals.getDataMap(IMekanismDataMapTypes.INSTANCE.heatedChemicalCoolant()).entrySet()) {
            ResourceKey<Chemical> key = entry.getKey();
            Optional<Reference<Chemical>> reference = chemicals.get(key);
            if (reference.isPresent()) {
                HeatedCoolant coolant = entry.getValue();
                //Amount of coolant that is actually used to
                int coolantAmount = Ints.saturatedCast(Math.round(waterToSteamHeatNecessary / coolant.thermalEnthalpy()));
                recipes.add(new BoilerRecipeViewerRecipe(
                      RegistryUtils.synthetic(key.identifier(), "boiler", Mekanism.MODID),
                      IngredientCreatorAccess.chemicalStack().fromHolder(reference.get(), coolantAmount), water,
                      steam, new ChemicalStackTemplate(coolant.otherVariant(), coolantAmount),
                      HeatUtils.BASE_BOIL_TEMP
                ));
            }
        }
        return recipes;
    }
}