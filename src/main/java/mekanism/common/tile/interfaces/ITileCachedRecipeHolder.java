package mekanism.common.tile.interfaces;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.ICachedRecipeHolder;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.world.World;

public interface ITileCachedRecipeHolder<RECIPE extends MekanismRecipe> extends ICachedRecipeHolder<RECIPE> {

    @Nullable
    World getWorld();

    @Nonnull
    MekanismRecipeType<RECIPE> getRecipeType();

    default boolean containsRecipe(@Nonnull Predicate<RECIPE> matchCriteria) {
        return getRecipeType().contains(getWorld(), matchCriteria);
    }

    @Nullable
    default RECIPE findFirstRecipe(@Nonnull Predicate<RECIPE> matchCriteria) {
        return getRecipeType().findFirst(getWorld(), matchCriteria);
    }
}