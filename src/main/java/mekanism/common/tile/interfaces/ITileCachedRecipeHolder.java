package mekanism.common.tile.interfaces;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.ICachedRecipeHolder;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface ITileCachedRecipeHolder<RECIPE extends MekanismRecipe> extends ICachedRecipeHolder<RECIPE> {

    //Note: This is not called getWorld() as we have to implement this separately in the different TEs
    // and otherwise it won't be found as the other getWorld will be reobfuscated making this one not be
    // implemented. This implementation allows for wrapping around that getWorld properly in this case.
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

    @Override
    default boolean invalidateCache() {
        return CommonWorldTickHandler.flushTagAndRecipeCaches;
    }
}