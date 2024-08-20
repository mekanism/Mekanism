package mekanism.common.registries;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipeSerializers;
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
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ClearConfigurationRecipe;
import mekanism.common.recipe.bin.BinExtractRecipe;
import mekanism.common.recipe.bin.BinInsertRecipe;
import mekanism.common.recipe.serializer.MekanismRecipeSerializer;
import mekanism.common.recipe.serializer.RotaryRecipeSerializer;
import mekanism.common.recipe.serializer.SawmillRecipeSerializer;
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
        MekanismRecipeSerializers.CRUSHING = RECIPE_SERIALIZERS.register("crushing", () -> MekanismRecipeSerializer.itemToItem(BasicCrushingRecipe::new));
        MekanismRecipeSerializers.ENRICHING = RECIPE_SERIALIZERS.register("enriching", () -> MekanismRecipeSerializer.itemToItem(BasicEnrichingRecipe::new));
        MekanismRecipeSerializers.SMELTING = RECIPE_SERIALIZERS.register("smelting", () -> MekanismRecipeSerializer.itemToItem(BasicSmeltingRecipe::new));

        MekanismRecipeSerializers.CHEMICAL_INFUSING = RECIPE_SERIALIZERS.register("chemical_infusing", () -> MekanismRecipeSerializer.chemicalChemicalToChemical(BasicChemicalInfuserRecipe::new, IngredientCreatorAccess.chemicalStack(), ChemicalStack.MAP_CODEC, ChemicalStack.STREAM_CODEC));

        MekanismRecipeSerializers.COMBINING = RECIPE_SERIALIZERS.register("combining", () -> MekanismRecipeSerializer.combining(BasicCombinerRecipe::new));

        MekanismRecipeSerializers.SEPARATING = RECIPE_SERIALIZERS.register("separating", () -> MekanismRecipeSerializer.separating(BasicElectrolysisRecipe::new));

        MekanismRecipeSerializers.WASHING = RECIPE_SERIALIZERS.register("washing", () -> MekanismRecipeSerializer.fluidChemicalToChemical(BasicWashingRecipe::new));

        MekanismRecipeSerializers.EVAPORATING = RECIPE_SERIALIZERS.register("evaporating", () -> MekanismRecipeSerializer.fluidToFluid(BasicFluidToFluidRecipe::new));

        MekanismRecipeSerializers.ACTIVATING = RECIPE_SERIALIZERS.register("activating", () -> MekanismRecipeSerializer.chemicalToChemical(BasicActivatingRecipe::new));
        MekanismRecipeSerializers.CENTRIFUGING = RECIPE_SERIALIZERS.register("centrifuging", () -> MekanismRecipeSerializer.chemicalToChemical(BasicCentrifugingRecipe::new));

        MekanismRecipeSerializers.CRYSTALLIZING = RECIPE_SERIALIZERS.register("crystallizing", () -> MekanismRecipeSerializer.crystallizing(BasicChemicalCrystallizerRecipe::new));

        MekanismRecipeSerializers.DISSOLUTION = RECIPE_SERIALIZERS.register("dissolution", () -> MekanismRecipeSerializer.dissolution(BasicChemicalDissolutionRecipe::new));

        MekanismRecipeSerializers.COMPRESSING = RECIPE_SERIALIZERS.register("compressing", () -> MekanismRecipeSerializer.itemChemicalToItem(BasicCompressingRecipe::new));
        MekanismRecipeSerializers.PURIFYING = RECIPE_SERIALIZERS.register("purifying", () -> MekanismRecipeSerializer.itemChemicalToItem(BasicPurifyingRecipe::new));
        MekanismRecipeSerializers.INJECTING = RECIPE_SERIALIZERS.register("injecting", () -> MekanismRecipeSerializer.itemChemicalToItem(BasicInjectingRecipe::new));

        MekanismRecipeSerializers.NUCLEOSYNTHESIZING = RECIPE_SERIALIZERS.register("nucleosynthesizing", () -> MekanismRecipeSerializer.nucleosynthesizing(BasicNucleosynthesizingRecipe::new));

        MekanismRecipeSerializers.ENERGY_CONVERSION = RECIPE_SERIALIZERS.register("energy_conversion", () -> MekanismRecipeSerializer.itemToEnergy(BasicItemStackToEnergyRecipe::new));

        MekanismRecipeSerializers.CHEMICAL_CONVERSION = RECIPE_SERIALIZERS.register("chemical_conversion", () -> MekanismRecipeSerializer.itemToChemical(BasicChemicalConversionRecipe::new, ChemicalStack.MAP_CODEC, ChemicalStack.STREAM_CODEC));
        MekanismRecipeSerializers.OXIDIZING = RECIPE_SERIALIZERS.register("oxidizing", () -> MekanismRecipeSerializer.itemToChemical(BasicChemicalOxidizerRecipe::new, ChemicalStack.MAP_CODEC, ChemicalStack.STREAM_CODEC));

        MekanismRecipeSerializers.PIGMENT_EXTRACTING = RECIPE_SERIALIZERS.register("pigment_extracting", () -> MekanismRecipeSerializer.itemToChemical(BasicPigmentExtractingRecipe::new, ChemicalStack.MAP_CODEC, ChemicalStack.STREAM_CODEC));

        MekanismRecipeSerializers.PIGMENT_MIXING = RECIPE_SERIALIZERS.register("pigment_mixing", () -> MekanismRecipeSerializer.chemicalChemicalToChemical(BasicPigmentMixingRecipe::new, IngredientCreatorAccess.chemicalStack(), ChemicalStack.MAP_CODEC, ChemicalStack.STREAM_CODEC));

        MekanismRecipeSerializers.METALLURGIC_INFUSING = RECIPE_SERIALIZERS.register("metallurgic_infusing", () -> MekanismRecipeSerializer.itemChemicalToItem(BasicMetallurgicInfuserRecipe::new));

        MekanismRecipeSerializers.PAINTING = RECIPE_SERIALIZERS.register("painting", () -> MekanismRecipeSerializer.itemChemicalToItem(BasicPaintingRecipe::new));

        MekanismRecipeSerializers.REACTION = RECIPE_SERIALIZERS.register("reaction", () -> MekanismRecipeSerializer.reaction(BasicPressurizedReactionRecipe::new));

        MekanismRecipeSerializers.ROTARY = RECIPE_SERIALIZERS.register("rotary", () -> new RotaryRecipeSerializer(BasicRotaryRecipe::new, BasicRotaryRecipe::new, BasicRotaryRecipe::new));

        MekanismRecipeSerializers.SAWING = RECIPE_SERIALIZERS.register("sawing", () -> new SawmillRecipeSerializer(BasicSawmillRecipe::new));

    }

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MekanismShapedRecipe>> MEK_DATA = RECIPE_SERIALIZERS.register("mek_data", () -> MekanismRecipeSerializer.wrapped(MekanismShapedRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ClearConfigurationRecipe>> CLEAR_CONFIGURATION = RECIPE_SERIALIZERS.register("clear_configuration", () -> new SimpleCraftingRecipeSerializer<>(ClearConfigurationRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BinInsertRecipe>> BIN_INSERT = RECIPE_SERIALIZERS.register("bin_insert", () -> new SimpleCraftingRecipeSerializer<>(BinInsertRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BinExtractRecipe>> BIN_EXTRACT = RECIPE_SERIALIZERS.register("bin_extract", () -> new SimpleCraftingRecipeSerializer<>(BinExtractRecipe::new));
}