package mekanism.client.render.item;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

//From: https://github.com/Shadows-of-Fire/Singularities/blob/master/src/main/java/shadows/singularity/client/ItemLayerWrapper.java
@SideOnly(Side.CLIENT)
public class ItemLayerWrapper implements IBakedModel {

    private final IBakedModel internal;

    private TransformType transform = TransformType.NONE;

    public ItemLayerWrapper(IBakedModel internal) {
        this.internal = internal;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return internal.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return internal.isAmbientOcclusion();
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
    public TextureAtlasSprite getParticleTexture() {
        return internal.getParticleTexture();
    }

    //TODO: Is this needed
    /*@Nonnull
    public ItemCameraTransforms getItemCameraTransforms() {
        return internal.getItemCameraTransforms();
    }*/

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

    @Nonnull
    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(@Nonnull TransformType type) {
        transform = type;
        //You can use a field on your TileEntityItemStackRenderer to store this TransformType for use in renderByItem, this method is always called before it.
        //return Pair.of(this, internal.handlePerspective(type).getRight());
        return Pair.of(this, transforms.get(type).getMatrix());
    }

    @Nonnull
    public TransformType getTransform() {
        return transform;
    }


    // Copy from old CTM
    public static Map<TransformType, TRSRTransformation> transforms = ImmutableMap.<TransformType, TRSRTransformation>builder()
          .put(TransformType.GUI, get(0, 0, 0, 30, 225, 0, 0.625f))
          .put(TransformType.THIRD_PERSON_RIGHT_HAND, get(0, 2.5f, 0, 75, 45, 0, 0.375f))
          .put(TransformType.THIRD_PERSON_LEFT_HAND, get(0, 2.5f, 0, 75, 45, 0, 0.375f))
          .put(TransformType.FIRST_PERSON_RIGHT_HAND, get(0, 0, 0, 0, 45, 0, 0.4f))
          .put(TransformType.FIRST_PERSON_LEFT_HAND, get(0, 0, 0, 0, 225, 0, 0.4f))
          .put(TransformType.GROUND, get(0, 2, 0, 0, 0, 0, 0.25f))
          .put(TransformType.HEAD, get(0, 0, 0, 0, 0, 0, 1))
          .put(TransformType.FIXED, get(0, 0, 0, 0, 0, 0, 1))
          .put(TransformType.NONE, get(0, 0, 0, 0, 0, 0, 0))
          .build();

    private static TRSRTransformation get(float tx, float ty, float tz, float ax, float ay, float az, float s) {
        return new TRSRTransformation(new Vector3f(tx / 16, ty / 16, tz / 16),
              TRSRTransformation.quatFromXYZDegrees(new Vector3f(ax, ay, az)), new Vector3f(s, s, s), null);
    }

}