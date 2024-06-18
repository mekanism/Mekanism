package mekanism.client.render.obj;

import appeng.block.networking.CableBusBlock;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.client.render.cablebus.FacadeBuilder;
import appeng.client.render.cablebus.FacadeRenderState;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.EnumMap;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.client.render.obj.TransmitterModelConfiguration.IconStatus;
import mekanism.common.Mekanism;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.SimpleModelState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.obj.ObjModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@NothingNullByDefault
public class TransmitterBakedModel extends BakedModelWrapper<BakedModel> {

    private static final ChunkRenderTypeSet CUTOUT = ChunkRenderTypeSet.of(RenderType.cutout());
    private static final ChunkRenderTypeSet FULL = ChunkRenderTypeSet.of(RenderType.cutout(), RenderType.translucent());
    private static final ModelProperty<FacadeData> FACADE_DATA = new ModelProperty<>();

    private final IGeometryBakingContext owner;
    private final ModelBaker baker;
    private final Function<Material, TextureAtlasSprite> spriteGetter;
    private final ModelState modelTransform;
    private final ItemOverrides overrides;
    private final LoadingCache<SidedConnection, List<BakedQuad>> internalPartsCache;
    @Nullable
    private final LoadingCache<SidedConnection, List<BakedQuad>> glassPartsCache;
    //If AE2 is installed this is a FacadeBuilder but if not it cannot be class-loaded
    @Nullable
    private final Object facadeBuilder;
    //TODO: Debate making transmitter models actually have cleanup code and have them also add listeners for opaque transmitters so that when the config
    // changes then these update accordingly
    private final LoadingCache<TransmitterDataKey, List<BakedQuad>> cache = CacheBuilder.newBuilder().build(new CacheLoader<>() {
        @NotNull
        @Override
        public List<BakedQuad> load(@NotNull TransmitterDataKey key) {
            //Glass cache should never be null if we have renderGlass as true
            LoadingCache<SidedConnection, List<BakedQuad>> partsCache = key.renderGlass ? Objects.requireNonNull(glassPartsCache) : internalPartsCache;
            List<BakedQuad> quads = new ArrayList<>();
            for (Direction side : EnumUtils.DIRECTIONS) {
                ConnectionType connectionType = key.data.getConnectionType(side);
                IconStatus iconStatus = TransmitterModelConfiguration.getIconStatus(key.data, side, connectionType);
                SidedConnection sidedConnection = new SidedConnection(side, connectionType, iconStatus);
                quads.addAll(partsCache.getUnchecked(sidedConnection));
            }
            if (facadeBuilder != null && key.facadeData != null) {
                CableBusRenderState renderState = (CableBusRenderState) key.facadeRenderState;
                ((FacadeBuilder) facadeBuilder).getFacadeMesh(renderState, () -> key.rand, key.facadeData.level, key.facadeData.facadeData, null).
                        forEach(view -> quads.add(view.toBlockBakedQuad()));
            }
            return quads;
        }
    });

    public TransmitterBakedModel(ObjModel internal, @Nullable ObjModel glass, IGeometryBakingContext owner, ModelBaker baker,
          Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, @Nullable Object facadeBuilder) {
        //We define our baked variant to be how the item is. As we should always have model data when we have a state
        super(internal.bake(new VisibleModelConfiguration(owner, Arrays.stream(EnumUtils.DIRECTIONS).map(side -> getPartName(side, ConnectionType.NONE)).toList()),
              baker, spriteGetter, modelTransform, overrides));
        this.owner = owner;
        this.baker = baker;
        this.spriteGetter = spriteGetter;
        this.modelTransform = modelTransform;
        this.overrides = overrides;
        this.internalPartsCache = CacheBuilder.newBuilder().build(createPartCacheLoader(internal));
        this.glassPartsCache = glass == null ? null : CacheBuilder.newBuilder().build(createPartCacheLoader(glass));
        this.facadeBuilder = facadeBuilder;
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        if (Mekanism.hooks.AE2Loaded) {
            CableBusRenderState renderState = modelData.get(CableBusRenderState.PROPERTY);
            if (renderState != null && !renderState.getFacades().isEmpty()) {
                BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
                EnumMap<Direction, ModelData> facadeModelData = new EnumMap<>(Direction.class);
                for (Map.Entry<Direction, FacadeRenderState> entry : renderState.getFacades().entrySet()) {
                    Direction side = entry.getKey();
                    CableBusBlock.RENDERING_FACADE_DIRECTION.set(side);
                    try {
                        BlockState facadeState = entry.getValue().getSourceBlock();
                        BakedModel model = dispatcher.getBlockModel(facadeState);
                        facadeModelData.put(side, model.getModelData(level, pos, facadeState, modelData));
                    } finally {
                        CableBusBlock.RENDERING_FACADE_DIRECTION.remove();
                    }
                }
                return super.getModelData(level, pos, state, modelData.derive().with(FACADE_DATA, new FacadeData(facadeModelData, level, pos)).build());
            }
        }
        return super.getModelData(level, pos, state, modelData);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData,
          @Nullable RenderType renderType) {
        if (side != null) {
            return Collections.emptyList();
        }
        TransmitterModelData data = extraData.get(TileEntityTransmitter.TRANSMITTER_PROPERTY);
        if (data != null) {
            boolean renderGlass = renderType == RenderType.translucent();
            if (renderGlass && (glassPartsCache == null || !data.getHasColor())) {
                //Skip rendering the glass if we don't actually have any glass, or we don't have a color for it
                return Collections.emptyList();
            }
            List<BakedQuad> quads;
            FacadeData facadeData = extraData.get(FACADE_DATA);
            if (facadeData != null) {
                CableBusRenderState renderState = extraData.get(CableBusRenderState.PROPERTY);
                quads = cache.getUnchecked(new TransmitterDataKey(data, renderGlass, renderState, facadeData, rand));
            } else {
                quads = cache.getUnchecked(new TransmitterDataKey(data, renderGlass, null, null, null));
            }
            return quads;
        }
        //Fallback to our "default" model arrangement. The item variant uses this
        return super.getQuads(state, null, rand, extraData, renderType);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return glassPartsCache == null ? CUTOUT : FULL;
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
        if (glassPartsCache == null) {
            return List.of(Sheets.cutoutBlockSheet());
        }
        return List.of(Sheets.cutoutBlockSheet(), fabulous ? Sheets.translucentCullBlockSheet() : Sheets.translucentItemSheet());
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        return Collections.singletonList(this);
    }

    private static String getPartName(Direction side, ConnectionType connectionType) {
        return side.getSerializedName() + connectionType.name();
    }

    private CacheLoader<SidedConnection, List<BakedQuad>> createPartCacheLoader(ObjModel model) {
        return new CacheLoader<>() {
            @NotNull
            @Override
            public List<BakedQuad> load(@NotNull SidedConnection key) {
                Direction side = key.side();
                ConnectionType connectionType = key.connection();
                String part = getPartName(side, connectionType);
                if (!model.getRootComponentNames().contains(part)) {
                    //Validate the model actually has the part (this should always be true but if for some reason it isn't short circuit)
                    return Collections.emptyList();
                }
                IconStatus iconStatus = key.status();
                ModelState transform = modelTransform;
                if (connectionType == ConnectionType.NONE && iconStatus.getAngle() > 0) {
                    //If the part should be rotated, then we need to use a custom IModelTransform
                    Vector3f vecForDirection = Vec3.atLowerCornerOf(side.getNormal()).toVector3f();
                    vecForDirection.mul(-1);
                    Quaternionf quaternion = new Quaternionf().setAngleAxis(iconStatus.getAngle(), vecForDirection.x, vecForDirection.y, vecForDirection.z);
                    Transformation matrix = new Transformation(null, quaternion, null, null);
                    transform = new SimpleModelState(transform.getRotation().compose(matrix), transform.isUvLocked());
                }
                BakedModel bakedModel = model.bake(new TransmitterModelConfiguration(owner, part, iconStatus), baker, spriteGetter, transform, overrides);
                //Note: We don't actually care about the state, or the side anywhere and the model returns the proper values even if we don't provide a render type
                // We also just use a new random source as we don't have one in our current context
                return bakedModel.getQuads(null, null, RandomSource.create(), ModelData.EMPTY, null);
            }
        };
    }

    private record SidedConnection(Direction side, ConnectionType connection, IconStatus status) {
    }

    private record FacadeData(EnumMap<Direction, ModelData> facadeData, BlockAndTintGetter level, BlockPos pos) {
    }

    private static class TransmitterDataKey {

        private final TransmitterModelData data;
        private final boolean renderGlass;
        //Actually null or a CableBusRenderState, but it can only be class-loaded when AE2 is actually loaded
        @Nullable
        private final Object facadeRenderState;
        //Just required for loading and not for the hash as facades are already in the renderState
        @Nullable
        private final FacadeData facadeData;
        //Also only for loading facades
        @Nullable
        private final RandomSource rand;
        private final int hash;

        public TransmitterDataKey(TransmitterModelData data, boolean renderGlass, @Nullable Object facadeRenderState, @Nullable FacadeData facadeData, @Nullable RandomSource rand) {
            this.data = data;
            this.renderGlass = renderGlass;
            this.facadeRenderState = facadeRenderState;
            this.facadeData = facadeData;
            this.rand = rand;
            if (facadeRenderState == null) {
                this.hash = Objects.hash(this.data.getConnectionsMap(), this.renderGlass);
            } else {
                CableBusRenderState state = (CableBusRenderState) facadeRenderState;
                this.hash = Objects.hash(this.data.getConnectionsMap(), this.renderGlass, state.getAttachments(), state.getFacades(), state.getPos());
            }
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            //Note: We don't compare data directly as if we aren't rendering glass it not being colored is irrelevant
            // and if we are rendering glass, it will always be colored as we short circuit when it isn't colored
            return obj instanceof TransmitterDataKey other && renderGlass == other.renderGlass && data.getConnectionsMap().equals(other.data.getConnectionsMap()) && Objects.equals(facadeData, other.facadeData);
        }
    }
}