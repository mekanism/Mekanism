package mekanism.client.model.baked;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.render.lib.QuadTransformation;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IModelData;

public class ExtensionBakedModel<T> implements IBakedModel {

    protected IBakedModel original;

    private final LoadingCache<QuadsKey<T>, List<BakedQuad>> cache = CacheBuilder.newBuilder().build(new CacheLoader<QuadsKey<T>, List<BakedQuad>>() {
        @Override
        public List<BakedQuad> load(@Nonnull QuadsKey<T> key) {
            return createQuads(key);
        }
    });

    public ExtensionBakedModel(IBakedModel original) {
        this.original = original;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, @Nonnull Random rand) {
        return original.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return original.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return original.isGui3d();
    }

    @Override
    public boolean func_230044_c_() {
        return original.func_230044_c_();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return original.isBuiltInRenderer();
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return original.getParticleTexture();
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return original.getOverrides();
    }

    @Override
    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack mat) {
        return original.handlePerspective(cameraTransformType, mat);
    }

    @Nonnull
    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return original.getItemCameraTransforms();
    }

    @Override
    public boolean doesHandlePerspectives() {
        return original.doesHandlePerspectives();
    }

    protected QuadsKey<T> createKey(QuadsKey<T> key, IModelData data) {
        return key;
    }

    protected List<BakedQuad> createQuads(QuadsKey<T> key) {
        return key.getQuads();
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData data) {
        List<BakedQuad> quads = original.getQuads(state, side, rand, data);
        if (state == null)
            return quads;
        QuadsKey<T> key = createKey(new QuadsKey<>(state, side, rand, quads), data);
        if (key == null)
            return quads;
        return cache.getUnchecked(key);
    }

    public static class QuadsKey<T> {

        private final BlockState state;
        private final Direction side;
        private final Random random;
        private final List<BakedQuad> quads;
        private QuadTransformation transformation;

        private T data;
        private int dataHash;
        private BiPredicate<T, T> equality;

        public QuadsKey(BlockState state, Direction side, Random random, List<BakedQuad> quads) {
            this.state = state;
            this.side = side;
            this.random = random;
            this.quads = quads;
        }

        public QuadsKey<T> transform(QuadTransformation transformation) {
            this.transformation = transformation;
            return this;
        }

        public QuadsKey<T> data(T data, int dataHash, BiPredicate<T, T> equality) {
            this.data = data;
            this.dataHash = dataHash;
            this.equality = equality;
            return this;
        }

        public BlockState getBlockState() { return state; }
        public Direction getSide() { return side; }
        public Random getRandom() { return random; }
        public List<BakedQuad> getQuads() { return quads; }
        public QuadTransformation getTransformation() { return transformation; }
        public T getData() { return data; }

        @Override
        public int hashCode() {
            return Objects.hash(state, side, transformation, dataHash);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof QuadsKey))
                return false;
            QuadsKey<?> other = (QuadsKey<?>) obj;
            if (side != other.side || !state.equals(other.state))
                return false;
            if (transformation != null && !transformation.equals(other.transformation))
                return false;
            return data == null || equality.test(data, (T) other.getData());
        }
    }
}
