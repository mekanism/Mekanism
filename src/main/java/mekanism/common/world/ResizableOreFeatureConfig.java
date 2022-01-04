package mekanism.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.IntSupplier;
import mekanism.api.functions.FloatSupplier;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.OreConfig;
import mekanism.common.resource.OreType;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;

public record ResizableOreFeatureConfig(List<TargetBlockState> targetStates, OreType oreType, IntSupplier size,
                                        FloatSupplier discardChanceOnAirExposure) implements FeatureConfiguration {

    public static final Codec<ResizableOreFeatureConfig> CODEC = RecordCodecBuilder.create(builder -> builder.group(
          Codec.list(OreConfiguration.TargetBlockState.CODEC).fieldOf("targets").forGetter(config -> config.targetStates),
          OreType.CODEC.fieldOf("oreType").forGetter(config -> config.oreType)
    ).apply(builder, (targetStates, oreType) -> {
        OreConfig oreConfig = MekanismConfig.world.ores.get(oreType);
        return new ResizableOreFeatureConfig(targetStates, oreType, oreConfig.maxVeinSize, oreConfig.discardChanceOnAirExposure);
    }));
}