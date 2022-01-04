package mekanism.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Random;
import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.config.MekanismConfig;
import mekanism.common.resource.OreType;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

public class ConfigurableConstantInt extends IntProvider {

    public static final Codec<ConfigurableConstantInt> CODEC = RecordCodecBuilder.create(builder -> builder.group(
          OreType.CODEC.optionalFieldOf("oreType").forGetter(config -> Optional.ofNullable(config.oreType))
    ).apply(builder, oreType -> {
        if (oreType.isPresent()) {
            OreType type = oreType.get();
            return new ConfigurableConstantInt(type, MekanismConfig.world.ores.get(type).perChunk);
        }
        return new ConfigurableConstantInt(null, MekanismConfig.world.salt.perChunk);
    }));

    @Nullable
    private final OreType oreType;
    private final IntSupplier value;

    public ConfigurableConstantInt(@Nullable OreType oreType, IntSupplier value) {
        this.oreType = oreType;
        this.value = value;
    }

    public int getValue() {
        return this.value.getAsInt();
    }

    @Override
    public int sample(@Nonnull Random random) {
        return getValue();
    }

    @Override
    public int getMinValue() {
        return getValue();
    }

    @Override
    public int getMaxValue() {
        return getValue();
    }

    @Nonnull
    @Override
    public IntProviderType<?> getType() {
        return GenHandler.CONFIGURABLE_CONSTANT;
    }

    @Override
    public String toString() {
        return Integer.toString(getValue());
    }
}