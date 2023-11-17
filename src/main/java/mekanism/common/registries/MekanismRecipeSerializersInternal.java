package mekanism.common.registries;

import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.basic.BasicActivatingRecipe;
import mekanism.api.recipes.basic.BasicCentrifugingRecipe;
import mekanism.api.recipes.basic.BasicChemicalCrystallizerRecipe;
import mekanism.api.recipes.basic.BasicChemicalDissolutionRecipe;
import mekanism.api.recipes.basic.BasicChemicalInfuserRecipe;
import mekanism.api.recipes.basic.BasicChemicalOxidizerRecipe;
import mekanism.api.recipes.basic.BasicCombinerRecipe;
import mekanism.api.recipes.basic.BasicCompressingRecipe;
import mekanism.api.recipes.basic.BasicCrushingRecipe;
import mekanism.api.recipes.basic.BasicElectrolysisRecipe;
import mekanism.api.recipes.basic.BasicEnrichingRecipe;
import mekanism.api.recipes.basic.BasicFluidSlurryToSlurryRecipe;
import mekanism.api.recipes.basic.BasicFluidToFluidRecipe;
import mekanism.api.recipes.basic.BasicGasConversionRecipe;
import mekanism.api.recipes.basic.BasicInjectingRecipe;
import mekanism.api.recipes.basic.BasicItemStackToEnergyRecipe;
import mekanism.api.recipes.basic.BasicItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.basic.BasicItemStackToPigmentRecipe;
import mekanism.api.recipes.basic.BasicMetallurgicInfuserRecipe;
import mekanism.api.recipes.basic.BasicNucleosynthesizingRecipe;
import mekanism.api.recipes.basic.BasicPaintingRecipe;
import mekanism.api.recipes.basic.BasicPigmentMixingRecipe;
import mekanism.api.recipes.basic.BasicPressurizedReactionRecipe;
import mekanism.api.recipes.basic.BasicPurifyingRecipe;
import mekanism.api.recipes.basic.BasicRotaryRecipe;
import mekanism.api.recipes.basic.BasicSawmillRecipe;
import mekanism.api.recipes.basic.BasicSmeltingRecipe;
import mekanism.common.Mekanism;
import mekanism.common.recipe.bin.BinExtractRecipe;
import mekanism.common.recipe.bin.BinInsertRecipe;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MekanismRecipeSerializersInternal {

    private MekanismRecipeSerializersInternal() {
    }

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Mekanism.MODID);

    static {
        MekanismRecipeSerializers.CRUSHING = RECIPE_SERIALIZERS.register("crushing", () -> new ItemStackToItemStackRecipeSerializer<>(BasicCrushingRecipe::new));
        MekanismRecipeSerializers.ENRICHING = RECIPE_SERIALIZERS.register("enriching", () -> new ItemStackToItemStackRecipeSerializer<>(BasicEnrichingRecipe::new));
        MekanismRecipeSerializers.SMELTING = RECIPE_SERIALIZERS.register("smelting", () -> new ItemStackToItemStackRecipeSerializer<>(BasicSmeltingRecipe::new));

        MekanismRecipeSerializers.CHEMICAL_INFUSING = RECIPE_SERIALIZERS.register("chemical_infusing", () -> new ChemicalInfuserRecipeSerializer(BasicChemicalInfuserRecipe::new));

        MekanismRecipeSerializers.COMBINING = RECIPE_SERIALIZERS.register("combining", () -> new CombinerRecipeSerializer(BasicCombinerRecipe::new));

        MekanismRecipeSerializers.SEPARATING = RECIPE_SERIALIZERS.register("separating", () -> new ElectrolysisRecipeSerializer(BasicElectrolysisRecipe::new));

        MekanismRecipeSerializers.WASHING = RECIPE_SERIALIZERS.register("washing", () -> new FluidSlurryToSlurryRecipeSerializer(BasicFluidSlurryToSlurryRecipe::new));

        MekanismRecipeSerializers.EVAPORATING = RECIPE_SERIALIZERS.register("evaporating", () -> new FluidToFluidRecipeSerializer<>(BasicFluidToFluidRecipe::new));

        MekanismRecipeSerializers.ACTIVATING = RECIPE_SERIALIZERS.register("activating", () -> new GasToGasRecipeSerializer<>(BasicActivatingRecipe::new));
        MekanismRecipeSerializers.CENTRIFUGING = RECIPE_SERIALIZERS.register("centrifuging", () -> new GasToGasRecipeSerializer<>(BasicCentrifugingRecipe::new));

        MekanismRecipeSerializers.CRYSTALLIZING = RECIPE_SERIALIZERS.register("crystallizing", () -> new ChemicalCrystallizerRecipeSerializer(BasicChemicalCrystallizerRecipe::new));

        MekanismRecipeSerializers.DISSOLUTION = RECIPE_SERIALIZERS.register("dissolution", () -> new ChemicalDissolutionRecipeSerializer(BasicChemicalDissolutionRecipe::new));

        MekanismRecipeSerializers.COMPRESSING = RECIPE_SERIALIZERS.register("compressing", () -> new ItemStackGasToItemStackRecipeSerializer<>(BasicCompressingRecipe::new));
        MekanismRecipeSerializers.PURIFYING = RECIPE_SERIALIZERS.register("purifying", () -> new ItemStackGasToItemStackRecipeSerializer<>(BasicPurifyingRecipe::new));
        MekanismRecipeSerializers.INJECTING = RECIPE_SERIALIZERS.register("injecting", () -> new ItemStackGasToItemStackRecipeSerializer<>(BasicInjectingRecipe::new));

        MekanismRecipeSerializers.NUCLEOSYNTHESIZING = RECIPE_SERIALIZERS.register("nucleosynthesizing", () -> new NucleosynthesizingRecipeSerializer(BasicNucleosynthesizingRecipe::new));

        MekanismRecipeSerializers.ENERGY_CONVERSION = RECIPE_SERIALIZERS.register("energy_conversion", () -> new ItemStackToEnergyRecipeSerializer<>(BasicItemStackToEnergyRecipe::new));

        MekanismRecipeSerializers.GAS_CONVERSION = RECIPE_SERIALIZERS.register("gas_conversion", () -> new ItemStackToGasRecipeSerializer<>(BasicGasConversionRecipe::new));
        MekanismRecipeSerializers.OXIDIZING = RECIPE_SERIALIZERS.register("oxidizing", () -> new ItemStackToGasRecipeSerializer<>(BasicChemicalOxidizerRecipe::new));

        MekanismRecipeSerializers.INFUSION_CONVERSION = RECIPE_SERIALIZERS.register("infusion_conversion", () -> new ItemStackToInfuseTypeRecipeSerializer<>(BasicItemStackToInfuseTypeRecipe::new));

        MekanismRecipeSerializers.PIGMENT_EXTRACTING = RECIPE_SERIALIZERS.register("pigment_extracting", () -> new ItemStackToPigmentRecipeSerializer<>(BasicItemStackToPigmentRecipe::new));

        MekanismRecipeSerializers.PIGMENT_MIXING = RECIPE_SERIALIZERS.register("pigment_mixing", () -> new PigmentMixingRecipeSerializer(BasicPigmentMixingRecipe::new));

        MekanismRecipeSerializers.METALLURGIC_INFUSING = RECIPE_SERIALIZERS.register("metallurgic_infusing", () -> new MetallurgicInfuserRecipeSerializer<>(BasicMetallurgicInfuserRecipe::new));

        MekanismRecipeSerializers.PAINTING = RECIPE_SERIALIZERS.register("painting", () -> new PaintingRecipeSerializer<>(BasicPaintingRecipe::new));

        MekanismRecipeSerializers.REACTION = RECIPE_SERIALIZERS.register("reaction", () -> new PressurizedReactionRecipeSerializer(BasicPressurizedReactionRecipe::new));

        MekanismRecipeSerializers.ROTARY = RECIPE_SERIALIZERS.register("rotary", () -> new RotaryRecipeSerializer(new BasicRotaryRecipe.Factory()));

        MekanismRecipeSerializers.SAWING = RECIPE_SERIALIZERS.register("sawing", () -> new SawmillRecipeSerializer(BasicSawmillRecipe::new));

    }

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MekanismShapedRecipe>> MEK_DATA = RECIPE_SERIALIZERS.register("mek_data", () -> new WrappedShapedRecipeSerializer<>(MekanismShapedRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BinInsertRecipe>> BIN_INSERT = RECIPE_SERIALIZERS.register("bin_insert", () -> new SimpleCraftingRecipeSerializer<>(BinInsertRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BinExtractRecipe>> BIN_EXTRACT = RECIPE_SERIALIZERS.register("bin_extract", () -> new SimpleCraftingRecipeSerializer<>(BinExtractRecipe::new));
}