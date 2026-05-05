package mekanism.client.model.baked;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.baked.ExtensionBakedModel.QuadsKey;
import mekanism.client.model.energycube.EnergyCubeBaseGeometry.CubeSideModelState;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import mekanism.common.Mekanism;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityEnergyCube.CubeSideState;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialFlags;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

@NothingNullByDefault
public class EnergyCubeModel implements DynamicBlockStateModel {

    private static final CubeSideState[] INACTIVE = Util.make(new CubeSideState[EnumUtils.DIRECTIONS.length], sideStates -> Arrays.fill(sideStates, CubeSideState.INACTIVE));
    //TODO - 26.1: fullbright should now be handled, but is the uvShift needed??? can we bake it into the json
    private static final QuadTransformation LED_TRANSFORMS = QuadTransformation.list(QuadTransformation.fullbright, QuadTransformation.uvShift(-0.125F, 0));
    private static final BiPredicate<CubeSideState[], CubeSideState[]> DATA_EQUALITY_CHECK = Arrays::equals;

    private final LoadingCache<QuadsKey<CubeSideState[]>, List<BlockStateModelPart>> cache = CacheBuilder.newBuilder().build(new CacheLoader<>() {
        @Override
        public List<BlockStateModelPart> load(QuadsKey<CubeSideState[]> key) {
            return collectParts(key);
        }
    });

    private final BlockStateModelPart frame;
    private final Map<RelativeSide, Map<CubeSideState, BlockStateModelPart>> dynamicParts;

    EnergyCubeModel(BlockStateModelPart frame, Map<RelativeSide, Map<CubeSideState, BlockStateModelPart>> dynamicParts) {
        this.frame = frame;
        this.dynamicParts = dynamicParts;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        ModelData modelData = level.getModelData(pos);
        CubeSideState[] sideStates = modelData.get(TileEntityEnergyCube.SIDE_STATE_PROPERTY);
        if (sideStates == null || sideStates.length != EnumUtils.SIDES.length) {
            //If there is no side data then treat everything as inactive
            sideStates = INACTIVE;
        }
        //Note: We intentionally ignore the state and use null here to minimize cache size as it doesn't actually matter
        // or get used for energy cube models
        //TODO - 26.1: Replace this quads key with a more reasonable key that just handles the side data
        QuadsKey<CubeSideState[]> key = new QuadsKey<>(null, null, random, null, Collections.emptyList());
        key.data(sideStates, Arrays.hashCode(sideStates), DATA_EQUALITY_CHECK);
        parts.addAll(cache.getUnchecked(key));
    }

    private List<BlockStateModelPart> collectParts(QuadsKey<CubeSideState[]> key) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        parts.add(frame);
        CubeSideState[] data = key.getData();
        if (data != null) {
            for (int i = 0; i < EnumUtils.SIDES.length; i++) {
                RelativeSide dir = EnumUtils.SIDES[i];
                CubeSideState sideState = data[i];
                if (sideState != null && sideState != CubeSideState.INACTIVE) {
                    parts.add(dynamicParts.get(dir).get(sideState));
                }
            }
        }
        return parts;
    }

    @Override
    @Deprecated
    public Baked particleMaterial() {
        return frame.particleMaterial();
    }

    @MaterialFlags
    @Override
    public int materialFlags() {
        return frame.materialFlags();
    }

    //TODO - 26.1: Figure this out https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/26.1/src/client/java/appeng/client/render/model/DriveModel.java
    //TODO - 26.1: Look into data genning the block state files for the energy cubes?
    //Once you've done that, I'd highly recommend data-genning your blockstate files (if you don't already do that), because you can use that Unbaked record as-is to have it generate the correct blockstate file with your custom properties.
    //i.e. using blockStateOutput.accept(createSimpleBlock(block, MultiVariant.of(new CustomBlockStateModelBuilder.Simple(new EnergyCube.Unbaked(... your props ...)))
    public record Unbaked(Variant tierModel) implements CustomUnbakedBlockStateModel {

        public static final Identifier ID = Mekanism.rl("energy_cube_sided");
        private static final Codec<Map<RelativeSide, Variant>> SUB_PART_CODEC = Codec.unboundedMap(RelativeSide.CODEC, Variant.MAP_CODEC.codec());
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
              Variant.MAP_CODEC.fieldOf("tier_model").forGetter(Unbaked::tierModel)
        ).apply(in, Unbaked::new));

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            Map<RelativeSide, Map<CubeSideState, BlockStateModelPart>> dynamicParts = new EnumMap<>(RelativeSide.class);
            ResolvedModel model = baker.getModel(tierModel.modelLocation());

            ModelState tierModelState = tierModel.modelState().asModelState();
            BlockStateModelPart frame = SimpleModelWrapper.bake(baker, model, tierModelState);

            for (RelativeSide side : EnumUtils.SIDES) {
                Map<CubeSideState, BlockStateModelPart> sideMap = new HashMap<>(2);
                dynamicParts.put(side, sideMap);
                addSideState(baker, side, sideMap, model, tierModelState, CubeSideState.ACTIVE_LIT);
                addSideState(baker, side, sideMap, model, tierModelState, CubeSideState.ACTIVE_UNLIT);
            }

            return new EnergyCubeModel(frame, dynamicParts);
        }

        private static void addSideState(ModelBaker baker, RelativeSide side, Map<CubeSideState, BlockStateModelPart> sideMap, ResolvedModel model, ModelState tierModelState, CubeSideState sideState) {
            sideMap.put(sideState, SimpleModelWrapper.bake(baker, model, new CubeSideModelState(tierModelState, side, sideState)));
        }

        public BlockStateModelPart transform(ModelBaker baker, BlockStateModelPart variant, QuadTransformation transformation) {
            //TODO - 26.1: Make this into more of a proper helper?
            QuadCollection.Builder builder = new QuadCollection.Builder();
            for (BakedQuad quad : QuadUtils.transformBakedQuads(variant.getQuads(null), transformation)) {
                builder.addUnculledFace(quad);
            }
            for (Direction direction : EnumUtils.DIRECTIONS) {
                for (BakedQuad quad : QuadUtils.transformBakedQuads(variant.getQuads(direction), transformation)) {
                    builder.addCulledFace(direction, quad);
                }
            }
            //TODO - 26.1: Do we need to somehow actually bake it so that it has a different name and such?
            //TODO - 26.1: Figure out the render type to pass?
            return new SimpleModelWrapper(builder.build(), variant.useAmbientOcclusion(), variant.particleMaterial());
        }

        @Override
        public MapCodec<Unbaked> codec() {
            return MAP_CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(tierModel.modelLocation());
        }
    }
}