package mekanism.additions.common.recipe;

import java.util.Map;
import java.util.function.Consumer;
import mekanism.additions.common.AdditionsTags;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.api.providers.IItemProvider;
import mekanism.api.text.EnumColor;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class PlasticSlabsRecipeProvider implements ISubRecipeProvider {

    private static final RecipePattern PLASTIC_SLAB = RecipePattern.createPattern(TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT));

    @Override
    public void addRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "plastic/slab/";
        registerPlasticSlabs(consumer, AdditionsBlocks.PLASTIC_SLABS, AdditionsBlocks.PLASTIC_BLOCKS, AdditionsTags.Items.SLABS_PLASTIC, false, basePath);
        registerPlasticSlabs(consumer, AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS, AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS,
              AdditionsTags.Items.SLABS_PLASTIC_TRANSPARENT, true, basePath + "transparent/");
        registerPlasticSlabs(consumer, AdditionsBlocks.PLASTIC_GLOW_SLABS, AdditionsBlocks.PLASTIC_GLOW_BLOCKS, AdditionsTags.Items.SLABS_PLASTIC_GLOW, false,
              basePath + "glow/");
    }

    private void registerPlasticSlabs(Consumer<FinishedRecipe> consumer, Map<EnumColor, ? extends IItemProvider> blocks, Map<EnumColor, ? extends IItemProvider> plastic,
          TagKey<Item> blockType, boolean transparent, String basePath) {
        for (Map.Entry<EnumColor, ? extends IItemProvider> entry : blocks.entrySet()) {
            EnumColor color = entry.getKey();
            registerPlasticSlab(consumer, color, entry.getValue(), plastic.get(color), blockType, transparent, basePath);
        }
    }

    private void registerPlasticSlab(Consumer<FinishedRecipe> consumer, EnumColor color, IItemProvider result, IItemProvider plastic,
          TagKey<Item> blockType, boolean transparent, String basePath) {
        ExtendedShapedRecipeBuilder.shapedRecipe(result, 6)
              .pattern(PLASTIC_SLAB)
              .key(Pattern.CONSTANT, plastic)
              .build(consumer, MekanismAdditions.rl(basePath + color.getRegistryPrefix()));
        if (transparent) {
            PlasticBlockRecipeProvider.registerTransparentRecolor(consumer, result, blockType, color, basePath);
        } else {
            PlasticBlockRecipeProvider.registerRecolor(consumer, result, blockType, color, basePath);
        }
    }
}