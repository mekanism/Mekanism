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
import net.minecraft.core.HolderSet;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

class PlasticSlabsRecipeProvider extends BaseSubRecipeProvider {

    private static final RecipePattern PLASTIC_SLAB = RecipePattern.createPattern(TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT));
    private static final RecipePattern PLASTIC_RECOMBINATION = RecipePattern.createPattern(Pattern.CONSTANT, Pattern.CONSTANT);

    PlasticSlabsRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer) {
        String basePath = "plastic/slab/";
        registerPlasticSlabs(consumer, AdditionsBlocks.PLASTIC_SLABS, AdditionsBlocks.PLASTIC_BLOCKS, AdditionsTags.Items.SLABS_PLASTIC_NORMAL, false, basePath, "");
        registerPlasticSlabs(consumer, AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS, AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS,
              AdditionsTags.Items.SLABS_PLASTIC_TRANSPARENT, true, basePath + "transparent/", "transparent_");
        registerPlasticSlabs(consumer, AdditionsBlocks.PLASTIC_GLOW_SLABS, AdditionsBlocks.PLASTIC_GLOW_BLOCKS, AdditionsTags.Items.SLABS_PLASTIC_GLOW, false,
              basePath + "glow/", "glow_");
    }

    private void registerPlasticSlabs(RecipeOutput consumer, EnumColorCollection<? extends BlockRegistryObject<?, ?>> blocks,
          EnumColorCollection<? extends BlockRegistryObject<?, ?>> plasticMap, TagKey<Item> blockType, boolean transparent, String basePath, String groupName) {
        HolderSet.Named<Item> typeTag = this.items.getOrThrow(blockType);
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, blocks, (color, block) -> {
            Holder<Item> slab = block.getItemHolder();
            Holder<Item> plastic = plasticMap.pick(color).getItemHolder();
            ExtendedShapedRecipeBuilder.shaped(slab, 6)
                  .pattern(PLASTIC_SLAB)
                  .define(Pattern.CONSTANT, plastic)
                  .category(RecipeCategory.BUILDING_BLOCKS)
                  .group(MekanismAdditions.rl(groupName + "plastic_slabs"))
                  .unlockedBy("has_plastic", has(plastic))
                  .save(consumer, MekanismAdditions.rl(basePath + color.getRegistryPrefix()));
            if (transparent) {
                PlasticBlockRecipeProvider.registerTransparentRecolor(consumer, this.items, this.chemicals, slab, typeTag, color, basePath, groupName + "plastic_slabs_recolor");
            } else {
                PlasticBlockRecipeProvider.registerRecolor(consumer, this.items, this.chemicals, slab, typeTag, color, basePath, groupName + "plastic_slabs_recolor");
            }
            ExtendedShapedRecipeBuilder.shaped(plastic, 1)
                  .pattern(PLASTIC_RECOMBINATION)
                  .define(Pattern.CONSTANT, slab)
                  .category(RecipeCategory.BUILDING_BLOCKS)
                  .group(MekanismAdditions.rl(groupName + "plastic_recombination"))
                  .unlockedBy("has_plastic_slab", has(slab))
                  .save(consumer, MekanismAdditions.rl(basePath + "recombination/" + color.getRegistryPrefix()));
        });
    }
}