package mekanism.api.recipes;

import mekanism.api.MekanismAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

public class MekanismRecipeTypes {

    public static final ResourceLocation NAME_CRUSHING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "crushing");
    public static final ResourceLocation NAME_ENRICHING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "enriching");
    public static final ResourceLocation NAME_SMELTING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "smelting");
    public static final ResourceLocation NAME_CHEMICAL_INFUSING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "chemical_infusing");
    public static final ResourceLocation NAME_COMBINING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "combining");
    public static final ResourceLocation NAME_SEPARATING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "separating");
    public static final ResourceLocation NAME_WASHING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "washing");
    public static final ResourceLocation NAME_EVAPORATING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "evaporating");
    public static final ResourceLocation NAME_ACTIVATING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "activating");
    public static final ResourceLocation NAME_CENTRIFUGING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "centrifuging");
    public static final ResourceLocation NAME_CRYSTALLIZING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "crystallizing");
    public static final ResourceLocation NAME_DISSOLUTION = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "dissolution");
    public static final ResourceLocation NAME_COMPRESSING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "compressing");
    public static final ResourceLocation NAME_PURIFYING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "purifying");
    public static final ResourceLocation NAME_INJECTING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "injecting");
    public static final ResourceLocation NAME_NUCLEOSYNTHESIZING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "nucleosynthesizing");
    public static final ResourceLocation NAME_ENERGY_CONVERSION = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "energy_conversion");
    public static final ResourceLocation NAME_GAS_CONVERSION = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "gas_conversion");
    public static final ResourceLocation NAME_OXIDIZING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "oxidizing");
    public static final ResourceLocation NAME_INFUSION_CONVERSION = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "infusion_conversion");
    public static final ResourceLocation NAME_PIGMENT_EXTRACTING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "pigment_extracting");
    public static final ResourceLocation NAME_PIGMENT_MIXING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "pigment_mixing");
    public static final ResourceLocation NAME_METALLURGIC_INFUSING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "metallurgic_infusing");
    public static final ResourceLocation NAME_PAINTING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "painting");
    public static final ResourceLocation NAME_REACTION = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "reaction");
    public static final ResourceLocation NAME_ROTARY = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "rotary");
    public static final ResourceLocation NAME_SAWING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "sawing");

    public static final RegistryObject<RecipeType<ItemStackToItemStackRecipe>> TYPE_CRUSHING = RegistryObject.create(NAME_CRUSHING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<ItemStackToItemStackRecipe>> TYPE_ENRICHING = RegistryObject.create(NAME_ENRICHING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<ItemStackToItemStackRecipe>> TYPE_SMELTING = RegistryObject.create(NAME_SMELTING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<ChemicalInfuserRecipe>> TYPE_CHEMICAL_INFUSING = RegistryObject.create(NAME_CHEMICAL_INFUSING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<CombinerRecipe>> TYPE_COMBINING = RegistryObject.create(NAME_COMBINING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<ElectrolysisRecipe>> TYPE_SEPARATING = RegistryObject.create(NAME_SEPARATING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<FluidSlurryToSlurryRecipe>> TYPE_WASHING = RegistryObject.create(NAME_WASHING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<FluidToFluidRecipe>> TYPE_EVAPORATING = RegistryObject.create(NAME_EVAPORATING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<GasToGasRecipe>> TYPE_ACTIVATING = RegistryObject.create(NAME_ACTIVATING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<GasToGasRecipe>> TYPE_CENTRIFUGING = RegistryObject.create(NAME_CENTRIFUGING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<ChemicalCrystallizerRecipe>> TYPE_CRYSTALLIZING = RegistryObject.create(NAME_CRYSTALLIZING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<ChemicalDissolutionRecipe>> TYPE_DISSOLUTION = RegistryObject.create(NAME_DISSOLUTION, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<ItemStackGasToItemStackRecipe>> TYPE_COMPRESSING = RegistryObject.create(NAME_COMPRESSING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<ItemStackGasToItemStackRecipe>> TYPE_PURIFYING = RegistryObject.create(NAME_PURIFYING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<ItemStackGasToItemStackRecipe>> TYPE_INJECTING = RegistryObject.create(NAME_INJECTING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<NucleosynthesizingRecipe>> TYPE_NUCLEOSYNTHESIZING = RegistryObject.create(NAME_NUCLEOSYNTHESIZING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<ItemStackToEnergyRecipe>> TYPE_ENERGY_CONVERSION = RegistryObject.create(NAME_ENERGY_CONVERSION, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<ItemStackToGasRecipe>> TYPE_GAS_CONVERSION = RegistryObject.create(NAME_GAS_CONVERSION, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<ItemStackToGasRecipe>> TYPE_OXIDIZING = RegistryObject.create(NAME_OXIDIZING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<ItemStackToInfuseTypeRecipe>> TYPE_INFUSION_CONVERSION = RegistryObject.create(NAME_INFUSION_CONVERSION, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<ItemStackToPigmentRecipe>> TYPE_PIGMENT_EXTRACTING = RegistryObject.create(NAME_PIGMENT_EXTRACTING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<PigmentMixingRecipe>> TYPE_PIGMENT_MIXING = RegistryObject.create(NAME_PIGMENT_MIXING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<MetallurgicInfuserRecipe>> TYPE_METALLURGIC_INFUSING = RegistryObject.create(NAME_METALLURGIC_INFUSING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<PaintingRecipe>> TYPE_PAINTING = RegistryObject.create(NAME_PAINTING, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<PressurizedReactionRecipe>> TYPE_REACTION = RegistryObject.create(NAME_REACTION, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<RotaryRecipe>> TYPE_ROTARY = RegistryObject.create(NAME_ROTARY, ForgeRegistries.RECIPE_TYPES);
    public static final RegistryObject<RecipeType<SawmillRecipe>> TYPE_SAWING = RegistryObject.create(NAME_SAWING, ForgeRegistries.RECIPE_TYPES);

}
