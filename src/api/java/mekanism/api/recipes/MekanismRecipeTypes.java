package mekanism.api.recipes;

import mekanism.api.MekanismAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MekanismRecipeTypes {

    public static final ResourceLocation NAME_CRUSHING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "crushing");
    public static final ResourceLocation NAME_ENRICHING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "enriching");
    public static final ResourceLocation NAME_SMELTING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "smelting");
    public static final ResourceLocation NAME_CHEMICAL_INFUSING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_infusing");
    public static final ResourceLocation NAME_COMBINING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "combining");
    public static final ResourceLocation NAME_SEPARATING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "separating");
    public static final ResourceLocation NAME_WASHING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "washing");
    public static final ResourceLocation NAME_EVAPORATING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "evaporating");
    public static final ResourceLocation NAME_ACTIVATING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "activating");
    public static final ResourceLocation NAME_CENTRIFUGING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "centrifuging");
    public static final ResourceLocation NAME_CRYSTALLIZING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "crystallizing");
    public static final ResourceLocation NAME_DISSOLUTION = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "dissolution");
    public static final ResourceLocation NAME_COMPRESSING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "compressing");
    public static final ResourceLocation NAME_PURIFYING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "purifying");
    public static final ResourceLocation NAME_INJECTING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "injecting");
    public static final ResourceLocation NAME_NUCLEOSYNTHESIZING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "nucleosynthesizing");
    public static final ResourceLocation NAME_ENERGY_CONVERSION = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "energy_conversion");
    public static final ResourceLocation NAME_CHEMICAL_CONVERSION = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_conversion");
    public static final ResourceLocation NAME_OXIDIZING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "oxidizing");
    public static final ResourceLocation NAME_PIGMENT_EXTRACTING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "pigment_extracting");
    public static final ResourceLocation NAME_PIGMENT_MIXING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "pigment_mixing");
    public static final ResourceLocation NAME_METALLURGIC_INFUSING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "metallurgic_infusing");
    public static final ResourceLocation NAME_PAINTING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "painting");
    public static final ResourceLocation NAME_REACTION = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "reaction");
    public static final ResourceLocation NAME_ROTARY = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "rotary");
    public static final ResourceLocation NAME_SAWING = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "sawing");

    public static final DeferredHolder<RecipeType<?>, RecipeType<ItemStackToItemStackRecipe>> TYPE_CRUSHING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_CRUSHING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ItemStackToItemStackRecipe>> TYPE_ENRICHING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_ENRICHING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ItemStackToItemStackRecipe>> TYPE_SMELTING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_SMELTING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ChemicalChemicalToChemicalRecipe>> TYPE_CHEMICAL_INFUSING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_CHEMICAL_INFUSING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<CombinerRecipe>> TYPE_COMBINING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_COMBINING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ElectrolysisRecipe>> TYPE_SEPARATING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_SEPARATING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<FluidChemicalToChemicalRecipe>> TYPE_WASHING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_WASHING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<FluidToFluidRecipe>> TYPE_EVAPORATING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_EVAPORATING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ChemicalToChemicalRecipe>> TYPE_ACTIVATING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_ACTIVATING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ChemicalToChemicalRecipe>> TYPE_CENTRIFUGING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_CENTRIFUGING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ChemicalCrystallizerRecipe>> TYPE_CRYSTALLIZING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_CRYSTALLIZING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ChemicalDissolutionRecipe>> TYPE_DISSOLUTION = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_DISSOLUTION);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ItemStackChemicalToItemStackRecipe>> TYPE_COMPRESSING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_COMPRESSING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ItemStackChemicalToItemStackRecipe>> TYPE_PURIFYING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_PURIFYING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ItemStackChemicalToItemStackRecipe>> TYPE_INJECTING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_INJECTING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<NucleosynthesizingRecipe>> TYPE_NUCLEOSYNTHESIZING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_NUCLEOSYNTHESIZING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ItemStackToEnergyRecipe>> TYPE_ENERGY_CONVERSION = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_ENERGY_CONVERSION);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ItemStackToChemicalRecipe>> TYPE_CHEMICAL_CONVERSION = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_CHEMICAL_CONVERSION);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ItemStackToChemicalRecipe>> TYPE_OXIDIZING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_OXIDIZING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ItemStackToChemicalRecipe>> TYPE_PIGMENT_EXTRACTING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_PIGMENT_EXTRACTING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ChemicalChemicalToChemicalRecipe>> TYPE_PIGMENT_MIXING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_PIGMENT_MIXING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ItemStackChemicalToItemStackRecipe>> TYPE_METALLURGIC_INFUSING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_METALLURGIC_INFUSING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ItemStackChemicalToItemStackRecipe>> TYPE_PAINTING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_PAINTING);
    public static final DeferredHolder<RecipeType<?>, RecipeType<PressurizedReactionRecipe>> TYPE_REACTION = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_REACTION);
    public static final DeferredHolder<RecipeType<?>, RecipeType<RotaryRecipe>> TYPE_ROTARY = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_ROTARY);
    public static final DeferredHolder<RecipeType<?>, RecipeType<SawmillRecipe>> TYPE_SAWING = DeferredHolder.create(Registries.RECIPE_TYPE, NAME_SAWING);

}
