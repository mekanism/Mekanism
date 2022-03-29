package mekanism.common.recipe.impl;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismPigments;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.EnumUtils;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Blocks;

class PaintingRecipeProvider implements ISubRecipeProvider {

    private static final Map<EnumColor, ItemLike> BEDS = new EnumMap<>(EnumColor.class);
    private static final Map<EnumColor, ItemLike> STAINED_GLASS = new EnumMap<>(EnumColor.class);
    private static final Map<EnumColor, ItemLike> STAINED_GLASS_PANES = new EnumMap<>(EnumColor.class);

    static {
        addTypes(EnumColor.WHITE, Blocks.WHITE_BED, Blocks.WHITE_STAINED_GLASS, Blocks.WHITE_STAINED_GLASS_PANE);
        addTypes(EnumColor.ORANGE, Blocks.ORANGE_BED, Blocks.ORANGE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS_PANE);
        addTypes(EnumColor.PINK, Blocks.MAGENTA_BED, Blocks.MAGENTA_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS_PANE);
        addTypes(EnumColor.INDIGO, Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE);
        addTypes(EnumColor.YELLOW, Blocks.YELLOW_BED, Blocks.YELLOW_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS_PANE);
        addTypes(EnumColor.BRIGHT_GREEN, Blocks.LIME_BED, Blocks.LIME_STAINED_GLASS, Blocks.LIME_STAINED_GLASS_PANE);
        addTypes(EnumColor.BRIGHT_PINK, Blocks.PINK_BED, Blocks.PINK_STAINED_GLASS, Blocks.PINK_STAINED_GLASS_PANE);
        addTypes(EnumColor.DARK_GRAY, Blocks.GRAY_BED, Blocks.GRAY_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS_PANE);
        addTypes(EnumColor.GRAY, Blocks.LIGHT_GRAY_BED, Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
        addTypes(EnumColor.DARK_AQUA, Blocks.CYAN_BED, Blocks.CYAN_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS_PANE);
        addTypes(EnumColor.PURPLE, Blocks.PURPLE_BED, Blocks.PURPLE_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS_PANE);
        addTypes(EnumColor.DARK_BLUE, Blocks.BLUE_BED, Blocks.BLUE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS_PANE);
        addTypes(EnumColor.BROWN, Blocks.BROWN_BED, Blocks.BROWN_STAINED_GLASS, Blocks.BROWN_STAINED_GLASS_PANE);
        addTypes(EnumColor.DARK_GREEN, Blocks.GREEN_BED, Blocks.GREEN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS_PANE);
        addTypes(EnumColor.RED, Blocks.RED_BED, Blocks.RED_STAINED_GLASS, Blocks.RED_STAINED_GLASS_PANE);
        addTypes(EnumColor.BLACK, Blocks.BLACK_BED, Blocks.BLACK_STAINED_GLASS, Blocks.BLACK_STAINED_GLASS_PANE);
    }

    private static void addTypes(EnumColor color, ItemLike bed, ItemLike stainedGlass, ItemLike stainedGlassPane) {
        BEDS.put(color, bed);
        STAINED_GLASS.put(color, stainedGlass);
        STAINED_GLASS_PANES.put(color, stainedGlassPane);
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "painting/";
        addDyeRecipes(consumer, basePath);
        long oneAtATime = PigmentExtractingRecipeProvider.DYE_RATE;
        long eightAtATime = oneAtATime / 8;
        //Some base input tags are effectively duplicates of vanilla, but are done to make sure we don't change
        // things that make no sense to be colored, such as some sort of fancy carpets, or a unique type of glass that
        // is tagged as glass, but shouldn't be able to be converted directly into stained-glass
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_WOOL, oneAtATime, PigmentExtractingRecipeProvider.WOOL, basePath + "wool/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_CARPETS, eightAtATime, PigmentExtractingRecipeProvider.CARPETS, basePath + "carpet/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_BEDS, oneAtATime, BEDS, basePath + "bed/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_CANDLE, oneAtATime, PigmentExtractingRecipeProvider.CANDLES, basePath + "candle/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_GLASS, eightAtATime, STAINED_GLASS, basePath + "glass/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_GLASS_PANES, eightAtATime, STAINED_GLASS_PANES, basePath + "glass_pane/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_TERRACOTTA, eightAtATime, PigmentExtractingRecipeProvider.TERRACOTTA, basePath + "terracotta/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_CONCRETE, eightAtATime, PigmentExtractingRecipeProvider.CONCRETE, basePath + "concrete/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_CONCRETE_POWDER, eightAtATime, PigmentExtractingRecipeProvider.CONCRETE_POWDER,
              basePath + "concrete_powder/");
        //TODO: Eventually we may want to consider taking patterns into account
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_BANNERS, oneAtATime, BannerBlock::byColor, basePath + "banner/");
    }

    private static void addDyeRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        basePath += "dye/";
        addDyeRecipe(consumer, EnumColor.WHITE, Items.WHITE_DYE, basePath);
        addDyeRecipe(consumer, EnumColor.ORANGE, Items.ORANGE_DYE, basePath);
        addDyeRecipe(consumer, EnumColor.PINK, Items.MAGENTA_DYE, basePath);
        addDyeRecipe(consumer, EnumColor.INDIGO, Items.LIGHT_BLUE_DYE, basePath);
        addDyeRecipe(consumer, EnumColor.YELLOW, Items.YELLOW_DYE, basePath);
        addDyeRecipe(consumer, EnumColor.BRIGHT_GREEN, Items.LIME_DYE, basePath);
        addDyeRecipe(consumer, EnumColor.BRIGHT_PINK, Items.PINK_DYE, basePath);
        addDyeRecipe(consumer, EnumColor.DARK_GRAY, Items.GRAY_DYE, basePath);
        addDyeRecipe(consumer, EnumColor.GRAY, Items.LIGHT_GRAY_DYE, basePath);
        addDyeRecipe(consumer, EnumColor.DARK_AQUA, Items.CYAN_DYE, basePath);
        addDyeRecipe(consumer, EnumColor.PURPLE, Items.PURPLE_DYE, basePath);
        addDyeRecipe(consumer, EnumColor.DARK_BLUE, Items.BLUE_DYE, basePath);
        addDyeRecipe(consumer, EnumColor.BROWN, Items.BROWN_DYE, basePath);
        addDyeRecipe(consumer, EnumColor.DARK_GREEN, Items.GREEN_DYE, basePath);
        addDyeRecipe(consumer, EnumColor.RED, Items.RED_DYE, basePath);
        addDyeRecipe(consumer, EnumColor.BLACK, Items.BLACK_DYE, basePath);
    }

    private static void addDyeRecipe(Consumer<FinishedRecipe> consumer, EnumColor color, ItemLike dye, String basePath) {
        ItemStackChemicalToItemStackRecipeBuilder.painting(
              IngredientCreatorAccess.item().from(MekanismItems.DYE_BASE),
              IngredientCreatorAccess.pigment().from(MekanismPigments.PIGMENT_COLOR_LOOKUP.get(color), PigmentExtractingRecipeProvider.DYE_RATE),
              new ItemStack(dye)
        ).build(consumer, Mekanism.rl(basePath + color.getRegistryPrefix()));
    }

    private static void addRecoloringRecipes(Consumer<FinishedRecipe> consumer, TagKey<Item> input, long rate, Function<DyeColor, ItemLike> output, String basePath) {
        for (EnumColor color : EnumUtils.COLORS) {
            DyeColor dye = color.getDyeColor();
            if (dye != null) {
                addRecoloringRecipe(consumer, color, input, output.apply(dye), rate, basePath);
            }
        }
    }

    private static void addRecoloringRecipes(Consumer<FinishedRecipe> consumer, TagKey<Item> input, long rate, Map<EnumColor, ItemLike> outputs, String basePath) {
        for (Map.Entry<EnumColor, ItemLike> entry : outputs.entrySet()) {
            addRecoloringRecipe(consumer, entry.getKey(), input, entry.getValue(), rate, basePath);
        }
    }

    private static void addRecoloringRecipe(Consumer<FinishedRecipe> consumer, EnumColor color, TagKey<Item> input, ItemLike result, long rate, String basePath) {
        ItemStackChemicalToItemStackRecipeBuilder.painting(
              IngredientCreatorAccess.item().from(BaseRecipeProvider.difference(input, result)),
              IngredientCreatorAccess.pigment().from(MekanismPigments.PIGMENT_COLOR_LOOKUP.get(color), rate),
              new ItemStack(result)
        ).build(consumer, Mekanism.rl(basePath + color.getRegistryPrefix()));
    }
}