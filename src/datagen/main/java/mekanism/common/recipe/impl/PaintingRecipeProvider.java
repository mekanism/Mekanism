package mekanism.common.recipe.impl;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
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
    private static final Map<EnumColor, ItemLike> CANDLES = new EnumMap<>(EnumColor.class);
    private static final Map<EnumColor, ItemLike> CONCRETE = new EnumMap<>(EnumColor.class);
    private static final Map<EnumColor, ItemLike> CONCRETE_POWDER = new EnumMap<>(EnumColor.class);
    private static final Map<EnumColor, ItemLike> CARPETS = new EnumMap<>(EnumColor.class);
    private static final Map<EnumColor, ItemLike> STAINED_GLASS = new EnumMap<>(EnumColor.class);
    private static final Map<EnumColor, ItemLike> STAINED_GLASS_PANES = new EnumMap<>(EnumColor.class);
    private static final Map<EnumColor, ItemLike> TERRACOTTA = new EnumMap<>(EnumColor.class);
    private static final Map<EnumColor, ItemLike> WOOL = new EnumMap<>(EnumColor.class);

    static {
        addTypes(EnumColor.WHITE, Blocks.WHITE_BED, Blocks.WHITE_CANDLE, Blocks.WHITE_CONCRETE, Blocks.WHITE_CONCRETE_POWDER, Blocks.WHITE_CARPET, Blocks.WHITE_STAINED_GLASS,
              Blocks.WHITE_STAINED_GLASS_PANE, Blocks.WHITE_TERRACOTTA, Blocks.WHITE_WOOL);
        addTypes(EnumColor.ORANGE, Blocks.ORANGE_BED, Blocks.ORANGE_CANDLE, Blocks.ORANGE_CONCRETE, Blocks.ORANGE_CONCRETE_POWDER, Blocks.ORANGE_CARPET, Blocks.ORANGE_STAINED_GLASS,
              Blocks.ORANGE_STAINED_GLASS_PANE, Blocks.ORANGE_TERRACOTTA, Blocks.ORANGE_WOOL);
        addTypes(EnumColor.PINK, Blocks.MAGENTA_BED, Blocks.MAGENTA_CANDLE, Blocks.MAGENTA_CONCRETE, Blocks.MAGENTA_CONCRETE_POWDER, Blocks.MAGENTA_CARPET, Blocks.MAGENTA_STAINED_GLASS,
              Blocks.MAGENTA_STAINED_GLASS_PANE, Blocks.MAGENTA_TERRACOTTA, Blocks.MAGENTA_WOOL);
        addTypes(EnumColor.INDIGO, Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_BLUE_CANDLE, Blocks.LIGHT_BLUE_CONCRETE, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CARPET,
              Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.LIGHT_BLUE_WOOL);
        addTypes(EnumColor.YELLOW, Blocks.YELLOW_BED, Blocks.YELLOW_CANDLE, Blocks.YELLOW_CONCRETE, Blocks.YELLOW_CONCRETE_POWDER, Blocks.YELLOW_CARPET, Blocks.YELLOW_STAINED_GLASS,
              Blocks.YELLOW_STAINED_GLASS_PANE, Blocks.YELLOW_TERRACOTTA, Blocks.YELLOW_WOOL);
        addTypes(EnumColor.BRIGHT_GREEN, Blocks.LIME_BED, Blocks.LIME_CANDLE, Blocks.LIME_CONCRETE, Blocks.LIME_CONCRETE_POWDER, Blocks.LIME_CARPET, Blocks.LIME_STAINED_GLASS,
              Blocks.LIME_STAINED_GLASS_PANE, Blocks.LIME_TERRACOTTA, Blocks.LIME_WOOL);
        addTypes(EnumColor.BRIGHT_PINK, Blocks.PINK_BED, Blocks.PINK_CANDLE, Blocks.PINK_CONCRETE, Blocks.PINK_CONCRETE_POWDER, Blocks.PINK_CARPET, Blocks.PINK_STAINED_GLASS,
              Blocks.PINK_STAINED_GLASS_PANE, Blocks.PINK_TERRACOTTA, Blocks.PINK_WOOL);
        addTypes(EnumColor.DARK_GRAY, Blocks.GRAY_BED, Blocks.GRAY_CANDLE, Blocks.GRAY_CONCRETE, Blocks.GRAY_CONCRETE_POWDER, Blocks.GRAY_CARPET, Blocks.GRAY_STAINED_GLASS,
              Blocks.GRAY_STAINED_GLASS_PANE, Blocks.GRAY_TERRACOTTA, Blocks.GRAY_WOOL);
        addTypes(EnumColor.GRAY, Blocks.LIGHT_GRAY_BED, Blocks.LIGHT_GRAY_CANDLE, Blocks.LIGHT_GRAY_CONCRETE, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CARPET,
              Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_WOOL);
        addTypes(EnumColor.DARK_AQUA, Blocks.CYAN_BED, Blocks.CYAN_CANDLE, Blocks.CYAN_CONCRETE, Blocks.CYAN_CONCRETE_POWDER, Blocks.CYAN_CARPET, Blocks.CYAN_STAINED_GLASS,
              Blocks.CYAN_STAINED_GLASS_PANE, Blocks.CYAN_TERRACOTTA, Blocks.CYAN_WOOL);
        addTypes(EnumColor.PURPLE, Blocks.PURPLE_BED, Blocks.PURPLE_CANDLE, Blocks.PURPLE_CONCRETE, Blocks.PURPLE_CONCRETE_POWDER, Blocks.PURPLE_CARPET, Blocks.PURPLE_STAINED_GLASS,
              Blocks.PURPLE_STAINED_GLASS_PANE, Blocks.PURPLE_TERRACOTTA, Blocks.PURPLE_WOOL);
        addTypes(EnumColor.DARK_BLUE, Blocks.BLUE_BED, Blocks.BLUE_CANDLE, Blocks.BLUE_CONCRETE, Blocks.BLUE_CONCRETE_POWDER, Blocks.BLUE_CARPET, Blocks.BLUE_STAINED_GLASS,
              Blocks.BLUE_STAINED_GLASS_PANE, Blocks.BLUE_TERRACOTTA, Blocks.BLUE_WOOL);
        addTypes(EnumColor.BROWN, Blocks.BROWN_BED, Blocks.BROWN_CANDLE, Blocks.BROWN_CONCRETE, Blocks.BROWN_CONCRETE_POWDER, Blocks.BROWN_CARPET, Blocks.BROWN_STAINED_GLASS,
              Blocks.BROWN_STAINED_GLASS_PANE, Blocks.BROWN_TERRACOTTA, Blocks.BROWN_WOOL);
        addTypes(EnumColor.DARK_GREEN, Blocks.GREEN_BED, Blocks.GREEN_CANDLE, Blocks.GREEN_CONCRETE, Blocks.GREEN_CONCRETE_POWDER, Blocks.GREEN_CARPET, Blocks.GREEN_STAINED_GLASS,
              Blocks.GREEN_STAINED_GLASS_PANE, Blocks.GREEN_TERRACOTTA, Blocks.GREEN_WOOL);
        addTypes(EnumColor.RED, Blocks.RED_BED, Blocks.RED_CANDLE, Blocks.RED_CONCRETE, Blocks.RED_CONCRETE_POWDER, Blocks.RED_CARPET, Blocks.RED_STAINED_GLASS, Blocks.RED_STAINED_GLASS_PANE,
              Blocks.RED_TERRACOTTA, Blocks.RED_WOOL);
        addTypes(EnumColor.BLACK, Blocks.BLACK_BED, Blocks.BLACK_CANDLE, Blocks.BLACK_CONCRETE, Blocks.BLACK_CONCRETE_POWDER, Blocks.BLACK_CARPET, Blocks.BLACK_STAINED_GLASS,
              Blocks.BLACK_STAINED_GLASS_PANE, Blocks.BLACK_TERRACOTTA, Blocks.BLACK_WOOL);
    }

    private static void addTypes(EnumColor color, ItemLike bed, ItemLike candle, ItemLike concrete, ItemLike concretePowder, ItemLike carpet, ItemLike stainedGlass,
          ItemLike stainedGlassPane, ItemLike terracotta, ItemLike wool) {
        BEDS.put(color, bed);
        CANDLES.put(color, candle);
        CONCRETE.put(color, concrete);
        CONCRETE_POWDER.put(color, concretePowder);
        CARPETS.put(color, carpet);
        STAINED_GLASS.put(color, stainedGlass);
        STAINED_GLASS_PANES.put(color, stainedGlassPane);
        TERRACOTTA.put(color, terracotta);
        WOOL.put(color, wool);
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "painting/";
        addDyeRecipes(consumer, basePath);
        long oneAtATime = PigmentExtractingRecipeProvider.DYE_RATE;
        long eightAtATime = oneAtATime / 8;
        //Some base input tags are effectively duplicates of vanilla, but are done to make sure we don't change
        // things that make no sense to be colored, such as some sort of fancy carpets, or a unique type of glass that
        // is tagged as glass, but shouldn't be able to be converted directly into stained-glass
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_WOOL, oneAtATime, WOOL, basePath + "wool/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_CARPETS, eightAtATime, CARPETS, basePath + "carpet/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_BEDS, oneAtATime, BEDS, basePath + "bed/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_CANDLE, oneAtATime, CANDLES, basePath + "candle/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_GLASS, eightAtATime, STAINED_GLASS, basePath + "glass/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_GLASS_PANES, eightAtATime, STAINED_GLASS_PANES, basePath + "glass_pane/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_TERRACOTTA, eightAtATime, TERRACOTTA, basePath + "terracotta/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_CONCRETE, eightAtATime, CONCRETE, basePath + "concrete/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_CONCRETE_POWDER, eightAtATime, CONCRETE_POWDER,
              basePath + "concrete_powder/");
        //TODO: Eventually we may want to consider taking patterns into account
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_BANNERS, oneAtATime, BannerBlock::byColor, basePath + "banner/");
    }

    private static void addDyeRecipes(RecipeOutput consumer, String basePath) {
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

    private static void addDyeRecipe(RecipeOutput consumer, EnumColor color, ItemLike dye, String basePath) {
        ItemStackChemicalToItemStackRecipeBuilder.painting(
              IngredientCreatorAccess.item().from(MekanismItems.DYE_BASE),
              IngredientCreatorAccess.chemicalStack().from(MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(color), PigmentExtractingRecipeProvider.DYE_RATE),
              new ItemStack(dye)
        ).build(consumer, Mekanism.rl(basePath + color.getRegistryPrefix()));
    }

    private static void addRecoloringRecipes(RecipeOutput consumer, TagKey<Item> input, long rate, Function<DyeColor, ItemLike> output, String basePath) {
        for (EnumColor color : EnumUtils.COLORS) {
            DyeColor dye = color.getDyeColor();
            if (dye != null) {
                addRecoloringRecipe(consumer, color, input, output.apply(dye), rate, basePath);
            }
        }
    }

    private static void addRecoloringRecipes(RecipeOutput consumer, TagKey<Item> input, long rate, Map<EnumColor, ItemLike> outputs, String basePath) {
        for (Map.Entry<EnumColor, ItemLike> entry : outputs.entrySet()) {
            addRecoloringRecipe(consumer, entry.getKey(), input, entry.getValue(), rate, basePath);
        }
    }

    private static void addRecoloringRecipe(RecipeOutput consumer, EnumColor color, TagKey<Item> input, ItemLike result, long rate, String basePath) {
        ItemStackChemicalToItemStackRecipeBuilder.painting(
              IngredientCreatorAccess.item().from(BaseRecipeProvider.difference(input, result)),
              IngredientCreatorAccess.chemicalStack().from(MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(color), rate),
              new ItemStack(result)
        ).build(consumer, Mekanism.rl(basePath + color.getRegistryPrefix()));
    }
}