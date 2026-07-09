package mekanism.client.render.transmitter;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import mekanism.client.ModelUtil;
import mekanism.common.Mekanism;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jspecify.annotations.Nullable;

public class TransmitterContentsManager {

    //TODO - 26.2: Evaluate if we want to change any of the properties defined in the model
    private static final Identifier MODEL_LOCATION = Mekanism.rl("transmitter_contents");
    @Nullable
    private static TransmitterContentsManager INSTANCE = null;
    private static final StandaloneModelKey<TransmitterContentsManager> STANDALONE_MODEL_KEY = new StandaloneModelKey<>(MODEL_LOCATION::toDebugFileName);

    public static TransmitterContentsManager get() {
        return Objects.requireNonNull(INSTANCE, "Not initialized");
    }

    @SubscribeEvent
    private static void bakingDone(ModelEvent.BakingCompleted e) {
        INSTANCE = e.getBakingResult().standaloneModels().get(STANDALONE_MODEL_KEY);
    }

    @SubscribeEvent
    private static void registerStandalone(ModelEvent.RegisterStandalone event) {
        event.register(STANDALONE_MODEL_KEY, new SimpleUnbakedStandaloneModel<>(MODEL_LOCATION, TransmitterContentsManager::new));
    }

    private final Table<Identifier, Integer, List<BlockStateModelPart>> bakedCache = HashBasedTable.create();
    private final BlockStateModelPart missingModelPart;
    private final List<BlockStateModelPart> bakedMissingModel;
    private final ResolvedModel resolved;
    private final ModelBaker modelBaker;

    private TransmitterContentsManager(ResolvedModel resolvedModel, ModelBaker modelBaker, ModelDebugName unused) {
        this.resolved = Objects.requireNonNull(resolvedModel);
        this.missingModelPart = modelBaker.missingBlockModelPart();
        this.bakedMissingModel = Collections.singletonList(this.missingModelPart);
        this.modelBaker = modelBaker;
    }

    public List<BlockStateModelPart> getBaked(@Nullable ConnectionType[] connectionTypes, Identifier texture) {
        int key = CacheKey.pack(connectionTypes);
        if (key == 0) {
            return Collections.emptyList();
        }
        List<BlockStateModelPart> cached = bakedCache.get(texture, key);
        if (cached == null) {
            Map<String, Boolean> connections = new HashMap<>(EnumUtils.DIRECTIONS.length);
            for (Direction side : EnumUtils.DIRECTIONS) {
                String sideName = side.getSerializedName();
                ConnectionType connectionType = connectionTypes[side.ordinal()];
                for (ConnectionType value : ConnectionType.values()) {
                    connections.put(sideName + value.name(), value == connectionType);
                }
            }
            cached = bake(texture, ModelUtil.partVisibility(resolved, connections));
            bakedCache.put(texture, key, cached);
        }
        return cached;
    }

    private List<BlockStateModelPart> bake(Identifier texture, ContextMap sideContext) {
        try {
            TextureSlots textureSlots = ModelUtil.makeTextureSlots(resolved, "contents", texture);
            QuadCollection quadCollection = resolved.getTopGeometry().bake(textureSlots, modelBaker, BlockModelRotation.IDENTITY, resolved, sideContext);
            //we don't intend to use the particle, so no point resolving it
            BlockStateModelPart bakedModel = new SimpleModelWrapper(quadCollection, resolved.getTopAmbientOcclusion(), missingModelPart.particleMaterial());
            return Collections.singletonList(bakedModel);
        } catch (Exception e) {
            Mekanism.logger.error("Unable to bake Transmitter Contents model due to exception", e);
            return bakedMissingModel;
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