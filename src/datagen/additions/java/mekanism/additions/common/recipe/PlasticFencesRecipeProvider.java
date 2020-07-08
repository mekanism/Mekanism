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
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.IFinishedRecipe;

public class PlasticFencesRecipeProvider implements ISubRecipeProvider {

    private static final RecipePattern PLASTIC_FENCE = RecipePattern.createPattern(
          TripleLine.of(Pattern.CONSTANT, AdditionsRecipeProvider.PLASTIC_ROD_CHAR, Pattern.CONSTANT),
          TripleLine.of(Pattern.CONSTANT, AdditionsRecipeProvider.PLASTIC_ROD_CHAR, Pattern.CONSTANT));
    private static final RecipePattern PLASTIC_FENCE_GATE = RecipePattern.createPattern(
          TripleLine.of(AdditionsRecipeProvider.PLASTIC_ROD_CHAR, Pattern.CONSTANT, AdditionsRecipeProvider.PLASTIC_ROD_CHAR),
          TripleLine.of(AdditionsRecipeProvider.PLASTIC_ROD_CHAR, Pattern.CONSTANT, AdditionsRecipeProvider.PLASTIC_ROD_CHAR));

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "plastic/";
        registerPlasticFences(consumer, basePath);
        registerPlasticFenceGates(consumer, basePath);
    }

    private void registerPlasticFences(Consumer<IFinishedRecipe> consumer, String basePath) {
        basePath += "fence/";
        registerPlasticFence(consumer, AdditionsBlocks.BLACK_PLASTIC_FENCE, AdditionsBlocks.BLACK_PLASTIC_BLOCK, basePath);
        registerPlasticFence(consumer, AdditionsBlocks.RED_PLASTIC_FENCE, AdditionsBlocks.RED_PLASTIC_BLOCK, basePath);
        registerPlasticFence(consumer, AdditionsBlocks.GREEN_PLASTIC_FENCE, AdditionsBlocks.GREEN_PLASTIC_BLOCK, basePath);
        registerPlasticFence(consumer, AdditionsBlocks.BROWN_PLASTIC_FENCE, AdditionsBlocks.BROWN_PLASTIC_BLOCK, basePath);
        registerPlasticFence(consumer, AdditionsBlocks.BLUE_PLASTIC_FENCE, AdditionsBlocks.BLUE_PLASTIC_BLOCK, basePath);
        registerPlasticFence(consumer, AdditionsBlocks.PURPLE_PLASTIC_FENCE, AdditionsBlocks.PURPLE_PLASTIC_BLOCK, basePath);
        registerPlasticFence(consumer, AdditionsBlocks.CYAN_PLASTIC_FENCE, AdditionsBlocks.CYAN_PLASTIC_BLOCK, basePath);
        registerPlasticFence(consumer, AdditionsBlocks.LIGHT_GRAY_PLASTIC_FENCE, AdditionsBlocks.LIGHT_GRAY_PLASTIC_BLOCK, basePath);
        registerPlasticFence(consumer, AdditionsBlocks.GRAY_PLASTIC_FENCE, AdditionsBlocks.GRAY_PLASTIC_BLOCK, basePath);
        registerPlasticFence(consumer, AdditionsBlocks.PINK_PLASTIC_FENCE, AdditionsBlocks.PINK_PLASTIC_BLOCK, basePath);
        registerPlasticFence(consumer, AdditionsBlocks.LIME_PLASTIC_FENCE, AdditionsBlocks.LIME_PLASTIC_BLOCK, basePath);
        registerPlasticFence(consumer, AdditionsBlocks.YELLOW_PLASTIC_FENCE, AdditionsBlocks.YELLOW_PLASTIC_BLOCK, basePath);
        registerPlasticFence(consumer, AdditionsBlocks.LIGHT_BLUE_PLASTIC_FENCE, AdditionsBlocks.LIGHT_BLUE_PLASTIC_BLOCK, basePath);
        registerPlasticFence(consumer, AdditionsBlocks.MAGENTA_PLASTIC_FENCE, AdditionsBlocks.MAGENTA_PLASTIC_BLOCK, basePath);
        registerPlasticFence(consumer, AdditionsBlocks.ORANGE_PLASTIC_FENCE, AdditionsBlocks.ORANGE_PLASTIC_BLOCK, basePath);
        registerPlasticFence(consumer, AdditionsBlocks.WHITE_PLASTIC_FENCE, AdditionsBlocks.WHITE_PLASTIC_BLOCK, basePath);
    }

    private void registerPlasticFence(Consumer<IFinishedRecipe> consumer, BlockRegistryObject<? extends IColoredBlock, ?> result, IItemProvider plastic, String basePath) {
        EnumColor color = result.getBlock().getColor();
        String colorString = color.getRegistryPrefix();
        ExtendedShapedRecipeBuilder.shapedRecipe(result, 3)
              .pattern(PLASTIC_FENCE)
              .key(AdditionsRecipeProvider.PLASTIC_ROD_CHAR, MekanismTags.Items.RODS_PLASTIC)
              .key(Pattern.CONSTANT, plastic)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        PlasticBlockRecipeProvider.registerRecolor(consumer, result, AdditionsTags.Items.FENCES_PLASTIC, color.getDyeTag(), basePath, colorString);
    }

    private void registerPlasticFenceGates(Consumer<IFinishedRecipe> consumer, String basePath) {
        basePath += "fence_gate/";
        registerPlasticFenceGate(consumer, AdditionsBlocks.BLACK_PLASTIC_FENCE_GATE, AdditionsBlocks.BLACK_PLASTIC_BLOCK, basePath);
        registerPlasticFenceGate(consumer, AdditionsBlocks.RED_PLASTIC_FENCE_GATE, AdditionsBlocks.RED_PLASTIC_BLOCK, basePath);
        registerPlasticFenceGate(consumer, AdditionsBlocks.GREEN_PLASTIC_FENCE_GATE, AdditionsBlocks.GREEN_PLASTIC_BLOCK, basePath);
        registerPlasticFenceGate(consumer, AdditionsBlocks.BROWN_PLASTIC_FENCE_GATE, AdditionsBlocks.BROWN_PLASTIC_BLOCK, basePath);
        registerPlasticFenceGate(consumer, AdditionsBlocks.BLUE_PLASTIC_FENCE_GATE, AdditionsBlocks.BLUE_PLASTIC_BLOCK, basePath);
        registerPlasticFenceGate(consumer, AdditionsBlocks.PURPLE_PLASTIC_FENCE_GATE, AdditionsBlocks.PURPLE_PLASTIC_BLOCK, basePath);
        registerPlasticFenceGate(consumer, AdditionsBlocks.CYAN_PLASTIC_FENCE_GATE, AdditionsBlocks.CYAN_PLASTIC_BLOCK, basePath);
        registerPlasticFenceGate(consumer, AdditionsBlocks.LIGHT_GRAY_PLASTIC_FENCE_GATE, AdditionsBlocks.LIGHT_GRAY_PLASTIC_BLOCK, basePath);
        registerPlasticFenceGate(consumer, AdditionsBlocks.GRAY_PLASTIC_FENCE_GATE, AdditionsBlocks.GRAY_PLASTIC_BLOCK, basePath);
        registerPlasticFenceGate(consumer, AdditionsBlocks.PINK_PLASTIC_FENCE_GATE, AdditionsBlocks.PINK_PLASTIC_BLOCK, basePath);
        registerPlasticFenceGate(consumer, AdditionsBlocks.LIME_PLASTIC_FENCE_GATE, AdditionsBlocks.LIME_PLASTIC_BLOCK, basePath);
        registerPlasticFenceGate(consumer, AdditionsBlocks.YELLOW_PLASTIC_FENCE_GATE, AdditionsBlocks.YELLOW_PLASTIC_BLOCK, basePath);
        registerPlasticFenceGate(consumer, AdditionsBlocks.LIGHT_BLUE_PLASTIC_FENCE_GATE, AdditionsBlocks.LIGHT_BLUE_PLASTIC_BLOCK, basePath);
        registerPlasticFenceGate(consumer, AdditionsBlocks.MAGENTA_PLASTIC_FENCE_GATE, AdditionsBlocks.MAGENTA_PLASTIC_BLOCK, basePath);
        registerPlasticFenceGate(consumer, AdditionsBlocks.ORANGE_PLASTIC_FENCE_GATE, AdditionsBlocks.ORANGE_PLASTIC_BLOCK, basePath);
        registerPlasticFenceGate(consumer, AdditionsBlocks.WHITE_PLASTIC_FENCE_GATE, AdditionsBlocks.WHITE_PLASTIC_BLOCK, basePath);
    }

    private void registerPlasticFenceGate(Consumer<IFinishedRecipe> consumer, BlockRegistryObject<? extends IColoredBlock, ?> result, IItemProvider plastic, String basePath) {
        EnumColor color = result.getBlock().getColor();
        String colorString = color.getRegistryPrefix();
        ExtendedShapedRecipeBuilder.shapedRecipe(result)
              .pattern(PLASTIC_FENCE_GATE)
              .key(AdditionsRecipeProvider.PLASTIC_ROD_CHAR, MekanismTags.Items.RODS_PLASTIC)
              .key(Pattern.CONSTANT, plastic)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        PlasticBlockRecipeProvider.registerRecolor(consumer, result, AdditionsTags.Items.FENCE_GATES_PLASTIC, color.getDyeTag(), basePath, colorString);
    }
}