package mekanism.client.model.baked;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.energycube.EnergyCubeBaseGeometry.CubeSideModelState;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import mekanism.common.Mekanism;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityEnergyCube.CubeSideState;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
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
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnergyCubeModel implements DynamicBlockStateModel {

    private static final CubeSideState[] INACTIVE = Util.make(new CubeSideState[EnumUtils.DIRECTIONS.length], sideStates -> Arrays.fill(sideStates, CubeSideState.INACTIVE));

    private final Int2ObjectMap<List<BlockStateModelPart>> cache = new Int2ObjectAVLTreeMap<>();
    private final IntFunction<List<BlockStateModelPart>> partGenerator = this::generateParts;

    private final BlockStateModelPart frame;
    private final Map<RelativeSide, Map<CubeSideState, BlockStateModelPart>> dynamicParts;

    EnergyCubeModel(BlockStateModelPart frame, Map<RelativeSide, Map<CubeSideState, BlockStateModelPart>> dynamicParts) {
        this.frame = frame;
        this.dynamicParts = dynamicParts;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        ModelData modelData = level.getModelData(pos);
        collectParts(parts, modelData.get(TileEntityEnergyCube.SIDE_STATE_PROPERTY));
    }

    public void collectParts(List<BlockStateModelPart> parts, CubeSideState @Nullable [] sideStates) {
        if (sideStates == null || sideStates.length != EnumUtils.SIDES.length) {
            //If there is no side data then treat everything as inactive
            sideStates = INACTIVE;
        }
        int key = CacheKey.pack(sideStates);
        parts.addAll(cache.computeIfAbsent(key, partGenerator));
    }

    private List<BlockStateModelPart> generateParts(int key) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        parts.add(frame);
        CubeSideState[] data = CacheKey.unpack(key);
        for (int i = 0; i < EnumUtils.SIDES.length; i++) {
            RelativeSide dir = EnumUtils.SIDES[i];
            CubeSideState sideState = data[i];
            if (sideState != null) {
                parts.add(dynamicParts.get(dir).get(sideState));
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

    private static class CacheKey {

        private static final int BITS_PER_STATE = 4; //nb: only 2 used (values 0-2)
        private static final int ORDINAL_MASK = 0xF;
        private static final CubeSideState[] CUBE_SIDE_STATES = CubeSideState.values();
        private static final int NUM_STATES = CUBE_SIDE_STATES.length;

        static int pack(CubeSideState[] states) {
            int size = states.length;
            int key = 0;
            for (int i = 0; i < size; i++) {
                key |= (states[i].ordinal() & ORDINAL_MASK) << (BITS_PER_STATE * i);
            }
            return key;
        }

        static CubeSideState[] unpack(int key) {
            if (key == 0) {
                return INACTIVE;
            }
            CubeSideState[] states = new CubeSideState[6];
            for (int i = 0; i < 6; i++) {
                int ordinal = (key >> (BITS_PER_STATE * i)) & ORDINAL_MASK;
                states[i] = ordinal < NUM_STATES ? CUBE_SIDE_STATES[ordinal] : CubeSideState.INACTIVE;
            }
            return states;
        }
    }

    public record Unbaked(Variant tierModel) implements CustomUnbakedBlockStateModel {

        public static final Identifier ID = Mekanism.rl("energy_cube_sided");
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
              Variant.MAP_CODEC.fieldOf("tier_model").forGetter(Unbaked::tierModel)
        ).apply(in, Unbaked::new));

        @Override
        public EnergyCubeModel bake(ModelBaker baker) {
            Map<RelativeSide, Map<CubeSideState, BlockStateModelPart>> dynamicParts = new EnumMap<>(RelativeSide.class);
            ResolvedModel model = baker.getModel(tierModel.modelLocation());

            ModelState tierModelState = tierModel.modelState().asModelState();
            BlockStateModelPart frame = SimpleModelWrapper.bake(baker, model, tierModelState);

            for (RelativeSide side : EnumUtils.SIDES) {
                Map<CubeSideState, BlockStateModelPart> sideMap = new HashMap<>(2);
                dynamicParts.put(side, sideMap);
                addSideState(baker, side, sideMap, model, tierModelState, CubeSideState.ACTIVE_LIT);
                addSideState(baker, side, sideMap, model, tierModelState, CubeSideState.ACTIVE_UNLIT);
                addSideState(baker, side, sideMap, model, tierModelState, CubeSideState.INACTIVE);
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