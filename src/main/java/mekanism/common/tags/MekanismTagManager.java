package mekanism.common.tags;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasTags;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfuseTypeTags;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketMekanismTags;
import net.minecraft.network.PacketBuffer;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.tags.Tag.Builder;
import net.minecraft.util.ResourceLocation;

public class MekanismTagManager implements IFutureReloadListener {

    private final ForgeRegistryTagCollection<Gas> gases = new ForgeRegistryTagCollection<>(MekanismAPI.GAS_REGISTRY, "tags/gases", "gas");
    private final ForgeRegistryTagCollection<InfuseType> infuseTypes = new ForgeRegistryTagCollection<>(MekanismAPI.INFUSE_TYPE_REGISTRY,
          "tags/infuse_types", "infuse_type");

    public ForgeRegistryTagCollection<Gas> getGases() {
        return this.gases;
    }

    public ForgeRegistryTagCollection<InfuseType> getInfuseTypes() {
        return this.infuseTypes;
    }

    public void write(PacketBuffer buffer) {
        this.gases.write(buffer);
        this.infuseTypes.write(buffer);
    }

    public static MekanismTagManager read(PacketBuffer buffer) {
        MekanismTagManager tagManager = new MekanismTagManager();
        tagManager.getGases().read(buffer);
        tagManager.getInfuseTypes().read(buffer);
        return tagManager;
    }

    @Nonnull
    @Override
    public CompletableFuture<Void> reload(@Nonnull IStage stage, @Nonnull IResourceManager resourceManager, @Nonnull IProfiler preparationsProfiler,
          @Nonnull IProfiler reloadProfiler, @Nonnull Executor backgroundExecutor, @Nonnull Executor gameExecutor) {
        //TODO: Support handling more of these
        CompletableFuture<Map<ResourceLocation, Builder<Gas>>> gasReload = this.gases.reload(resourceManager, backgroundExecutor);
        CompletableFuture<Map<ResourceLocation, Builder<InfuseType>>> infuseTypeReload = this.infuseTypes.reload(resourceManager, backgroundExecutor);
        return gasReload.thenCombine(infuseTypeReload, ReloadResults::new).thenCompose(stage::markCompleteAwaitingOthers)
              .thenAcceptAsync(reloadResults -> {
                  this.gases.registerAll(reloadResults.gases);
                  this.infuseTypes.registerAll(reloadResults.infuseTypes);
                  GasTags.setCollection(this.gases);
                  InfuseTypeTags.setCollection(this.infuseTypes);
                  //TODO: Double check this is correct
                  Mekanism.packetHandler.sendToAll(new PacketMekanismTags(Mekanism.instance.getTagManager()));
              }, gameExecutor);
    }

    public static class ReloadResults {

        final Map<ResourceLocation, Tag.Builder<Gas>> gases;
        final Map<ResourceLocation, Tag.Builder<InfuseType>> infuseTypes;

        public ReloadResults(Map<ResourceLocation, Tag.Builder<Gas>> gases, Map<ResourceLocation, Tag.Builder<InfuseType>> infuseTypes) {
            this.gases = gases;
            this.infuseTypes = infuseTypes;
        }
    }
}