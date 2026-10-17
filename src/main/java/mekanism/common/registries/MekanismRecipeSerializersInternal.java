package mekanism.common.registries;

import java.util.function.Supplier;
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
import mekanism.api.recipes.basic.BasicEvaporatingRecipe;
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
import mekanism.api.recipes.book.ActivatingBookCategory.ActivatingBookInfo;
import mekanism.api.recipes.book.CentrifugingBookCategory.CentrifugingBookInfo;
import mekanism.api.recipes.book.ChemicalConversionBookCategory.ChemicalConversionBookInfo;
import mekanism.api.recipes.book.ChemicalInfusingBookCategory.ChemicalInfusingBookInfo;
import mekanism.api.recipes.book.CompressingBookCategory.CompressingBookInfo;
import mekanism.api.recipes.book.CrushingBookCategory.CrushingBookInfo;
import mekanism.api.recipes.book.EnrichingBookCategory.EnrichingBookInfo;
import mekanism.api.recipes.book.EvaporatingBookCategory.EvaporatingBookInfo;
import mekanism.api.recipes.book.InjectingBookCategory.InjectingBookInfo;
import mekanism.api.recipes.book.LiquificationBookCategory;
import mekanism.api.recipes.book.LiquificationBookCategory.LiquificationBookInfo;
import mekanism.api.recipes.book.MetallurgicInfusingBookCategory.MetallurgicInfusingBookInfo;
import mekanism.api.recipes.book.OxidizingBookCategory.OxidizingBookInfo;
import mekanism.api.recipes.book.PaintingBookCategory.PaintingBookInfo;
import mekanism.api.recipes.book.PigmentExtractingBookCategory.PigmentExtractingBookInfo;
import mekanism.api.recipes.book.PigmentMixingBookCategory.PigmentMixingBookInfo;
import mekanism.api.recipes.book.PurifyingBookCategory.PurifyingBookInfo;
import mekanism.api.recipes.book.WashingBookCategory.WashingBookInfo;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ClearConfigurationRecipe;
import mekanism.common.recipe.WrappedShapedRecipe;
import mekanism.common.recipe.WrappedSmelterRecipe;
import mekanism.common.recipe.bin.BinExtractRecipe;
import mekanism.common.recipe.bin.BinInsertRecipe;
import mekanism.common.recipe.impl.NutritionalLiquifierRecipe;
import mekanism.common.recipe.serializer.MekanismRecipeSerializer;
import mekanism.common.recipe.serializer.RotaryRecipeSerializer;
import mekanism.common.recipe.upgrade.MekanismShapedRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.AbstractCookingRecipe.CookingBookInfo;
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
        register(Names.CRUSHING, () -> MekanismRecipeSerializer.itemToItem(BasicCrushingRecipe::new, CrushingBookInfo.MAP_CODEC, CrushingBookInfo.STREAM_CODEC));
        register(Names.ENRICHING, () -> MekanismRecipeSerializer.itemToItem(BasicEnrichingRecipe::new, EnrichingBookInfo.MAP_CODEC, EnrichingBookInfo.STREAM_CODEC));
        register(Names.SMELTING, () -> MekanismRecipeSerializer.itemToItem(BasicSmeltingRecipe::new, CookingBookInfo.MAP_CODEC, CookingBookInfo.STREAM_CODEC));

        register(Names.CHEMICAL_INFUSING, () -> MekanismRecipeSerializer.chemicalChemicalToChemical(BasicChemicalInfuserRecipe::new, ChemicalInfusingBookInfo.MAP_CODEC, ChemicalInfusingBookInfo.STREAM_CODEC));

        register(Names.COMBINING, () -> MekanismRecipeSerializer.combining(BasicCombinerRecipe::new));

        register(Names.SEPARATING, () -> MekanismRecipeSerializer.separating(BasicElectrolysisRecipe::new));

        register(Names.WASHING, () -> MekanismRecipeSerializer.fluidChemicalToChemical(BasicWashingRecipe::new, WashingBookInfo.MAP_CODEC, WashingBookInfo.STREAM_CODEC));

        register(Names.EVAPORATING, () -> MekanismRecipeSerializer.fluidToFluid(BasicEvaporatingRecipe::new, EvaporatingBookInfo.MAP_CODEC, EvaporatingBookInfo.STREAM_CODEC));

        register(Names.ACTIVATING, () -> MekanismRecipeSerializer.chemicalToChemical(BasicActivatingRecipe::new, ActivatingBookInfo.MAP_CODEC, ActivatingBookInfo.STREAM_CODEC));
        register(Names.CENTRIFUGING, () -> MekanismRecipeSerializer.chemicalToChemical(BasicCentrifugingRecipe::new, CentrifugingBookInfo.MAP_CODEC, CentrifugingBookInfo.STREAM_CODEC));

        register(Names.CRYSTALLIZING, () -> MekanismRecipeSerializer.crystallizing(BasicChemicalCrystallizerRecipe::new));

        register(Names.DISSOLUTION, () -> MekanismRecipeSerializer.dissolution(BasicChemicalDissolutionRecipe::new));

        register(Names.COMPRESSING, () -> MekanismRecipeSerializer.itemChemicalToItem(BasicCompressingRecipe::new, CompressingBookInfo.MAP_CODEC, CompressingBookInfo.STREAM_CODEC));
        register(Names.PURIFYING, () -> MekanismRecipeSerializer.itemChemicalToItem(BasicPurifyingRecipe::new, PurifyingBookInfo.MAP_CODEC, PurifyingBookInfo.STREAM_CODEC));
        register(Names.INJECTING, () -> MekanismRecipeSerializer.itemChemicalToItem(BasicInjectingRecipe::new, InjectingBookInfo.MAP_CODEC, InjectingBookInfo.STREAM_CODEC));

        register(Names.NUCLEOSYNTHESIZING, () -> MekanismRecipeSerializer.nucleosynthesizing(BasicNucleosynthesizingRecipe::new));

        register(Names.ENERGY_CONVERSION, () -> MekanismRecipeSerializer.itemToEnergy(BasicItemStackToEnergyRecipe::new));

        register(Names.CHEMICAL_CONVERSION, () -> MekanismRecipeSerializer.itemToChemical(BasicChemicalConversionRecipe::new, ChemicalConversionBookInfo.MAP_CODEC, ChemicalConversionBookInfo.STREAM_CODEC));
        register(Names.OXIDIZING, () -> MekanismRecipeSerializer.itemToChemical(BasicChemicalOxidizerRecipe::new, OxidizingBookInfo.MAP_CODEC, OxidizingBookInfo.STREAM_CODEC));

        register(Names.PIGMENT_EXTRACTING, () -> MekanismRecipeSerializer.itemToChemical(BasicPigmentExtractingRecipe::new, PigmentExtractingBookInfo.MAP_CODEC, PigmentExtractingBookInfo.STREAM_CODEC));

        register(Names.PIGMENT_MIXING, () -> MekanismRecipeSerializer.chemicalChemicalToChemical(BasicPigmentMixingRecipe::new, PigmentMixingBookInfo.MAP_CODEC, PigmentMixingBookInfo.STREAM_CODEC));

        register(Names.METALLURGIC_INFUSING, () -> MekanismRecipeSerializer.itemChemicalToItem(BasicMetallurgicInfuserRecipe::new, MetallurgicInfusingBookInfo.MAP_CODEC, MetallurgicInfusingBookInfo.STREAM_CODEC));

        register(Names.PAINTING, () -> MekanismRecipeSerializer.itemChemicalToItem(BasicPaintingRecipe::new, PaintingBookInfo.MAP_CODEC, PaintingBookInfo.STREAM_CODEC));

        register(Names.REACTION, () -> MekanismRecipeSerializer.reaction(BasicPressurizedReactionRecipe::new));

        register(Names.ROTARY, () -> RotaryRecipeSerializer.create(BasicRotaryRecipe::new, BasicRotaryRecipe::new, BasicRotaryRecipe::new));

        register(Names.SAWING, () -> MekanismRecipeSerializer.sawing(BasicSawmillRecipe::new));

    }

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MekanismShapedRecipe>> MEK_DATA = RECIPE_SERIALIZERS.register("mek_data", () -> WrappedShapedRecipe.serializer(MekanismShapedRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ClearConfigurationRecipe>> CLEAR_CONFIGURATION = RECIPE_SERIALIZERS.register("clear_configuration", () -> MekanismRecipeSerializer.singleton(ClearConfigurationRecipe.INSTANCE));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BinInsertRecipe>> BIN_INSERT = RECIPE_SERIALIZERS.register("bin_insert", () -> MekanismRecipeSerializer.singleton(BinInsertRecipe.INSTANCE));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BinExtractRecipe>> BIN_EXTRACT = RECIPE_SERIALIZERS.register("bin_extract", () -> MekanismRecipeSerializer.singleton(BinExtractRecipe.INSTANCE));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<WrappedSmelterRecipe>> WRAPPED_SMELTER = RECIPE_SERIALIZERS.register("wrapped_smelter", () -> WrappedSmelterRecipe.SERIALIZER);

    //Currently this is just a synthetic recipe
    @SuppressWarnings("RedundantTypeArguments")//Necessary to compile
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<NutritionalLiquifierRecipe>> LIQUIFIER = RECIPE_SERIALIZERS.register("liquifier",
          () -> MekanismRecipeSerializer.<LiquificationBookCategory, LiquificationBookInfo, NutritionalLiquifierRecipe>itemToFluidOptionalItem(NutritionalLiquifierRecipe::new, LiquificationBookInfo.MAP_CODEC, LiquificationBookInfo.STREAM_CODEC));
}