package mekanism.client.model.energycube;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import mekanism.api.RelativeSide;
import mekanism.client.model.baked.ExtensionBakedModel.QuadsKey;
import mekanism.client.model.energycube.EnergyCubeGeometry.FaceData;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityEnergyCube.CubeSideState;
import mekanism.common.util.EnumUtils;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyCubeBakedModel implements IDynamicBakedModel {

    private static final CubeSideState[] INACTIVE = Util.make(new CubeSideState[EnumUtils.DIRECTIONS.length], sideStates -> Arrays.fill(sideStates, CubeSideState.INACTIVE));
    private static final QuadTransformation LED_TRANSFORMS = QuadTransformation.list(QuadTransformation.fullbright, QuadTransformation.uvShift(-0.125F, 0));
    private static final BiPredicate<CubeSideState[], CubeSideState[]> DATA_EQUALITY_CHECK = Arrays::equals;

    private final LoadingCache<QuadsKey<CubeSideState[]>, List<BakedQuad>> cache = CacheBuilder.newBuilder().build(new CacheLoader<>() {
        @NotNull
        @Override
        public List<BakedQuad> load(@NotNull QuadsKey<CubeSideState[]> key) {
            return createQuads(key);
        }
    });

    private final FaceData frame;
    private final Map<RelativeSide, FaceData> leds;
    private final Map<RelativeSide, FaceData> activeLEDs;
    private final Map<RelativeSide, FaceData> ports;
    private final Map<RelativeSide, FaceData> activePorts;
    private final ChunkRenderTypeSet blockRenderTypes;
    private final List<RenderType> itemRenderTypes;
    private final List<RenderType> fabulousItemRenderTypes;
    private final boolean isAmbientOcclusion;
    private final boolean usesBlockLight;
    private final boolean isGui3d;
    private final TextureAtlasSprite particle;
    private final ItemOverrides overrides;
    private final ItemTransforms transforms;

    EnergyCubeBakedModel(boolean useAmbientOcclusion, boolean usesBlockLight, boolean isGui3d, ItemTransforms transforms, ItemOverrides overrides,
          TextureAtlasSprite particle, FaceData frame, Map<RelativeSide, FaceData> leds, Map<RelativeSide, FaceData> ports, RenderTypeGroup renderTypes) {
        this.isAmbientOcclusion = useAmbientOcclusion;
        this.usesBlockLight = usesBlockLight;
        this.isGui3d = isGui3d;
        this.overrides = overrides;
        this.transforms = transforms;
        this.particle = particle;
        this.frame = frame;
        this.leds = leds;
        this.ports = ports;
        this.activeLEDs = new EnumMap<>(RelativeSide.class);
        this.activePorts = new EnumMap<>(RelativeSide.class);
        //Note: We don't bother having any form of lazy transformations take place here as this should only have a memory
        // impact equivalent to having two models: one with the leds and ports off, and one with all of them active
        for (Map.Entry<RelativeSide, FaceData> entry : this.leds.entrySet()) {
            activeLEDs.put(entry.getKey(), entry.getValue().transform(LED_TRANSFORMS));
        }
        for (Map.Entry<RelativeSide, FaceData> entry : this.ports.entrySet()) {
            activePorts.put(entry.getKey(), entry.getValue().transform(QuadTransformation.filtered_fullbright));
        }
        if (renderTypes.isEmpty()) {
            this.blockRenderTypes = null;
            this.itemRenderTypes = null;
            this.fabulousItemRenderTypes = null;
        } else {
            this.blockRenderTypes = ChunkRenderTypeSet.of(renderTypes.block());
            this.itemRenderTypes = Collections.singletonList(renderTypes.entity());
            this.fabulousItemRenderTypes = Collections.singletonList(renderTypes.entityFabulous());
        }
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data,
          @Nullable RenderType renderType) {
        CubeSideState[] sideStates = data.get(TileEntityEnergyCube.SIDE_STATE_PROPERTY);
        if (sideStates == null || sideStates.length != EnumUtils.SIDES.length) {
            //If there is no side data then treat everything as inactive
            sideStates = INACTIVE;
        }
        //Note: We intentionally ignore the state and use null here to minimize cache size as it doesn't actually matter
        // or get used for energy cube models
        QuadsKey<CubeSideState[]> key = new QuadsKey<>(null, side, rand, renderType, frame.getFaces(side));
        key.data(sideStates, Arrays.hashCode(sideStates), DATA_EQUALITY_CHECK);
        return cache.getUnchecked(key);
    }

    private List<BakedQuad> createQuads(QuadsKey<CubeSideState[]> key) {
        Direction side = key.getSide();
        CubeSideState[] data = Objects.requireNonNull(key.getData());
        //Make the list of quads mutable so that we can add the proper extra portions to it
        List<BakedQuad> quads = new ArrayList<>(key.getQuads());
        for (int i = 0; i < EnumUtils.SIDES.length; i++) {
            RelativeSide dir = EnumUtils.SIDES[i];
            CubeSideState sideState = data[i];
            if (sideState == CubeSideState.ACTIVE_LIT) {
                quads.addAll(activeLEDs.get(dir).getFaces(side));
                quads.addAll(activePorts.get(dir).getFaces(side));
            } else {
                quads.addAll(leds.get(dir).getFaces(side));
                if (sideState == CubeSideState.ACTIVE_UNLIT) {
                    quads.addAll(ports.get(dir).getFaces(side));
                }
            }
        }
        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return isAmbientOcclusion;
    }

    @Override
    public boolean isGui3d() {
        return isGui3d;
    }

    @Override
    public boolean usesBlockLight() {
        return usesBlockLight;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @NotNull
    @Override
    @Deprecated
    public TextureAtlasSprite getParticleIcon() {
        return particle;
    }

    @NotNull
    @Override
    @Deprecated
    public ItemOverrides getOverrides() {
        return overrides;
    }

    @NotNull
    @Override
    @Deprecated
    public ItemTransforms getTransforms() {
        return transforms;
    }

    @NotNull
    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return blockRenderTypes == null ? IDynamicBakedModel.super.getRenderTypes(state, rand, data) : blockRenderTypes;
    }

    @NotNull
    @Override
    public List<RenderType> getRenderTypes(@NotNull ItemStack stack, boolean fabulous) {
        if (fabulous) {
            if (fabulousItemRenderTypes != null) {
                return fabulousItemRenderTypes;
            }
        } else if (itemRenderTypes != null) {
            return itemRenderTypes;
        }
        return IDynamicBakedModel.super.getRenderTypes(stack, fabulous);
    }
}