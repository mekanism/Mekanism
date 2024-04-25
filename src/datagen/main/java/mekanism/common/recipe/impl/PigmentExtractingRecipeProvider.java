package mekanism.common.recipe.impl;

import java.util.EnumMap;
import java.util.Map;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.providers.IPigmentProvider;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismPigments;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Blocks;

public class PigmentExtractingRecipeProvider implements ISubRecipeProvider {

    public static final long DYE_RATE = 256;
    private static final long BANNER_RATE = DYE_RATE / 4;//64
    private static final long CONCRETE_POWDER_RATE = DYE_RATE / 8;//32
    private static final long CANDLE_RATE = DYE_RATE * 7 / 8;//224
    //Concrete shares a rate with terracotta
    private static final long CONCRETE_RATE = CONCRETE_POWDER_RATE * 3 / 4;//24
    private static final long STAINED_GLASS_RATE = DYE_RATE / 16;//16
    private static final long STAINED_GLASS_PANE_RATE = STAINED_GLASS_RATE * 3 / 8;//6
    private static final long WOOL_RATE = DYE_RATE * 3 / 4;//192
    private static final long CARPET_RATE = WOOL_RATE * 2 / 3;//128

    static final Map<EnumColor, ItemLike> CANDLES = new EnumMap<>(EnumColor.class);
    static final Map<EnumColor, ItemLike> CONCRETE = new EnumMap<>(EnumColor.class);
    static final Map<EnumColor, ItemLike> CONCRETE_POWDER = new EnumMap<>(EnumColor.class);
    static final Map<EnumColor, ItemLike> CARPETS = new EnumMap<>(EnumColor.class);
    static final Map<EnumColor, ItemLike> TERRACOTTA = new EnumMap<>(EnumColor.class);
    private static final Map<EnumColor, ItemLike> STAINED_GLASS = new EnumMap<>(EnumColor.class);
    private static final Map<EnumColor, ItemLike> STAINED_GLASS_PANES = new EnumMap<>(EnumColor.class);
    static final Map<EnumColor, ItemLike> WOOL = new EnumMap<>(EnumColor.class);

    static {
        addTypes(EnumColor.WHITE, Blocks.WHITE_CANDLE, Blocks.WHITE_CONCRETE, Blocks.WHITE_CONCRETE_POWDER, Blocks.WHITE_CARPET, Blocks.WHITE_TERRACOTTA,
              Blocks.WHITE_STAINED_GLASS, Blocks.WHITE_STAINED_GLASS_PANE, Blocks.WHITE_WOOL);
        addTypes(EnumColor.ORANGE, Blocks.ORANGE_CANDLE, Blocks.ORANGE_CONCRETE, Blocks.ORANGE_CONCRETE_POWDER, Blocks.ORANGE_CARPET, Blocks.ORANGE_TERRACOTTA,
              Blocks.ORANGE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS_PANE, Blocks.ORANGE_WOOL);
        addTypes(EnumColor.PINK, Blocks.MAGENTA_CANDLE, Blocks.MAGENTA_CONCRETE, Blocks.MAGENTA_CONCRETE_POWDER, Blocks.MAGENTA_CARPET, Blocks.MAGENTA_TERRACOTTA,
              Blocks.MAGENTA_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS_PANE, Blocks.MAGENTA_WOOL);
        addTypes(EnumColor.INDIGO, Blocks.LIGHT_BLUE_CANDLE, Blocks.LIGHT_BLUE_CONCRETE, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CARPET,
              Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Blocks.LIGHT_BLUE_WOOL);
        addTypes(EnumColor.YELLOW, Blocks.YELLOW_CANDLE, Blocks.YELLOW_CONCRETE, Blocks.YELLOW_CONCRETE_POWDER, Blocks.YELLOW_CARPET, Blocks.YELLOW_TERRACOTTA,
              Blocks.YELLOW_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS_PANE, Blocks.YELLOW_WOOL);
        addTypes(EnumColor.BRIGHT_GREEN, Blocks.LIME_CANDLE, Blocks.LIME_CONCRETE, Blocks.LIME_CONCRETE_POWDER, Blocks.LIME_CARPET, Blocks.LIME_TERRACOTTA,
              Blocks.LIME_STAINED_GLASS, Blocks.LIME_STAINED_GLASS_PANE, Blocks.LIME_WOOL);
        addTypes(EnumColor.BRIGHT_PINK, Blocks.PINK_CANDLE, Blocks.PINK_CONCRETE, Blocks.PINK_CONCRETE_POWDER, Blocks.PINK_CARPET, Blocks.PINK_TERRACOTTA,
              Blocks.PINK_STAINED_GLASS, Blocks.PINK_STAINED_GLASS_PANE, Blocks.PINK_WOOL);
        addTypes(EnumColor.DARK_GRAY, Blocks.GRAY_CANDLE, Blocks.GRAY_CONCRETE, Blocks.GRAY_CONCRETE_POWDER, Blocks.GRAY_CARPET, Blocks.GRAY_TERRACOTTA,
              Blocks.GRAY_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS_PANE, Blocks.GRAY_WOOL);
        addTypes(EnumColor.GRAY, Blocks.LIGHT_GRAY_CANDLE, Blocks.LIGHT_GRAY_CONCRETE, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CARPET,
              Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Blocks.LIGHT_GRAY_WOOL);
        addTypes(EnumColor.DARK_AQUA, Blocks.CYAN_CANDLE, Blocks.CYAN_CONCRETE, Blocks.CYAN_CONCRETE_POWDER, Blocks.CYAN_CARPET, Blocks.CYAN_TERRACOTTA,
              Blocks.CYAN_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS_PANE, Blocks.CYAN_WOOL);
        addTypes(EnumColor.PURPLE, Blocks.PURPLE_CANDLE, Blocks.PURPLE_CONCRETE, Blocks.PURPLE_CONCRETE_POWDER, Blocks.PURPLE_CARPET, Blocks.PURPLE_TERRACOTTA,
              Blocks.PURPLE_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS_PANE, Blocks.PURPLE_WOOL);
        addTypes(EnumColor.DARK_BLUE, Blocks.BLUE_CANDLE, Blocks.BLUE_CONCRETE, Blocks.BLUE_CONCRETE_POWDER, Blocks.BLUE_CARPET, Blocks.BLUE_TERRACOTTA,
              Blocks.BLUE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS_PANE, Blocks.BLUE_WOOL);
        addTypes(EnumColor.BROWN, Blocks.BROWN_CANDLE, Blocks.BROWN_CONCRETE, Blocks.BROWN_CONCRETE_POWDER, Blocks.BROWN_CARPET, Blocks.BROWN_TERRACOTTA,
              Blocks.BROWN_STAINED_GLASS, Blocks.BROWN_STAINED_GLASS_PANE, Blocks.BROWN_WOOL);
        addTypes(EnumColor.DARK_GREEN, Blocks.GREEN_CANDLE, Blocks.GREEN_CONCRETE, Blocks.GREEN_CONCRETE_POWDER, Blocks.GREEN_CARPET, Blocks.GREEN_TERRACOTTA,
              Blocks.GREEN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS_PANE, Blocks.GREEN_WOOL);
        addTypes(EnumColor.RED, Blocks.RED_CANDLE, Blocks.RED_CONCRETE, Blocks.RED_CONCRETE_POWDER, Blocks.RED_CARPET, Blocks.RED_TERRACOTTA, Blocks.RED_STAINED_GLASS,
              Blocks.RED_STAINED_GLASS_PANE, Blocks.RED_WOOL);
        addTypes(EnumColor.BLACK, Blocks.BLACK_CANDLE, Blocks.BLACK_CONCRETE, Blocks.BLACK_CONCRETE_POWDER, Blocks.BLACK_CARPET, Blocks.BLACK_TERRACOTTA,
              Blocks.BLACK_STAINED_GLASS, Blocks.BLACK_STAINED_GLASS_PANE, Blocks.BLACK_WOOL);
    }

    private static void addTypes(EnumColor color, ItemLike candle, ItemLike concrete, ItemLike concretePowder, ItemLike carpet, ItemLike terracotta,
          ItemLike stainedGlass, ItemLike stainedGlassPane, ItemLike wool) {
        CANDLES.put(color, candle);
        CONCRETE.put(color, concrete);
        CONCRETE_POWDER.put(color, concretePowder);
        CARPETS.put(color, carpet);
        TERRACOTTA.put(color, terracotta);
        STAINED_GLASS.put(color, stainedGlass);
        STAINED_GLASS_PANES.put(color, stainedGlassPane);
        WOOL.put(color, wool);
    }

    @Override
    public void addRecipes(RecipeOutput consumer) {
        String basePath = "pigment_extracting/";
        addExtractionRecipes(consumer, basePath);
        addFlowerExtractionRecipes(consumer, basePath);
    }

    private static void addFlowerExtractionRecipes(RecipeOutput consumer, String basePath) {
        basePath += "flower/";
        //Flowers -> 4x dye output
        //Note: We use this higher rate as the pigment extractor is rather effective at extracting
        // pigments from the base materials. This is equivalent to the rate you would get for mixing
        // if using an enrichment chamber and then a combiner, but allows the same increased rate for
        // base types. Technically this then allows a round about way of getting to 8x for intermediate rates.
        long flowerRate = 3 * DYE_RATE;
        long largeFlowerRate = 2 * flowerRate;
        //Red
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Blocks.ROSE_BUSH),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.RED).getStack(largeFlowerRate)
        ).build(consumer, Mekanism.rl(basePath + "large_red"));
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Ingredient.of(
                    Items.BEETROOT,
                    Blocks.POPPY,
                    Blocks.RED_TULIP
              )),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.RED).getStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "small_red"));
        //Cyan
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Blocks.PITCHER_PLANT),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.DARK_AQUA).getStack(largeFlowerRate)
        ).build(consumer, Mekanism.rl(basePath + "large_cyan"));
        //Green
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Blocks.CACTUS),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.DARK_GREEN).getStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "green"));
        //Light gray
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Ingredient.of(
                    Blocks.OXEYE_DAISY,
                    Blocks.AZURE_BLUET,
                    Blocks.WHITE_TULIP
              )),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.GRAY).getStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "light_gray"));
        //Pink
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Blocks.PEONY),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.BRIGHT_PINK).getStack(largeFlowerRate)
        ).build(consumer, Mekanism.rl(basePath + "large_pink"));
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Ingredient.of(
                    Blocks.PINK_TULIP,
                    Blocks.PINK_PETALS
              )),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.BRIGHT_PINK).getStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "small_pink"));
        //Lime
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Blocks.SEA_PICKLE),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.BRIGHT_GREEN).getStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "lime"));
        //Yellow
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Blocks.SUNFLOWER),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.YELLOW).getStack(largeFlowerRate)
        ).build(consumer, Mekanism.rl(basePath + "large_yellow"));
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Blocks.DANDELION),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.YELLOW).getStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "small_yellow"));
        //Light blue
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Blocks.BLUE_ORCHID),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.INDIGO).getStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "light_blue"));
        //Magenta
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Blocks.LILAC),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.PINK).getStack(largeFlowerRate)
        ).build(consumer, Mekanism.rl(basePath + "large_magenta"));
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Blocks.ALLIUM),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.PINK).getStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "small_magenta"));
        //Orange
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Ingredient.of(
                    Blocks.ORANGE_TULIP,
                    Blocks.TORCHFLOWER
              )),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.ORANGE).getStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "orange"));
        //Blue
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Ingredient.of(
                    Blocks.CORNFLOWER,
                    Items.LAPIS_LAZULI
              )),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.DARK_BLUE).getStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "blue"));
        //Brown
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Items.COCOA_BEANS),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.BROWN).getStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "brown"));
        //Black
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Ingredient.of(
                    Items.INK_SAC,
                    Blocks.WITHER_ROSE
              )),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.BLACK).getStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "black"));
        //White
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Ingredient.of(
                    Items.BONE_MEAL,
                    Blocks.LILY_OF_THE_VALLEY
              )),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.WHITE).getStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "white"));
    }

    private static void addExtractionRecipes(RecipeOutput consumer, String basePath) {
        for (Map.Entry<EnumColor, IPigmentProvider> entry : MekanismPigments.PIGMENT_COLOR_LOOKUP.entrySet()) {
            EnumColor color = entry.getKey();
            IPigmentProvider pigment = entry.getValue();
            DyeColor dye = color.getDyeColor();
            if (dye != null) {
                ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                      IngredientCreatorAccess.item().from(dye.getTag()),
                      pigment.getStack(DYE_RATE)
                ).build(consumer, Mekanism.rl(basePath + "dye/" + color.getRegistryPrefix()));
                //TODO: Eventually we may want to consider taking patterns into account
                ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                      IngredientCreatorAccess.item().from(BannerBlock.byColor(dye)),
                      pigment.getStack(BANNER_RATE)
                ).build(consumer, Mekanism.rl(basePath + "banner/" + color.getRegistryPrefix()));
                addExtractionRecipe(consumer, color, CANDLES, pigment, CANDLE_RATE, basePath + "candle/");
                addExtractionRecipe(consumer, color, CONCRETE, pigment, CONCRETE_RATE, basePath + "concrete/");
                addExtractionRecipe(consumer, color, CONCRETE_POWDER, pigment, CONCRETE_POWDER_RATE, basePath + "concrete_powder/");
                addExtractionRecipe(consumer, color, CARPETS, pigment, CARPET_RATE, basePath + "carpet/");
                addExtractionRecipe(consumer, color, TERRACOTTA, pigment, CONCRETE_RATE, basePath + "terracotta/");
                addExtractionRecipe(consumer, color, STAINED_GLASS, pigment, STAINED_GLASS_RATE, basePath + "stained_glass/");
                addExtractionRecipe(consumer, color, STAINED_GLASS_PANES, pigment, STAINED_GLASS_PANE_RATE, basePath + "stained_glass_pane/");
                addExtractionRecipe(consumer, color, WOOL, pigment, WOOL_RATE, basePath + "wool/");
            }
        }
    }

    private static void addExtractionRecipe(RecipeOutput consumer, EnumColor color, Map<EnumColor, ItemLike> input, IPigmentProvider pigment, long rate,
          String basePath) {
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(input.get(color)),
              pigment.getStack(rate)
        ).build(consumer, Mekanism.rl(basePath + color.getRegistryPrefix()));
    }
}