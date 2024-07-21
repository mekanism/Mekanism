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
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.EnumUtils;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;

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

    private static final Map<EnumColor, TagKey<Item>> DYED_TAGS = new EnumMap<>(EnumColor.class);

    static {
        for (EnumColor color : EnumUtils.COLORS) {
            DyeColor dyeColor = color.getDyeColor();
            if (dyeColor != null) {
                DYED_TAGS.put(color, dyeColor.getDyedTag());
            }
        }
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
              IngredientCreatorAccess.item().from(
                    Items.BEETROOT,
                    Blocks.POPPY,
                    Blocks.RED_TULIP
              ),
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
              IngredientCreatorAccess.item().from(
                    Blocks.OXEYE_DAISY,
                    Blocks.AZURE_BLUET,
                    Blocks.WHITE_TULIP
              ),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.GRAY).getStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "light_gray"));
        //Pink
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Blocks.PEONY),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.BRIGHT_PINK).getStack(largeFlowerRate)
        ).build(consumer, Mekanism.rl(basePath + "large_pink"));
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(
                    Blocks.PINK_TULIP,
                    Blocks.PINK_PETALS
              ),
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
              IngredientCreatorAccess.item().from(
                    Blocks.ORANGE_TULIP,
                    Blocks.TORCHFLOWER
              ),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.ORANGE).getStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "orange"));
        //Blue
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(
                    Blocks.CORNFLOWER,
                    Items.LAPIS_LAZULI
              ),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.DARK_BLUE).getStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "blue"));
        //Brown
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Items.COCOA_BEANS),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.BROWN).getStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "brown"));
        //Black
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(
                    Items.INK_SAC,
                    Blocks.WITHER_ROSE
              ),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.BLACK).getStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "black"));
        //White
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(
                    Items.BONE_MEAL,
                    Blocks.LILY_OF_THE_VALLEY
              ),
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
                addExtractionRecipe(consumer, color, MekanismTags.Items.COLORABLE_CANDLE, pigment, CANDLE_RATE, basePath + "candle/");
                addExtractionRecipe(consumer, color, MekanismTags.Items.COLORABLE_CONCRETE, pigment, CONCRETE_RATE, basePath + "concrete/");
                addExtractionRecipe(consumer, color, MekanismTags.Items.COLORABLE_CONCRETE_POWDER, pigment, CONCRETE_POWDER_RATE, basePath + "concrete_powder/");
                addExtractionRecipe(consumer, color, MekanismTags.Items.COLORABLE_CARPETS, pigment, CARPET_RATE, basePath + "carpet/");
                addExtractionRecipe(consumer, color, MekanismTags.Items.COLORABLE_TERRACOTTA, pigment, CONCRETE_RATE, basePath + "terracotta/");
                addExtractionRecipe(consumer, color, MekanismTags.Items.COLORABLE_GLASS, pigment, STAINED_GLASS_RATE, basePath + "stained_glass/");
                addExtractionRecipe(consumer, color, MekanismTags.Items.COLORABLE_GLASS_PANES, pigment, STAINED_GLASS_PANE_RATE, basePath + "stained_glass_pane/");
                addExtractionRecipe(consumer, color, MekanismTags.Items.COLORABLE_WOOL, pigment, WOOL_RATE, basePath + "wool/");
            }
        }
    }

    private static void addExtractionRecipe(RecipeOutput consumer, EnumColor color, TagKey<Item> input, IPigmentProvider pigment, long rate, String basePath) {
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(IntersectionIngredient.of(
                    Ingredient.of(input),
                    Ingredient.of(DYED_TAGS.get(color))
              )),
              pigment.getStack(rate)
        ).build(consumer, Mekanism.rl(basePath + color.getRegistryPrefix()));
    }
}