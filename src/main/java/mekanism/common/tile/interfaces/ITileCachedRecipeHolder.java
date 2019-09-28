package mekanism.common.tile.interfaces;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.ICachedRecipeHolder;
import mekanism.common.recipe.RecipeHandler.RecipeWrapper;
import net.minecraft.world.World;

public interface ITileCachedRecipeHolder<RECIPE extends MekanismRecipe> extends ICachedRecipeHolder<RECIPE> {

    @Nullable
    World getWorld();

    @Nonnull
    RecipeWrapper<RECIPE> getRecipeWrapper();

    default boolean containsRecipe(@Nonnull Predicate<RECIPE> matchCriteria) {
        return getRecipeWrapper().contains(getWorld(), matchCriteria);
    }

    @Nullable
    default RECIPE findFirstRecipe(@Nonnull Predicate<RECIPE> matchCriteria) {
        return getRecipeWrapper().findFirst(getWorld(), matchCriteria);
    }
}