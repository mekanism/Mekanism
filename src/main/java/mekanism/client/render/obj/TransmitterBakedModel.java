package mekanism.client.render.obj;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.client.render.obj.TransmitterModelConfiguration.IconStatus;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
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
    private final Function<RenderMaterial, TextureAtlasSprite> spriteGetter;
    private final IModelTransform modelTransform;
    private final ItemOverrideList overrides;
    private final ResourceLocation modelLocation;
    private final IBakedModel bakedVariant;

    private final Map<QuickHash, List<BakedQuad>> modelCache;

    public TransmitterBakedModel(OBJModel internal, @Nullable OBJModel glass, IModelConfiguration owner, ModelBakery bakery,
          Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
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
        List<String> visible = Arrays.stream(EnumUtils.DIRECTIONS).map(side -> side.getName2() + (side.getAxis() == Axis.Y ? "NORMAL" : "NONE")).collect(Collectors.toList());
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
        if (extraData.hasProperty(TileEntityTransmitter.TRANSMITTER_PROPERTY)) {
            TransmitterModelData data = extraData.getData(TileEntityTransmitter.TRANSMITTER_PROPERTY);
            RenderType layer = MinecraftForgeClient.getRenderLayer();
            boolean hasColor = data.getHasColor() && layer == RenderType.getTranslucent();
            QuickHash hash = new QuickHash(data.getConnectionsMap(), hasColor);
            if (!modelCache.containsKey(hash)) {
                List<String> visible = new ArrayList<>();
                for (Direction dir : EnumUtils.DIRECTIONS) {
                    visible.add(dir.getString() + data.getConnectionType(dir).getString().toUpperCase(Locale.ROOT));
                }
                List<BakedQuad> result = bake(new TransmitterModelConfiguration(owner, visible, extraData), hasColor).getQuads(state, null, rand, extraData);
                modelCache.put(hash, result);
                return result;
            }
            return modelCache.get(hash);
        }
        //Fallback to our "default" model arrangement. The item variant uses this
        return bakedVariant.getQuads(state, null, rand, extraData);
    }

    /**
     * Rotates the pieces that need rotating.
     */
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
                String name = part.name();
                IModelTransform transform = modelTransform;
                if (name.endsWith("NONE")) {
                    Direction dir = directionForPiece(name);
                    //We should not have been able to get here if dir was null but check just in case
                    IconStatus status = configuration.getIconStatus(dir);
                    if (dir != null && status.getAngle() > 0) {
                        //If the part should be rotated, then we need to use a custom IModelTransform
                        transform = new TransmitterModelTransform(transform, dir, status.getAngle());
                    }
                }
                part.addQuads(configuration, builder, bakery, spriteGetter, transform, modelLocation);
            }
        }
    }

    @Nullable
    private static Direction directionForPiece(@Nonnull String piece) {
        return Arrays.stream(EnumUtils.DIRECTIONS).filter(dir -> piece.startsWith(dir.getName2())).findFirst().orElse(null);
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
    public boolean isSideLit() {
        return bakedVariant.isSideLit();
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

    @Override
    public IBakedModel handlePerspective(TransformType cameraTransformType, MatrixStack mat) {
        return bakedVariant.handlePerspective(cameraTransformType, mat);
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return bakedVariant.getOverrides();
    }

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
        return bakedVariant.getModelData(world, pos, state, tileData);
    }

    @Nonnull
    @Override
    @Deprecated
    public ItemCameraTransforms getItemCameraTransforms() {
        return bakedVariant.getItemCameraTransforms();
    }

    public static class QuickHash {

        private final Object[] objs;

        public QuickHash(Object... objs) {
            this.objs = objs;
        }

        @Override
        public int hashCode() {
            //TODO: Cache the hashcode?
            return Arrays.hashCode(objs);
        }

        public Object[] get() {
            return objs;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof QuickHash && Arrays.deepEquals(objs, ((QuickHash) obj).objs);
        }
    }
}