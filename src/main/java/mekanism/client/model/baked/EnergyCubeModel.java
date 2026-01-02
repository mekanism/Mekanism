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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.baked.ExtensionBakedModel.QuadsKey;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import mekanism.common.Mekanism;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityEnergyCube.CubeSideState;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

@NothingNullByDefault
public class EnergyCubeModel implements DynamicBlockStateModel {

    private static final CubeSideState[] INACTIVE = Util.make(new CubeSideState[EnumUtils.DIRECTIONS.length], sideStates -> Arrays.fill(sideStates, CubeSideState.INACTIVE));
    private static final QuadTransformation LED_TRANSFORMS = QuadTransformation.list(QuadTransformation.fullbright, QuadTransformation.uvShift(-0.125F, 0));
    private static final BiPredicate<CubeSideState[], CubeSideState[]> DATA_EQUALITY_CHECK = Arrays::equals;

    private final LoadingCache<QuadsKey<CubeSideState[]>, List<BlockModelPart>> cache = CacheBuilder.newBuilder().build(new CacheLoader<>() {
        @Override
        public List<BlockModelPart> load(QuadsKey<CubeSideState[]> key) {
            return collectParts(key);
        }
    });

    private final BlockModelPart frame;
    private final Map<RelativeSide, BlockModelPart> leds;
    private final Map<RelativeSide, BlockModelPart> activeLEDs;
    private final Map<RelativeSide, BlockModelPart> ports;
    private final Map<RelativeSide, BlockModelPart> activePorts;

    EnergyCubeModel(BlockModelPart frame, Map<RelativeSide, BlockModelPart> leds, Map<RelativeSide, BlockModelPart> activeLEDs, Map<RelativeSide, BlockModelPart> ports, Map<RelativeSide, BlockModelPart> activePorts) {
        this.frame = frame;
        this.leds = leds;
        this.activeLEDs = activeLEDs;
        this.ports = ports;
        this.activePorts = activePorts;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        ModelData modelData = level.getModelData(pos);
        CubeSideState[] sideStates = modelData.get(TileEntityEnergyCube.SIDE_STATE_PROPERTY);
        if (sideStates == null || sideStates.length != EnumUtils.SIDES.length) {
            //If there is no side data then treat everything as inactive
            sideStates = INACTIVE;
        }
        //Note: We intentionally ignore the state and use null here to minimize cache size as it doesn't actually matter
        // or get used for energy cube models
        //TODO - 1.21.11: Replace this quads key with a more reasonable key that just handles the side data
        QuadsKey<CubeSideState[]> key = new QuadsKey<>(null, null, random, null, Collections.emptyList());
        key.data(sideStates, Arrays.hashCode(sideStates), DATA_EQUALITY_CHECK);
        parts.addAll(cache.getUnchecked(key));
    }

    private List<BlockModelPart> collectParts(QuadsKey<CubeSideState[]> key) {
        Direction side = key.getSide();
        CubeSideState[] data = Objects.requireNonNull(key.getData());
        //Make the list of quads mutable so that we can add the proper extra portions to it
        List<BlockModelPart> parts = new ArrayList<>();
        parts.add(frame);
        for (int i = 0; i < EnumUtils.SIDES.length; i++) {
            RelativeSide dir = EnumUtils.SIDES[i];
            CubeSideState sideState = data[i];
            if (sideState == CubeSideState.ACTIVE_LIT) {
                parts.add(activeLEDs.get(dir));
                parts.add(activePorts.get(dir));
            } else {
                parts.add(leds.get(dir));
                if (sideState == CubeSideState.ACTIVE_UNLIT) {
                    parts.add(ports.get(dir));
                }
            }
        }
        return parts;
    }

    @Override
    @Deprecated
    public TextureAtlasSprite particleIcon() {
        return frame.particleIcon();
    }

    //TODO - 1.21.11: Figure this out https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/26.1/src/client/java/appeng/client/render/model/DriveModel.java
    //TODO - 1.21.11: Look into data genning the block state files for the energy cubes?
    //Once you've done that, I'd highly recommend data-genning your blockstate files (if you don't already do that), because you can use that Unbaked record as-is to have it generate the correct blockstate file with your custom properties.
    //i.e. using blockStateOutput.accept(createSimpleBlock(block, MultiVariant.of(new CustomBlockStateModelBuilder.Simple(new EnergyCube.Unbaked(... your props ...)))
    public record Unbaked(Variant frame, Map<RelativeSide, Variant> leds, Map<RelativeSide, Variant> ports) implements CustomUnbakedBlockStateModel {

        public static final Identifier ID = Mekanism.rl("energy_cube");
        private static final Codec<Map<RelativeSide, Variant>> SUB_PART_CODEC = Codec.unboundedMap(RelativeSide.CODEC, Variant.MAP_CODEC.codec());
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
              Variant.MAP_CODEC.fieldOf("frame").forGetter(Unbaked::frame),
              SUB_PART_CODEC.fieldOf("leds").forGetter(Unbaked::leds),
              SUB_PART_CODEC.fieldOf("ports").forGetter(Unbaked::ports)
        ).apply(in, Unbaked::new));

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            Map<RelativeSide, BlockModelPart> leds = new EnumMap<>(RelativeSide.class);
            Map<RelativeSide, BlockModelPart> activeLEDs = new EnumMap<>(RelativeSide.class);
            Map<RelativeSide, BlockModelPart> ports = new EnumMap<>(RelativeSide.class);
            Map<RelativeSide, BlockModelPart> activePorts = new EnumMap<>(RelativeSide.class);
            //Note: We don't bother having any form of lazy transformations take place here as this should only have a memory
            // impact equivalent to having two models: one with the leds and ports off, and one with all of them active
            for (Map.Entry<RelativeSide, Variant> entry : this.leds.entrySet()) {
                RelativeSide side = entry.getKey();
                Variant variant = entry.getValue();
                BlockModelPart led = SimpleModelWrapper.bake(baker, variant.modelLocation(), variant.modelState().asModelState());
                leds.put(side, led);
                activeLEDs.put(side, transform(baker, led, LED_TRANSFORMS));
            }
            for (Map.Entry<RelativeSide, Variant> entry : this.ports.entrySet()) {
                RelativeSide side = entry.getKey();
                Variant variant = entry.getValue();
                BlockModelPart port = SimpleModelWrapper.bake(baker, variant.modelLocation(), variant.modelState().asModelState());
                ports.put(side, port);
                activePorts.put(side, transform(baker, port, QuadTransformation.filtered_fullbright));
            }

            BlockModelPart baseModel = SimpleModelWrapper.bake(baker, frame.modelLocation(), frame.modelState().asModelState());

            return new EnergyCubeModel(baseModel, leds, activeLEDs, ports, activePorts);
        }

        public BlockModelPart transform(ModelBaker baker, BlockModelPart variant, QuadTransformation transformation) {
            //TODO - 1.21.11: Make this into more of a proper helper?
            QuadCollection.Builder builder = new QuadCollection.Builder();
            for (BakedQuad quad : QuadUtils.transformBakedQuads(variant.getQuads(null), transformation)) {
                builder.addUnculledFace(quad);
            }
            for (Direction direction : EnumUtils.DIRECTIONS) {
                for (BakedQuad quad : QuadUtils.transformBakedQuads(variant.getQuads(direction), transformation)) {
                    builder.addCulledFace(direction, quad);
                }
            }
            //TODO - 1.21.11: Do we need to somehow actually bake it so that it has a different name and such?
            //TODO - 1.21.11: Figure out the render type to pass?
            return new SimpleModelWrapper(builder.build(), variant.useAmbientOcclusion(), variant.particleIcon(), null);
            //return SimpleModelWrapper.bake(baker, variant.modelLocation(), variant.modelState().asModelState());
        }

        @Override
        public MapCodec<Unbaked> codec() {
            return MAP_CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            //TODO - 1.21.11: Figure out the dependencies?
        }
    }
}