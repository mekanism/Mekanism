package mekanism.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.IntSupplier;
import mekanism.api.SerializationConstants;
import mekanism.api.functions.FloatSupplier;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.OreVeinConfig;
import mekanism.common.resource.ore.OreType.OreVeinType;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;

public record ResizableOreFeatureConfig(List<TargetBlockState> targetStates, OreVeinType oreVeinType, IntSupplier size,
                                        FloatSupplier discardChanceOnAirExposure) implements FeatureConfiguration {

    public static final Codec<ResizableOreFeatureConfig> CODEC = RecordCodecBuilder.create(builder -> builder.group(
          Codec.list(OreConfiguration.TargetBlockState.CODEC).fieldOf(SerializationConstants.TARGETS).forGetter(config -> config.targetStates),
          OreVeinType.CODEC.fieldOf(SerializationConstants.ORE_TYPE).forGetter(config -> config.oreVeinType)
    ).apply(builder, (targetStates, oreVeinType) -> {
        OreVeinConfig veinConfig = MekanismConfig.world.getVeinConfig(oreVeinType);
        return new ResizableOreFeatureConfig(targetStates, oreVeinType, veinConfig.maxVeinSize(), veinConfig.discardChanceOnAirExposure());
    }));
}