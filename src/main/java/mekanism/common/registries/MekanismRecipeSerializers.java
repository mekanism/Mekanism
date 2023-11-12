package mekanism.common.registries;

import mekanism.api.recipes.basic.BasicChemicalCrystallizerRecipe;
import mekanism.api.recipes.basic.BasicChemicalInfuserRecipe;
import mekanism.api.recipes.basic.BasicCombinerRecipe;
import mekanism.api.recipes.basic.BasicElectrolysisRecipe;
import mekanism.api.recipes.basic.BasicFluidSlurryToSlurryRecipe;
import mekanism.api.recipes.basic.BasicFluidToFluidRecipe;
import mekanism.api.recipes.basic.BasicItemStackToPigmentRecipe;
import mekanism.api.recipes.basic.BasicMetallurgicInfuserRecipe;
import mekanism.api.recipes.basic.BasicNucleosynthesizingRecipe;
import mekanism.api.recipes.basic.BasicPaintingRecipe;
import mekanism.api.recipes.basic.BasicPigmentMixingRecipe;
import mekanism.api.recipes.basic.BasicPressurizedReactionRecipe;
import mekanism.api.recipes.basic.BasicRotaryRecipe;
import mekanism.api.recipes.basic.BasicSawmillRecipe;
import mekanism.common.Mekanism;
import mekanism.common.recipe.bin.BinExtractRecipe;
import mekanism.common.recipe.bin.BinInsertRecipe;
import mekanism.api.recipes.basic.BasicActivatingRecipe;
import mekanism.common.recipe.impl.BasicCentrifugingRecipe;
import mekanism.common.recipe.impl.ChemicalCrystallizerIRecipe;
import mekanism.common.recipe.impl.ChemicalDissolutionIRecipe;
import mekanism.common.recipe.impl.ChemicalInfuserIRecipe;
import mekanism.common.recipe.impl.BasicChemicalOxidizerRecipe;
import mekanism.common.recipe.impl.CombinerIRecipe;
import mekanism.common.recipe.impl.BasicCompressingRecipe;
import mekanism.common.recipe.impl.BasicCrushingRecipe;
import mekanism.common.recipe.impl.ElectrolysisIRecipe;
import mekanism.common.recipe.impl.EnergyConversionIRecipe;
import mekanism.common.recipe.impl.BasicEnrichingRecipe;
import mekanism.common.recipe.impl.FluidSlurryToSlurryIRecipe;
import mekanism.common.recipe.impl.FluidToFluidIRecipe;
import mekanism.common.recipe.impl.BasicGasConversionRecipe;
import mekanism.common.recipe.impl.InfusionConversionIRecipe;
import mekanism.common.recipe.impl.BasicInjectingRecipe;
import mekanism.common.recipe.impl.MetallurgicInfuserIRecipe;
import mekanism.common.recipe.impl.NucleosynthesizingIRecipe;
import mekanism.common.recipe.impl.PaintingIRecipe;
import mekanism.common.recipe.impl.PigmentExtractingIRecipe;
import mekanism.common.recipe.impl.PigmentMixingIRecipe;
import mekanism.common.recipe.impl.PressurizedReactionIRecipe;
import mekanism.common.recipe.impl.BasicPurifyingRecipe;
import mekanism.common.recipe.impl.RotaryIRecipe;
import mekanism.common.recipe.impl.SawmillIRecipe;
import mekanism.common.recipe.impl.BasicSmeltingRecipe;
import mekanism.common.recipe.serializer.ChemicalCrystallizerRecipeSerializer;
import mekanism.common.recipe.serializer.ChemicalDissolutionRecipeSerializer;
import mekanism.common.recipe.serializer.ChemicalInfuserRecipeSerializer;
import mekanism.common.recipe.serializer.CombinerRecipeSerializer;
import mekanism.common.recipe.serializer.ElectrolysisRecipeSerializer;
import mekanism.common.recipe.serializer.FluidSlurryToSlurryRecipeSerializer;
import mekanism.common.recipe.serializer.FluidToFluidRecipeSerializer;
import mekanism.common.recipe.serializer.GasToGasRecipeSerializer;
import mekanism.common.recipe.serializer.ItemStackGasToItemStackRecipeSerializer;
import mekanism.common.recipe.serializer.ItemStackToEnergyRecipeSerializer;
import mekanism.common.recipe.serializer.ItemStackToGasRecipeSerializer;
import mekanism.common.recipe.serializer.ItemStackToInfuseTypeRecipeSerializer;
import mekanism.common.recipe.serializer.ItemStackToItemStackRecipeSerializer;
import mekanism.common.recipe.serializer.ItemStackToPigmentRecipeSerializer;
import mekanism.common.recipe.serializer.MetallurgicInfuserRecipeSerializer;
import mekanism.common.recipe.serializer.NucleosynthesizingRecipeSerializer;
import mekanism.common.recipe.serializer.PaintingRecipeSerializer;
import mekanism.common.recipe.serializer.PigmentMixingRecipeSerializer;
import mekanism.common.recipe.serializer.PressurizedReactionRecipeSerializer;
import mekanism.common.recipe.serializer.RotaryRecipeSerializer;
import mekanism.common.recipe.serializer.SawmillRecipeSerializer;
import mekanism.common.recipe.serializer.WrappedShapedRecipeSerializer;
import mekanism.common.recipe.upgrade.MekanismShapedRecipe;
import mekanism.common.registration.impl.RecipeSerializerDeferredRegister;
import mekanism.common.registration.impl.RecipeSerializerRegistryObject;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public class MekanismRecipeSerializers {

    private MekanismRecipeSerializers() {
    }

    public static final RecipeSerializerDeferredRegister RECIPE_SERIALIZERS = new RecipeSerializerDeferredRegister(Mekanism.MODID);

    public static final RecipeSerializerRegistryObject<BasicCrushingRecipe> CRUSHING = RECIPE_SERIALIZERS.register("crushing", () -> new ItemStackToItemStackRecipeSerializer<>(BasicCrushingRecipe::new));
    public static final RecipeSerializerRegistryObject<BasicEnrichingRecipe> ENRICHING = RECIPE_SERIALIZERS.register("enriching", () -> new ItemStackToItemStackRecipeSerializer<>(BasicEnrichingRecipe::new));
    public static final RecipeSerializerRegistryObject<BasicSmeltingRecipe> SMELTING = RECIPE_SERIALIZERS.register("smelting", () -> new ItemStackToItemStackRecipeSerializer<>(BasicSmeltingRecipe::new));

    public static final RecipeSerializerRegistryObject<BasicChemicalInfuserRecipe> CHEMICAL_INFUSING = RECIPE_SERIALIZERS.register("chemical_infusing", () -> new ChemicalInfuserRecipeSerializer(ChemicalInfuserIRecipe::new));

    public static final RecipeSerializerRegistryObject<BasicCombinerRecipe> COMBINING = RECIPE_SERIALIZERS.register("combining", () -> new CombinerRecipeSerializer(CombinerIRecipe::new));

    public static final RecipeSerializerRegistryObject<BasicElectrolysisRecipe> SEPARATING = RECIPE_SERIALIZERS.register("separating", () -> new ElectrolysisRecipeSerializer(ElectrolysisIRecipe::new));

    public static final RecipeSerializerRegistryObject<BasicFluidSlurryToSlurryRecipe> WASHING = RECIPE_SERIALIZERS.register("washing", () -> new FluidSlurryToSlurryRecipeSerializer(FluidSlurryToSlurryIRecipe::new));

    public static final RecipeSerializerRegistryObject<BasicFluidToFluidRecipe> EVAPORATING = RECIPE_SERIALIZERS.register("evaporating", () -> new FluidToFluidRecipeSerializer<>(FluidToFluidIRecipe::new));

    public static final RecipeSerializerRegistryObject<BasicActivatingRecipe> ACTIVATING = RECIPE_SERIALIZERS.register("activating", () -> new GasToGasRecipeSerializer<>(BasicActivatingRecipe::new));
    public static final RecipeSerializerRegistryObject<BasicCentrifugingRecipe> CENTRIFUGING = RECIPE_SERIALIZERS.register("centrifuging", () -> new GasToGasRecipeSerializer<>(BasicCentrifugingRecipe::new));

    public static final RecipeSerializerRegistryObject<BasicChemicalCrystallizerRecipe> CRYSTALLIZING = RECIPE_SERIALIZERS.register("crystallizing", () -> new ChemicalCrystallizerRecipeSerializer(ChemicalCrystallizerIRecipe::new));

    public static final RecipeSerializerRegistryObject<ChemicalDissolutionIRecipe> DISSOLUTION = RECIPE_SERIALIZERS.register("dissolution", () -> new ChemicalDissolutionRecipeSerializer(ChemicalDissolutionIRecipe::new));

    public static final RecipeSerializerRegistryObject<BasicCompressingRecipe> COMPRESSING = RECIPE_SERIALIZERS.register("compressing", () -> new ItemStackGasToItemStackRecipeSerializer<>(BasicCompressingRecipe::new));
    public static final RecipeSerializerRegistryObject<BasicPurifyingRecipe> PURIFYING = RECIPE_SERIALIZERS.register("purifying", () -> new ItemStackGasToItemStackRecipeSerializer<>(BasicPurifyingRecipe::new));
    public static final RecipeSerializerRegistryObject<BasicInjectingRecipe> INJECTING = RECIPE_SERIALIZERS.register("injecting", () -> new ItemStackGasToItemStackRecipeSerializer<>(BasicInjectingRecipe::new));

    public static final RecipeSerializerRegistryObject<BasicNucleosynthesizingRecipe> NUCLEOSYNTHESIZING = RECIPE_SERIALIZERS.register("nucleosynthesizing", () -> new NucleosynthesizingRecipeSerializer(NucleosynthesizingIRecipe::new));

    public static final RecipeSerializerRegistryObject<EnergyConversionIRecipe> ENERGY_CONVERSION = RECIPE_SERIALIZERS.register("energy_conversion", () -> new ItemStackToEnergyRecipeSerializer<>(EnergyConversionIRecipe::new));

    public static final RecipeSerializerRegistryObject<BasicGasConversionRecipe> GAS_CONVERSION = RECIPE_SERIALIZERS.register("gas_conversion", () -> new ItemStackToGasRecipeSerializer<>(BasicGasConversionRecipe::new));
    public static final RecipeSerializerRegistryObject<BasicChemicalOxidizerRecipe> OXIDIZING = RECIPE_SERIALIZERS.register("oxidizing", () -> new ItemStackToGasRecipeSerializer<>(BasicChemicalOxidizerRecipe::new));

    public static final RecipeSerializerRegistryObject<InfusionConversionIRecipe> INFUSION_CONVERSION = RECIPE_SERIALIZERS.register("infusion_conversion", () -> new ItemStackToInfuseTypeRecipeSerializer<>(InfusionConversionIRecipe::new));

    public static final RecipeSerializerRegistryObject<BasicItemStackToPigmentRecipe> PIGMENT_EXTRACTING = RECIPE_SERIALIZERS.register("pigment_extracting", () -> new ItemStackToPigmentRecipeSerializer<>(PigmentExtractingIRecipe::new));

    public static final RecipeSerializerRegistryObject<BasicPigmentMixingRecipe> PIGMENT_MIXING = RECIPE_SERIALIZERS.register("pigment_mixing", () -> new PigmentMixingRecipeSerializer(PigmentMixingIRecipe::new));

    public static final RecipeSerializerRegistryObject<BasicMetallurgicInfuserRecipe> METALLURGIC_INFUSING = RECIPE_SERIALIZERS.register("metallurgic_infusing", () -> new MetallurgicInfuserRecipeSerializer<>(MetallurgicInfuserIRecipe::new));

    public static final RecipeSerializerRegistryObject<BasicPaintingRecipe> PAINTING = RECIPE_SERIALIZERS.register("painting", () -> new PaintingRecipeSerializer<>(PaintingIRecipe::new));

    public static final RecipeSerializerRegistryObject<BasicPressurizedReactionRecipe> REACTION = RECIPE_SERIALIZERS.register("reaction", () -> new PressurizedReactionRecipeSerializer(PressurizedReactionIRecipe::new));

    public static final RecipeSerializerRegistryObject<BasicRotaryRecipe> ROTARY = RECIPE_SERIALIZERS.register("rotary", () -> new RotaryRecipeSerializer(new RotaryIRecipe.Factory()));

    public static final RecipeSerializerRegistryObject<BasicSawmillRecipe> SAWING = RECIPE_SERIALIZERS.register("sawing", () -> new SawmillRecipeSerializer(SawmillIRecipe::new));

    public static final RecipeSerializerRegistryObject<MekanismShapedRecipe> MEK_DATA = RECIPE_SERIALIZERS.register("mek_data", () -> new WrappedShapedRecipeSerializer<>(MekanismShapedRecipe::new));
    public static final RecipeSerializerRegistryObject<BinInsertRecipe> BIN_INSERT = RECIPE_SERIALIZERS.register("bin_insert", () -> new SimpleCraftingRecipeSerializer<>(BinInsertRecipe::new));
    public static final RecipeSerializerRegistryObject<BinExtractRecipe> BIN_EXTRACT = RECIPE_SERIALIZERS.register("bin_extract", () -> new SimpleCraftingRecipeSerializer<>(BinExtractRecipe::new));
}