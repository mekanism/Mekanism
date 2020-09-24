package mekanism.additions.common.recipe;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.additions.common.AdditionsTags;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.item.ItemBalloon;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.ISubRecipeProvider;
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
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.Tags;

@ParametersAreNonnullByDefault
public class AdditionsRecipeProvider extends BaseRecipeProvider {

    static final char TNT_CHAR = 'T';
    static final char OBSIDIAN_CHAR = 'O';
    static final char GLASS_PANES_CHAR = 'P';
    static final char PLASTIC_SHEET_CHAR = 'H';
    static final char PLASTIC_ROD_CHAR = 'R';
    static final char SAND_CHAR = 'S';
    static final char SLIME_CHAR = 'S';


    private static final RecipePattern GLOW_PANEL = RecipePattern.createPattern(
          TripleLine.of(GLASS_PANES_CHAR, PLASTIC_SHEET_CHAR, GLASS_PANES_CHAR),
          TripleLine.of(PLASTIC_SHEET_CHAR, Pattern.DYE, PLASTIC_SHEET_CHAR),
          TripleLine.of(Pattern.GLOWSTONE, PLASTIC_SHEET_CHAR, Pattern.GLOWSTONE));

    public AdditionsRecipeProvider(DataGenerator gen) {
        super(gen, MekanismAdditions.MODID);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        super.registerRecipes(consumer);
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
              .key(TNT_CHAR, Blocks.TNT)
              .build(consumer);
        registerBalloons(consumer);
        registerGlowPanels(consumer);
    }

    @Override
    protected List<ISubRecipeProvider> getSubRecipeProviders() {
        return Arrays.asList(
              new PlasticBlockRecipeProvider(),
              new PlasticFencesRecipeProvider(),
              new PlasticSlabsRecipeProvider(),
              new PlasticStairsRecipeProvider()
        );
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
        PlasticBlockRecipeProvider.registerRecolor(consumer, result, AdditionsTags.Items.GLOW_PANELS, dye, basePath, colorString);
    }
}