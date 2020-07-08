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

public class PlasticStairsRecipeProvider implements ISubRecipeProvider {

    private static final RecipePattern PLASTIC_STAIRS = RecipePattern.createPattern(
          TripleLine.of(Pattern.CONSTANT, Pattern.EMPTY, Pattern.EMPTY),
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.EMPTY),
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT));

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "plastic/stairs/";
        registerPlasticStairs(consumer, basePath);
        registerPlasticGlowStairs(consumer, basePath);
        registerPlasticTransparentStairs(consumer, basePath);
    }

    private void registerPlasticStairs(Consumer<IFinishedRecipe> consumer, String basePath) {
        registerPlasticStairs(consumer, AdditionsBlocks.BLACK_PLASTIC_STAIRS, AdditionsBlocks.BLACK_PLASTIC_BLOCK, basePath);
        registerPlasticStairs(consumer, AdditionsBlocks.RED_PLASTIC_STAIRS, AdditionsBlocks.RED_PLASTIC_BLOCK, basePath);
        registerPlasticStairs(consumer, AdditionsBlocks.GREEN_PLASTIC_STAIRS, AdditionsBlocks.GREEN_PLASTIC_BLOCK, basePath);
        registerPlasticStairs(consumer, AdditionsBlocks.BROWN_PLASTIC_STAIRS, AdditionsBlocks.BROWN_PLASTIC_BLOCK, basePath);
        registerPlasticStairs(consumer, AdditionsBlocks.BLUE_PLASTIC_STAIRS, AdditionsBlocks.BLUE_PLASTIC_BLOCK, basePath);
        registerPlasticStairs(consumer, AdditionsBlocks.PURPLE_PLASTIC_STAIRS, AdditionsBlocks.PURPLE_PLASTIC_BLOCK, basePath);
        registerPlasticStairs(consumer, AdditionsBlocks.CYAN_PLASTIC_STAIRS, AdditionsBlocks.CYAN_PLASTIC_BLOCK, basePath);
        registerPlasticStairs(consumer, AdditionsBlocks.LIGHT_GRAY_PLASTIC_STAIRS, AdditionsBlocks.LIGHT_GRAY_PLASTIC_BLOCK, basePath);
        registerPlasticStairs(consumer, AdditionsBlocks.GRAY_PLASTIC_STAIRS, AdditionsBlocks.GRAY_PLASTIC_BLOCK, basePath);
        registerPlasticStairs(consumer, AdditionsBlocks.PINK_PLASTIC_STAIRS, AdditionsBlocks.PINK_PLASTIC_BLOCK, basePath);
        registerPlasticStairs(consumer, AdditionsBlocks.LIME_PLASTIC_STAIRS, AdditionsBlocks.LIME_PLASTIC_BLOCK, basePath);
        registerPlasticStairs(consumer, AdditionsBlocks.YELLOW_PLASTIC_STAIRS, AdditionsBlocks.YELLOW_PLASTIC_BLOCK, basePath);
        registerPlasticStairs(consumer, AdditionsBlocks.LIGHT_BLUE_PLASTIC_STAIRS, AdditionsBlocks.LIGHT_BLUE_PLASTIC_BLOCK, basePath);
        registerPlasticStairs(consumer, AdditionsBlocks.MAGENTA_PLASTIC_STAIRS, AdditionsBlocks.MAGENTA_PLASTIC_BLOCK, basePath);
        registerPlasticStairs(consumer, AdditionsBlocks.ORANGE_PLASTIC_STAIRS, AdditionsBlocks.ORANGE_PLASTIC_BLOCK, basePath);
        registerPlasticStairs(consumer, AdditionsBlocks.WHITE_PLASTIC_STAIRS, AdditionsBlocks.WHITE_PLASTIC_BLOCK, basePath);
    }

    private void registerPlasticStairs(Consumer<IFinishedRecipe> consumer, BlockRegistryObject<? extends IColoredBlock, ?> result, IItemProvider plastic, String basePath) {
        EnumColor color = result.getBlock().getColor();
        String colorString = color.getRegistryPrefix();
        ExtendedShapedRecipeBuilder.shapedRecipe(result, 4)
              .pattern(PLASTIC_STAIRS)
              .key(Pattern.CONSTANT, plastic)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        PlasticBlockRecipeProvider.registerRecolor(consumer, result, AdditionsTags.Items.STAIRS_PLASTIC, color.getDyeTag(), basePath, colorString);
    }

    private void registerPlasticTransparentStairs(Consumer<IFinishedRecipe> consumer, String basePath) {
        basePath += "transparent/";
        registerPlasticTransparentStairs(consumer, AdditionsBlocks.BLACK_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.BLACK_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentStairs(consumer, AdditionsBlocks.RED_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.RED_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentStairs(consumer, AdditionsBlocks.GREEN_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.GREEN_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentStairs(consumer, AdditionsBlocks.BROWN_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.BROWN_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentStairs(consumer, AdditionsBlocks.BLUE_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.BLUE_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentStairs(consumer, AdditionsBlocks.PURPLE_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.PURPLE_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentStairs(consumer, AdditionsBlocks.CYAN_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.CYAN_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentStairs(consumer, AdditionsBlocks.LIGHT_GRAY_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.LIGHT_GRAY_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentStairs(consumer, AdditionsBlocks.GRAY_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.GRAY_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentStairs(consumer, AdditionsBlocks.PINK_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.PINK_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentStairs(consumer, AdditionsBlocks.LIME_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.LIME_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentStairs(consumer, AdditionsBlocks.YELLOW_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.YELLOW_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentStairs(consumer, AdditionsBlocks.LIGHT_BLUE_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.LIGHT_BLUE_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentStairs(consumer, AdditionsBlocks.MAGENTA_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.MAGENTA_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentStairs(consumer, AdditionsBlocks.ORANGE_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.ORANGE_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparentStairs(consumer, AdditionsBlocks.WHITE_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.WHITE_PLASTIC_TRANSPARENT_BLOCK, basePath);
    }

    private void registerPlasticTransparentStairs(Consumer<IFinishedRecipe> consumer, BlockRegistryObject<? extends IColoredBlock, ?> result, IItemProvider plastic,
          String basePath) {
        EnumColor color = result.getBlock().getColor();
        String colorString = color.getRegistryPrefix();
        ExtendedShapedRecipeBuilder.shapedRecipe(result, 4)
              .pattern(PLASTIC_STAIRS)
              .key(Pattern.CONSTANT, plastic)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        PlasticBlockRecipeProvider.registerTransparentRecolor(consumer, result, AdditionsTags.Items.STAIRS_PLASTIC_TRANSPARENT, color.getDyeTag(), basePath, colorString);
    }

    private void registerPlasticGlowStairs(Consumer<IFinishedRecipe> consumer, String basePath) {
        basePath += "glow/";
        registerPlasticGlowStairs(consumer, AdditionsBlocks.BLACK_PLASTIC_GLOW_STAIRS, AdditionsBlocks.BLACK_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowStairs(consumer, AdditionsBlocks.RED_PLASTIC_GLOW_STAIRS, AdditionsBlocks.RED_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowStairs(consumer, AdditionsBlocks.GREEN_PLASTIC_GLOW_STAIRS, AdditionsBlocks.GREEN_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowStairs(consumer, AdditionsBlocks.BROWN_PLASTIC_GLOW_STAIRS, AdditionsBlocks.BROWN_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowStairs(consumer, AdditionsBlocks.BLUE_PLASTIC_GLOW_STAIRS, AdditionsBlocks.BLUE_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowStairs(consumer, AdditionsBlocks.PURPLE_PLASTIC_GLOW_STAIRS, AdditionsBlocks.PURPLE_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowStairs(consumer, AdditionsBlocks.CYAN_PLASTIC_GLOW_STAIRS, AdditionsBlocks.CYAN_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowStairs(consumer, AdditionsBlocks.LIGHT_GRAY_PLASTIC_GLOW_STAIRS, AdditionsBlocks.LIGHT_GRAY_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowStairs(consumer, AdditionsBlocks.GRAY_PLASTIC_GLOW_STAIRS, AdditionsBlocks.GRAY_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowStairs(consumer, AdditionsBlocks.PINK_PLASTIC_GLOW_STAIRS, AdditionsBlocks.PINK_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowStairs(consumer, AdditionsBlocks.LIME_PLASTIC_GLOW_STAIRS, AdditionsBlocks.LIME_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowStairs(consumer, AdditionsBlocks.YELLOW_PLASTIC_GLOW_STAIRS, AdditionsBlocks.YELLOW_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowStairs(consumer, AdditionsBlocks.LIGHT_BLUE_PLASTIC_GLOW_STAIRS, AdditionsBlocks.LIGHT_BLUE_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowStairs(consumer, AdditionsBlocks.MAGENTA_PLASTIC_GLOW_STAIRS, AdditionsBlocks.MAGENTA_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowStairs(consumer, AdditionsBlocks.ORANGE_PLASTIC_GLOW_STAIRS, AdditionsBlocks.ORANGE_PLASTIC_GLOW_BLOCK, basePath);
        registerPlasticGlowStairs(consumer, AdditionsBlocks.WHITE_PLASTIC_GLOW_STAIRS, AdditionsBlocks.WHITE_PLASTIC_GLOW_BLOCK, basePath);
    }

    private void registerPlasticGlowStairs(Consumer<IFinishedRecipe> consumer, BlockRegistryObject<? extends IColoredBlock, ?> result, IItemProvider plastic, String basePath) {
        EnumColor color = result.getBlock().getColor();
        String colorString = color.getRegistryPrefix();
        ExtendedShapedRecipeBuilder.shapedRecipe(result, 4)
              .pattern(PLASTIC_STAIRS)
              .key(Pattern.CONSTANT, plastic)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        PlasticBlockRecipeProvider.registerRecolor(consumer, result, AdditionsTags.Items.STAIRS_PLASTIC_GLOW, color.getDyeTag(), basePath, colorString);
    }
}