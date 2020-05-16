package mekanism.client.render.item;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraftforge.client.model.data.IModelData;

//From: https://github.com/Shadows-of-Fire/Singularities/blob/master/src/main/java/shadows/singularity/client/ItemLayerWrapper.java
public class ItemLayerWrapper implements IBakedModel {

    private final IBakedModel internal;

    private TransformType transform = TransformType.NONE;

    public ItemLayerWrapper(IBakedModel internal) {
        this.internal = internal;
    }

    @Override
    public IBakedModel getBakedModel() {
        return internal;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        return internal.getQuads(state, side, rand, extraData);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand) {
        return internal.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion(BlockState state) {
        return internal.isAmbientOcclusion(state);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return internal.isAmbientOcclusion();
    }

    @Override
    public boolean func_230044_c_() {
        return internal.func_230044_c_();
    }

    @Override
    public boolean isGui3d() {
        return internal.isGui3d();
    }

    public IBakedModel getInternal() {
        return internal;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return true;
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
        return internal.getParticleTexture(data);
    }

    @Nonnull
    @Override
    @Deprecated
    public TextureAtlasSprite getParticleTexture() {
        return internal.getParticleTexture();
    }

    @Nonnull
    @Override
    @Deprecated
    public ItemCameraTransforms getItemCameraTransforms() {
        return internal.getItemCameraTransforms();
    }

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull ILightReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
        return internal.getModelData(world, pos, state, tileData);
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

    @Nonnull
    @Override
    public IBakedModel handlePerspective(@Nonnull TransformType type, MatrixStack matrix) {
        transform = type;
        //You can use a field on your TileEntityItemStackRenderer to store this TransformType for use in renderByItem, this method is always called before it.
        TransformationMatrix tr = transforms.get(type);
        if (!tr.isIdentity()) {
            tr.push(matrix);
        }
        return this;
    }

    @Nonnull
    public TransformType getTransform() {
        return transform;
    }

    // Copy from old CTM
    public static Map<TransformType, TransformationMatrix> transforms = ImmutableMap.<TransformType, TransformationMatrix>builder()
          .put(TransformType.GUI, get(0, 0, 0, 30, 225, 0, 0.625F))
          .put(TransformType.THIRD_PERSON_RIGHT_HAND, get(0, 2.5F, 0, 75, 45, 0, 0.375F))
          .put(TransformType.THIRD_PERSON_LEFT_HAND, get(0, 2.5F, 0, 75, 45, 0, 0.375F))
          .put(TransformType.FIRST_PERSON_RIGHT_HAND, get(0, 0, 0, 0, 45, 0, 0.4F))
          .put(TransformType.FIRST_PERSON_LEFT_HAND, get(0, 0, 0, 0, 225, 0, 0.4F))
          .put(TransformType.GROUND, get(0, 2, 0, 0, 0, 0, 0.25F))
          .put(TransformType.HEAD, get(0, 0, 0, 0, 0, 0, 1))
          .put(TransformType.FIXED, get(0, 0, 0, 0, 0, 0, 1))
          .put(TransformType.NONE, get(0, 0, 0, 0, 0, 0, 0))
          .build();

    private static TransformationMatrix get(float tx, float ty, float tz, float ax, float ay, float az, float s) {
        return new TransformationMatrix(new Vector3f(tx / 16, ty / 16, tz / 16),
              new Quaternion(ax, ay, az, true),
              new Vector3f(s, s, s), null);
    }
}