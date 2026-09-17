package mekanism.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.BitSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntSupplier;
import mekanism.api.SerializationConstants;
import mekanism.api.functions.FloatSupplier;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.OreVeinConfig;
import mekanism.common.registries.MekanismFeatureTypes;
import mekanism.common.resource.ore.OreType.OreVeinType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.AbstractOreFeature;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;

//Modified copy of vanilla's OreFeature but to support ResizableOreFeatureConfig
public class ResizableOreFeature extends AbstractOreFeature {

    public static final MapCodec<ResizableOreFeature> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          Codec.list(BlockReplacement.CODEC).fieldOf(SerializationConstants.TARGETS).forGetter(feature -> feature.targetStates),
          OreVeinType.CODEC.fieldOf(SerializationConstants.ORE_TYPE).forGetter(feature -> feature.oreVeinType),
          Codec.BOOL.optionalFieldOf(SerializationConstants.RETRO_GEN, false).forGetter(feature -> feature.retrogen)
    ).apply(builder, (targetStates, oreVeinType, retrogen) -> {
        OreVeinConfig veinConfig = MekanismConfig.world.getVeinConfig(oreVeinType);
        return new ResizableOreFeature(targetStates, oreVeinType, veinConfig.maxVeinSize(), veinConfig.discardChanceOnAirExposure(), retrogen);
    }));

    private final OreVeinType oreVeinType;
    private final IntSupplier size;
    private final FloatSupplier discardChanceOnAirExposure;
    private final boolean retrogen;

    public ResizableOreFeature(List<BlockReplacement> targetStates, OreVeinType oreVeinType, IntSupplier size, FloatSupplier discardChanceOnAirExposure, boolean retrogen) {
        super(targetStates, size.getAsInt(), discardChanceOnAirExposure.getAsFloat());
        this.oreVeinType = oreVeinType;
        this.size = size;
        this.discardChanceOnAirExposure = discardChanceOnAirExposure;
        this.retrogen = retrogen;
    }

    private Heightmap.Types getHeightmapType() {
        //Use OCEAN_FLOOR instead of OCEAN_FLOOR_WG as the chunks are already generated
        return retrogen ? Heightmap.Types.OCEAN_FLOOR : Heightmap.Types.OCEAN_FLOOR_WG;
    }

    @Override
    public MapCodec<ResizableOreFeature> codec() {
        return MekanismFeatureTypes.ORE.get();
    }

    @Override
    public final int size() {
        return this.size.getAsInt();
    }

    @Override
    public final float discardChanceOnAirExposure() {
        return this.discardChanceOnAirExposure.getAsFloat();
    }

    @Override
    public boolean canPlaceOre(BlockState state, Function<BlockPos, BlockState> blockGetter, RandomSource random, BlockReplacement targetState, BlockPos.MutableBlockPos orePos) {
        if (!targetState.target().test(state, orePos, random)) {
            return false;
        }
        return shouldSkipAirCheck(random, discardChanceOnAirExposure()) || !isAdjacentToAir(blockGetter, orePos);
    }

    /// Copy of [net.minecraft.world.level.levelgen.feature.OreFeature#place] but modified to query our adjustable size and heightmap
    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        float dir = random.nextFloat() * (float) Math.PI;
        int size = size();
        float spreadXY = size / 8.0F;
        int maxRadius = Mth.ceil((size / 16.0F * 2.0F + 1.0F) / 2.0F);
        double x0 = origin.getX() + Math.sin(dir) * spreadXY;
        double x1 = origin.getX() - Math.sin(dir) * spreadXY;
        double z0 = origin.getZ() + Math.cos(dir) * spreadXY;
        double z1 = origin.getZ() - Math.cos(dir) * spreadXY;
        int spreadY = 2;
        double y0 = origin.getY() + random.nextInt(3) - spreadY;
        double y1 = origin.getY() + random.nextInt(3) - spreadY;
        int xStart = origin.getX() - Mth.ceil(spreadXY) - maxRadius;
        int yStart = origin.getY() - spreadY - maxRadius;
        int zStart = origin.getZ() - Mth.ceil(spreadXY) - maxRadius;
        int sizeXZ = 2 * (Mth.ceil(spreadXY) + maxRadius);
        int sizeY = spreadY * (spreadY + maxRadius);

        for (int xprobe = xStart; xprobe <= xStart + sizeXZ; xprobe++) {
            for (int zprobe = zStart; zprobe <= zStart + sizeXZ; zprobe++) {
                if (yStart <= level.getHeight(getHeightmapType(), xprobe, zprobe)) {
                    return this.doPlace(level, random, x0, x1, z0, z1, y0, y1, xStart, yStart, zStart, sizeXZ, sizeY);
                }
            }
        }
        return false;
    }

    /// Copy of [net.minecraft.world.level.levelgen.feature.OreFeature#doPlace] but modified to query our adjustable size
    protected boolean doPlace(WorldGenLevel level, RandomSource random, double x0, double x1, double z0, double z1, double y0, double y1, int xStart, int yStart,
          int zStart, int sizeXZ, int sizeY) {
        int placed = 0;
        BitSet tested = new BitSet(sizeXZ * sizeY * sizeXZ);
        BlockPos.MutableBlockPos orePos = new BlockPos.MutableBlockPos();
        int size = size();
        double[] data = new double[size * 4];

        for (int i = 0; i < size; i++) {
            float step = (float) i / size;
            double xx = Mth.lerp(step, x0, x1);
            double yy = Mth.lerp(step, y0, y1);
            double zz = Mth.lerp(step, z0, z1);
            double ss = random.nextDouble() * size / 16D;
            double r = ((Mth.sin(Mth.PI * step) + 1) * ss + 1) / 2D;
            int i4 = i * 4;
            data[i4] = xx;
            data[i4 + 1] = yy;
            data[i4 + 2] = zz;
            data[i4 + 3] = r;
        }

        for (int i = 0; i < size - 1; i++) {
            int i4 = i * 4;
            if (data[i4 + 3] > 0.0) {
                for (int j = i + 1; j < size; j++) {
                    int j4 = j * 4;
                    if (data[j4 + 3] > 0.0) {
                        double dx = data[i4] - data[j4];
                        double dy = data[i4 + 1] - data[j4 + 1];
                        double dz = data[i4 + 2] - data[j4 + 2];
                        double dr = data[i4 + 3] - data[j4 + 3];
                        if (dr * dr > dx * dx + dy * dy + dz * dz) {
                            if (dr > 0.0) {
                                data[j4 + 3] = -1.0;
                            } else {
                                data[i4 + 3] = -1;
                            }
                        }
                    }
                }
            }
        }

        try (BulkSectionAccess sectionGetter = new BulkSectionAccess(level)) {
            for (int i = 0; i < size; i++) {
                double r = data[i * 4 + 3];
                if (!(r < 0.0)) {
                    double xx = data[i * 4];
                    double yy = data[i * 4 + 1];
                    double zz = data[i * 4 + 2];
                    int xMin = Math.max(Mth.floor(xx - r), xStart);
                    int yMin = Math.max(Mth.floor(yy - r), yStart);
                    int zMin = Math.max(Mth.floor(zz - r), zStart);
                    int xMax = Math.max(Mth.floor(xx + r), xMin);
                    int yMax = Math.max(Mth.floor(yy + r), yMin);
                    int zMax = Math.max(Mth.floor(zz + r), zMin);

                    for (int x = xMin; x <= xMax; x++) {
                        double xd = (x + 0.5 - xx) / r;
                        if (xd * xd < 1.0) {
                            for (int y = yMin; y <= yMax; y++) {
                                double yd = (y + 0.5 - yy) / r;
                                if (xd * xd + yd * yd < 1.0) {
                                    for (int z = zMin; z <= zMax; z++) {
                                        double zd = (z + 0.5 - zz) / r;
                                        if (xd * xd + yd * yd + zd * zd < 1.0 && !level.isOutsideBuildHeight(y)) {
                                            int bitSetIndex = x - xStart + (y - yStart) * sizeXZ + (z - zStart) * sizeXZ * sizeY;
                                            if (!tested.get(bitSetIndex)) {
                                                tested.set(bitSetIndex);
                                                orePos.set(x, y, z);
                                                if (level.ensureCanWrite(orePos)) {
                                                    LevelChunkSection section = sectionGetter.getSection(orePos);
                                                    if (section != null) {
                                                        int sectionRelativeX = SectionPos.sectionRelative(x);
                                                        int sectionRelativeY = SectionPos.sectionRelative(y);
                                                        int sectionRelativeZ = SectionPos.sectionRelative(z);
                                                        BlockState blockState = section.getBlockState(sectionRelativeX, sectionRelativeY, sectionRelativeZ);

                                                        for (BlockReplacement targetState : this.targetStates) {
                                                            if (canPlaceOre(blockState, sectionGetter::getBlockState, random, targetState, orePos)) {
                                                                section.setBlockState(sectionRelativeX, sectionRelativeY, sectionRelativeZ, targetState.state(), false);
                                                                placed++;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return placed > 0;
    }
}