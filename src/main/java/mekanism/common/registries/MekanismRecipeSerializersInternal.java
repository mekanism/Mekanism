package mekanism.common.registries;

import java.util.function.Supplier;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MekanismRecipeSerializersInternal extends MekanismRecipeSerializers {

    private MekanismRecipeSerializersInternal() {
    }

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Mekanism.MODID);

    private static <T extends RecipeSerializer<?>> void register(ResourceKey<RecipeSerializer<?>> key, Supplier<T> supplier) {
        RECIPE_SERIALIZERS.register(key.identifier().getPath(), supplier);
    }
    static {
        register(Names.CRUSHING, () -> MekanismRecipeSerializer.itemToItem(BasicCrushingRecipe::new));
        register(Names.ENRICHING, () -> MekanismRecipeSerializer.itemToItem(BasicEnrichingRecipe::new));
        register(Names.SMELTING, () -> MekanismRecipeSerializer.itemToItem(BasicSmeltingRecipe::new));

        register(Names.CHEMICAL_INFUSING, () -> MekanismRecipeSerializer.chemicalChemicalToChemical(BasicChemicalInfuserRecipe::new, IngredientCreatorAccess.chemicalStack(), ChemicalStack.MAP_CODEC, ChemicalStack.STREAM_CODEC));

        register(Names.COMBINING, () -> MekanismRecipeSerializer.combining(BasicCombinerRecipe::new));

        register(Names.SEPARATING, () -> MekanismRecipeSerializer.separating(BasicElectrolysisRecipe::new));

        register(Names.WASHING, () -> MekanismRecipeSerializer.fluidChemicalToChemical(BasicWashingRecipe::new));

        register(Names.EVAPORATING, () -> MekanismRecipeSerializer.fluidToFluid(BasicFluidToFluidRecipe::new));

        register(Names.ACTIVATING, () -> MekanismRecipeSerializer.chemicalToChemical(BasicActivatingRecipe::new));
        register(Names.CENTRIFUGING, () -> MekanismRecipeSerializer.chemicalToChemical(BasicCentrifugingRecipe::new));

        register(Names.CRYSTALLIZING, () -> MekanismRecipeSerializer.crystallizing(BasicChemicalCrystallizerRecipe::new));

        register(Names.DISSOLUTION, () -> MekanismRecipeSerializer.dissolution(BasicChemicalDissolutionRecipe::new));

        register(Names.COMPRESSING, () -> MekanismRecipeSerializer.itemChemicalToItem(BasicCompressingRecipe::new));
        register(Names.PURIFYING, () -> MekanismRecipeSerializer.itemChemicalToItem(BasicPurifyingRecipe::new));
        register(Names.INJECTING, () -> MekanismRecipeSerializer.itemChemicalToItem(BasicInjectingRecipe::new));

        register(Names.NUCLEOSYNTHESIZING, () -> MekanismRecipeSerializer.nucleosynthesizing(BasicNucleosynthesizingRecipe::new));

        register(Names.ENERGY_CONVERSION, () -> MekanismRecipeSerializer.itemToEnergy(BasicItemStackToEnergyRecipe::new));

        register(Names.CHEMICAL_CONVERSION, () -> MekanismRecipeSerializer.itemToChemical(BasicChemicalConversionRecipe::new, ChemicalStack.MAP_CODEC, ChemicalStack.STREAM_CODEC));
        register(Names.OXIDIZING, () -> MekanismRecipeSerializer.itemToChemical(BasicChemicalOxidizerRecipe::new, ChemicalStack.MAP_CODEC, ChemicalStack.STREAM_CODEC));

        register(Names.PIGMENT_EXTRACTING, () -> MekanismRecipeSerializer.itemToChemical(BasicPigmentExtractingRecipe::new, ChemicalStack.MAP_CODEC, ChemicalStack.STREAM_CODEC));

        register(Names.PIGMENT_MIXING, () -> MekanismRecipeSerializer.chemicalChemicalToChemical(BasicPigmentMixingRecipe::new, IngredientCreatorAccess.chemicalStack(), ChemicalStack.MAP_CODEC, ChemicalStack.STREAM_CODEC));

        register(Names.METALLURGIC_INFUSING, () -> MekanismRecipeSerializer.itemChemicalToItem(BasicMetallurgicInfuserRecipe::new));

        register(Names.PAINTING, () -> MekanismRecipeSerializer.itemChemicalToItem(BasicPaintingRecipe::new));

        register(Names.REACTION, () -> MekanismRecipeSerializer.reaction(BasicPressurizedReactionRecipe::new));

        register(Names.ROTARY, () -> RotaryRecipeSerializer.create(BasicRotaryRecipe::new, BasicRotaryRecipe::new, BasicRotaryRecipe::new));

        register(Names.SAWING, () -> SawmillRecipeSerializer.create(BasicSawmillRecipe::new));

    }

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MekanismShapedRecipe>> MEK_DATA = RECIPE_SERIALIZERS.register("mek_data", () -> MekanismRecipeSerializer.wrapped(MekanismShapedRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ClearConfigurationRecipe>> CLEAR_CONFIGURATION = RECIPE_SERIALIZERS.register("clear_configuration", () -> new CustomRecipe.Serializer<>(ClearConfigurationRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BinInsertRecipe>> BIN_INSERT = RECIPE_SERIALIZERS.register("bin_insert", () -> new CustomRecipe.Serializer<>(BinInsertRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BinExtractRecipe>> BIN_EXTRACT = RECIPE_SERIALIZERS.register("bin_extract", () -> new CustomRecipe.Serializer<>(BinExtractRecipe::new));
}