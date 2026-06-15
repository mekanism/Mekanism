package mekanism.client.model.robit;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import mekanism.api.robit.RobitSkin;
import mekanism.client.RobitSpriteUploader;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismRobitSkins;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.context.ContextMap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.client.model.standalone.UnbakedStandaloneModel;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class RobitSkinManager {

    public static final Identifier BASE_ROBIT_MODEL = Mekanism.rl("robit/robit");
    @Nullable
    private static RobitSkinManager INSTANCE = null;
    private static final ModelGatherer GATHERER = new ModelGatherer();

    public static RobitSkinManager get() {
        return Objects.requireNonNull(INSTANCE, "Not initialized");
    }

    @SubscribeEvent
    private static void bakingDone(ModelEvent.BakingCompleted e) {
        INSTANCE = new RobitSkinManager(e.getModelBakery(), e.getBakingResult().missingModels());
    }

    @SubscribeEvent
    public static void registerReloader(AddClientReloadListenersEvent event) {
        event.addListener(Mekanism.rl("robit_model_gatherer"), GATHERER);
    }

    @SubscribeEvent
    public static void registerFakeStandalone(ModelEvent.RegisterStandalone event) {
        event.register(new StandaloneModelKey<>(()->"robit_model_bridge"), new FakeStandaloneModel());
    }

    private final Map<Identifier, ResolvedModel> resolvedModelMap;
    private final Table<Identifier, Identifier, BakeResult> bakedCache = HashBasedTable.create();
    private final BlockStateModelPart missingModelPart;
    private final BakeResult bakedMissingModel;
    private final ModelBaker modelBaker;

    private RobitSkinManager(ModelBakery bakery, ModelBakery.MissingModels missingModels) {
        this.resolvedModelMap = bakery.resolvedModels;
        missingModelPart = missingModels.blockPart();
        //TODO - 26.2: Validate this render sheet
        this.bakedMissingModel = new BakeResult(Collections.singletonList(missingModelPart), Sheets.cutoutBlockItemSheet());
        modelBaker = bakery.new ModelBakerImpl(new RobitLateMaterialBaker(), new ModelBakery.InternerImpl(), missingModels);
    }

    public BakeResult getMissing() {
        return bakedMissingModel;
    }

    public Vector3fc[] getExtents() {
        BakeResult defaultModel = getBaked(BASE_ROBIT_MODEL, MekanismRobitSkins.BASE_SKIN_TEXTURE);
        return CuboidItemModelWrapper.computeExtents(defaultModel.model.getFirst().getQuads(null));
    }

    public BakeResult getBaked(RobitSkin skin, @Nullable Identifier activeTexture) {
        Identifier model = Objects.requireNonNullElse(skin.customModel(), BASE_ROBIT_MODEL);
        return getBaked(model, activeTexture);
    }

    private BakeResult getBaked(Identifier model, @Nullable Identifier activeTexture) {
        BakeResult cached = bakedCache.get(model, activeTexture);
        if (cached != null) {
            return cached;
        }

        cached = bake(model, activeTexture);
        bakedCache.put(model, activeTexture, cached);
        return cached;
    }

    private BakeResult bake(Identifier skin, @Nullable Identifier activeTexture) {
        ResolvedModel resolved = resolvedModelMap.get(skin);
        if (resolved == null) {
            Mekanism.logger.error("Requested robit model not found: {}", skin);
            return bakedMissingModel;
        }
        try {
            //nb: can't use bakeTopGeometry as that is the vanilla-baked one
            QuadCollection quadCollection = resolved.getTopGeometry().bake(makeTextureSlots(resolved, activeTexture), modelBaker, BlockModelRotation.IDENTITY, resolved, ContextMap.EMPTY);
            BlockStateModelPart bakedModel = new SimpleModelWrapper(
                  quadCollection,
                  resolved.getTopAmbientOcclusion(),
                  missingModelPart.particleMaterial()//we don't intend to use this, so no point resolving it
            );
            return new BakeResult(Collections.singletonList(bakedModel), RobitSpriteUploader.RENDER_TYPE);
        } catch (Exception e) {
            Mekanism.logger.error("Unable to bake robit model {} due to exception", skin, e);
            return bakedMissingModel;
        }
    }

    /// from [ResolvedModel#findTopTextureSlots(ResolvedModel)]
    private static TextureSlots makeTextureSlots(ResolvedModel top, @Nullable Identifier activeTexture) {
        ResolvedModel current = top;

        TextureSlots.Resolver resolver;
        for (resolver = new TextureSlots.Resolver(); current != null; current = current.parent()) {
            resolver.addLast(current.wrapped().textureSlots());
        }

        if (activeTexture == null) {
            activeTexture = MekanismRobitSkins.BASE_SKIN_TEXTURE;
        }

        resolver.addLast(
              new TextureSlots.Data.Builder()
                    .addTexture("robit", new Material(activeTexture))
                    .build()
        );

        return resolver.resolve(top);
    }

    /// @param model      Model parts for submitting
    /// @param renderType Render type to use - the one for missing will be different, this lets the renderer not care
    public record BakeResult(List<BlockStateModelPart> model, RenderType renderType) {}

    public static class RobitLateMaterialBaker extends MaterialBaker {

        public RobitLateMaterialBaker() {
            super(RobitSpriteUploader.getAtlas().missingSprite());
        }

        @Override
        public Material.@Nullable Baked bake(Material material) {
            TextureAtlasSprite sprite = RobitSpriteUploader.getSprite(material.sprite());
            if (sprite == RobitSpriteUploader.getAtlas().missingSprite()) {
                Mekanism.logger.error("Missing sprite: {}", material.sprite());
                return replacementForMissingMaterial(material);
            }
            return new Material.Baked(sprite, material.forceTranslucent());
        }
    }

    /// Gathers a list of models in models/robit/ by doing what the model manager does, then filtering.
    /// Stored in a CompletableFuture so we can use it in the standalone model to force them to resolve
    private static class ModelGatherer implements PreparableReloadListener {
        @Nullable
        private static CompletableFuture<Set<Identifier>> robitModelIds = null;

        @Override
        public CompletableFuture<Void> reload(SharedState currentReload, Executor taskExecutor, PreparationBarrier preparationBarrier, Executor reloadExecutor) {
            ResourceManager manager = currentReload.resourceManager();
            return CompletableFuture.supplyAsync(() -> ModelManager.MODEL_LISTER.listMatchingResources(manager), taskExecutor)
            .thenAccept(
                resources -> {
                    Set<Identifier> robitModels = new HashSet<>();
                    for (Map.Entry<Identifier, Resource> resource : resources.entrySet()) {
                        Identifier modelId = ModelManager.MODEL_LISTER.fileToId(resource.getKey());
                        if (modelId.getPath().startsWith("robit/")) {
                            robitModels.add(modelId);
                        }
                    }
                    Objects.requireNonNull(robitModelIds, "prepareSharedState not called??").complete(robitModels);
                })
                  .thenCompose(preparationBarrier::wait)
                  .thenAccept(_->robitModelIds = null);//prevent use after the preparation barrier
        }

        @Override
        public void prepareSharedState(SharedState currentReload) {
            robitModelIds = new CompletableFuture<>();
        }
    }

    /// Exists to bridge the Robit models (in models/robit/) into resolved models
    private static class FakeStandaloneModel implements UnbakedStandaloneModel<Unit> {

        @Override
        public Unit bake(ModelBaker baker, ModelDebugName name) {
            return Unit.INSTANCE;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            CompletableFuture<Set<Identifier>> robitModelIds = ModelGatherer.robitModelIds;
            if (robitModelIds == null) {
                Mekanism.logger.error("ModelGatherer hasn't prepared yet??", new Exception());
                return;
            }
            try {
                Set<Identifier> identifiers = robitModelIds.get(5, TimeUnit.MINUTES);
                for (Identifier id : identifiers) {
                    resolver.markDependency(id);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}