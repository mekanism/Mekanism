package mekanism.client.model.robit;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.RobitSpriteUploader;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.IModelData;

public class RobitModelDataBakedModel implements IBakedModel {

    private final IBakedModel original;
    private final IModelData modelData;

    public RobitModelDataBakedModel(@Nonnull IBakedModel original, @Nonnull IModelData data) {
        this.original = original;
        this.modelData = data;
    }

    @Nonnull
    @Override
    @Deprecated
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand) {
        return getQuads(state, side, rand, modelData);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return original.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return original.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return original.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return original.isCustomRenderer();
    }

    @Nonnull
    @Override
    @Deprecated
    public TextureAtlasSprite getParticleIcon() {
        return getParticleTexture(modelData);
    }

    @Nonnull
    @Override
    @Deprecated
    public ItemCameraTransforms getTransforms() {
        return original.getTransforms();
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        return original.getQuads(state, side, rand, extraData);
    }

    @Override
    public boolean isAmbientOcclusion(BlockState state) {
        return original.isAmbientOcclusion(state);
    }

    @Override
    public boolean doesHandlePerspectives() {
        return original.doesHandlePerspectives();
    }

    @Override
    public IBakedModel handlePerspective(TransformType cameraTransformType, MatrixStack mat) {
        // have the original model apply any perspective transforms onto the MatrixStack
        original.handlePerspective(cameraTransformType, mat);
        // return this model, as we want to draw the item variant quads ourselves
        return this;
    }

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
        return original.getModelData(world, pos, state, tileData);
    }

    @Override
    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
        return original.getParticleTexture(data);
    }

    @Override
    public boolean isLayered() {
        //Pretend our model is layered so that we can override the render type
        return true;
    }

    @Override
    public List<Pair<IBakedModel, RenderType>> getLayerModels(ItemStack stack, boolean fabulous) {
        //TODO: Handle the original model being layered properly as currently we don't have any way to properly bounce them
        return Collections.singletonList(Pair.of(this, RobitSpriteUploader.RENDER_TYPE));
    }
}