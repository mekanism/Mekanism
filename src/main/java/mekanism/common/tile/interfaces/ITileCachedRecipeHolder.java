package mekanism.common.tile.interfaces;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.ICachedRecipeHolder;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface ITileCachedRecipeHolder<RECIPE extends MekanismRecipe> extends ICachedRecipeHolder<RECIPE> {

    @Nullable
    default World getTileWorld() {
        if (this instanceof TileEntity) {
            return ((TileEntity) this).getWorld();
        }
        return null;
    }

    @Nonnull
    MekanismRecipeType<RECIPE> getRecipeType();

    default boolean containsRecipe(@Nonnull Predicate<RECIPE> matchCriteria) {
        return getRecipeType().contains(getTileWorld(), matchCriteria);
    }

    @Nullable
    default RECIPE findFirstRecipe(@Nonnull Predicate<RECIPE> matchCriteria) {
        return getRecipeType().findFirst(getTileWorld(), matchCriteria);
    }
}