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
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BannerBlock;

class PaintingRecipeProvider implements ISubRecipeProvider {

    private static final Map<EnumColor, Item> BEDS = new EnumMap<>(EnumColor.class);
    private static final Map<EnumColor, Item> CANDLES = new EnumMap<>(EnumColor.class);
    private static final Map<EnumColor, Item> CONCRETE = new EnumMap<>(EnumColor.class);
    private static final Map<EnumColor, Item> CONCRETE_POWDER = new EnumMap<>(EnumColor.class);
    private static final Map<EnumColor, Item> CARPETS = new EnumMap<>(EnumColor.class);
    private static final Map<EnumColor, Item> STAINED_GLASS = new EnumMap<>(EnumColor.class);
    private static final Map<EnumColor, Item> STAINED_GLASS_PANES = new EnumMap<>(EnumColor.class);
    private static final Map<EnumColor, Item> TERRACOTTA = new EnumMap<>(EnumColor.class);
    private static final Map<EnumColor, Item> WOOL = new EnumMap<>(EnumColor.class);

    static {
        addTypes(EnumColor.WHITE, Items.WHITE_BED, Items.WHITE_CANDLE, Items.WHITE_CONCRETE, Items.WHITE_CONCRETE_POWDER, Items.WHITE_CARPET, Items.WHITE_STAINED_GLASS,
              Items.WHITE_STAINED_GLASS_PANE, Items.WHITE_TERRACOTTA, Items.WHITE_WOOL);
        addTypes(EnumColor.ORANGE, Items.ORANGE_BED, Items.ORANGE_CANDLE, Items.ORANGE_CONCRETE, Items.ORANGE_CONCRETE_POWDER, Items.ORANGE_CARPET, Items.ORANGE_STAINED_GLASS,
              Items.ORANGE_STAINED_GLASS_PANE, Items.ORANGE_TERRACOTTA, Items.ORANGE_WOOL);
        addTypes(EnumColor.PINK, Items.MAGENTA_BED, Items.MAGENTA_CANDLE, Items.MAGENTA_CONCRETE, Items.MAGENTA_CONCRETE_POWDER, Items.MAGENTA_CARPET, Items.MAGENTA_STAINED_GLASS,
              Items.MAGENTA_STAINED_GLASS_PANE, Items.MAGENTA_TERRACOTTA, Items.MAGENTA_WOOL);
        addTypes(EnumColor.INDIGO, Items.LIGHT_BLUE_BED, Items.LIGHT_BLUE_CANDLE, Items.LIGHT_BLUE_CONCRETE, Items.LIGHT_BLUE_CONCRETE_POWDER, Items.LIGHT_BLUE_CARPET,
              Items.LIGHT_BLUE_STAINED_GLASS, Items.LIGHT_BLUE_STAINED_GLASS_PANE, Items.LIGHT_BLUE_TERRACOTTA, Items.LIGHT_BLUE_WOOL);
        addTypes(EnumColor.YELLOW, Items.YELLOW_BED, Items.YELLOW_CANDLE, Items.YELLOW_CONCRETE, Items.YELLOW_CONCRETE_POWDER, Items.YELLOW_CARPET, Items.YELLOW_STAINED_GLASS,
              Items.YELLOW_STAINED_GLASS_PANE, Items.YELLOW_TERRACOTTA, Items.YELLOW_WOOL);
        addTypes(EnumColor.BRIGHT_GREEN, Items.LIME_BED, Items.LIME_CANDLE, Items.LIME_CONCRETE, Items.LIME_CONCRETE_POWDER, Items.LIME_CARPET, Items.LIME_STAINED_GLASS,
              Items.LIME_STAINED_GLASS_PANE, Items.LIME_TERRACOTTA, Items.LIME_WOOL);
        addTypes(EnumColor.BRIGHT_PINK, Items.PINK_BED, Items.PINK_CANDLE, Items.PINK_CONCRETE, Items.PINK_CONCRETE_POWDER, Items.PINK_CARPET, Items.PINK_STAINED_GLASS,
              Items.PINK_STAINED_GLASS_PANE, Items.PINK_TERRACOTTA, Items.PINK_WOOL);
        addTypes(EnumColor.DARK_GRAY, Items.GRAY_BED, Items.GRAY_CANDLE, Items.GRAY_CONCRETE, Items.GRAY_CONCRETE_POWDER, Items.GRAY_CARPET, Items.GRAY_STAINED_GLASS,
              Items.GRAY_STAINED_GLASS_PANE, Items.GRAY_TERRACOTTA, Items.GRAY_WOOL);
        addTypes(EnumColor.GRAY, Items.LIGHT_GRAY_BED, Items.LIGHT_GRAY_CANDLE, Items.LIGHT_GRAY_CONCRETE, Items.LIGHT_GRAY_CONCRETE_POWDER, Items.LIGHT_GRAY_CARPET,
              Items.LIGHT_GRAY_STAINED_GLASS, Items.LIGHT_GRAY_STAINED_GLASS_PANE, Items.LIGHT_GRAY_TERRACOTTA, Items.LIGHT_GRAY_WOOL);
        addTypes(EnumColor.DARK_AQUA, Items.CYAN_BED, Items.CYAN_CANDLE, Items.CYAN_CONCRETE, Items.CYAN_CONCRETE_POWDER, Items.CYAN_CARPET, Items.CYAN_STAINED_GLASS,
              Items.CYAN_STAINED_GLASS_PANE, Items.CYAN_TERRACOTTA, Items.CYAN_WOOL);
        addTypes(EnumColor.PURPLE, Items.PURPLE_BED, Items.PURPLE_CANDLE, Items.PURPLE_CONCRETE, Items.PURPLE_CONCRETE_POWDER, Items.PURPLE_CARPET, Items.PURPLE_STAINED_GLASS,
              Items.PURPLE_STAINED_GLASS_PANE, Items.PURPLE_TERRACOTTA, Items.PURPLE_WOOL);
        addTypes(EnumColor.DARK_BLUE, Items.BLUE_BED, Items.BLUE_CANDLE, Items.BLUE_CONCRETE, Items.BLUE_CONCRETE_POWDER, Items.BLUE_CARPET, Items.BLUE_STAINED_GLASS,
              Items.BLUE_STAINED_GLASS_PANE, Items.BLUE_TERRACOTTA, Items.BLUE_WOOL);
        addTypes(EnumColor.BROWN, Items.BROWN_BED, Items.BROWN_CANDLE, Items.BROWN_CONCRETE, Items.BROWN_CONCRETE_POWDER, Items.BROWN_CARPET, Items.BROWN_STAINED_GLASS,
              Items.BROWN_STAINED_GLASS_PANE, Items.BROWN_TERRACOTTA, Items.BROWN_WOOL);
        addTypes(EnumColor.DARK_GREEN, Items.GREEN_BED, Items.GREEN_CANDLE, Items.GREEN_CONCRETE, Items.GREEN_CONCRETE_POWDER, Items.GREEN_CARPET, Items.GREEN_STAINED_GLASS,
              Items.GREEN_STAINED_GLASS_PANE, Items.GREEN_TERRACOTTA, Items.GREEN_WOOL);
        addTypes(EnumColor.RED, Items.RED_BED, Items.RED_CANDLE, Items.RED_CONCRETE, Items.RED_CONCRETE_POWDER, Items.RED_CARPET, Items.RED_STAINED_GLASS, Items.RED_STAINED_GLASS_PANE,
              Items.RED_TERRACOTTA, Items.RED_WOOL);
        addTypes(EnumColor.BLACK, Items.BLACK_BED, Items.BLACK_CANDLE, Items.BLACK_CONCRETE, Items.BLACK_CONCRETE_POWDER, Items.BLACK_CARPET, Items.BLACK_STAINED_GLASS,
              Items.BLACK_STAINED_GLASS_PANE, Items.BLACK_TERRACOTTA, Items.BLACK_WOOL);
    }

    private static void addTypes(EnumColor color, Item bed, Item candle, Item concrete, Item concretePowder, Item carpet, Item stainedGlass, Item stainedGlassPane,
          Item terracotta, Item wool) {
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

    private static void addDyeRecipe(RecipeOutput consumer, EnumColor color, Item dye, String basePath) {
        ItemStackChemicalToItemStackRecipeBuilder.painting(
              IngredientCreatorAccess.item().from(MekanismItems.DYE_BASE),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(color), PigmentExtractingRecipeProvider.DYE_RATE),
              new ItemStack(dye),
              false
        ).build(consumer, Mekanism.rl(basePath + color.getRegistryPrefix()));
    }

    private static void addRecoloringRecipes(RecipeOutput consumer, TagKey<Item> input, long rate, Function<DyeColor, ItemLike> output, String basePath) {
        for (EnumColor color : EnumUtils.COLORS) {
            DyeColor dye = color.getDyeColor();
            if (dye != null) {
                addRecoloringRecipe(consumer, color, input, output.apply(dye).asItem().builtInRegistryHolder(), rate, basePath);
            }
        }
    }

    private static void addRecoloringRecipes(RecipeOutput consumer, TagKey<Item> input, long rate, Map<EnumColor, Item> outputs, String basePath) {
        for (Map.Entry<EnumColor, Item> entry : outputs.entrySet()) {
            addRecoloringRecipe(consumer, entry.getKey(), input, entry.getValue().builtInRegistryHolder(), rate, basePath);
        }
    }

    private static void addRecoloringRecipe(RecipeOutput consumer, EnumColor color, TagKey<Item> input, Holder<Item> result, long rate, String basePath) {
        ItemStackChemicalToItemStackRecipeBuilder.painting(
              IngredientCreatorAccess.item().from(BaseRecipeProvider.difference(input, result)),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(color), rate),
              new ItemStack(result),
              false
        ).build(consumer, Mekanism.rl(basePath + color.getRegistryPrefix()));
    }
}