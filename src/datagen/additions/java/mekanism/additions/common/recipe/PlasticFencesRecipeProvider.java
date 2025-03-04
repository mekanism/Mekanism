package mekanism.additions.common.recipe;

import java.util.Map;
import mekanism.additions.common.AdditionsTags;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.block.plastic.BlockPlasticFence;
import mekanism.additions.common.block.plastic.BlockPlasticFenceGate;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.api.text.EnumColor;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;

public class PlasticFencesRecipeProvider implements ISubRecipeProvider {

    private static final RecipePattern PLASTIC_FENCE = RecipePattern.createPattern(
          TripleLine.of(Pattern.CONSTANT, AdditionsRecipeProvider.PLASTIC_ROD_CHAR, Pattern.CONSTANT),
          TripleLine.of(Pattern.CONSTANT, AdditionsRecipeProvider.PLASTIC_ROD_CHAR, Pattern.CONSTANT));
    private static final RecipePattern PLASTIC_FENCE_GATE = RecipePattern.createPattern(
          TripleLine.of(AdditionsRecipeProvider.PLASTIC_ROD_CHAR, Pattern.CONSTANT, AdditionsRecipeProvider.PLASTIC_ROD_CHAR),
          TripleLine.of(AdditionsRecipeProvider.PLASTIC_ROD_CHAR, Pattern.CONSTANT, AdditionsRecipeProvider.PLASTIC_ROD_CHAR));

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "plastic/";
        registerPlasticFences(consumer, basePath);
        registerPlasticFenceGates(consumer, basePath);
    }

    private void registerPlasticFences(RecipeOutput consumer, String basePath) {
        basePath += "fence/";
        for (Map.Entry<EnumColor, ? extends BlockRegistryObject<BlockPlasticFence, ?>> entry : AdditionsBlocks.PLASTIC_FENCES.entrySet()) {
            EnumColor color = entry.getKey();
            registerPlasticFence(consumer, color, entry.getValue().getItemHolder(), AdditionsBlocks.PLASTIC_BLOCKS.get(color).getItemHolder(), basePath);
        }
    }

    private void registerPlasticFence(RecipeOutput consumer, EnumColor color, Holder<Item> result, Holder<Item> plastic, String basePath) {
        ExtendedShapedRecipeBuilder.shapedRecipe(result, 3)
              .pattern(PLASTIC_FENCE)
              .key(AdditionsRecipeProvider.PLASTIC_ROD_CHAR, MekanismTags.Items.RODS_PLASTIC)
              .key(Pattern.CONSTANT, plastic)
              .category(RecipeCategory.DECORATIONS)
              .build(consumer, MekanismAdditions.rl(basePath + color.getRegistryPrefix()));
        PlasticBlockRecipeProvider.registerRecolor(consumer, result, AdditionsTags.Items.FENCES_PLASTIC_NORMAL, color, basePath);
    }

    private void registerPlasticFenceGates(RecipeOutput consumer, String basePath) {
        basePath += "fence_gate/";
        for (Map.Entry<EnumColor, ? extends BlockRegistryObject<BlockPlasticFenceGate, ?>> entry : AdditionsBlocks.PLASTIC_FENCE_GATES.entrySet()) {
            EnumColor color = entry.getKey();
            registerPlasticFenceGate(consumer, color, entry.getValue().getItemHolder(), AdditionsBlocks.PLASTIC_BLOCKS.get(color).getItemHolder(), basePath);
        }
    }

    private void registerPlasticFenceGate(RecipeOutput consumer, EnumColor color, Holder<Item> result, Holder<Item> plastic, String basePath) {
        ExtendedShapedRecipeBuilder.shapedRecipe(result)
              .pattern(PLASTIC_FENCE_GATE)
              .key(AdditionsRecipeProvider.PLASTIC_ROD_CHAR, MekanismTags.Items.RODS_PLASTIC)
              .key(Pattern.CONSTANT, plastic)
              .category(RecipeCategory.REDSTONE)
              .build(consumer, MekanismAdditions.rl(basePath + color.getRegistryPrefix()));
        PlasticBlockRecipeProvider.registerRecolor(consumer, result, AdditionsTags.Items.FENCE_GATES_PLASTIC_NORMAL, color, basePath);
    }
}