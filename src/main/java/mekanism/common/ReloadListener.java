package mekanism.common;

import javax.annotation.Nonnull;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class ReloadListener implements ResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {
        CommonWorldTickHandler.flushTagAndRecipeCaches = true;
        MekanismRecipeType.clearCache();
    }
}