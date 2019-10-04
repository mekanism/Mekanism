package mekanism.common.recipe;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketClearRecipeCache;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;

public class RecipeCacheManager implements IFutureReloadListener {

    @Nonnull
    @Override
    public CompletableFuture<Void> reload(@Nonnull IStage stage, @Nonnull IResourceManager resourceManager, @Nonnull IProfiler preparationsProfiler,
          @Nonnull IProfiler reloadProfiler, @Nonnull Executor backgroundExecutor, @Nonnull Executor gameExecutor) {
        //TODO: Invalidate the cached recipes stored in the machines
        return CompletableFuture.runAsync(() -> {
            MekanismRecipeType.clearCache();
            Mekanism.packetHandler.sendToAll(new PacketClearRecipeCache());
        }, gameExecutor).thenCompose(stage::markCompleteAwaitingOthers);
    }
}