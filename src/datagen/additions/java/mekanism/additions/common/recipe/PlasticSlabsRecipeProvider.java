package mekanism.additions.common.recipe;

import java.util.Map;
import mekanism.additions.common.AdditionsTags;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.api.text.EnumColor;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
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

public class PlasticSlabsRecipeProvider implements ISubRecipeProvider {

    private static final RecipePattern PLASTIC_SLAB = RecipePattern.createPattern(TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT));
    private static final RecipePattern PLASTIC_RECOMBINATION = RecipePattern.createPattern(Pattern.CONSTANT, Pattern.CONSTANT);

    private final HolderGetter<Item> items;

    public PlasticSlabsRecipeProvider(HolderGetter<Item> items) {
        this.items = items;
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "plastic/slab/";
        registerPlasticSlabs(consumer, AdditionsBlocks.PLASTIC_SLABS, AdditionsBlocks.PLASTIC_BLOCKS, AdditionsTags.Items.SLABS_PLASTIC_NORMAL, false, basePath);
        registerPlasticSlabs(consumer, AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS, AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS,
              AdditionsTags.Items.SLABS_PLASTIC_TRANSPARENT, true, basePath + "transparent/");
        registerPlasticSlabs(consumer, AdditionsBlocks.PLASTIC_GLOW_SLABS, AdditionsBlocks.PLASTIC_GLOW_BLOCKS, AdditionsTags.Items.SLABS_PLASTIC_GLOW, false,
              basePath + "glow/");
    }

    private void registerPlasticSlabs(RecipeOutput consumer, Map<EnumColor, ? extends BlockRegistryObject<?, ?>> blocks,
          Map<EnumColor, ? extends BlockRegistryObject<?, ?>> plasticMap, TagKey<Item> blockType, boolean transparent, String basePath) {
        HolderSet<Item> typeTag = this.items.getOrThrow(blockType);
        for (Map.Entry<EnumColor, ? extends BlockRegistryObject<?, ?>> entry : blocks.entrySet()) {
            EnumColor color = entry.getKey();
            Holder<Item> slab = entry.getValue().getItemHolder();
            Holder<Item> plastic = plasticMap.get(color).getItemHolder();
            ExtendedShapedRecipeBuilder.shapedRecipe(slab, 6)
                  .pattern(PLASTIC_SLAB)
                  .key(Pattern.CONSTANT, plastic)
                  .category(RecipeCategory.BUILDING_BLOCKS)
                  .save(consumer, MekanismAdditions.rl(basePath + color.getRegistryPrefix()));
            if (transparent) {
                PlasticBlockRecipeProvider.registerTransparentRecolor(consumer, this.items, slab, typeTag, color, basePath);
            } else {
                PlasticBlockRecipeProvider.registerRecolor(consumer, this.items, slab, typeTag, color, basePath);
            }
            ExtendedShapedRecipeBuilder.shapedRecipe(plastic, 1)
                  .pattern(PLASTIC_RECOMBINATION)
                  .key(Pattern.CONSTANT, slab)
                  .category(RecipeCategory.BUILDING_BLOCKS)
                  .save(consumer, MekanismAdditions.rl(basePath + "recombination/" + color.getRegistryPrefix()));
        }
    }
}