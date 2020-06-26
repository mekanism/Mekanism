package mekanism.common.tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketMekanismTags;
import net.minecraft.network.PacketBuffer;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MekanismTagManager implements IFutureReloadListener {

    private final List<ForgeRegistryTagCollection<?>> tagCollections = new ArrayList<>();

    public MekanismTagManager() {
        for (ManagedTagType<?> managedType : ManagedTagType.getManagedTypes()) {
            tagCollections.add(managedType.getTagCollection());
        }
    }

    @Override
    public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler,
          Executor backgroundExecutor, Executor gameExecutor) {
        CompletableFuture<List<TagInfo<?>>> reloadResults = CompletableFuture.completedFuture(new ArrayList<>());
        for (ForgeRegistryTagCollection<?> tagCollection : tagCollections) {
            reloadResults = combine(reloadResults, tagCollection, resourceManager, backgroundExecutor);
        }
        return reloadResults.thenCompose(stage::markCompleteAwaitingOthers).thenAcceptAsync(results -> {
            results.forEach(TagInfo::registerAndSet);
            Mekanism.packetHandler.sendToAll(new PacketMekanismTags(Mekanism.instance.getTagManager()));
        }, gameExecutor);
    }

    private <T extends IForgeRegistryEntry<T>> CompletableFuture<List<TagInfo<?>>> combine(CompletableFuture<List<TagInfo<?>>> reloadResults,
          ForgeRegistryTagCollection<T> tagCollection, IResourceManager resourceManager, Executor backgroundExecutor) {
        return reloadResults.thenCombine(tagCollection.reload(resourceManager, backgroundExecutor), (results, result) -> {
            results.add(new TagInfo<>(tagCollection, result));
            return results;
        });
    }

    public void setCollections() {
        tagCollections.forEach(ForgeRegistryTagCollection::setCollection);
    }

    public void write(PacketBuffer buffer) {
        tagCollections.forEach(tagCollection -> tagCollection.write(buffer));
    }

    public static MekanismTagManager read(PacketBuffer buffer) {
        MekanismTagManager tagManager = new MekanismTagManager();
        tagManager.tagCollections.forEach(tagCollection -> tagCollection.read(buffer));
        return tagManager;
    }

    private static class TagInfo<T extends IForgeRegistryEntry<T>> {

        private final ForgeRegistryTagCollection<T> tagCollection;
        private final Map<ResourceLocation, Tag.Builder> results;

        private TagInfo(ForgeRegistryTagCollection<T> tagCollection, Map<ResourceLocation, Tag.Builder> result) {
            this.tagCollection = tagCollection;
            this.results = result;
        }

        private void registerAndSet() {
            tagCollection.registerAll(results);
            tagCollection.setCollection();
        }
    }
}