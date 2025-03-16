package mekanism.common.recipe.impl;

import java.util.EnumMap;
import java.util.Map;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registration.impl.DeferredChemical;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.BannerBlock;
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
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
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
              IngredientCreatorAccess.item().from(Items.ROSE_BUSH),
              MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.RED).asStack(largeFlowerRate)
        ).build(consumer, Mekanism.rl(basePath + "large_red"));
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(
                    Items.BEETROOT,
                    Items.POPPY,
                    Items.RED_TULIP
              ),
              MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.RED).asStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "small_red"));
        //Cyan
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Items.PITCHER_PLANT),
              MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.DARK_AQUA).asStack(largeFlowerRate)
        ).build(consumer, Mekanism.rl(basePath + "large_cyan"));
        //Green
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Items.CACTUS),
              MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.DARK_GREEN).asStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "green"));
        //Light gray
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(
                    Items.OXEYE_DAISY,
                    Items.AZURE_BLUET,
                    Items.WHITE_TULIP
              ),
              MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.GRAY).asStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "light_gray"));
        //Pink
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Items.PEONY),
              MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.BRIGHT_PINK).asStack(largeFlowerRate)
        ).build(consumer, Mekanism.rl(basePath + "large_pink"));
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(
                    Items.PINK_TULIP,
                    Items.PINK_PETALS
              ),
              MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.BRIGHT_PINK).asStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "small_pink"));
        //Lime
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Items.SEA_PICKLE),
              MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.BRIGHT_GREEN).asStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "lime"));
        //Yellow
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Items.SUNFLOWER),
              MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.YELLOW).asStack(largeFlowerRate)
        ).build(consumer, Mekanism.rl(basePath + "large_yellow"));
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Items.DANDELION),
              MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.YELLOW).asStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "small_yellow"));
        //Light blue
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Items.BLUE_ORCHID),
              MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.INDIGO).asStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "light_blue"));
        //Magenta
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Items.LILAC),
              MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.PINK).asStack(largeFlowerRate)
        ).build(consumer, Mekanism.rl(basePath + "large_magenta"));
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Items.ALLIUM),
              MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.PINK).asStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "small_magenta"));
        //Orange
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(
                    Items.ORANGE_TULIP,
                    Items.TORCHFLOWER
              ),
              MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.ORANGE).asStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "orange"));
        //Blue
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(
                    Items.CORNFLOWER,
                    Items.LAPIS_LAZULI
              ),
              MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.DARK_BLUE).asStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "blue"));
        //Brown
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(Items.COCOA_BEANS),
              MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.BROWN).asStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "brown"));
        //Black
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(
                    Items.INK_SAC,
                    Items.WITHER_ROSE
              ),
              MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.BLACK).asStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "black"));
        //White
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(
                    Items.BONE_MEAL,
                    Items.LILY_OF_THE_VALLEY
              ),
              MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.WHITE).asStack(flowerRate)
        ).build(consumer, Mekanism.rl(basePath + "white"));
    }

    private static void addExtractionRecipes(RecipeOutput consumer, String basePath) {
        for (Map.Entry<EnumColor, DeferredChemical<Chemical>> entry : MekanismChemicals.PIGMENT_COLOR_LOOKUP.entrySet()) {
            EnumColor color = entry.getKey();
            DeferredChemical<Chemical> pigment = entry.getValue();
            DyeColor dye = color.getDyeColor();
            if (dye != null) {
                ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                      IngredientCreatorAccess.item().from(dye.getTag()),
                      pigment.asStack(DYE_RATE)
                ).build(consumer, Mekanism.rl(basePath + "dye/" + color.getRegistryPrefix()));
                //TODO: Eventually we may want to consider taking patterns into account
                ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                      IngredientCreatorAccess.item().from(BannerBlock.byColor(dye)),
                      pigment.asStack(BANNER_RATE)
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

    private static void addExtractionRecipe(RecipeOutput consumer, EnumColor color, TagKey<Item> input, Holder<Chemical> pigment, long rate, String basePath) {
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(IntersectionIngredient.of(
                    Ingredient.of(input),
                    Ingredient.of(DYED_TAGS.get(color))
              )),
              new ChemicalStack(pigment, rate)
        ).build(consumer, Mekanism.rl(basePath + color.getRegistryPrefix()));
    }
}