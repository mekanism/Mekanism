package mekanism.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;

public class ReloadListener implements IFutureReloadListener {

    @Nonnull
    @Override
    public CompletableFuture<Void> reload(@Nonnull IStage stage, @Nonnull IResourceManager resourceManager, @Nonnull IProfiler preparationsProfiler,
          @Nonnull IProfiler reloadProfiler, @Nonnull Executor backgroundExecutor, @Nonnull Executor gameExecutor) {
        return CompletableFuture.runAsync(() -> {
            CommonWorldTickHandler.flushTagAndRecipeCaches = true;
            MekanismRecipeType.clearCache();
        }, gameExecutor).thenCompose(stage::wait);
    }
}