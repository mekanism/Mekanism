package mekanism.additions.common.recipe;

import java.util.Map;
import java.util.function.Consumer;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.providers.IItemProvider;
import mekanism.api.providers.IPigmentProvider;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.text.EnumColor;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.impl.PigmentExtractingRecipeProvider;
import mekanism.common.registration.impl.PigmentRegistryObject;
import mekanism.common.registries.MekanismPigments;
import net.minecraft.data.IFinishedRecipe;

public class PigmentExtractingPlasticRecipeProvider implements ISubRecipeProvider {

    private static final long PLASTIC_BLOCK_RATE = PigmentExtractingRecipeProvider.DYE_RATE * 3 / 16;//48
    private static final long SLICK_PLASTIC_BLOCK_RATE = PLASTIC_BLOCK_RATE * 7 / 8;//42
    private static final long PLASTIC_GLOW_BLOCK_RATE = PLASTIC_BLOCK_RATE * 7 / 8;//42
    private static final long REINFORCED_PLASTIC_BLOCK_RATE = PLASTIC_BLOCK_RATE * 7 / 8;//42
    private static final long PLASTIC_ROAD_RATE = PLASTIC_BLOCK_RATE * 7 / 8;//42
    private static final long TRANSPARENT_PLASTIC_BLOCK_RATE = PLASTIC_BLOCK_RATE * 7 / 8;//42
    private static final long PLASTIC_STAIRS_RATE = PLASTIC_BLOCK_RATE * 2 / 3;//32
    private static final long PLASTIC_SLAB_RATE = PLASTIC_BLOCK_RATE / 2;//24
    private static final long PLASTIC_GLOW_STAIRS_RATE = PLASTIC_GLOW_BLOCK_RATE * 2 / 3;//28
    private static final long PLASTIC_GLOW_SLAB_RATE = PLASTIC_GLOW_BLOCK_RATE / 2;//21
    private static final long TRANSPARENT_PLASTIC_STAIRS_RATE = TRANSPARENT_PLASTIC_BLOCK_RATE * 2 / 3;//28
    private static final long TRANSPARENT_PLASTIC_SLAB_RATE = TRANSPARENT_PLASTIC_BLOCK_RATE / 2;//21

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "pigment_extracting/plastic/";
        for (Map.Entry<EnumColor, PigmentRegistryObject<Pigment>> entry : MekanismPigments.PIGMENT_COLOR_LOOKUP.entrySet()) {
            EnumColor color = entry.getKey();
            IPigmentProvider pigment = entry.getValue();
            addExtractionRecipe(consumer, color, AdditionsBlocks.PLASTIC_BLOCKS, pigment, PLASTIC_BLOCK_RATE, basePath + "block/");
            addExtractionRecipe(consumer, color, AdditionsBlocks.SLICK_PLASTIC_BLOCKS, pigment, SLICK_PLASTIC_BLOCK_RATE, basePath + "slick/");
            addExtractionRecipe(consumer, color, AdditionsBlocks.PLASTIC_GLOW_BLOCKS, pigment, PLASTIC_GLOW_BLOCK_RATE, basePath + "glow/");
            addExtractionRecipe(consumer, color, AdditionsBlocks.REINFORCED_PLASTIC_BLOCKS, pigment, REINFORCED_PLASTIC_BLOCK_RATE, basePath + "reinforced/");
            addExtractionRecipe(consumer, color, AdditionsBlocks.PLASTIC_ROADS, pigment, PLASTIC_ROAD_RATE, basePath + "road/");
            addExtractionRecipe(consumer, color, AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS, pigment, TRANSPARENT_PLASTIC_BLOCK_RATE, basePath + "transparent/");
            addExtractionRecipe(consumer, color, AdditionsBlocks.PLASTIC_STAIRS, pigment, PLASTIC_STAIRS_RATE, basePath + "stairs/");
            addExtractionRecipe(consumer, color, AdditionsBlocks.PLASTIC_SLABS, pigment, PLASTIC_SLAB_RATE, basePath + "slab/");
            addExtractionRecipe(consumer, color, AdditionsBlocks.PLASTIC_GLOW_STAIRS, pigment, PLASTIC_GLOW_STAIRS_RATE, basePath + "stairs/glow/");
            addExtractionRecipe(consumer, color, AdditionsBlocks.PLASTIC_GLOW_SLABS, pigment, PLASTIC_GLOW_SLAB_RATE, basePath + "slab/glow/");
            addExtractionRecipe(consumer, color, AdditionsBlocks.TRANSPARENT_PLASTIC_STAIRS, pigment, TRANSPARENT_PLASTIC_STAIRS_RATE, basePath + "stairs/transparent");
            addExtractionRecipe(consumer, color, AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS, pigment, TRANSPARENT_PLASTIC_SLAB_RATE, basePath + "slab/transparent");
        }
    }

    private static void addExtractionRecipe(Consumer<IFinishedRecipe> consumer, EnumColor color, Map<EnumColor, ? extends IItemProvider> input, IPigmentProvider pigment,
          long rate, String basePath) {
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              ItemStackIngredient.from(input.get(color)),
              pigment.getStack(rate)
        ).build(consumer, MekanismAdditions.rl(basePath + color.getRegistryPrefix()));
    }
}