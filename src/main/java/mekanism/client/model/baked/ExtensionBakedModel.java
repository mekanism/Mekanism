package mekanism.client.model.baked;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;

public class ExtensionBakedModel<T> implements BakedModel {

    protected final BakedModel original;

    private final LoadingCache<QuadsKey<T>, List<BakedQuad>> cache = CacheBuilder.newBuilder().build(new CacheLoader<>() {
        @Nonnull
        @Override
        public List<BakedQuad> load(@Nonnull QuadsKey<T> key) {
            return createQuads(key);
        }
    });
    private final Map<List<Pair<BakedModel, RenderType>>, List<Pair<BakedModel, RenderType>>> cachedLayerRedirects = new Object2ObjectOpenHashMap<>();

    public ExtensionBakedModel(BakedModel original) {
        this.original = original;
    }

    @Nonnull
    @Override
    @Deprecated
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand) {
        return original.getQuads(state, side, rand);
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
        return original.getParticleIcon();
    }

    @Nonnull
    @Override
    public ItemOverrides getOverrides() {
        return original.getOverrides();
    }

    @Override
    public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack mat) {
        return original.handlePerspective(cameraTransformType, mat);
    }

    @Nonnull
    @Override
    @Deprecated
    public ItemTransforms getTransforms() {
        return original.getTransforms();
    }

    @Override
    public boolean doesHandlePerspectives() {
        return original.doesHandlePerspectives();
    }

    @Nullable
    protected QuadsKey<T> createKey(QuadsKey<T> key, IModelData data) {
        return key;
    }

    protected List<BakedQuad> createQuads(QuadsKey<T> key) {
        List<BakedQuad> ret = key.getQuads();
        if (key.getTransformation() != null) {
            ret = QuadUtils.transformBakedQuads(ret, key.getTransformation());
        }
        return ret;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData data) {
        List<BakedQuad> quads = original.getQuads(state, side, rand, data);
        QuadsKey<T> key = createKey(new QuadsKey<>(state, side, rand, MinecraftForgeClient.getRenderType(), quads), data);
        if (key == null) {
            return quads;
        }
        return cache.getUnchecked(key);
    }

    @Override
    public boolean isLayered() {
        return original.isLayered();
    }

    @Nonnull
    @Override
    public List<Pair<BakedModel, RenderType>> getLayerModels(ItemStack stack, boolean fabulous) {
        //Cache the remappings so then the inner wrapped ones can cache their quads
        return cachedLayerRedirects.computeIfAbsent(original.getLayerModels(stack, fabulous), originalLayerModels ->
              originalLayerModels.stream().map(layerModel -> layerModel.<BakedModel>mapFirst(this::wrapModel)).toList());
    }

    protected ExtensionBakedModel<T> wrapModel(BakedModel model) {
        return new ExtensionBakedModel<>(model);
    }

    public static class LightedBakedModel extends TransformedBakedModel<Void> {

        public LightedBakedModel(BakedModel original) {
            super(original, QuadTransformation.filtered_fullbright);
        }

        @Override
        protected LightedBakedModel wrapModel(BakedModel model) {
            return new LightedBakedModel(model);
        }
    }

    public static class TransformedBakedModel<T> extends ExtensionBakedModel<T> {

        private final QuadTransformation transform;

        public TransformedBakedModel(BakedModel original, QuadTransformation transform) {
            super(original);
            this.transform = transform;
        }

        @Nonnull
        @Override
        @Deprecated
        public List<BakedQuad> getQuads(BlockState state, Direction side, @Nonnull Random rand) {
            return QuadUtils.transformBakedQuads(original.getQuads(state, side, rand), transform);
        }

        @Override
        public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack mat) {
            // have the original model apply any perspective transforms onto the MatrixStack
            original.handlePerspective(cameraTransformType, mat);
            // return this model, as we want to draw the item variant quads ourselves
            return this;
        }

        @Nullable
        @Override
        protected QuadsKey<T> createKey(QuadsKey<T> key, IModelData data) {
            return key.transform(transform);
        }

        @Override
        protected TransformedBakedModel<T> wrapModel(BakedModel model) {
            return new TransformedBakedModel<>(model, transform);
        }
    }

    public static class QuadsKey<T> {

        @Nullable
        private final BlockState state;
        @Nullable
        private final Direction side;
        private final Random random;
        private final RenderType layer;
        private final List<BakedQuad> quads;
        private QuadTransformation transformation;

        private T data;
        private int dataHash;
        private BiPredicate<T, T> equality;

        public QuadsKey(@Nullable BlockState state, @Nullable Direction side, Random random, RenderType layer, List<BakedQuad> quads) {
            this.state = state;
            this.side = side;
            this.random = random;
            this.layer = layer;
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

        @Nullable
        public BlockState getBlockState() {
            return state;
        }

        @Nullable
        public Direction getSide() {
            return side;
        }

        public Random getRandom() {
            return random;
        }

        public RenderType getLayer() {
            return layer;
        }

        public List<BakedQuad> getQuads() {
            return quads;
        }

        public QuadTransformation getTransformation() {
            return transformation;
        }

        public T getData() {
            return data;
        }

        @Override
        public int hashCode() {
            return Objects.hash(state, side, layer, transformation, dataHash);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof QuadsKey<?> other)) {
                return false;
            } else if (side != other.side || layer != other.layer || !Objects.equals(state, other.state)) {
                return false;
            } else if (transformation != null && !transformation.equals(other.transformation)) {
                return false;
            }
            return data == null || equality.test(data, (T) other.getData());
        }
    }
}
