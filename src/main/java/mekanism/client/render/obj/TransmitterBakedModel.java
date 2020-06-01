package mekanism.client.render.obj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import mekanism.common.tile.transmitter.TileEntitySidedPipe;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import mekanism.common.util.EnumUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ILightReader;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometryPart;
import net.minecraftforge.client.model.obj.OBJModel;

public class TransmitterBakedModel implements IBakedModel {

    private final OBJModel internal;
    @Nullable
    private final OBJModel glass;
    private final IModelConfiguration owner;
    private final ModelBakery bakery;
    private final Function<Material, TextureAtlasSprite> spriteGetter;
    private final IModelTransform modelTransform;
    private final ItemOverrideList overrides;
    private final ResourceLocation modelLocation;
    private final IBakedModel bakedVariant;

    private final Map<QuickHash, List<BakedQuad>> modelCache;
    private List<BakedQuad> itemCache;

    public TransmitterBakedModel(OBJModel internal, @Nullable OBJModel glass, IModelConfiguration owner, ModelBakery bakery,
          Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        //4^6 number of states, if we have a glass texture (support coloring), multiply by 2
        this.modelCache = new Object2ObjectOpenHashMap<>(glass == null ? 4_096 : 8_192);
        this.internal = internal;
        this.glass = glass;
        this.owner = owner;
        this.bakery = bakery;
        this.spriteGetter = spriteGetter;
        this.modelTransform = modelTransform;
        this.overrides = overrides;
        this.modelLocation = modelLocation;
        //We define our baked variant to be how the item is. As we should always have model data when we have a state
        List<String> visible = Arrays.asList("transmitter_up", "transmitter_down", "core_west", "core_east", "core_north", "core_south");
        bakedVariant = internal.bake(new VisibleModelConfiguration(owner, visible), bakery, spriteGetter, modelTransform, overrides, modelLocation);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand) {
        return getQuads(state, side, rand, EmptyModelData.INSTANCE);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        if (side != null) {
            return ImmutableList.of();
        }
        if (extraData.hasProperty(TileEntitySidedPipe.TRANSMITTER_PROPERTY)) {
            TransmitterModelData data = extraData.getData(TileEntitySidedPipe.TRANSMITTER_PROPERTY);
            RenderType layer = MinecraftForgeClient.getRenderLayer();
            boolean hasColor = data.getHasColor() && layer == RenderType.getTranslucent();
            QuickHash hash = new QuickHash(data.getConnectionsMap(), hasColor);
            if (!modelCache.containsKey(hash)) {
                List<String> visible = getVisibleParts(data);
                List<BakedQuad> quads = bake(new TransmitterModelConfiguration(owner, visible, extraData), hasColor).getQuads(state, side, rand, extraData);
                List<BakedQuad> leds = bake(new TransmitterModelConfiguration(owner, getVisibleLEDs(data), extraData), hasColor).getQuads(state, side, rand, extraData);
                quads.addAll(QuadUtils.transformBakedQuads(leds, QuadTransformation.fullbright));
                quads = prepQuads(quads);
                modelCache.put(hash, quads);
                return quads;
            }
            return modelCache.get(hash);
        }
        //Fallback to our "default" model arrangement. The item variant uses this
        if (itemCache == null) {
            itemCache = prepQuads(bakedVariant.getQuads(state, side, rand, extraData));
        }
        return itemCache;
    }

    private List<BakedQuad> prepQuads(List<BakedQuad> quads) {
        List<Quad> list = QuadUtils.unpack(quads);
        list.addAll(QuadUtils.flip(list));
        return QuadUtils.transformAndBake(list, QuadTransformation.translate(new Vec3d(0.5, 0.5, 0.5)));
    }

    private List<String> getVisibleParts(TransmitterModelData data) {
        if (data.check(ConnectionType.TRANSMITTER, ConnectionType.TRANSMITTER, ConnectionType.NONE, ConnectionType.NONE, ConnectionType.NONE, ConnectionType.NONE)) {
            return Arrays.asList("connector_up_down", "transmitter_up", "transmitter_down");
        } else if (data.check(ConnectionType.NONE, ConnectionType.NONE, ConnectionType.TRANSMITTER, ConnectionType.TRANSMITTER, ConnectionType.NONE, ConnectionType.NONE)) {
            return Arrays.asList("connector_north_south", "transmitter_north", "transmitter_south");
        } else if (data.check(ConnectionType.NONE, ConnectionType.NONE, ConnectionType.NONE, ConnectionType.NONE, ConnectionType.TRANSMITTER, ConnectionType.TRANSMITTER)) {
            return Arrays.asList("connector_east_west", "transmitter_east", "transmitter_west");
        }
        List<String> ret = new ArrayList<>();
        for (Direction side : EnumUtils.DIRECTIONS) {
            ConnectionType type = data.getConnectionType(side);
            if (type == ConnectionType.NONE) {
                ret.add("core_" + side.getName());
            } else if (type == ConnectionType.TRANSMITTER) {
                ret.add("transmitter_" + side.getName());
            } else {
                ret.addAll(Arrays.asList("transmitter_" + side.getName(), "panel_" + side.getName()));
                if (type == ConnectionType.PULL) {
                    ret.add("switch_" + side.getName());
                } else if (type == ConnectionType.PUSH) {
                    ret.add("switch_" + side.getName() + "_closed");
                }
            }
        }
        return ret;
    }

    private List<String> getVisibleLEDs(TransmitterModelData data) {
        List<String> ret = new ArrayList<>();
        for (Direction side : EnumUtils.DIRECTIONS) {
            if (data.getConnectionType(side) == ConnectionType.PULL) {
                for (int i = 1; i <= 4; i++) {
                    ret.add("indicator_" + side.getName() + "_led" + i);
                }
            }
        }
        return ret;
    }

    private IBakedModel bake(TransmitterModelConfiguration configuration, boolean hasColor) {
        TextureAtlasSprite particle = spriteGetter.apply(configuration.resolveTexture("particle"));
        IModelBuilder<?> builder = IModelBuilder.of(configuration, overrides, particle);
        addPartQuads(configuration, builder, internal);
        if (glass != null && hasColor && MinecraftForgeClient.getRenderLayer() == RenderType.getTranslucent()) {
            addPartQuads(configuration, builder, glass);
        }
        return builder.build();
    }

    private void addPartQuads(TransmitterModelConfiguration configuration, IModelBuilder<?> builder, OBJModel glass) {
        for (IModelGeometryPart part : glass.getParts()) {
            if (configuration.getPartVisibility(part)) {
                part.addQuads(configuration, builder, bakery, spriteGetter, modelTransform, modelLocation);
            }
        }
    }

    @Nullable
    private static Direction directionForPiece(@Nonnull String piece) {
        return Arrays.stream(EnumUtils.DIRECTIONS).filter(dir -> piece.startsWith(dir.getName().toLowerCase())).findFirst().orElse(null);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return bakedVariant.isAmbientOcclusion();
    }

    @Override
    public boolean isAmbientOcclusion(BlockState state) {
        return bakedVariant.isAmbientOcclusion(state);
    }

    @Override
    public boolean isGui3d() {
        return bakedVariant.isGui3d();
    }

    @Override
    public boolean func_230044_c_() {
        return bakedVariant.func_230044_c_();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return bakedVariant.isBuiltInRenderer();
    }

    @Nonnull
    @Override
    @Deprecated
    public TextureAtlasSprite getParticleTexture() {
        return bakedVariant.getParticleTexture();
    }

    @Override
    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
        return bakedVariant.getParticleTexture(data);
    }

    @Override
    public boolean doesHandlePerspectives() {
        return bakedVariant.doesHandlePerspectives();
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return bakedVariant.getOverrides();
    }

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull ILightReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
        return bakedVariant.getModelData(world, pos, state, tileData);
    }

    @Nonnull
    @Override
    @Deprecated
    public ItemCameraTransforms getItemCameraTransforms() {
        return bakedVariant.getItemCameraTransforms();
    }

    private class QuickHash {

        private Object[] objs;

        public QuickHash(Object... objs) {
            this.objs = objs;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(objs);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            return obj instanceof QuickHash && Arrays.deepEquals(objs, ((QuickHash) obj).objs);
        }
    }
 }