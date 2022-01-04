package mekanism.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Random;
import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.OreConfig;
import mekanism.common.resource.OreType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;

public class ConfigurableUniformHeight extends HeightProvider {

    public static final Codec<ConfigurableUniformHeight> CODEC = RecordCodecBuilder.create(builder -> builder.group(
          OreType.CODEC.fieldOf("oreType").forGetter(config -> config.oreType)
    ).apply(builder, type -> new ConfigurableUniformHeight(type, MekanismConfig.world.ores.get(type))));

    private final OreType oreType;
    private final IntSupplier minSupplier;
    private final IntSupplier maxSupplier;
    private final LongSet warnedFor = new LongOpenHashSet();

    private ConfigurableUniformHeight(OreType oreType, OreConfig oreConfig) {
        this.oreType = oreType;
        //TODO - 1.18: Source the usages of these properly from the oreConfig
        this.minSupplier = oreConfig.bottomOffset;//VerticalAnchor.bottom();
        this.maxSupplier = oreConfig.maxHeight;//VerticalAnchor.absolute(72);
    }

    public static ConfigurableUniformHeight of(OreType type, OreConfig oreConfig) {
        return new ConfigurableUniformHeight(type, oreConfig);
    }

    @Override
    public int sample(@Nonnull Random random, @Nonnull WorldGenerationContext context) {
        int min = context.getMinGenY() + minSupplier.getAsInt();
        int max = maxSupplier.getAsInt();
        if (min > max) {
            if (warnedFor.add((long) min << 32 | (long) max)) {
                Mekanism.logger.warn("Empty height range: {}", this);
            }
            return min;
        }
        return Mth.randomBetweenInclusive(random, min, max);
    }

    @Nonnull
    @Override
    public HeightProviderType<?> getType() {
        return GenHandler.CONFIGURABLE_UNIFORM_HEIGHT;
    }

    @Override
    public String toString() {
        return oreType.toString();
    }
}