package mekanism.additions.common.recipe;

import java.util.function.Consumer;
import mekanism.additions.common.AdditionsTags;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.ExtendedShapelessRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.Tags;

public class PlasticBlockRecipeProvider implements ISubRecipeProvider {

    private static final RecipePattern PLASTIC = RecipePattern.createPattern(
          TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
          TripleLine.of(Pattern.CONSTANT, Pattern.DYE, Pattern.CONSTANT),
          TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY));
    private static final RecipePattern PLASTIC_TRANSPARENT = RecipePattern.createPattern(
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
          TripleLine.of(Pattern.CONSTANT, Pattern.DYE, Pattern.CONSTANT),
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT));
    private static final RecipePattern REINFORCED_PLASTIC = RecipePattern.createPattern(
          TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
          TripleLine.of(Pattern.CONSTANT, Pattern.OSMIUM, Pattern.CONSTANT),
          TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY));
    private static final RecipePattern PLASTIC_ROAD = RecipePattern.createPattern(
          TripleLine.of(AdditionsRecipeProvider.SAND_CHAR, AdditionsRecipeProvider.SAND_CHAR, AdditionsRecipeProvider.SAND_CHAR),
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
          TripleLine.of(AdditionsRecipeProvider.SAND_CHAR, AdditionsRecipeProvider.SAND_CHAR, AdditionsRecipeProvider.SAND_CHAR));
    private static final RecipePattern SLICK_PLASTIC = RecipePattern.createPattern(
          TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
          TripleLine.of(Pattern.CONSTANT, AdditionsRecipeProvider.SLIME_CHAR, Pattern.CONSTANT),
          TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY));

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "plastic/";
        registerPlasticBlocks(consumer, basePath);
        registerPlasticGlow(consumer, basePath);
        registerReinforcedPlastic(consumer, basePath);
        registerPlasticRoads(consumer, basePath);
        registerSlickPlastic(consumer, basePath);
        registerPlasticTransparent(consumer, basePath);
    }

    private void registerPlasticBlocks(Consumer<IFinishedRecipe> consumer, String basePath) {
        basePath += "block/";
        registerPlasticBlock(consumer, AdditionsBlocks.BLACK_PLASTIC_BLOCK, basePath);
        registerPlasticBlock(consumer, AdditionsBlocks.RED_PLASTIC_BLOCK, basePath);
        registerPlasticBlock(consumer, AdditionsBlocks.GREEN_PLASTIC_BLOCK, basePath);
        registerPlasticBlock(consumer, AdditionsBlocks.BROWN_PLASTIC_BLOCK, basePath);
        registerPlasticBlock(consumer, AdditionsBlocks.BLUE_PLASTIC_BLOCK, basePath);
        registerPlasticBlock(consumer, AdditionsBlocks.PURPLE_PLASTIC_BLOCK, basePath);
        registerPlasticBlock(consumer, AdditionsBlocks.CYAN_PLASTIC_BLOCK, basePath);
        registerPlasticBlock(consumer, AdditionsBlocks.LIGHT_GRAY_PLASTIC_BLOCK, basePath);
        registerPlasticBlock(consumer, AdditionsBlocks.GRAY_PLASTIC_BLOCK, basePath);
        registerPlasticBlock(consumer, AdditionsBlocks.PINK_PLASTIC_BLOCK, basePath);
        registerPlasticBlock(consumer, AdditionsBlocks.LIME_PLASTIC_BLOCK, basePath);
        registerPlasticBlock(consumer, AdditionsBlocks.YELLOW_PLASTIC_BLOCK, basePath);
        registerPlasticBlock(consumer, AdditionsBlocks.LIGHT_BLUE_PLASTIC_BLOCK, basePath);
        registerPlasticBlock(consumer, AdditionsBlocks.MAGENTA_PLASTIC_BLOCK, basePath);
        registerPlasticBlock(consumer, AdditionsBlocks.ORANGE_PLASTIC_BLOCK, basePath);
        registerPlasticBlock(consumer, AdditionsBlocks.WHITE_PLASTIC_BLOCK, basePath);
    }

    private void registerPlasticBlock(Consumer<IFinishedRecipe> consumer, BlockRegistryObject<? extends IColoredBlock, ?> result, String basePath) {
        EnumColor color = result.getBlock().getColor();
        String colorString = color.getRegistryPrefix();
        ITag<Item> dye = color.getDyeTag();
        ExtendedShapedRecipeBuilder.shapedRecipe(result, 4)
              .pattern(PLASTIC)
              .key(Pattern.CONSTANT, MekanismItems.HDPE_SHEET)
              .key(Pattern.DYE, dye)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        registerRecolor(consumer, result, AdditionsTags.Items.PLASTIC_BLOCKS_PLASTIC, dye, basePath, colorString);
    }

    private void registerPlasticTransparent(Consumer<IFinishedRecipe> consumer, String basePath) {
        basePath += "transparent/";
        registerPlasticTransparent(consumer, AdditionsBlocks.BLACK_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparent(consumer, AdditionsBlocks.RED_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparent(consumer, AdditionsBlocks.GREEN_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparent(consumer, AdditionsBlocks.BROWN_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparent(consumer, AdditionsBlocks.BLUE_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparent(consumer, AdditionsBlocks.PURPLE_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparent(consumer, AdditionsBlocks.CYAN_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparent(consumer, AdditionsBlocks.LIGHT_GRAY_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparent(consumer, AdditionsBlocks.GRAY_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparent(consumer, AdditionsBlocks.PINK_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparent(consumer, AdditionsBlocks.LIME_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparent(consumer, AdditionsBlocks.YELLOW_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparent(consumer, AdditionsBlocks.LIGHT_BLUE_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparent(consumer, AdditionsBlocks.MAGENTA_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparent(consumer, AdditionsBlocks.ORANGE_PLASTIC_TRANSPARENT_BLOCK, basePath);
        registerPlasticTransparent(consumer, AdditionsBlocks.WHITE_PLASTIC_TRANSPARENT_BLOCK, basePath);
    }

    private void registerPlasticTransparent(Consumer<IFinishedRecipe> consumer, BlockRegistryObject<? extends IColoredBlock, ?> result, String basePath) {
        EnumColor color = result.getBlock().getColor();
        String colorString = color.getRegistryPrefix();
        ITag<Item> dye = color.getDyeTag();
        ExtendedShapedRecipeBuilder.shapedRecipe(result, 8)
              .pattern(PLASTIC_TRANSPARENT)
              .key(Pattern.CONSTANT, MekanismItems.HDPE_SHEET)
              .key(Pattern.DYE, dye)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        registerTransparentRecolor(consumer, result, AdditionsTags.Items.PLASTIC_BLOCKS_TRANSPARENT, dye, basePath, colorString);
    }

    private void registerPlasticGlow(Consumer<IFinishedRecipe> consumer, String basePath) {
        basePath += "glow/";
        registerPlasticGlow(consumer, AdditionsBlocks.BLACK_PLASTIC_GLOW_BLOCK, AdditionsBlocks.BLACK_PLASTIC_BLOCK, basePath);
        registerPlasticGlow(consumer, AdditionsBlocks.RED_PLASTIC_GLOW_BLOCK, AdditionsBlocks.RED_PLASTIC_BLOCK, basePath);
        registerPlasticGlow(consumer, AdditionsBlocks.GREEN_PLASTIC_GLOW_BLOCK, AdditionsBlocks.GREEN_PLASTIC_BLOCK, basePath);
        registerPlasticGlow(consumer, AdditionsBlocks.BROWN_PLASTIC_GLOW_BLOCK, AdditionsBlocks.BROWN_PLASTIC_BLOCK, basePath);
        registerPlasticGlow(consumer, AdditionsBlocks.BLUE_PLASTIC_GLOW_BLOCK, AdditionsBlocks.BLUE_PLASTIC_BLOCK, basePath);
        registerPlasticGlow(consumer, AdditionsBlocks.PURPLE_PLASTIC_GLOW_BLOCK, AdditionsBlocks.PURPLE_PLASTIC_BLOCK, basePath);
        registerPlasticGlow(consumer, AdditionsBlocks.CYAN_PLASTIC_GLOW_BLOCK, AdditionsBlocks.CYAN_PLASTIC_BLOCK, basePath);
        registerPlasticGlow(consumer, AdditionsBlocks.LIGHT_GRAY_PLASTIC_GLOW_BLOCK, AdditionsBlocks.LIGHT_GRAY_PLASTIC_BLOCK, basePath);
        registerPlasticGlow(consumer, AdditionsBlocks.GRAY_PLASTIC_GLOW_BLOCK, AdditionsBlocks.GRAY_PLASTIC_BLOCK, basePath);
        registerPlasticGlow(consumer, AdditionsBlocks.PINK_PLASTIC_GLOW_BLOCK, AdditionsBlocks.PINK_PLASTIC_BLOCK, basePath);
        registerPlasticGlow(consumer, AdditionsBlocks.LIME_PLASTIC_GLOW_BLOCK, AdditionsBlocks.LIME_PLASTIC_BLOCK, basePath);
        registerPlasticGlow(consumer, AdditionsBlocks.YELLOW_PLASTIC_GLOW_BLOCK, AdditionsBlocks.YELLOW_PLASTIC_BLOCK, basePath);
        registerPlasticGlow(consumer, AdditionsBlocks.LIGHT_BLUE_PLASTIC_GLOW_BLOCK, AdditionsBlocks.LIGHT_BLUE_PLASTIC_BLOCK, basePath);
        registerPlasticGlow(consumer, AdditionsBlocks.MAGENTA_PLASTIC_GLOW_BLOCK, AdditionsBlocks.MAGENTA_PLASTIC_BLOCK, basePath);
        registerPlasticGlow(consumer, AdditionsBlocks.ORANGE_PLASTIC_GLOW_BLOCK, AdditionsBlocks.ORANGE_PLASTIC_BLOCK, basePath);
        registerPlasticGlow(consumer, AdditionsBlocks.WHITE_PLASTIC_GLOW_BLOCK, AdditionsBlocks.WHITE_PLASTIC_BLOCK, basePath);
    }

    private void registerPlasticGlow(Consumer<IFinishedRecipe> consumer, BlockRegistryObject<? extends IColoredBlock, ?> result, IItemProvider plastic, String basePath) {
        EnumColor color = result.getBlock().getColor();
        String colorString = color.getRegistryPrefix();
        ExtendedShapelessRecipeBuilder.shapelessRecipe(result, 3)
              .addIngredient(plastic, 3)
              .addIngredient(Tags.Items.DUSTS_GLOWSTONE)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        registerRecolor(consumer, result, AdditionsTags.Items.PLASTIC_BLOCKS_GLOW, color.getDyeTag(), basePath, colorString);
    }

    private void registerReinforcedPlastic(Consumer<IFinishedRecipe> consumer, String basePath) {
        basePath += "reinforced/";
        registerReinforcedPlastic(consumer, AdditionsBlocks.BLACK_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.BLACK_PLASTIC_BLOCK, basePath);
        registerReinforcedPlastic(consumer, AdditionsBlocks.RED_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.RED_PLASTIC_BLOCK, basePath);
        registerReinforcedPlastic(consumer, AdditionsBlocks.GREEN_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.GREEN_PLASTIC_BLOCK, basePath);
        registerReinforcedPlastic(consumer, AdditionsBlocks.BROWN_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.BROWN_PLASTIC_BLOCK, basePath);
        registerReinforcedPlastic(consumer, AdditionsBlocks.BLUE_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.BLUE_PLASTIC_BLOCK, basePath);
        registerReinforcedPlastic(consumer, AdditionsBlocks.PURPLE_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.PURPLE_PLASTIC_BLOCK, basePath);
        registerReinforcedPlastic(consumer, AdditionsBlocks.CYAN_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.CYAN_PLASTIC_BLOCK, basePath);
        registerReinforcedPlastic(consumer, AdditionsBlocks.LIGHT_GRAY_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.LIGHT_GRAY_PLASTIC_BLOCK, basePath);
        registerReinforcedPlastic(consumer, AdditionsBlocks.GRAY_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.GRAY_PLASTIC_BLOCK, basePath);
        registerReinforcedPlastic(consumer, AdditionsBlocks.PINK_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.PINK_PLASTIC_BLOCK, basePath);
        registerReinforcedPlastic(consumer, AdditionsBlocks.LIME_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.LIME_PLASTIC_BLOCK, basePath);
        registerReinforcedPlastic(consumer, AdditionsBlocks.YELLOW_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.YELLOW_PLASTIC_BLOCK, basePath);
        registerReinforcedPlastic(consumer, AdditionsBlocks.LIGHT_BLUE_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.LIGHT_BLUE_PLASTIC_BLOCK, basePath);
        registerReinforcedPlastic(consumer, AdditionsBlocks.MAGENTA_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.MAGENTA_PLASTIC_BLOCK, basePath);
        registerReinforcedPlastic(consumer, AdditionsBlocks.ORANGE_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.ORANGE_PLASTIC_BLOCK, basePath);
        registerReinforcedPlastic(consumer, AdditionsBlocks.WHITE_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.WHITE_PLASTIC_BLOCK, basePath);
    }

    private void registerReinforcedPlastic(Consumer<IFinishedRecipe> consumer, BlockRegistryObject<? extends IColoredBlock, ?> result, IItemProvider plastic, String basePath) {
        EnumColor color = result.getBlock().getColor();
        String colorString = color.getRegistryPrefix();
        ExtendedShapedRecipeBuilder.shapedRecipe(result, 4)
              .pattern(REINFORCED_PLASTIC)
              .key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.OSMIUM))
              .key(Pattern.CONSTANT, plastic)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        registerRecolor(consumer, result, AdditionsTags.Items.PLASTIC_BLOCKS_REINFORCED, color.getDyeTag(), basePath, colorString);
    }

    private void registerPlasticRoads(Consumer<IFinishedRecipe> consumer, String basePath) {
        basePath += "road/";
        registerPlasticRoad(consumer, AdditionsBlocks.BLACK_PLASTIC_ROAD, AdditionsBlocks.BLACK_SLICK_PLASTIC_BLOCK, basePath);
        registerPlasticRoad(consumer, AdditionsBlocks.RED_PLASTIC_ROAD, AdditionsBlocks.RED_SLICK_PLASTIC_BLOCK, basePath);
        registerPlasticRoad(consumer, AdditionsBlocks.GREEN_PLASTIC_ROAD, AdditionsBlocks.GREEN_SLICK_PLASTIC_BLOCK, basePath);
        registerPlasticRoad(consumer, AdditionsBlocks.BROWN_PLASTIC_ROAD, AdditionsBlocks.BROWN_SLICK_PLASTIC_BLOCK, basePath);
        registerPlasticRoad(consumer, AdditionsBlocks.BLUE_PLASTIC_ROAD, AdditionsBlocks.BLUE_SLICK_PLASTIC_BLOCK, basePath);
        registerPlasticRoad(consumer, AdditionsBlocks.PURPLE_PLASTIC_ROAD, AdditionsBlocks.PURPLE_SLICK_PLASTIC_BLOCK, basePath);
        registerPlasticRoad(consumer, AdditionsBlocks.CYAN_PLASTIC_ROAD, AdditionsBlocks.CYAN_SLICK_PLASTIC_BLOCK, basePath);
        registerPlasticRoad(consumer, AdditionsBlocks.LIGHT_GRAY_PLASTIC_ROAD, AdditionsBlocks.LIGHT_GRAY_SLICK_PLASTIC_BLOCK, basePath);
        registerPlasticRoad(consumer, AdditionsBlocks.GRAY_PLASTIC_ROAD, AdditionsBlocks.GRAY_SLICK_PLASTIC_BLOCK, basePath);
        registerPlasticRoad(consumer, AdditionsBlocks.PINK_PLASTIC_ROAD, AdditionsBlocks.PINK_SLICK_PLASTIC_BLOCK, basePath);
        registerPlasticRoad(consumer, AdditionsBlocks.LIME_PLASTIC_ROAD, AdditionsBlocks.LIME_SLICK_PLASTIC_BLOCK, basePath);
        registerPlasticRoad(consumer, AdditionsBlocks.YELLOW_PLASTIC_ROAD, AdditionsBlocks.YELLOW_SLICK_PLASTIC_BLOCK, basePath);
        registerPlasticRoad(consumer, AdditionsBlocks.LIGHT_BLUE_PLASTIC_ROAD, AdditionsBlocks.LIGHT_BLUE_SLICK_PLASTIC_BLOCK, basePath);
        registerPlasticRoad(consumer, AdditionsBlocks.MAGENTA_PLASTIC_ROAD, AdditionsBlocks.MAGENTA_SLICK_PLASTIC_BLOCK, basePath);
        registerPlasticRoad(consumer, AdditionsBlocks.ORANGE_PLASTIC_ROAD, AdditionsBlocks.ORANGE_SLICK_PLASTIC_BLOCK, basePath);
        registerPlasticRoad(consumer, AdditionsBlocks.WHITE_PLASTIC_ROAD, AdditionsBlocks.WHITE_SLICK_PLASTIC_BLOCK, basePath);
    }

    private void registerPlasticRoad(Consumer<IFinishedRecipe> consumer, BlockRegistryObject<? extends IColoredBlock, ?> result, IItemProvider slickPlastic, String basePath) {
        EnumColor color = result.getBlock().getColor();
        String colorString = color.getRegistryPrefix();
        ExtendedShapedRecipeBuilder.shapedRecipe(result, 3)
              .pattern(PLASTIC_ROAD)
              .key(AdditionsRecipeProvider.SAND_CHAR, Tags.Items.SAND)
              .key(Pattern.CONSTANT, slickPlastic)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        registerRecolor(consumer, result, AdditionsTags.Items.PLASTIC_BLOCKS_ROAD, color.getDyeTag(), basePath, colorString);
    }

    private void registerSlickPlastic(Consumer<IFinishedRecipe> consumer, String basePath) {
        basePath += "slick/";
        registerSlickPlastic(consumer, AdditionsBlocks.BLACK_SLICK_PLASTIC_BLOCK, AdditionsBlocks.BLACK_PLASTIC_BLOCK, basePath);
        registerSlickPlastic(consumer, AdditionsBlocks.RED_SLICK_PLASTIC_BLOCK, AdditionsBlocks.RED_PLASTIC_BLOCK, basePath);
        registerSlickPlastic(consumer, AdditionsBlocks.GREEN_SLICK_PLASTIC_BLOCK, AdditionsBlocks.GREEN_PLASTIC_BLOCK, basePath);
        registerSlickPlastic(consumer, AdditionsBlocks.BROWN_SLICK_PLASTIC_BLOCK, AdditionsBlocks.BROWN_PLASTIC_BLOCK, basePath);
        registerSlickPlastic(consumer, AdditionsBlocks.BLUE_SLICK_PLASTIC_BLOCK, AdditionsBlocks.BLUE_PLASTIC_BLOCK, basePath);
        registerSlickPlastic(consumer, AdditionsBlocks.PURPLE_SLICK_PLASTIC_BLOCK, AdditionsBlocks.PURPLE_PLASTIC_BLOCK, basePath);
        registerSlickPlastic(consumer, AdditionsBlocks.CYAN_SLICK_PLASTIC_BLOCK, AdditionsBlocks.CYAN_PLASTIC_BLOCK, basePath);
        registerSlickPlastic(consumer, AdditionsBlocks.LIGHT_GRAY_SLICK_PLASTIC_BLOCK, AdditionsBlocks.LIGHT_GRAY_PLASTIC_BLOCK, basePath);
        registerSlickPlastic(consumer, AdditionsBlocks.GRAY_SLICK_PLASTIC_BLOCK, AdditionsBlocks.GRAY_PLASTIC_BLOCK, basePath);
        registerSlickPlastic(consumer, AdditionsBlocks.PINK_SLICK_PLASTIC_BLOCK, AdditionsBlocks.PINK_PLASTIC_BLOCK, basePath);
        registerSlickPlastic(consumer, AdditionsBlocks.LIME_SLICK_PLASTIC_BLOCK, AdditionsBlocks.LIME_PLASTIC_BLOCK, basePath);
        registerSlickPlastic(consumer, AdditionsBlocks.YELLOW_SLICK_PLASTIC_BLOCK, AdditionsBlocks.YELLOW_PLASTIC_BLOCK, basePath);
        registerSlickPlastic(consumer, AdditionsBlocks.LIGHT_BLUE_SLICK_PLASTIC_BLOCK, AdditionsBlocks.LIGHT_BLUE_PLASTIC_BLOCK, basePath);
        registerSlickPlastic(consumer, AdditionsBlocks.MAGENTA_SLICK_PLASTIC_BLOCK, AdditionsBlocks.MAGENTA_PLASTIC_BLOCK, basePath);
        registerSlickPlastic(consumer, AdditionsBlocks.ORANGE_SLICK_PLASTIC_BLOCK, AdditionsBlocks.ORANGE_PLASTIC_BLOCK, basePath);
        registerSlickPlastic(consumer, AdditionsBlocks.WHITE_SLICK_PLASTIC_BLOCK, AdditionsBlocks.WHITE_PLASTIC_BLOCK, basePath);
    }

    private void registerSlickPlastic(Consumer<IFinishedRecipe> consumer, BlockRegistryObject<? extends IColoredBlock, ?> result, IItemProvider plastic, String basePath) {
        EnumColor color = result.getBlock().getColor();
        String colorString = color.getRegistryPrefix();
        ExtendedShapedRecipeBuilder.shapedRecipe(result, 4)
              .pattern(SLICK_PLASTIC)
              .key(Pattern.CONSTANT, plastic)
              .key(AdditionsRecipeProvider.SLIME_CHAR, Tags.Items.SLIMEBALLS)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        //Enriching recipes
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(plastic),
              result.getItemStack()
        ).build(consumer, MekanismAdditions.rl(basePath + "enriching/" + colorString));
        //Recolor recipes
        registerRecolor(consumer, result, AdditionsTags.Items.PLASTIC_BLOCKS_SLICK, color.getDyeTag(), basePath, colorString);
    }

    public static void registerRecolor(Consumer<IFinishedRecipe> consumer, IItemProvider result, ITag<Item> blockType, ITag<Item> dye, String basePath, String colorString) {
        ExtendedShapedRecipeBuilder.shapedRecipe(result, 4)
              .pattern(PLASTIC)
              .key(Pattern.CONSTANT, blockType)
              .key(Pattern.DYE, dye)
              .build(consumer, MekanismAdditions.rl(basePath + "recolor/" + colorString));
    }

    public static void registerTransparentRecolor(Consumer<IFinishedRecipe> consumer, IItemProvider result, ITag<Item> blockType, ITag<Item> dye, String basePath,
          String colorString) {
        ExtendedShapedRecipeBuilder.shapedRecipe(result, 8)
              .pattern(PLASTIC_TRANSPARENT)
              .key(Pattern.CONSTANT, blockType)
              .key(Pattern.DYE, dye)
              .build(consumer, MekanismAdditions.rl(basePath + "recolor/" + colorString));
    }
}