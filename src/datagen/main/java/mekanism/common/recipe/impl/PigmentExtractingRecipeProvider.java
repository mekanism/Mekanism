package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.api.text.EnumColorCollection;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.BlockItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;

public class PigmentExtractingRecipeProvider extends BaseSubRecipeProvider {

    public static final int DYE_RATE = 256;
    private static final int BANNER_RATE = DYE_RATE / 4;//64
    private static final int CONCRETE_POWDER_RATE = DYE_RATE / 8;//32
    private static final int CANDLE_RATE = DYE_RATE * 7 / 8;//224
    //Concrete shares a rate with terracotta
    private static final int CONCRETE_RATE = CONCRETE_POWDER_RATE * 3 / 4;//24
    private static final int STAINED_GLASS_RATE = DYE_RATE / 16;//16
    private static final int STAINED_GLASS_PANE_RATE = STAINED_GLASS_RATE * 3 / 8;//6
    private static final int WOOL_RATE = DYE_RATE * 3 / 4;//192
    private static final int CARPET_RATE = WOOL_RATE * 2 / 3;//128

    PigmentExtractingRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "pigment_extracting/";
        addExtractionRecipes(consumer, basePath);
        addFlowerExtractionRecipes(consumer, basePath);
    }

    private void addFlowerExtractionRecipes(RecipeOutput consumer, String basePath) {
        basePath += "flower/";
        //Flowers -> 4x dye output
        //Note: We use this higher rate as the pigment extractor is rather effective at extracting
        // pigments from the base materials. This is equivalent to the rate you would get for mixing
        // if using an enrichment chamber and then a combiner, but allows the same increased rate for
        // base types. Technically this then allows a round about way of getting to 8x for intermediate rates.
        int flowerRate = 3 * DYE_RATE;
        int largeFlowerRate = 2 * flowerRate;
        //Red
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(items, BlockItemIds.ROSE_BUSH),
              chemicalTemplate(MekanismChemicals.SIMPLE_PIGMENTS.red(), largeFlowerRate)
        ).save(consumer, Mekanism.rl(basePath + "large_red"));
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(
                    Items.BEETROOT,
                    Items.POPPY,
                    Items.RED_TULIP
              ),
              chemicalTemplate(MekanismChemicals.SIMPLE_PIGMENTS.red(), flowerRate)
        ).save(consumer, Mekanism.rl(basePath + "small_red"));
        //Cyan
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(items, BlockItemIds.PITCHER_PLANT),
              chemicalTemplate(MekanismChemicals.SIMPLE_PIGMENTS.darkAqua(), largeFlowerRate)
        ).save(consumer, Mekanism.rl(basePath + "large_cyan"));
        //Green
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(items, BlockItemIds.CACTUS),
              chemicalTemplate(MekanismChemicals.SIMPLE_PIGMENTS.darkGreen(), flowerRate)
        ).save(consumer, Mekanism.rl(basePath + "green"));
        //Light gray
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(
                    Items.OXEYE_DAISY,
                    Items.AZURE_BLUET,
                    Items.WHITE_TULIP
              ),
              chemicalTemplate(MekanismChemicals.SIMPLE_PIGMENTS.gray(), flowerRate)
        ).save(consumer, Mekanism.rl(basePath + "light_gray"));
        //Pink
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(items, BlockItemIds.PEONY),
              chemicalTemplate(MekanismChemicals.SIMPLE_PIGMENTS.brightPink(), largeFlowerRate)
        ).save(consumer, Mekanism.rl(basePath + "large_pink"));
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(
                    Items.PINK_TULIP,
                    Items.PINK_PETALS
              ),
              chemicalTemplate(MekanismChemicals.SIMPLE_PIGMENTS.brightPink(), flowerRate)
        ).save(consumer, Mekanism.rl(basePath + "small_pink"));
        //Lime
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(items, BlockItemIds.SEA_PICKLE),
              chemicalTemplate(MekanismChemicals.SIMPLE_PIGMENTS.brightGreen(), flowerRate)
        ).save(consumer, Mekanism.rl(basePath + "lime"));
        //Yellow
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(items, BlockItemIds.SUNFLOWER),
              chemicalTemplate(MekanismChemicals.SIMPLE_PIGMENTS.yellow(), largeFlowerRate)
        ).save(consumer, Mekanism.rl(basePath + "large_yellow"));
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(items, BlockItemIds.DANDELION),
              chemicalTemplate(MekanismChemicals.SIMPLE_PIGMENTS.yellow(), flowerRate)
        ).save(consumer, Mekanism.rl(basePath + "small_yellow"));
        //Light blue
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(items, BlockItemIds.BLUE_ORCHID),
              chemicalTemplate(MekanismChemicals.SIMPLE_PIGMENTS.indigo(), flowerRate)
        ).save(consumer, Mekanism.rl(basePath + "light_blue"));
        //Magenta
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(items, BlockItemIds.LILAC),
              chemicalTemplate(MekanismChemicals.SIMPLE_PIGMENTS.pink(), largeFlowerRate)
        ).save(consumer, Mekanism.rl(basePath + "large_magenta"));
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(items, BlockItemIds.ALLIUM),
              chemicalTemplate(MekanismChemicals.SIMPLE_PIGMENTS.pink(), flowerRate)
        ).save(consumer, Mekanism.rl(basePath + "small_magenta"));
        //Orange
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(
                    Items.ORANGE_TULIP,
                    Items.TORCHFLOWER
              ),
              chemicalTemplate(MekanismChemicals.SIMPLE_PIGMENTS.orange(), flowerRate)
        ).save(consumer, Mekanism.rl(basePath + "orange"));
        //Blue
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(
                    Items.CORNFLOWER,
                    Items.LAPIS_LAZULI
              ),
              chemicalTemplate(MekanismChemicals.SIMPLE_PIGMENTS.darkBlue(), flowerRate)
        ).save(consumer, Mekanism.rl(basePath + "blue"));
        //Brown
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(items, BlockItemIds.COCOA_CROP),
              chemicalTemplate(MekanismChemicals.SIMPLE_PIGMENTS.brown(), flowerRate)
        ).save(consumer, Mekanism.rl(basePath + "brown"));
        //Black
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(
                    Items.INK_SAC,
                    Items.WITHER_ROSE
              ),
              chemicalTemplate(MekanismChemicals.SIMPLE_PIGMENTS.black(), flowerRate)
        ).save(consumer, Mekanism.rl(basePath + "black"));
        //White
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(
                    Items.BONE_MEAL,
                    Items.LILY_OF_THE_VALLEY
              ),
              chemicalTemplate(MekanismChemicals.SIMPLE_PIGMENTS.white(), flowerRate)
        ).save(consumer, Mekanism.rl(basePath + "white"));
    }

    private void addExtractionRecipes(RecipeOutput consumer, String basePath) {
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, MekanismChemicals.SIMPLE_PIGMENTS, (color, pigment) -> {
            DyeColor dye = color.getDyeColor();
            if (dye != null) {
                ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                      IngredientCreatorAccess.item().from(this.items, dye.getTag()),
                      chemicalTemplate(pigment, DYE_RATE)
                ).save(consumer, Mekanism.rl(basePath + "dye/" + color.getRegistryPrefix()));
                //TODO: Eventually we may want to consider taking patterns into account
                addExtractionRecipe(consumer, color, dye, MekanismTags.Items.COLORABLE_BANNERS, pigment, BANNER_RATE, basePath + "banner/");
                addExtractionRecipe(consumer, color, dye, MekanismTags.Items.COLORABLE_CANDLE, pigment, CANDLE_RATE, basePath + "candle/");
                addExtractionRecipe(consumer, color, dye, MekanismTags.Items.COLORABLE_CONCRETE, pigment, CONCRETE_RATE, basePath + "concrete/");
                addExtractionRecipe(consumer, color, dye, MekanismTags.Items.COLORABLE_CONCRETE_POWDER, pigment, CONCRETE_POWDER_RATE, basePath + "concrete_powder/");
                addExtractionRecipe(consumer, color, dye, MekanismTags.Items.COLORABLE_CARPETS, pigment, CARPET_RATE, basePath + "carpet/");
                addExtractionRecipe(consumer, color, dye, MekanismTags.Items.COLORABLE_TERRACOTTA, pigment, CONCRETE_RATE, basePath + "terracotta/");
                addExtractionRecipe(consumer, color, dye, MekanismTags.Items.COLORABLE_GLASS, pigment, STAINED_GLASS_RATE, basePath + "stained_glass/");
                addExtractionRecipe(consumer, color, dye, MekanismTags.Items.COLORABLE_GLASS_PANES, pigment, STAINED_GLASS_PANE_RATE, basePath + "stained_glass_pane/");
                addExtractionRecipe(consumer, color, dye, MekanismTags.Items.COLORABLE_WOOL, pigment, WOOL_RATE, basePath + "wool/");
            }
        });
    }

    private void addExtractionRecipe(RecipeOutput consumer, EnumColor color, DyeColor dye, TagKey<Item> input, ResourceKey<Chemical> pigment, int rate, String basePath) {
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(IntersectionIngredient.of(
                    Ingredient.of(this.items.getOrThrow(input)),
                    Ingredient.of(this.items.getOrThrow(dye.getDyedTag()))
              )),
              chemicalTemplate(pigment, rate)
        ).save(consumer, Mekanism.rl(basePath + color.getRegistryPrefix()));
    }
}