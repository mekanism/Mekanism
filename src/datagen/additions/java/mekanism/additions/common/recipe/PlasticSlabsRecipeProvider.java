package mekanism.additions.common.recipe;

import java.util.function.Consumer;
import mekanism.additions.common.AdditionsTags;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.api.providers.IItemProvider;
import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.data.IFinishedRecipe;

public class PlasticSlabsRecipeProvider implements ISubRecipeProvider {

    private static final RecipePattern PLASTIC_SLAB = RecipePattern.createPattern(TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT));

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "plastic/slab/";
        registerPlasticSlabs(consumer, basePath);
        registerPlasticGlowSlabs(consumer, basePath);
        registerPlasticTransparentSlabs(consumer, basePath);
    }

    private void registerPlasticSlabs(Consumer<IFinishedRecipe> consumer, String basePath) {
        registerPlasticSlab(consumer, AdditionsBlocks.BLACK_PLASTIC_SLAB, AdditionsBlocks.BLACK_PLASTIC_BLOCK, basePath);
        registerPlasticSlab(consumer, AdditionsBlocks.RED_PLASTIC_SLAB, AdditionsBlocks.RED_PLASTIC_BLOCK, basePath);
        registerPlasticSlab(consumer, AdditionsBlocks.GREEN_PLASTIC_SLAB, AdditionsBlocks.GREEN_PLASTIC_BLOCK, basePath);
        registerPlasticSlab(consumer, AdditionsBlocks.BROWN_PLASTIC_SLAB, AdditionsBlocks.BROWN_PLASTIC_BLOCK, basePath);
        registerPlasticSlab(consumer, AdditionsBlocks.BLUE_PLASTIC_SLAB, AdditionsBlocks.BLUE_PLASTIC_BLOCK, basePath);
        registerPlasticSlab(consumer, AdditionsBlocks.PURPLE_PLASTIC_SLAB, AdditionsBlocks.PURPLE_PLASTIC_BLOCK, basePath);
        registerPlasticSlab(consumer, AdditionsBlocks.CYAN_PLASTIC_SLAB, AdditionsBlocks.CYAN_PLASTIC_BLOCK, basePath);
        registerPlasticSlab(consumer, AdditionsBlocks.LIGHT_GRAY_PLASTIC_SLAB, AdditionsBlocks.LIGHT_GRAY_PLASTIC_BLOCK, basePath);
        registerPlasticSlab(consumer, AdditionsBlocks.GRAY_PLASTIC_SLAB, AdditionsBlocks.GRAY_PLASTIC_BLOCK, basePath);
        registerPlasticSlab(consumer, AdditionsBlocks.PINK_PLASTIC_SLAB, AdditionsBlocks.PINK_PLASTIC_BLOCK, basePath);
        registerPlasticSlab(consumer, AdditionsBlocks.LIME_PLASTIC_SLAB, AdditionsBlocks.LIME_PLASTIC_BLOCK, basePath);
        registerPlasticSlab(consumer, AdditionsBlocks.YELLOW_PLASTIC_SLAB, AdditionsBlocks.YELLOW_PLASTIC_BLOCK, basePath);
        registerPlasticSlab(consumer, AdditionsBlocks.LIGHT_BLUE_PLASTIC_SLAB, AdditionsBlocks.LIGHT_BLUE_PLASTIC_BLOCK, basePath);
        registerPlasticSlab(consumer, AdditionsBlocks.MAGENTA_PLASTIC_SLAB, AdditionsBlocks.MAGENTA_PLASTIC_BLOCK, basePath);
        registerPlasticSlab(consumer, AdditionsBlocks.ORANGE_PLASTIC_SLAB, AdditionsBlocks.ORANGE_PLASTIC_BLOCK, basePath);
        registerPlasticSlab(consumer, AdditionsBlocks.WHITE_PLASTIC_SLAB, AdditionsBlocks.WHITE_PLASTIC_BLOCK, basePath);
    }

    private void registerPlasticSlab(Consumer<IFinishedRecipe> consumer, BlockRegistryObject<? extends IColoredBlock, ?> result, IItemProvider plastic, String basePath) {
        EnumColor color = result.getBlock().getColor();
        String colorString = color.getRegistryPrefix();
        ExtendedShapedRecipeBuilder.shapedRecipe(result, 6)
              .pattern(PLASTIC_SLAB)
              .key(Pattern.CONSTANT, plastic)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        PlasticBlockRecipeProvider.registerRecolor(consumer, result, AdditionsTags.Items.SLABS_PLASTIC, color.getDyeTag(), basePath, colorString);
    }

    private void registerPlasticTransparentSlabs(Consumer<IFinishedRecipe> consumer, String basePath) {
        basePath += "transparent/";
        registerPlasticTransparentSlab(consumer, AdditionsBlocks.BLACK_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.BLACK_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentSlab(consumer, AdditionsBlocks.RED_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.RED_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentSlab(consumer, AdditionsBlocks.GREEN_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.GREEN_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentSlab(consumer, AdditionsBlocks.BROWN_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.BROWN_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentSlab(consumer, AdditionsBlocks.BLUE_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.BLUE_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentSlab(consumer, AdditionsBlocks.PURPLE_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.PURPLE_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentSlab(consumer, AdditionsBlocks.CYAN_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.CYAN_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentSlab(consumer, AdditionsBlocks.LIGHT_GRAY_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.LIGHT_GRAY_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentSlab(consumer, AdditionsBlocks.GRAY_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.GRAY_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentSlab(consumer, AdditionsBlocks.PINK_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.PINK_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentSlab(consumer, AdditionsBlocks.LIME_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.LIME_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentSlab(consumer, AdditionsBlocks.YELLOW_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.YELLOW_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentSlab(consumer, AdditionsBlocks.LIGHT_BLUE_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.LIGHT_BLUE_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentSlab(consumer, AdditionsBlocks.MAGENTA_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.MAGENTA_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentSlab(consumer, AdditionsBlocks.ORANGE_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.ORANGE_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentSlab(consumer, AdditionsBlocks.WHITE_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.WHITE_PLASTIC_TRANSPARENT_BLOCK, basePath);
    }

    private void registerPlasticTransparentSlab(Consumer<IFinishedRecipe> consumer, BlockRegistryObject<? extends IColoredBlock, ?> result, IItemProvider plastic,
          String basePath) {
        EnumColor color = result.getBlock().getColor();
        String colorString = color.getRegistryPrefix();
        ExtendedShapedRecipeBuilder.shapedRecipe(result, 6)
              .pattern(PLASTIC_SLAB)
              .key(Pattern.CONSTANT, plastic)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        PlasticBlockRecipeProvider.registerTransparentRecolor(consumer, result, AdditionsTags.Items.SLABS_PLASTIC_TRANSPARENT, color.getDyeTag(), basePath, colorString);
    }

    private void registerPlasticGlowSlabs(Consumer<IFinishedRecipe> consumer, String basePath) {
        basePath += "glow/";
        registerPlasticGlowSlab(consumer, AdditionsBlocks.BLACK_PLASTIC_GLOW_SLAB, AdditionsBlocks.BLACK_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowSlab(consumer, AdditionsBlocks.RED_PLASTIC_GLOW_SLAB, AdditionsBlocks.RED_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowSlab(consumer, AdditionsBlocks.GREEN_PLASTIC_GLOW_SLAB, AdditionsBlocks.GREEN_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowSlab(consumer, AdditionsBlocks.BROWN_PLASTIC_GLOW_SLAB, AdditionsBlocks.BROWN_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowSlab(consumer, AdditionsBlocks.BLUE_PLASTIC_GLOW_SLAB, AdditionsBlocks.BLUE_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowSlab(consumer, AdditionsBlocks.PURPLE_PLASTIC_GLOW_SLAB, AdditionsBlocks.PURPLE_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowSlab(consumer, AdditionsBlocks.CYAN_PLASTIC_GLOW_SLAB, AdditionsBlocks.CYAN_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowSlab(consumer, AdditionsBlocks.LIGHT_GRAY_PLASTIC_GLOW_SLAB, AdditionsBlocks.LIGHT_GRAY_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowSlab(consumer, AdditionsBlocks.GRAY_PLASTIC_GLOW_SLAB, AdditionsBlocks.GRAY_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowSlab(consumer, AdditionsBlocks.PINK_PLASTIC_GLOW_SLAB, AdditionsBlocks.PINK_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowSlab(consumer, AdditionsBlocks.LIME_PLASTIC_GLOW_SLAB, AdditionsBlocks.LIME_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowSlab(consumer, AdditionsBlocks.YELLOW_PLASTIC_GLOW_SLAB, AdditionsBlocks.YELLOW_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowSlab(consumer, AdditionsBlocks.LIGHT_BLUE_PLASTIC_GLOW_SLAB, AdditionsBlocks.LIGHT_BLUE_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowSlab(consumer, AdditionsBlocks.MAGENTA_PLASTIC_GLOW_SLAB, AdditionsBlocks.MAGENTA_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowSlab(consumer, AdditionsBlocks.ORANGE_PLASTIC_GLOW_SLAB, AdditionsBlocks.ORANGE_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowSlab(consumer, AdditionsBlocks.WHITE_PLASTIC_GLOW_SLAB, AdditionsBlocks.WHITE_PLASTIC_GLOW_BLOCK, basePath);
    }

    private void registerPlasticGlowSlab(Consumer<IFinishedRecipe> consumer, BlockRegistryObject<? extends IColoredBlock, ?> result, IItemProvider plastic, String basePath) {
        EnumColor color = result.getBlock().getColor();
        String colorString = color.getRegistryPrefix();
        ExtendedShapedRecipeBuilder.shapedRecipe(result, 6)
              .pattern(PLASTIC_SLAB)
              .key(Pattern.CONSTANT, plastic)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        PlasticBlockRecipeProvider.registerRecolor(consumer, result, AdditionsTags.Items.SLABS_PLASTIC_GLOW, color.getDyeTag(), basePath, colorString);
    }
}