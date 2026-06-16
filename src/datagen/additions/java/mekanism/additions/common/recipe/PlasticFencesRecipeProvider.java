package mekanism.additions.common.recipe;

import java.util.Map;
import mekanism.additions.common.AdditionsTags;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.block.plastic.BlockPlasticFence;
import mekanism.additions.common.block.plastic.BlockPlasticFenceGate;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.api.chemical.Chemical;
import mekanism.api.text.EnumColor;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.impl.BaseSubRecipeProvider;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

class PlasticFencesRecipeProvider extends BaseSubRecipeProvider {

    private static final RecipePattern PLASTIC_FENCE = RecipePattern.createPattern(
          TripleLine.of(Pattern.CONSTANT, AdditionsRecipeProvider.PLASTIC_ROD_CHAR, Pattern.CONSTANT),
          TripleLine.of(Pattern.CONSTANT, AdditionsRecipeProvider.PLASTIC_ROD_CHAR, Pattern.CONSTANT));
    private static final RecipePattern PLASTIC_FENCE_GATE = RecipePattern.createPattern(
          TripleLine.of(AdditionsRecipeProvider.PLASTIC_ROD_CHAR, Pattern.CONSTANT, AdditionsRecipeProvider.PLASTIC_ROD_CHAR),
          TripleLine.of(AdditionsRecipeProvider.PLASTIC_ROD_CHAR, Pattern.CONSTANT, AdditionsRecipeProvider.PLASTIC_ROD_CHAR));

    PlasticFencesRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "plastic/";
        registerPlasticFences(consumer, basePath);
        registerPlasticFenceGates(consumer, basePath);
    }

    private void registerPlasticFences(RecipeOutput consumer, String basePath) {
        basePath += "fence/";
        HolderSet<Item> fencesTag = this.items.getOrThrow(AdditionsTags.Items.FENCES_PLASTIC_NORMAL);
        for (Map.Entry<EnumColor, ? extends BlockRegistryObject<BlockPlasticFence, ?>> entry : AdditionsBlocks.PLASTIC_FENCES.entrySet()) {
            EnumColor color = entry.getKey();
            Holder<Item> result = entry.getValue().getItemHolder();
            ExtendedShapedRecipeBuilder.shapedRecipe(result, 3)
                  .pattern(PLASTIC_FENCE)
                  .key(AdditionsRecipeProvider.PLASTIC_ROD_CHAR, this.items, MekanismTags.Items.RODS_PLASTIC)
                  .key(Pattern.CONSTANT, AdditionsBlocks.PLASTIC_BLOCKS.get(color))
                  .category(RecipeCategory.DECORATIONS)
                  .save(consumer, MekanismAdditions.rl(basePath + color.getRegistryPrefix()));
            PlasticBlockRecipeProvider.registerRecolor(consumer, this.items, result, fencesTag, color, basePath);
        }
    }

    private void registerPlasticFenceGates(RecipeOutput consumer, String basePath) {
        basePath += "fence_gate/";
        HolderSet<Item> gatesTag = this.items.getOrThrow(AdditionsTags.Items.FENCE_GATES_PLASTIC_NORMAL);
        for (Map.Entry<EnumColor, ? extends BlockRegistryObject<BlockPlasticFenceGate, ?>> entry : AdditionsBlocks.PLASTIC_FENCE_GATES.entrySet()) {
            EnumColor color = entry.getKey();
            Holder<Item> result = entry.getValue().getItemHolder();
            ExtendedShapedRecipeBuilder.shapedRecipe(result)
                  .pattern(PLASTIC_FENCE_GATE)
                  .key(AdditionsRecipeProvider.PLASTIC_ROD_CHAR, this.items, MekanismTags.Items.RODS_PLASTIC)
                  .key(Pattern.CONSTANT, AdditionsBlocks.PLASTIC_BLOCKS.get(color))
                  .category(RecipeCategory.REDSTONE)
                  .save(consumer, MekanismAdditions.rl(basePath + color.getRegistryPrefix()));
            PlasticBlockRecipeProvider.registerRecolor(consumer, this.items, result, gatesTag, color, basePath);
        }
    }
}