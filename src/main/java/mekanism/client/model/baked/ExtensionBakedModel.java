package mekanism.client.model.baked;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ExtensionBakedModel<T> extends BakedModelWrapper<BakedModel> {

    private final LoadingCache<QuadsKey<T>, List<BakedQuad>> cache = CacheBuilder.newBuilder().build(new CacheLoader<>() {
        @Override
        public List<BakedQuad> load(QuadsKey<T> key) {
            return createQuads(key);
        }
    });
    //Note: While we may have a bit better memory usage if we cache individual models and their transformations,
    // for now we are not as the input lists are immutable, and we don't want to add the overhead of looping and
    // creating a new list each time it is rendered and getRenderPasses is called. If at some point this gets
    // profiled in detail, and we find out that doesn't cause any major performance impact we should consider
    // switching this to a Map<BakedModel, BakedModel>
    private final Map<List<BakedModel>, List<BakedModel>> cachedRenderPasses = new Object2ObjectOpenHashMap<>();

    public ExtensionBakedModel(BakedModel original) {
        super(original);
    }

    @Nullable
    protected QuadsKey<T> createKey(QuadsKey<T> key, ModelData data) {
        return key;
    }

    protected List<BakedQuad> createQuads(QuadsKey<T> key) {
        List<BakedQuad> ret = key.getQuads();
        if (key.getTransformation() != null) {
            ret = QuadUtils.transformBakedQuads(ret, key.getTransformation());
        }
        return ret;
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        List<BakedQuad> quads = super.getQuads(state, side, rand, data, renderType);
        QuadsKey<T> key = createKey(new QuadsKey<>(state, side, rand, renderType, quads), data);
        if (key == null) {
            return quads;
        }
        return cache.getUnchecked(key);
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        //Cache the remappings so then the inner wrapped ones can cache their quads
        List<BakedModel> superPasses = super.getRenderPasses(stack, fabulous);
        List<BakedModel> passes = cachedRenderPasses.get(superPasses);
        if (passes == null) {
            passes = superPasses.stream().<BakedModel>map(this::wrapModel).toList();
            cachedRenderPasses.put(superPasses, passes);
        }
        return passes;
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

        @Override
        @Deprecated
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
            return QuadUtils.transformBakedQuads(super.getQuads(state, side, rand), transform);
        }

        @Override
        public BakedModel applyTransform(ItemDisplayContext displayContext, PoseStack mat, boolean applyLeftHandTransform) {
            // have the original model apply any perspective transforms onto the MatrixStack
            super.applyTransform(displayContext, mat, applyLeftHandTransform);
            // return this model, as we want to draw the item variant quads ourselves
            return this;
        }

        @Nullable
        @Override
        protected QuadsKey<T> createKey(QuadsKey<T> key, ModelData data) {
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
        private final RandomSource random;
        @Nullable
        private final RenderType layer;
        private final List<BakedQuad> quads;
        @Nullable
        private QuadTransformation transformation;

        @Nullable
        private T data;
        private int dataHash;
        @Nullable
        private BiPredicate<T, T> equality;

        public QuadsKey(@Nullable BlockState state, @Nullable Direction side, RandomSource random, @Nullable RenderType layer, List<BakedQuad> quads) {
            this.state = state;
            this.side = side;
            this.random = random;
            this.layer = layer;
            this.quads = quads;
        }

        public QuadsKey<T> transform(Supplier<? extends QuadTransformation> transformation) {
            return transform(transformation.get());
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

        public RandomSource getRandom() {
            return random;
        }

        @Nullable
        public RenderType getLayer() {
            return layer;
        }

        public List<BakedQuad> getQuads() {
            return quads;
        }

        @Nullable
        public QuadTransformation getTransformation() {
            return transformation;
        }

        @Nullable
        public T getData() {
            return data;
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(state);
            result = 31 * result + Objects.hashCode(side);
            result = 31 * result + Objects.hashCode(layer);
            result = 31 * result + Objects.hashCode(transformation);
            result = 31 * result + dataHash;
            return result;
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
            return data == null || equality != null && equality.test(data, (T) other.getData());
        }
    }
}
