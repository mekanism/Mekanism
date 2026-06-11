package mekanism.common.recipe.builder;

import mekanism.common.recipe.upgrade.MekanismShapedRecipe;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

public class MekDataShapedRecipeBuilder extends ExtendedShapedRecipeBuilder {

    private MekDataShapedRecipeBuilder(Holder<Item> result, int count) {
        super(result, count);
    }

    public static MekDataShapedRecipeBuilder shapedRecipe(BlockRegistryObject<?, ?> result) {
        return shapedRecipe(result, 1);
    }

    public static MekDataShapedRecipeBuilder shapedRecipe(BlockRegistryObject<?, ?> result, int count) {
        return shapedRecipe(result.getItemHolder(), count);
    }

    public static MekDataShapedRecipeBuilder shapedRecipe(Holder<Item> result) {
        return shapedRecipe(result, 1);
    }

    public static MekDataShapedRecipeBuilder shapedRecipe(Holder<Item> result, int count) {
        return new MekDataShapedRecipeBuilder(result, count);
    }

    @Override
    protected Recipe<?> wrapRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, ItemStackTemplate result) {
        return new MekanismShapedRecipe(commonInfo, bookInfo, pattern, result);
    }
}