package mekanism.api.recipes;

import mekanism.api.MekanismAPI;
import mekanism.api.recipes.basic.BasicActivatingRecipe;
import mekanism.api.recipes.basic.BasicCentrifugingRecipe;
import mekanism.api.recipes.basic.BasicChemicalConversionRecipe;
import mekanism.api.recipes.basic.BasicChemicalCrystallizerRecipe;
import mekanism.api.recipes.basic.BasicChemicalDissolutionRecipe;
import mekanism.api.recipes.basic.BasicChemicalInfuserRecipe;
import mekanism.api.recipes.basic.BasicChemicalOxidizerRecipe;
import mekanism.api.recipes.basic.BasicCombinerRecipe;
import mekanism.api.recipes.basic.BasicCompressingRecipe;
import mekanism.api.recipes.basic.BasicCrushingRecipe;
import mekanism.api.recipes.basic.BasicElectrolysisRecipe;
import mekanism.api.recipes.basic.BasicEnrichingRecipe;
import mekanism.api.recipes.basic.BasicFluidToFluidRecipe;
import mekanism.api.recipes.basic.BasicInjectingRecipe;
import mekanism.api.recipes.basic.BasicItemStackToEnergyRecipe;
import mekanism.api.recipes.basic.BasicMetallurgicInfuserRecipe;
import mekanism.api.recipes.basic.BasicNucleosynthesizingRecipe;
import mekanism.api.recipes.basic.BasicPaintingRecipe;
import mekanism.api.recipes.basic.BasicPigmentExtractingRecipe;
import mekanism.api.recipes.basic.BasicPigmentMixingRecipe;
import mekanism.api.recipes.basic.BasicPressurizedReactionRecipe;
import mekanism.api.recipes.basic.BasicPurifyingRecipe;
import mekanism.api.recipes.basic.BasicRotaryRecipe;
import mekanism.api.recipes.basic.BasicSawmillRecipe;
import mekanism.api.recipes.basic.BasicSmeltingRecipe;
import mekanism.api.recipes.basic.BasicWashingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MekanismRecipeSerializers {

    public static class Names {

        private static ResourceKey<RecipeSerializer<?>> key(String name) {
            return ResourceKey.create(Registries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, name));
        }

        public static final ResourceKey<RecipeSerializer<?>> CRUSHING = key("crushing");
        public static final ResourceKey<RecipeSerializer<?>> ENRICHING = key("enriching");
        public static final ResourceKey<RecipeSerializer<?>> SMELTING = key("smelting");
        public static final ResourceKey<RecipeSerializer<?>> CHEMICAL_INFUSING = key("chemical_infusing");
        public static final ResourceKey<RecipeSerializer<?>> COMBINING = key("combining");
        public static final ResourceKey<RecipeSerializer<?>> SEPARATING = key("separating");
        public static final ResourceKey<RecipeSerializer<?>> WASHING = key("washing");
        public static final ResourceKey<RecipeSerializer<?>> EVAPORATING = key("evaporating");
        public static final ResourceKey<RecipeSerializer<?>> ACTIVATING = key("activating");
        public static final ResourceKey<RecipeSerializer<?>> CENTRIFUGING = key("centrifuging");
        public static final ResourceKey<RecipeSerializer<?>> CRYSTALLIZING = key("crystallizing");
        public static final ResourceKey<RecipeSerializer<?>> DISSOLUTION = key("dissolution");
        public static final ResourceKey<RecipeSerializer<?>> COMPRESSING = key("compressing");
        public static final ResourceKey<RecipeSerializer<?>> PURIFYING = key("purifying");
        public static final ResourceKey<RecipeSerializer<?>> INJECTING = key("injecting");
        public static final ResourceKey<RecipeSerializer<?>> NUCLEOSYNTHESIZING = key("nucleosynthesizing");
        public static final ResourceKey<RecipeSerializer<?>> ENERGY_CONVERSION = key("energy_conversion");
        public static final ResourceKey<RecipeSerializer<?>> CHEMICAL_CONVERSION = key("chemical_conversion");
        public static final ResourceKey<RecipeSerializer<?>> OXIDIZING = key("oxidizing");
        public static final ResourceKey<RecipeSerializer<?>> PIGMENT_EXTRACTING = key("pigment_extracting");
        public static final ResourceKey<RecipeSerializer<?>> PIGMENT_MIXING = key("pigment_mixing");
        public static final ResourceKey<RecipeSerializer<?>> METALLURGIC_INFUSING = key("metallurgic_infusing");
        public static final ResourceKey<RecipeSerializer<?>> PAINTING = key("painting");
        public static final ResourceKey<RecipeSerializer<?>> REACTION = key("reaction");
        public static final ResourceKey<RecipeSerializer<?>> ROTARY = key("rotary");
        public static final ResourceKey<RecipeSerializer<?>> SAWING = key("sawing");
    }

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicCrushingRecipe>> CRUSHING = DeferredHolder.create(Names.CRUSHING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicEnrichingRecipe>> ENRICHING = DeferredHolder.create(Names.ENRICHING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicSmeltingRecipe>> SMELTING = DeferredHolder.create(Names.SMELTING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicChemicalInfuserRecipe>> CHEMICAL_INFUSING = DeferredHolder.create(Names.CHEMICAL_INFUSING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicCombinerRecipe>> COMBINING = DeferredHolder.create(Names.COMBINING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicElectrolysisRecipe>> SEPARATING = DeferredHolder.create(Names.SEPARATING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicWashingRecipe>> WASHING = DeferredHolder.create(Names.WASHING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicFluidToFluidRecipe>> EVAPORATING = DeferredHolder.create(Names.EVAPORATING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicActivatingRecipe>> ACTIVATING = DeferredHolder.create(Names.ACTIVATING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicCentrifugingRecipe>> CENTRIFUGING = DeferredHolder.create(Names.CENTRIFUGING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicChemicalCrystallizerRecipe>> CRYSTALLIZING = DeferredHolder.create(Names.CRYSTALLIZING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicChemicalDissolutionRecipe>> DISSOLUTION = DeferredHolder.create(Names.DISSOLUTION);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicCompressingRecipe>> COMPRESSING = DeferredHolder.create(Names.COMPRESSING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicPurifyingRecipe>> PURIFYING = DeferredHolder.create(Names.PURIFYING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicInjectingRecipe>> INJECTING = DeferredHolder.create(Names.INJECTING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicNucleosynthesizingRecipe>> NUCLEOSYNTHESIZING = DeferredHolder.create(Names.NUCLEOSYNTHESIZING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicItemStackToEnergyRecipe>> ENERGY_CONVERSION = DeferredHolder.create(Names.ENERGY_CONVERSION);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicChemicalConversionRecipe>> CHEMICAL_CONVERSION = DeferredHolder.create(Names.CHEMICAL_CONVERSION);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicChemicalOxidizerRecipe>> OXIDIZING = DeferredHolder.create(Names.OXIDIZING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicPigmentExtractingRecipe>> PIGMENT_EXTRACTING = DeferredHolder.create(Names.PIGMENT_EXTRACTING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicPigmentMixingRecipe>> PIGMENT_MIXING = DeferredHolder.create(Names.PIGMENT_MIXING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicMetallurgicInfuserRecipe>> METALLURGIC_INFUSING = DeferredHolder.create(Names.METALLURGIC_INFUSING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicPaintingRecipe>> PAINTING = DeferredHolder.create(Names.PAINTING);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicPressurizedReactionRecipe>> REACTION = DeferredHolder.create(Names.REACTION);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicRotaryRecipe>> ROTARY = DeferredHolder.create(Names.ROTARY);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BasicSawmillRecipe>> SAWING = DeferredHolder.create(Names.SAWING);
}
