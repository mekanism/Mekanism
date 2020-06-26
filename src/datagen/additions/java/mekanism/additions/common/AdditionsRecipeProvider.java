package mekanism.additions.common;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.additions.common.item.ItemBalloon;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.ExtendedShapelessRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.Tags;

@ParametersAreNonnullByDefault
public class AdditionsRecipeProvider extends BaseRecipeProvider {

    private static final char TNT_CHAR = 'T';
    private static final char OBSIDIAN_CHAR = 'O';
    private static final char GLASS_PANES_CHAR = 'P';
    private static final char PLASTIC_SHEET_CHAR = 'H';
    private static final char PLASTIC_ROD_CHAR = 'R';
    private static final char SAND_CHAR = 'S';
    private static final char SLIME_CHAR = 'S';

    private static final RecipePattern BLOCK_RECOLOR = RecipePattern.createPattern(
          TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
          TripleLine.of(Pattern.CONSTANT, Pattern.DYE, Pattern.CONSTANT),
          TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY));
    private static final RecipePattern GLOW_PANEL = RecipePattern.createPattern(
          TripleLine.of(GLASS_PANES_CHAR, PLASTIC_SHEET_CHAR, GLASS_PANES_CHAR),
          TripleLine.of(PLASTIC_SHEET_CHAR, Pattern.DYE, PLASTIC_SHEET_CHAR),
          TripleLine.of(Pattern.GLOWSTONE, PLASTIC_SHEET_CHAR, Pattern.GLOWSTONE));
    private static final RecipePattern PLASTIC = RecipePattern.createPattern(
          TripleLine.of(PLASTIC_SHEET_CHAR, PLASTIC_SHEET_CHAR, PLASTIC_SHEET_CHAR),
          TripleLine.of(PLASTIC_SHEET_CHAR, Pattern.DYE, PLASTIC_SHEET_CHAR),
          TripleLine.of(PLASTIC_SHEET_CHAR, PLASTIC_SHEET_CHAR, PLASTIC_SHEET_CHAR));
    private static final RecipePattern PLASTIC_FENCE = RecipePattern.createPattern(
          TripleLine.of(Pattern.CONSTANT, PLASTIC_ROD_CHAR, Pattern.CONSTANT),
          TripleLine.of(Pattern.CONSTANT, PLASTIC_ROD_CHAR, Pattern.CONSTANT));
    private static final RecipePattern PLASTIC_FENCE_GATE = RecipePattern.createPattern(
          TripleLine.of(PLASTIC_ROD_CHAR, Pattern.CONSTANT, PLASTIC_ROD_CHAR),
          TripleLine.of(PLASTIC_ROD_CHAR, Pattern.CONSTANT, PLASTIC_ROD_CHAR));
    private static final RecipePattern REINFORCED_PLASTIC = RecipePattern.createPattern(
          TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
          TripleLine.of(Pattern.CONSTANT, Pattern.OSMIUM, Pattern.CONSTANT),
          TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY));
    private static final RecipePattern PLASTIC_ROAD = RecipePattern.createPattern(
          TripleLine.of(SAND_CHAR, SAND_CHAR, SAND_CHAR),
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
          TripleLine.of(SAND_CHAR, SAND_CHAR, SAND_CHAR));
    private static final RecipePattern PLASTIC_SLAB = RecipePattern.createPattern(TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT));
    private static final RecipePattern SLICK_PLASTIC = RecipePattern.createPattern(
          TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
          TripleLine.of(Pattern.CONSTANT, SLIME_CHAR, Pattern.CONSTANT),
          TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY));
    private static final RecipePattern PLASTIC_STAIRS = RecipePattern.createPattern(
          TripleLine.of(Pattern.CONSTANT, Pattern.EMPTY, Pattern.EMPTY),
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.EMPTY),
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT));

    public AdditionsRecipeProvider(DataGenerator gen) {
        super(gen, MekanismAdditions.MODID);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        ExtendedShapedRecipeBuilder.shapedRecipe(AdditionsItems.WALKIE_TALKIE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.EMPTY, Pattern.OSMIUM),
                    TripleLine.of(Pattern.STEEL, Pattern.CIRCUIT, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM))
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .build(consumer);
        ExtendedShapedRecipeBuilder.shapedRecipe(AdditionsBlocks.OBSIDIAN_TNT)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(OBSIDIAN_CHAR, OBSIDIAN_CHAR, OBSIDIAN_CHAR),
                    TripleLine.of(TNT_CHAR, TNT_CHAR, TNT_CHAR),
                    TripleLine.of(OBSIDIAN_CHAR, OBSIDIAN_CHAR, OBSIDIAN_CHAR))
              ).key(OBSIDIAN_CHAR, Tags.Items.OBSIDIAN)
              .key(TNT_CHAR, Items.TNT)
              .build(consumer);
        registerBalloons(consumer);
        registerGlowPanels(consumer);
        registerPlasticBlocks(consumer);
        registerPlasticFences(consumer);
        registerPlasticFenceGates(consumer);
        registerPlasticGlow(consumer);
        registerReinforcedPlastic(consumer);
        registerPlasticRoads(consumer);
        registerPlasticSlabs(consumer);
        registerSlickPlastic(consumer);
        registerPlasticStairs(consumer);
    }

    private void registerBalloons(Consumer<IFinishedRecipe> consumer) {
        String basePath = "balloon/";
        registerBalloon(consumer, AdditionsItems.BLACK_BALLOON, basePath);
        registerBalloon(consumer, AdditionsItems.RED_BALLOON, basePath);
        registerBalloon(consumer, AdditionsItems.GREEN_BALLOON, basePath);
        registerBalloon(consumer, AdditionsItems.BROWN_BALLOON, basePath);
        registerBalloon(consumer, AdditionsItems.BLUE_BALLOON, basePath);
        registerBalloon(consumer, AdditionsItems.PURPLE_BALLOON, basePath);
        registerBalloon(consumer, AdditionsItems.CYAN_BALLOON, basePath);
        registerBalloon(consumer, AdditionsItems.LIGHT_GRAY_BALLOON, basePath);
        registerBalloon(consumer, AdditionsItems.GRAY_BALLOON, basePath);
        registerBalloon(consumer, AdditionsItems.PINK_BALLOON, basePath);
        registerBalloon(consumer, AdditionsItems.LIME_BALLOON, basePath);
        registerBalloon(consumer, AdditionsItems.YELLOW_BALLOON, basePath);
        registerBalloon(consumer, AdditionsItems.LIGHT_BLUE_BALLOON, basePath);
        registerBalloon(consumer, AdditionsItems.MAGENTA_BALLOON, basePath);
        registerBalloon(consumer, AdditionsItems.ORANGE_BALLOON, basePath);
        registerBalloon(consumer, AdditionsItems.WHITE_BALLOON, basePath);
    }

    private void registerBalloon(Consumer<IFinishedRecipe> consumer, ItemRegistryObject<ItemBalloon> result, String basePath) {
        EnumColor color = result.getItem().getColor();
        String colorString = color.getRegistryPrefix();
        ITag<Item> dye = color.getDyeTag();
        ExtendedShapelessRecipeBuilder.shapelessRecipe(result, 2)
              .addIngredient(Tags.Items.LEATHER)
              .addIngredient(Tags.Items.STRING)
              .addIngredient(dye)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        ExtendedShapelessRecipeBuilder.shapelessRecipe(result)
              .addIngredient(AdditionsTags.Items.BALLOONS)
              .addIngredient(dye)
              .build(consumer, MekanismAdditions.rl(basePath + "recolor/" + colorString));
    }

    private void registerGlowPanels(Consumer<IFinishedRecipe> consumer) {
        String basePath = "glow_panel/";
        registerGlowPanel(consumer, AdditionsBlocks.BLACK_GLOW_PANEL, basePath);
        registerGlowPanel(consumer, AdditionsBlocks.RED_GLOW_PANEL, basePath);
        registerGlowPanel(consumer, AdditionsBlocks.GREEN_GLOW_PANEL, basePath);
        registerGlowPanel(consumer, AdditionsBlocks.BROWN_GLOW_PANEL, basePath);
        registerGlowPanel(consumer, AdditionsBlocks.BLUE_GLOW_PANEL, basePath);
        registerGlowPanel(consumer, AdditionsBlocks.PURPLE_GLOW_PANEL, basePath);
        registerGlowPanel(consumer, AdditionsBlocks.CYAN_GLOW_PANEL, basePath);
        registerGlowPanel(consumer, AdditionsBlocks.LIGHT_GRAY_GLOW_PANEL, basePath);
        registerGlowPanel(consumer, AdditionsBlocks.GRAY_GLOW_PANEL, basePath);
        registerGlowPanel(consumer, AdditionsBlocks.PINK_GLOW_PANEL, basePath);
        registerGlowPanel(consumer, AdditionsBlocks.LIME_GLOW_PANEL, basePath);
        registerGlowPanel(consumer, AdditionsBlocks.YELLOW_GLOW_PANEL, basePath);
        registerGlowPanel(consumer, AdditionsBlocks.LIGHT_BLUE_GLOW_PANEL, basePath);
        registerGlowPanel(consumer, AdditionsBlocks.MAGENTA_GLOW_PANEL, basePath);
        registerGlowPanel(consumer, AdditionsBlocks.ORANGE_GLOW_PANEL, basePath);
        registerGlowPanel(consumer, AdditionsBlocks.WHITE_GLOW_PANEL, basePath);
    }

    private void registerGlowPanel(Consumer<IFinishedRecipe> consumer, BlockRegistryObject<? extends IColoredBlock, ?> result, String basePath) {
        EnumColor color = result.getBlock().getColor();
        String colorString = color.getRegistryPrefix();
        ITag<Item> dye = color.getDyeTag();
        ExtendedShapedRecipeBuilder.shapedRecipe(result, 2)
              .pattern(GLOW_PANEL)
              .key(PLASTIC_SHEET_CHAR, MekanismItems.HDPE_SHEET)
              .key(GLASS_PANES_CHAR, Tags.Items.GLASS_PANES)
              .key(Pattern.GLOWSTONE, Tags.Items.DUSTS_GLOWSTONE)
              .key(Pattern.DYE, dye)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        registerRecolor(consumer, result, AdditionsTags.Items.GLOW_PANELS, dye, basePath + "recolor/" + colorString);
    }

    private void registerPlasticBlocks(Consumer<IFinishedRecipe> consumer) {
        String basePath = "plastic/block/";
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
              .key(PLASTIC_SHEET_CHAR, MekanismItems.HDPE_SHEET)
              .key(Pattern.DYE, dye)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        registerRecolor(consumer, result, AdditionsTags.Items.PLASTIC_BLOCKS_PLASTIC, dye, basePath + "recolor/" + colorString);
    }

    private void registerPlasticFences(Consumer<IFinishedRecipe> consumer) {
        String basePath = "plastic/fence/";
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
              .key(PLASTIC_ROD_CHAR, MekanismTags.Items.RODS_PLASTIC)
              .key(Pattern.CONSTANT, plastic)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        registerRecolor(consumer, result, AdditionsTags.Items.FENCES_PLASTIC, color.getDyeTag(), basePath + "recolor/" + colorString);
    }

    private void registerPlasticFenceGates(Consumer<IFinishedRecipe> consumer) {
        String basePath = "plastic/fence_gate/";
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
              .key(PLASTIC_ROD_CHAR, MekanismTags.Items.RODS_PLASTIC)
              .key(Pattern.CONSTANT, plastic)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        registerRecolor(consumer, result, AdditionsTags.Items.FENCE_GATES_PLASTIC, color.getDyeTag(), basePath + "recolor/" + colorString);
    }

    private void registerPlasticGlow(Consumer<IFinishedRecipe> consumer) {
        String basePath = "plastic/glow/";
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
        registerRecolor(consumer, result, AdditionsTags.Items.PLASTIC_BLOCKS_GLOW, color.getDyeTag(), basePath + "recolor/" + colorString);
    }

    private void registerReinforcedPlastic(Consumer<IFinishedRecipe> consumer) {
        String basePath = "plastic/reinforced/";
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
        registerRecolor(consumer, result, AdditionsTags.Items.PLASTIC_BLOCKS_REINFORCED, color.getDyeTag(), basePath + "recolor/" + colorString);
    }

    private void registerPlasticRoads(Consumer<IFinishedRecipe> consumer) {
        String basePath = "plastic/road/";
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
              .key(SAND_CHAR, Tags.Items.SAND)
              .key(Pattern.CONSTANT, slickPlastic)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        registerRecolor(consumer, result, AdditionsTags.Items.PLASTIC_BLOCKS_ROAD, color.getDyeTag(), basePath + "recolor/" + colorString);
    }

    private void registerPlasticSlabs(Consumer<IFinishedRecipe> consumer) {
        String basePath = "plastic/slab/";
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
        registerRecolor(consumer, result, AdditionsTags.Items.SLABS_PLASTIC, color.getDyeTag(), basePath + "recolor/" + colorString);
    }

    private void registerSlickPlastic(Consumer<IFinishedRecipe> consumer) {
        String basePath = "plastic/slick/";
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
              .key(SLIME_CHAR, Tags.Items.SLIMEBALLS)
              .build(consumer, MekanismAdditions.rl(basePath + colorString));
        //Enriching recipes
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(plastic),
              result.getItemStack()
        ).build(consumer, MekanismAdditions.rl(basePath + "enriching/" + colorString));
        //Recolor recipes
        registerRecolor(consumer, result, AdditionsTags.Items.PLASTIC_BLOCKS_SLICK, color.getDyeTag(), basePath + "recolor/" + colorString);
    }

    private void registerPlasticStairs(Consumer<IFinishedRecipe> consumer) {
        String basePath = "plastic/stairs/";
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
        registerRecolor(consumer, result, AdditionsTags.Items.STAIRS_PLASTIC, color.getDyeTag(), basePath + "recolor/" + colorString);
    }

    private void registerRecolor(Consumer<IFinishedRecipe> consumer, IItemProvider result, ITag<Item> blockType, ITag<Item> dye, String path) {
        ExtendedShapedRecipeBuilder.shapedRecipe(result, 4)
              .pattern(BLOCK_RECOLOR)
              .key(Pattern.CONSTANT, blockType)
              .key(Pattern.DYE, dye)
              .build(consumer, MekanismAdditions.rl(path));
    }
}