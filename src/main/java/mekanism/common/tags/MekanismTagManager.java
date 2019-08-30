package mekanism.common.tags;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasTags;
import net.minecraft.network.PacketBuffer;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

//TODO: Sync this
public class MekanismTagManager implements IFutureReloadListener {

    private final ForgeRegistryTagCollection<Gas> gases = new ForgeRegistryTagCollection<>(MekanismAPI.GAS_REGISTRY, "tags/gases", "gas");

    public ForgeRegistryTagCollection<Gas> getGases() {
        return this.gases;
    }

    public void write(PacketBuffer buffer) {
        this.gases.write(buffer);
    }

    public static MekanismTagManager read(PacketBuffer buffer) {
        MekanismTagManager tagManager = new MekanismTagManager();
        tagManager.getGases().read(buffer);
        return tagManager;
    }

    @Nonnull
    @Override
    public CompletableFuture<Void> reload(IFutureReloadListener.IStage stage, @Nonnull IResourceManager resourceManager, @Nonnull IProfiler preparationsProfiler,
          @Nonnull IProfiler reloadProfiler, @Nonnull Executor backgroundExecutor, @Nonnull Executor gameExecutor) {
        return this.gases.reload(resourceManager, backgroundExecutor).thenApply(ReloadResults::new).thenCompose(stage::markCompleteAwaitingOthers)
              .thenAcceptAsync(reloadResults -> {
                  this.gases.registerAll(reloadResults.gases);
                  GasTags.setCollection(this.gases);
              }, gameExecutor);
    }

    public static class ReloadResults {

        final Map<ResourceLocation, Tag.Builder<Gas>> gases;

        public ReloadResults(Map<ResourceLocation, Tag.Builder<Gas>> gases) {
            this.gases = gases;
        }
    }
}