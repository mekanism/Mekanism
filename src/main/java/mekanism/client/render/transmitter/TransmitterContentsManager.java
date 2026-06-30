package mekanism.client.render.transmitter;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import mekanism.common.Mekanism;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.NeoForgeModelProperties;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jspecify.annotations.Nullable;

public class TransmitterContentsManager {

    //TODO - 26.2: Evaluate if we want to change any of the properties defined in the model
    private static final Identifier MODEL_LOCATION = Mekanism.rl("transmitter_contents");
    @Nullable
    private static TransmitterContentsManager INSTANCE = null;

    public static TransmitterContentsManager get() {
        return Objects.requireNonNull(INSTANCE, "Not initialized");
    }

    @SubscribeEvent
    private static void bakingDone(ModelEvent.BakingCompleted e) {
        INSTANCE = new TransmitterContentsManager(e.getModelBakery(), e.getBakingResult().missingModels());
    }

    @SubscribeEvent
    private static void registerFakeStandalone(ModelEvent.RegisterStandalone event) {
        event.register(new StandaloneModelKey<>(MODEL_LOCATION::toDebugFileName), new SimpleUnbakedStandaloneModel<>(MODEL_LOCATION, (_, _, _) -> Unit.INSTANCE));
    }

    private final Table<Identifier, Integer, List<BlockStateModelPart>> bakedCache = HashBasedTable.create();
    private final BlockStateModelPart missingModelPart;
    private final List<BlockStateModelPart> bakedMissingModel;
    private final ResolvedModel resolved;
    private final ModelBaker modelBaker;

    private TransmitterContentsManager(ModelBakery bakery, ModelBakery.MissingModels missingModels) {
        this.resolved = Objects.requireNonNull(bakery.resolvedModels.get(MODEL_LOCATION));
        this.missingModelPart = missingModels.blockPart();
        this.bakedMissingModel = Collections.singletonList(missingModelPart);
        this.modelBaker = bakery.new ModelBakerImpl(new TransmitterLateMaterialBaker(), new ModelBakery.InternerImpl(), missingModels);
    }

    public List<BlockStateModelPart> getBaked(@Nullable ConnectionType[] connectionTypes, Identifier texture) {
        int key = CacheKey.pack(connectionTypes);
        List<BlockStateModelPart> cached = bakedCache.get(texture, key);
        if (cached == null) {
            Map<String, Boolean> connections = new HashMap<>(EnumUtils.DIRECTIONS.length);
            boolean hasNonNull = false;
            for (Direction side : EnumUtils.DIRECTIONS) {
                String sideName = side.getSerializedName();
                ConnectionType connectionType = connectionTypes[side.ordinal()];
                if (connectionType != null) {
                    hasNonNull = true;
                }
                for (ConnectionType value : ConnectionType.values()) {
                    connections.put(sideName + value.name(), value == connectionType);
                }
            }
            if (hasNonNull) {
                cached = bake(texture, new ContextMap.Builder()
                      .withParameter(NeoForgeModelProperties.PART_VISIBILITY, connections)
                      .create(ContextKeySet.EMPTY)
                );
            } else {
                cached = Collections.emptyList();
            }
            bakedCache.put(texture, key, cached);
        }
        return cached;
    }

    private List<BlockStateModelPart> bake(Identifier texture, ContextMap sideContext) {
        try {
            QuadCollection quadCollection = resolved.getTopGeometry().bake(makeTextureSlots(texture), modelBaker, BlockModelRotation.IDENTITY, resolved, sideContext);
            //we don't intend to use the particle, so no point resolving it
            BlockStateModelPart bakedModel = new SimpleModelWrapper(quadCollection, resolved.getTopAmbientOcclusion(), missingModelPart.particleMaterial());
            return Collections.singletonList(bakedModel);
        } catch (Exception e) {
            Mekanism.logger.error("Unable to bake Transmitter Contents model due to exception", e);
            return bakedMissingModel;
        }
    }

    /// from [ResolvedModel#findTopTextureSlots(ResolvedModel)]
    private TextureSlots makeTextureSlots(Identifier texture) {
        ResolvedModel current = resolved;
        TextureSlots.Resolver resolver;
        for (resolver = new TextureSlots.Resolver(); current != null; current = current.parent()) {
            resolver.addLast(current.wrapped().textureSlots());
        }
        resolver.addLast(new TextureSlots.Data.Builder()
              .addTexture("contents", new Material(texture))
              .build()
        );
        return resolver.resolve(resolved);
    }

    private static class TransmitterLateMaterialBaker extends MaterialBaker {

        private static TextureAtlas getAtlas() {
            return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        }

        public TransmitterLateMaterialBaker() {
            super(getAtlas().missingSprite());
        }

        @Override
        public Material.@Nullable Baked bake(Material material) {
            TextureAtlas atlas = getAtlas();
            TextureAtlasSprite sprite = atlas.getSprite(material.sprite());
            if (sprite == atlas.missingSprite()) {
                Mekanism.logger.error("Missing sprite: {}", material.sprite());
                return replacementForMissingMaterial(material);
            }
            return new Material.Baked(sprite, material.forceTranslucent());
        }
    }

    private static class CacheKey {

        private static final int BITS_PER_STATE = 4; //nb: only 3 used (values 0-5)
        private static final int INDEX_MASK = 0xF;

        static int pack(@Nullable ConnectionType[] types) {
            int key = 0;
            for (int i = 0; i < types.length; i++) {
                ConnectionType type = types[i];
                int index = type == null ? 0 : type.ordinal() + 1;
                key |= (index & INDEX_MASK) << (BITS_PER_STATE * i);
            }
            return key;
        }
    }
}