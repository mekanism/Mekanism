package mekanism.additions.common.recipe;

import mekanism.additions.common.AdditionsTags;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.api.chemical.Chemical;
import mekanism.api.text.EnumColorCollection;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.impl.BaseSubRecipeProvider;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

class PlasticStairsRecipeProvider extends BaseSubRecipeProvider {

    private static final RecipePattern PLASTIC_STAIRS = RecipePattern.createPattern(
          TripleLine.of(Pattern.CONSTANT, Pattern.EMPTY, Pattern.EMPTY),
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.EMPTY),
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT));

    PlasticStairsRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "plastic/stairs/";
        registerPlasticStairs(consumer, AdditionsBlocks.PLASTIC_STAIRS, AdditionsBlocks.PLASTIC_BLOCKS, AdditionsTags.Items.STAIRS_PLASTIC_NORMAL, false, basePath);
        registerPlasticStairs(consumer, AdditionsBlocks.TRANSPARENT_PLASTIC_STAIRS, AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS,
              AdditionsTags.Items.STAIRS_PLASTIC_TRANSPARENT, true, basePath + "transparent/");
        registerPlasticStairs(consumer, AdditionsBlocks.PLASTIC_GLOW_STAIRS, AdditionsBlocks.PLASTIC_GLOW_BLOCKS, AdditionsTags.Items.STAIRS_PLASTIC_GLOW, false,
              basePath + "glow/");
    }

    private void registerPlasticStairs(RecipeOutput consumer, EnumColorCollection<? extends BlockRegistryObject<?, ?>> blocks,
          EnumColorCollection<? extends BlockRegistryObject<?, ?>> plasticMap, TagKey<Item> blockType, boolean transparent, String basePath) {
        HolderSet<Item> typeTag = this.items.getOrThrow(blockType);
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, blocks, (color, block) -> {
            Holder<Item> result = block.getItemHolder();
            Holder<Item> plastic = plasticMap.pick(color).getItemHolder();
            ExtendedShapedRecipeBuilder.shapedRecipe(result, 4)
                  .pattern(PLASTIC_STAIRS)
                  .key(Pattern.CONSTANT, plastic)
                  .category(RecipeCategory.BUILDING_BLOCKS)
                  .save(consumer, MekanismAdditions.rl(basePath + color.getRegistryPrefix()));
            if (transparent) {
                PlasticBlockRecipeProvider.registerTransparentRecolor(consumer, this.items, this.chemicals, result, typeTag, color, basePath);
            } else {
                PlasticBlockRecipeProvider.registerRecolor(consumer, this.items, this.chemicals, result, typeTag, color, basePath);
            }
        });
    }
}