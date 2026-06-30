package mekanism.client.render.transmitter;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.Arrays;
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
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.NeoForgeModelProperties;
import net.neoforged.neoforge.client.model.obj.ObjGeometry;
import net.neoforged.neoforge.client.model.obj.ObjLoader;
import org.jspecify.annotations.Nullable;

public class TransmitterContentsManager {

    private static final Identifier MODEL_LOCATION = Mekanism.rl("models/transmitter_contents.obj");
    @Nullable
    private static TransmitterContentsManager INSTANCE = null;

    public static TransmitterContentsManager get() {
        return Objects.requireNonNull(INSTANCE, "Not initialized");
    }

    @SubscribeEvent
    private static void bakingDone(ModelEvent.BakingCompleted e) {
        //TODO - 26.2: Params
        ObjGeometry geometry = ObjLoader.INSTANCE.loadGeometry(new ObjGeometry.Settings(MODEL_LOCATION, true, false, true, true, null));
        INSTANCE = new TransmitterContentsManager(e.getModelBakery(), e.getBakingResult().missingModels(), geometry);
    }

    private final Table<Identifier, @Nullable ConnectionType[], List<BlockStateModelPart>> bakedCache = HashBasedTable.create();
    private final BlockStateModelPart missingModelPart;
    private final List<BlockStateModelPart> bakedMissingModel;
    private final ObjGeometry resolved;
    private final ModelDebugName debugName;
    private final ModelBaker modelBaker;

    private TransmitterContentsManager(ModelBakery bakery, ModelBakery.MissingModels missingModels, ObjGeometry resolved) {
        this.resolved = resolved;
        this.debugName = this.resolved.modelLocation::toDebugFileName;
        this.missingModelPart = missingModels.blockPart();
        this.bakedMissingModel = Collections.singletonList(missingModelPart);
        this.modelBaker = bakery.new ModelBakerImpl(new TransmitterLateMaterialBaker(), new ModelBakery.InternerImpl(), missingModels);
    }

    public List<BlockStateModelPart> getBaked(@Nullable ConnectionType[] connectionTypes, Identifier texture) {
        if (Arrays.stream(connectionTypes).allMatch(Objects::isNull)) {
            return Collections.emptyList();
        }
        List<BlockStateModelPart> cached = bakedCache.get(texture, connectionTypes);
        if (cached == null) {
            Map<String, Boolean> connections = new HashMap<>(EnumUtils.DIRECTIONS.length);
            for (Direction side : EnumUtils.DIRECTIONS) {
                String sideName = side.getSerializedName();
                ConnectionType connectionType = connectionTypes[side.ordinal()];
                for (ConnectionType value : ConnectionType.values()) {
                    connections.put(sideName + value.name(), value == connectionType);
                }
            }
            cached = bake(texture, new ContextMap.Builder()
                  .withParameter(NeoForgeModelProperties.PART_VISIBILITY, connections)
                  .create(ContextKeySet.EMPTY)
            );
            bakedCache.put(texture, connectionTypes, cached);
        }
        return cached;
    }

    private List<BlockStateModelPart> bake(Identifier texture, ContextMap sideContext) {
        try {
            QuadCollection quadCollection = resolved.bake(makeTextureSlots(texture), modelBaker, BlockModelRotation.IDENTITY, debugName, sideContext);
            //TODO - 26.2: Params
            BlockStateModelPart bakedModel = new SimpleModelWrapper(
                  quadCollection,
                  true,//resolved.getTopAmbientOcclusion(),
                  missingModelPart.particleMaterial()//we don't intend to use this, so no point resolving it
            );
            return Collections.singletonList(bakedModel);
        } catch (Exception e) {
            Mekanism.logger.error("Unable to bake Transmitter Contents model due to exception", e);
            return bakedMissingModel;
        }
    }

    private TextureSlots makeTextureSlots(Identifier activeTexture) {
        TextureSlots.Resolver resolver = new TextureSlots.Resolver();
        resolver.addLast(new TextureSlots.Data.Builder()
              .addTexture("contents", new Material(activeTexture))
              .build()
        );
        return resolver.resolve(debugName);
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
}