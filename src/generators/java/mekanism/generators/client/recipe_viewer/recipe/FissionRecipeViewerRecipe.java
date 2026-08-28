package mekanism.generators.client.recipe_viewer.recipe;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import mekanism.api.MekanismRegistries;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.chemical.attribute.CooledCoolant;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackSlotDisplay;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.recipe_viewer.INamedRVRecipe;
import mekanism.common.Mekanism;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.RegistryUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

//If null -> coolant is water
public record FissionRecipeViewerRecipe(Identifier id, @Nullable ChemicalStackIngredient inputCoolant, ChemicalStackIngredient fuel, ChemicalStackTemplate outputCoolant,
                                        ChemicalStackTemplate waste) implements INamedRVRecipe {

    public static final Codec<FissionRecipeViewerRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Identifier.CODEC.fieldOf(SerializationConstants.ID).forGetter(FissionRecipeViewerRecipe::id),
          ChemicalStackIngredient.CODEC.optionalFieldOf(SerializationConstants.EXTRA_INPUT).forGetter(recipe -> Optional.ofNullable(recipe.inputCoolant())),
          ChemicalStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(FissionRecipeViewerRecipe::fuel),
          ChemicalStackTemplate.CODEC.fieldOf(SerializationConstants.SECONDARY_OUTPUT).forGetter(FissionRecipeViewerRecipe::outputCoolant),
          ChemicalStackTemplate.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(FissionRecipeViewerRecipe::waste)
    ).apply(instance, (id, inputCoolant, fuel, outputCoolant, waste) ->
          new FissionRecipeViewerRecipe(id, inputCoolant.orElse(null), fuel, outputCoolant, waste)));

    public FluidStackIngredient waterInput() {
        return IngredientCreatorAccess.fluid().from(BuiltInRegistries.FLUID, FluidTags.WATER, outputCoolant().amount());
    }

    public static List<FissionRecipeViewerRecipe> getFissionRecipes(HolderLookup.Provider registries) {
        Optional<? extends HolderLookup.RegistryLookup<Chemical>> optionalRegistry = registries.lookup(MekanismRegistries.Keys.CHEMICAL);
        if (optionalRegistry.isEmpty()) {
            //Something went horribly wrong, bail
            Mekanism.logger.warn("Failed to find chemical registry when generating fission recipes");
            return Collections.emptyList();
        }
        HolderLookup.RegistryLookup<Chemical> chemicals = optionalRegistry.get();
        Optional<Reference<Chemical>> wasteReference = chemicals.get(ChemicalIds.NUCLEAR_WASTE);
        Optional<Reference<Chemical>> fuelReference = chemicals.get(ChemicalIds.FISSILE_FUEL);
        if (wasteReference.isEmpty() || fuelReference.isEmpty()) {
            return Collections.emptyList();
        }
        Holder<Chemical> waste = wasteReference.get();
        Holder<Chemical> fuel = fuelReference.get();

        //Note: The recipes below ignore thermal conductivity and just take enthalpy into account and it rounds the amount of coolant
        //TODO: Eventually we may want to try and improve on that but for now this should be fine
        List<FissionRecipeViewerRecipe> recipes = new ArrayList<>();
        long energyPerFuel = MekanismGeneratorsConfig.generators.energyPerFissionFuel.get();
        //Special case water recipe
        int coolantAmount = Ints.saturatedCast(Math.round(energyPerFuel * HeatUtils.getSteamEnergyEfficiency() / HeatUtils.getWaterThermalEnthalpy()));
        Optional<Reference<Chemical>> steamReference = chemicals.get(ChemicalIds.STEAM);
        //noinspection OptionalIsPresent - Capturing lambda
        if (steamReference.isPresent()) {
            recipes.add(new FissionRecipeViewerRecipe(
                  RegistryUtils.synthetic(MekanismGenerators.rl("water"), "fission"),
                  null, IngredientCreatorAccess.chemicalStack().fromHolder(fuel, 1),
                  new ChemicalStackTemplate(steamReference.get(), coolantAmount), new ChemicalStackTemplate(waste, 1)
            ));
        }
        //Add recipes for all cooled coolants
        for (Entry<ResourceKey<Chemical>, CooledCoolant> entry : chemicals.getDataMap(IMekanismDataMapTypes.INSTANCE.cooledChemicalCoolant()).entrySet()) {
            ResourceKey<Chemical> key = entry.getKey();
            Optional<Reference<Chemical>> reference = chemicals.get(key);
            if (reference.isPresent()) {
                CooledCoolant coolant = entry.getValue();
                int amount = Ints.saturatedCast(Math.round(energyPerFuel / coolant.thermalEnthalpy()));
                recipes.add(new FissionRecipeViewerRecipe(
                      RegistryUtils.synthetic(key.identifier(), "fission", MekanismGenerators.MODID),
                      IngredientCreatorAccess.chemicalStack().fromHolder(reference.get(), amount),
                      IngredientCreatorAccess.chemicalStack().fromHolder(fuel, 1),
                      new ChemicalStackTemplate(coolant.otherVariant(), amount), new ChemicalStackTemplate(waste, 1)
                ));
            }
        }
        return recipes;
    }

    public SlotDisplay wasteDisplay() {
        return new ChemicalStackSlotDisplay(waste);
    }

    public SlotDisplay outputCoolantDisplay() {
        return new ChemicalStackSlotDisplay(outputCoolant);
    }
}