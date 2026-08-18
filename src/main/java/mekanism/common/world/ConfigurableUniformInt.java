package mekanism.common.world;

import com.mojang.serialization.MapCodec;
import mekanism.common.config.MekanismConfig;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;

public class ConfigurableUniformInt implements IntProvider {

    public static final ConfigurableUniformInt SALT = new ConfigurableUniformInt();
    public static final MapCodec<ConfigurableUniformInt> CODEC = MapCodec.unit(SALT);

    private ConfigurableUniformInt() {
    }

    @Override
    public int sample(RandomSource random) {
        return random.nextIntBetweenInclusive(minInclusive(), maxInclusive());
    }

    @Override
    public int minInclusive() {
        return MekanismConfig.world.salt.minRadius.get();
    }

    @Override
    public int maxInclusive() {
        return MekanismConfig.world.salt.maxRadius.get();
    }

    @Override
    public MapCodec<? extends IntProvider> codec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "[" + minInclusive() + "-" + maxInclusive() + "]";
    }
}