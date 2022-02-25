package mekanism.client.model.robit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.RobitSpriteUploader;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;

public class RobitModelDataBakedModel implements BakedModel {

    private final BakedModel original;
    private final IModelData modelData;

    public RobitModelDataBakedModel(@Nonnull BakedModel original, @Nonnull IModelData data) {
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
        return getParticleIcon(modelData);
    }

    @Nonnull
    @Override
    @Deprecated
    public ItemTransforms getTransforms() {
        return original.getTransforms();
    }

    @Nonnull
    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        return original.getQuads(state, side, rand, extraData);
    }

    @Override
    public boolean useAmbientOcclusion(BlockState state) {
        return original.useAmbientOcclusion(state);
    }

    @Override
    public boolean doesHandlePerspectives() {
        return original.doesHandlePerspectives();
    }

    @Override
    public BakedModel handlePerspective(TransformType cameraTransformType, PoseStack mat) {
        // have the original model apply any perspective transforms onto the MatrixStack
        original.handlePerspective(cameraTransformType, mat);
        // return this model, as we want to draw the item variant quads ourselves
        return this;
    }

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
        return original.getModelData(world, pos, state, tileData);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@Nonnull IModelData data) {
        return original.getParticleIcon(data);
    }

    @Override
    public boolean isLayered() {
        //Pretend our model is layered so that we can override the render type
        return true;
    }

    @Override
    public List<Pair<BakedModel, RenderType>> getLayerModels(ItemStack stack, boolean fabulous) {
        //TODO: Handle the original model being layered properly as currently we don't have any way to properly bounce them
        return Collections.singletonList(Pair.of(this, RobitSpriteUploader.RENDER_TYPE));
    }
}