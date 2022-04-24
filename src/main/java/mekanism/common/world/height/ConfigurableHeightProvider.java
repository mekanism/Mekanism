package mekanism.common.world.height;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.OreVeinConfig;
import mekanism.common.registries.MekanismHeightProviderTypes;
import mekanism.common.resource.ore.OreType.OreVeinType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;

public class ConfigurableHeightProvider extends HeightProvider {

    public static final Codec<ConfigurableHeightProvider> CODEC = RecordCodecBuilder.create(builder -> builder.group(
          OreVeinType.CODEC.fieldOf("oreVeinType").forGetter(config -> config.oreVeinType)
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
    public int sample(@Nonnull Random random, @Nonnull WorldGenerationContext context) {
        int min = range.minInclusive().resolveY(context);
        int max = range.maxInclusive().resolveY(context);
        if (min > max) {
            if (warnedFor == null) {
                warnedFor = new LongOpenHashSet();
            }
            if (warnedFor.add((long) min << 32 | (long) max)) {
                Mekanism.logger.warn("Empty height range: {}", this);
            }
            return min;
        }
        return switch (range.shape().get()) {
            case TRAPEZOID -> sampleTrapezoid(random, min, max);
            case UNIFORM -> Mth.randomBetweenInclusive(random, min, max);
        };
    }

    private int sampleTrapezoid(@Nonnull Random random, int min, int max) {
        int plateau = range.plateau().getAsInt();
        int range = max - min;
        if (plateau >= range) {
            return Mth.randomBetweenInclusive(random, min, max);
        }
        int middle = (range - plateau) / 2;
        return min + Mth.randomBetweenInclusive(random, 0, range - middle) + Mth.randomBetweenInclusive(random, 0, middle);
    }

    @Nonnull
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