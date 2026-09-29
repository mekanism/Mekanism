package mekanism.additions.common.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.additions.common.registries.AdditionsIntProviderTypes;
import mekanism.api.SerializationConstants;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;

public record ScaledIntProvider(IntProvider base, float scale) implements IntProvider {

    public static final MapCodec<ScaledIntProvider> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          IntProviders.CODEC.fieldOf(SerializationConstants.BASE).forGetter(ScaledIntProvider::base),
          ExtraCodecs.NON_NEGATIVE_FLOAT.fieldOf(SerializationConstants.SCALE).forGetter(ScaledIntProvider::scale)
    ).apply(builder, ScaledIntProvider::new));

    @Override
    public int sample(RandomSource random) {
        return scaleValue(base.sample(random));
    }

    @Override
    public int minInclusive() {
        return scaleValue(base.minInclusive());
    }

    @Override
    public int maxInclusive() {
        return scaleValue(base.maxInclusive());
    }

    @Override
    public MapCodec<? extends IntProvider> codec() {
        return AdditionsIntProviderTypes.SCALED.get();
    }

    private int scaleValue(int value) {
        return scale == 0 ? 0 : Mth.ceil(value * scale);
    }
}