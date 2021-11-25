package mekanism.common;

import javax.annotation.Nonnull;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;

public class ReloadListener implements IResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {
        CommonWorldTickHandler.flushTagAndRecipeCaches = true;
        MekanismRecipeType.clearCache();
    }
}