package mekanism.common.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.recipe.upgrade.MekanismShapedRecipe;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

@NothingNullByDefault
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
    protected Recipe<?> wrapRecipe(ShapedRecipe recipe) {
        return new MekanismShapedRecipe(recipe);
    }
}