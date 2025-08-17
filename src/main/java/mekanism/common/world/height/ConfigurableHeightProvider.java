package mekanism.common.world.height;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import mekanism.api.SerializationConstants;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.OreVeinConfig;
import mekanism.common.registries.MekanismHeightProviderTypes;
import mekanism.common.resource.ore.OreType.OreVeinType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import org.jetbrains.annotations.NotNull;

public class ConfigurableHeightProvider extends HeightProvider {

    public static final MapCodec<ConfigurableHeightProvider> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          OreVeinType.CODEC.fieldOf(SerializationConstants.ORE_TYPE).forGetter(config -> config.oreVeinType)
    ).apply(builder, type -> new ConfigurableHeightProvider(type, MekanismConfig.world.getVeinConfig(type))));

    private final OreVeinType oreVeinType;
    private final ConfigurableHeightRange range;
    private LongSet warnedFor;

    private ConfigurableHeightProvider(OreVeinType oreVeinType, OreVeinConfig oreConfig) {
        this.oreVeinType = oreVeinType;
        this.range = oreConfig.range();
    }

    public static ConfigurableHeightProvider of(OreVeinType type, OreVeinConfig oreConfig) {
        return new ConfigurableHeightProvider(type, oreConfig);
    }

    @Override
    public int sample(@NotNull RandomSource random, @NotNull WorldGenerationContext context) {
        int min = range.minInclusive().resolveY(context);
        int max = range.maxInclusive().resolveY(context);
        if (min > max) {
            if (warnedFor == null) {
                warnedFor = new LongOpenHashSet();
            }
            if (warnedFor.add((long) min << 32 | max)) {
                Mekanism.logger.warn("Empty height range: {}", this);
            }
            return min;
        }
        return switch (range.shape().get()) {
            case TRAPEZOID -> sampleTrapezoid(random, min, max);
            case UNIFORM -> Mth.randomBetweenInclusive(random, min, max);
        };
    }

    private int sampleTrapezoid(@NotNull RandomSource random, int min, int max) {
        int plateau = range.plateau().getAsInt();
        int range = max - min;
        if (plateau >= range) {
            return Mth.randomBetweenInclusive(random, min, max);
        }
        int middle = (range - plateau) / 2;
        return min + Mth.randomBetweenInclusive(random, 0, range - middle) + Mth.randomBetweenInclusive(random, 0, middle);
    }

    @NotNull
    @Override
    public HeightProviderType<?> getType() {
        return MekanismHeightProviderTypes.CONFIGURABLE.get();
    }

    @Override
    public String toString() {
        switch (range.shape().get()) {
            case TRAPEZOID -> {
                int plateau = range.plateau().getAsInt();
                if (plateau == 0) {
                    return oreVeinType.name() + " triangle [" + range.minInclusive() + "-" + range.maxInclusive() + "]";
                }
                return oreVeinType.name() + " trapezoid(" + plateau + ") in [" + range.minInclusive() + "-" + range.maxInclusive() + "]";
            }
            case UNIFORM -> {
                return oreVeinType.name() + " uniform [" + range.minInclusive() + "-" + range.maxInclusive() + "]";
            }
        }
        return oreVeinType.name();
    }
}