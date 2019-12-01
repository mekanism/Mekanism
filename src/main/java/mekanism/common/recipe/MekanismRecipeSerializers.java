package mekanism.common.recipe;

import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.GasToItemStackRecipe;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.common.Mekanism;
import mekanism.common.recipe.impl.ChemicalCrystallizerIRecipe;
import mekanism.common.recipe.impl.ChemicalInfuserIRecipe;
import mekanism.common.recipe.impl.ChemicalOxidizerIRecipe;
import mekanism.common.recipe.impl.CombinerIRecipe;
import mekanism.common.recipe.impl.CompressingIRecipe;
import mekanism.common.recipe.impl.CrushingIRecipe;
import mekanism.common.recipe.impl.ElectrolysisIRecipe;
import mekanism.common.recipe.impl.EnrichingIRecipe;
import mekanism.common.recipe.impl.FluidGasToGasIRecipe;
import mekanism.common.recipe.impl.FluidToFluidIRecipe;
import mekanism.common.recipe.impl.GasConversionIRecipe;
import mekanism.common.recipe.impl.GasToGasIRecipe;
import mekanism.common.recipe.impl.InfusionConversionIRecipe;
import mekanism.common.recipe.impl.InjectingIRecipe;
import mekanism.common.recipe.impl.ItemStackGasToGasIRecipe;
import mekanism.common.recipe.impl.MetallurgicInfuserIRecipe;
import mekanism.common.recipe.impl.PressurizedReactionIRecipe;
import mekanism.common.recipe.impl.PurifyingIRecipe;
import mekanism.common.recipe.impl.RotaryIRecipe;
import mekanism.common.recipe.impl.SawmillIRecipe;
import mekanism.common.recipe.impl.SmeltingIRecipe;
import mekanism.common.recipe.serializer.ChemicalInfuserRecipeSerializer;
import mekanism.common.recipe.serializer.CombinerRecipeSerializer;
import mekanism.common.recipe.serializer.ElectrolysisRecipeSerializer;
import mekanism.common.recipe.serializer.FluidGasToGasRecipeSerializer;
import mekanism.common.recipe.serializer.FluidToFluidRecipeSerializer;
import mekanism.common.recipe.serializer.GasToGasRecipeSerializer;
import mekanism.common.recipe.serializer.GasToItemStackRecipeSerializer;
import mekanism.common.recipe.serializer.ItemStackGasToGasRecipeSerializer;
import mekanism.common.recipe.serializer.ItemStackGasToItemStackRecipeSerializer;
import mekanism.common.recipe.serializer.ItemStackToGasRecipeSerializer;
import mekanism.common.recipe.serializer.ItemStackToInfuseTypeRecipeSerializer;
import mekanism.common.recipe.serializer.ItemStackToItemStackRecipeSerializer;
import mekanism.common.recipe.serializer.MetallurgicInfuserRecipeSerializer;
import mekanism.common.recipe.serializer.PressurizedReactionRecipeSerializer;
import mekanism.common.recipe.serializer.RotaryRecipeSerializer;
import mekanism.common.recipe.serializer.SawmillRecipeSerializer;
import mekanism.common.registration.impl.IRecipeSerializerDeferredRegister;
import mekanism.common.registration.impl.IRecipeSerializerRegistryObject;

//TODO: Should this and the serializer implementations be moved to API package? Probably at least the declarations should be moved
// Ideally we would not have to have a separate class for each one that is basically just a factory for setting the:
// serializer, recipe type, group, and icon.
// Is there some way we could declare those statically for each of the things
public class MekanismRecipeSerializers {

    public static final IRecipeSerializerDeferredRegister RECIPE_SERIALIZERS = new IRecipeSerializerDeferredRegister(Mekanism.MODID);

    public static final IRecipeSerializerRegistryObject<ItemStackToItemStackRecipe> CRUSHING = RECIPE_SERIALIZERS.register("crushing", () -> new ItemStackToItemStackRecipeSerializer<>(CrushingIRecipe::new));
    public static final IRecipeSerializerRegistryObject<ItemStackToItemStackRecipe> ENRICHING = RECIPE_SERIALIZERS.register("enriching", () -> new ItemStackToItemStackRecipeSerializer<>(EnrichingIRecipe::new));
    public static final IRecipeSerializerRegistryObject<ItemStackToItemStackRecipe> SMELTING = RECIPE_SERIALIZERS.register("smelting", () -> new ItemStackToItemStackRecipeSerializer<>(SmeltingIRecipe::new));

    public static final IRecipeSerializerRegistryObject<ChemicalInfuserRecipe> CHEMICAL_INFUSING = RECIPE_SERIALIZERS.register("chemical_infusing", () -> new ChemicalInfuserRecipeSerializer<>(ChemicalInfuserIRecipe::new));

    public static final IRecipeSerializerRegistryObject<CombinerRecipe> COMBINING = RECIPE_SERIALIZERS.register("combining", () -> new CombinerRecipeSerializer<>(CombinerIRecipe::new));

    public static final IRecipeSerializerRegistryObject<ElectrolysisRecipe> SEPARATING = RECIPE_SERIALIZERS.register("separating", () -> new ElectrolysisRecipeSerializer<>(ElectrolysisIRecipe::new));

    public static final IRecipeSerializerRegistryObject<FluidGasToGasRecipe> WASHING = RECIPE_SERIALIZERS.register("washing", () -> new FluidGasToGasRecipeSerializer<>(FluidGasToGasIRecipe::new));

    public static final IRecipeSerializerRegistryObject<FluidToFluidRecipe> EVAPORATING = RECIPE_SERIALIZERS.register("evaporating", () -> new FluidToFluidRecipeSerializer<>(FluidToFluidIRecipe::new));

    public static final IRecipeSerializerRegistryObject<GasToGasRecipe> ACTIVATING = RECIPE_SERIALIZERS.register("activating", () -> new GasToGasRecipeSerializer<>(GasToGasIRecipe::new));

    public static final IRecipeSerializerRegistryObject<GasToItemStackRecipe> CRYSTALLIZING = RECIPE_SERIALIZERS.register("crystallizing", () -> new GasToItemStackRecipeSerializer<>(ChemicalCrystallizerIRecipe::new));

    public static final IRecipeSerializerRegistryObject<ItemStackGasToGasRecipe> DISSOLUTION = RECIPE_SERIALIZERS.register("dissolution", () -> new ItemStackGasToGasRecipeSerializer<>(ItemStackGasToGasIRecipe::new));

    public static final IRecipeSerializerRegistryObject<ItemStackGasToItemStackRecipe> COMPRESSING = RECIPE_SERIALIZERS.register("compressing", () -> new ItemStackGasToItemStackRecipeSerializer<>(CompressingIRecipe::new));
    public static final IRecipeSerializerRegistryObject<ItemStackGasToItemStackRecipe> PURIFYING = RECIPE_SERIALIZERS.register("purifying", () -> new ItemStackGasToItemStackRecipeSerializer<>(PurifyingIRecipe::new));
    public static final IRecipeSerializerRegistryObject<ItemStackGasToItemStackRecipe> INJECTING = RECIPE_SERIALIZERS.register("injecting", () -> new ItemStackGasToItemStackRecipeSerializer<>(InjectingIRecipe::new));

    public static final IRecipeSerializerRegistryObject<ItemStackToGasRecipe> GAS_CONVERSION = RECIPE_SERIALIZERS.register("gas_conversion", () -> new ItemStackToGasRecipeSerializer<>(GasConversionIRecipe::new));
    public static final IRecipeSerializerRegistryObject<ItemStackToGasRecipe> OXIDIZING = RECIPE_SERIALIZERS.register("oxidizing", () -> new ItemStackToGasRecipeSerializer<>(ChemicalOxidizerIRecipe::new));

    public static final IRecipeSerializerRegistryObject<ItemStackToInfuseTypeRecipe> INFUSION_CONVERSION = RECIPE_SERIALIZERS.register("infusion_conversion", () -> new ItemStackToInfuseTypeRecipeSerializer<>(InfusionConversionIRecipe::new));

    public static final IRecipeSerializerRegistryObject<MetallurgicInfuserRecipe> METALLURGIC_INFUSING = RECIPE_SERIALIZERS.register("metallurgic_infusing", () -> new MetallurgicInfuserRecipeSerializer<>(MetallurgicInfuserIRecipe::new));

    public static final IRecipeSerializerRegistryObject<PressurizedReactionRecipe> REACTION = RECIPE_SERIALIZERS.register("reaction", () -> new PressurizedReactionRecipeSerializer<>(PressurizedReactionIRecipe::new));

    public static final IRecipeSerializerRegistryObject<RotaryRecipe> ROTARY = RECIPE_SERIALIZERS.register("rotary", () -> new RotaryRecipeSerializer<>(new RotaryIRecipe.Factory()));

    public static final IRecipeSerializerRegistryObject<SawmillRecipe> SAWING = RECIPE_SERIALIZERS.register("sawing", () -> new SawmillRecipeSerializer<>(SawmillIRecipe::new));
}