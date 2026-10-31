package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.api.text.EnumColorCollection;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;

public class PigmentExtractingRecipeProvider extends BaseSubRecipeProvider {

    public static final int DYE_RATE = 256;
    private static final int BANNER_RATE = DYE_RATE / 4;//64
    private static final int CONCRETE_POWDER_RATE = DYE_RATE / 8;//32
    private static final int CANDLE_RATE = DYE_RATE * 7 / 8;//224
    //Concrete shares a rate with terracotta
    private static final int CONCRETE_RATE = CONCRETE_POWDER_RATE * 3 / 4;//24
    private static final int CONCRETE_SLAB_RATE = CONCRETE_RATE / 2;//12
    private static final int GLAZED_TERRACOTTA_RATE = CONCRETE_RATE * 3 / 4;//18
    private static final int STAINED_GLASS_RATE = DYE_RATE / 16;//16
    private static final int STAINED_GLASS_PANE_RATE = STAINED_GLASS_RATE * 3 / 8;//6
    private static final int WOOL_RATE = DYE_RATE * 3 / 4;//192
    private static final int WOOL_SLAB_RATE = WOOL_RATE / 2;//96
    private static final int WOOL_STAIR_RATE = WOOL_RATE * 3 / 2;//288
    private static final int CUSHION_RATE = WOOL_SLAB_RATE * 3;//288
    private static final int CARPET_RATE = WOOL_RATE * 2 / 3;//128

    PigmentExtractingRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput output) {
        String basePath = "pigment_extracting/";
        addExtractionRecipes(output, basePath);
        addFlowerExtractionRecipes(output, basePath);
    }

    private void addFlowerExtractionRecipes(RecipeOutput output, String basePath) {
        basePath += "flower/";
        //Flowers -> 4x dye output
        //Note: We use this higher rate as the pigment extractor is rather effective at extracting
        // pigments from the base materials. This is equivalent to the rate you would get for mixing
        // if using an enrichment chamber and then a combiner, but allows the same increased rate for
        // base types. Technically this then allows a round about way of getting to 8x for intermediate rates.
        int flowerRate = 3 * DYE_RATE;
        int largeFlowerRate = 2 * flowerRate;
        //Red
        RecipeProviderUtil.addPigmentExtractingDyeRecipe(output, items, chemicals, basePath, EnumColor.RED, largeFlowerRate, "large_red", BlockItemIds.ROSE_BUSH);
        RecipeProviderUtil.addPigmentExtractingDyeRecipe(output, items, chemicals, basePath, EnumColor.RED, flowerRate, "small_red",
              ItemIds.BEETROOT,
              BlockItemIds.POPPY.item(),
              BlockItemIds.RED_TULIP.item()
        );
        //Cyan
        RecipeProviderUtil.addPigmentExtractingDyeRecipe(output, items, chemicals, basePath, EnumColor.DARK_AQUA, largeFlowerRate, "large_cyan", BlockItemIds.PITCHER_PLANT);
        //Green
        RecipeProviderUtil.addPigmentExtractingDyeRecipe(output, items, chemicals, basePath, EnumColor.DARK_GREEN, flowerRate, "green", Tags.Items.CROPS_CACTUS);
        //Light gray
        RecipeProviderUtil.addPigmentExtractingDyeRecipe(output, items, chemicals, basePath, EnumColor.GRAY, flowerRate, "light_gray",
              BlockItemIds.AZURE_BLUET,
              BlockItemIds.OXEYE_DAISY,
              BlockItemIds.WHITE_TULIP
        );
        //Gray
        RecipeProviderUtil.addPigmentExtractingDyeRecipe(output, items, chemicals, basePath, EnumColor.DARK_GRAY, flowerRate, "gray", BlockItemIds.CLOSED_EYEBLOSSOM);
        //Pink
        RecipeProviderUtil.addPigmentExtractingDyeRecipe(output, items, chemicals, basePath, EnumColor.BRIGHT_PINK, largeFlowerRate, "large_pink", BlockItemIds.PEONY);
        RecipeProviderUtil.addPigmentExtractingDyeRecipe(output, items, chemicals, basePath, EnumColor.BRIGHT_PINK, flowerRate, "small_pink",
              BlockItemIds.CACTUS_FLOWER,
              BlockItemIds.PINK_TULIP,
              BlockItemIds.PINK_PETALS
        );
        //Lime
        RecipeProviderUtil.addPigmentExtractingDyeRecipe(output, items, chemicals, basePath, EnumColor.BRIGHT_GREEN, flowerRate, "lime", BlockItemIds.SEA_PICKLE);
        //Yellow
        RecipeProviderUtil.addPigmentExtractingDyeRecipe(output, items, chemicals, basePath, EnumColor.YELLOW, largeFlowerRate, "large_yellow", BlockItemIds.SUNFLOWER);
        RecipeProviderUtil.addPigmentExtractingDyeRecipe(output, items, chemicals, basePath, EnumColor.YELLOW, flowerRate, "small_yellow",
              BlockItemIds.DANDELION,
              BlockItemIds.GOLDEN_DANDELION,
              BlockItemIds.WILDFLOWERS
        );
        //Light blue
        RecipeProviderUtil.addPigmentExtractingDyeRecipe(output, items, chemicals, basePath, EnumColor.INDIGO, flowerRate, "light_blue", BlockItemIds.BLUE_ORCHID);
        //Magenta
        RecipeProviderUtil.addPigmentExtractingDyeRecipe(output, items, chemicals, basePath, EnumColor.PINK, largeFlowerRate, "large_magenta", BlockItemIds.LILAC);
        RecipeProviderUtil.addPigmentExtractingDyeRecipe(output, items, chemicals, basePath, EnumColor.PINK, flowerRate, "small_magenta", BlockItemIds.ALLIUM);
        //Orange
        RecipeProviderUtil.addPigmentExtractingDyeRecipe(output, items, chemicals, basePath, EnumColor.ORANGE, flowerRate, "orange",
              BlockItemIds.OPEN_EYEBLOSSOM,
              BlockItemIds.ORANGE_TULIP,
              BlockItemIds.TORCHFLOWER
        );
        //Blue
        RecipeProviderUtil.addPigmentExtractingDyeRecipe(output, items, chemicals, basePath, EnumColor.DARK_BLUE, flowerRate, "blue",
              BlockItemIds.CORNFLOWER.item(),
              ItemIds.LAPIS_LAZULI
        );
        //Brown
        RecipeProviderUtil.addPigmentExtractingDyeRecipe(output, items, chemicals, basePath, EnumColor.BROWN, flowerRate, "brown", Tags.Items.CROPS_COCOA_BEAN);
        //Black
        RecipeProviderUtil.addPigmentExtractingDyeRecipe(output, items, chemicals, basePath, EnumColor.BLACK, flowerRate, "black",
              ItemIds.INK_SAC,
              BlockItemIds.WITHER_ROSE.item()
        );
        //White
        RecipeProviderUtil.addPigmentExtractingDyeRecipe(output, items, chemicals, basePath, EnumColor.WHITE, flowerRate, "white",
              ItemIds.BONE_MEAL,
              BlockItemIds.LILY_OF_THE_VALLEY.item()
        );
    }

    private void addExtractionRecipes(RecipeOutput output, String basePath) {
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, ChemicalIds.SIMPLE_PIGMENTS, (color, pigment) -> {
            DyeColor dye = color.getDyeColor();
            if (dye != null) {
                ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                      IngredientCreatorAccess.item().from(this.items, dye.getTag()),
                      chemicalTemplate(pigment, DYE_RATE)
                ).group(ItemIds.DYE.pick(dye))
                      .unlockedBy("has_dye", has(dye.getTag()))
                      .save(output, basePath + "dye/" + color.getRegistryPrefix());
                //TODO: Eventually we may want to consider taking patterns into account
                addExtractionRecipe(output, color, dye, MekanismTags.Items.COLORABLE_BANNERS, pigment, BANNER_RATE, basePath + "banner/");
                addExtractionRecipe(output, color, dye, MekanismTags.Items.COLORABLE_CANDLE, pigment, CANDLE_RATE, basePath + "candle/");
                addExtractionRecipe(output, color, dye, MekanismTags.Items.COLORABLE_CUSHIONS, pigment, CUSHION_RATE, basePath + "cushion/");
                addExtractionRecipe(output, color, dye, MekanismTags.Items.COLORABLE_CONCRETE, pigment, CONCRETE_RATE, basePath + "concrete/");
                addExtractionRecipe(output, color, dye, MekanismTags.Items.COLORABLE_CONCRETE_SLABS, pigment, CONCRETE_SLAB_RATE, basePath + "concrete/slabs/");
                //1:1 with concrete via stone cutter
                addExtractionRecipe(output, color, dye, MekanismTags.Items.COLORABLE_CONCRETE_STAIRS, pigment, CONCRETE_RATE, basePath + "concrete/stairs/");
                addExtractionRecipe(output, color, dye, MekanismTags.Items.COLORABLE_CONCRETE_POWDER, pigment, CONCRETE_POWDER_RATE, basePath + "concrete_powder/");
                addExtractionRecipe(output, color, dye, MekanismTags.Items.COLORABLE_CARPETS, pigment, CARPET_RATE, basePath + "carpet/");
                addExtractionRecipe(output, color, dye, MekanismTags.Items.COLORABLE_TERRACOTTA, pigment, CONCRETE_RATE, basePath + "terracotta/");
                addExtractionRecipe(output, color, dye, MekanismTags.Items.COLORABLE_GLAZED_TERRACOTTA, pigment, GLAZED_TERRACOTTA_RATE, basePath + "terracotta/glazed/");
                addExtractionRecipe(output, color, dye, MekanismTags.Items.COLORABLE_GLASS, pigment, STAINED_GLASS_RATE, basePath + "stained_glass/");
                addExtractionRecipe(output, color, dye, MekanismTags.Items.COLORABLE_GLASS_PANES, pigment, STAINED_GLASS_PANE_RATE, basePath + "stained_glass_pane/");
                addExtractionRecipe(output, color, dye, MekanismTags.Items.COLORABLE_WOOL, pigment, WOOL_RATE, basePath + "wool/");
                addExtractionRecipe(output, color, dye, MekanismTags.Items.COLORABLE_WOOL_SLABS, pigment, WOOL_SLAB_RATE, basePath + "wool/slabs/");
                addExtractionRecipe(output, color, dye, MekanismTags.Items.COLORABLE_WOOL_STAIRS, pigment, WOOL_STAIR_RATE, basePath + "wool/stairs/");
            }
        });
    }

    private void addExtractionRecipe(RecipeOutput output, EnumColor color, DyeColor dye, TagKey<Item> input, ResourceKey<Chemical> pigment, int rate, String basePath) {
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(IntersectionIngredient.of(
                    Ingredient.of(this.items.getOrThrow(input)),
                    Ingredient.of(this.items.getOrThrow(dye.getDyedTag()))
              )),
              chemicalTemplate(pigment, rate)
        ).group(ItemIds.DYE.pick(dye))
              .unlockedBy("has_dye", has(dye.getTag()))
              .save(output, basePath + color.getRegistryPrefix());
    }
}