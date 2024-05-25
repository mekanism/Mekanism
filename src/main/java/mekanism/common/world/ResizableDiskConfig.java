package mekanism.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.IntSupplier;
import mekanism.api.SerializationConstants;
import mekanism.common.config.MekanismConfig;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;

public record ResizableDiskConfig(RuleBasedBlockStateProvider stateProvider, BlockPredicate target, IntProvider radius, IntSupplier halfHeight) implements FeatureConfiguration {

    public static final Codec<ResizableDiskConfig> CODEC = RecordCodecBuilder.create(builder -> builder.group(
          RuleBasedBlockStateProvider.CODEC.fieldOf(SerializationConstants.STATE_PROVIDER).forGetter(ResizableDiskConfig::stateProvider),
          BlockPredicate.CODEC.fieldOf(SerializationConstants.TARGET).forGetter(ResizableDiskConfig::target),
          IntProvider.CODEC.fieldOf(SerializationConstants.RADIUS).forGetter(ResizableDiskConfig::radius)
    ).apply(builder, ResizableDiskConfig::new));

    public ResizableDiskConfig(RuleBasedBlockStateProvider stateProvider, BlockPredicate target, IntProvider radius) {
        this(stateProvider, target, radius, MekanismConfig.world.salt.halfHeight);
    }

    public DiskConfiguration asVanillaConfig() {
        return new DiskConfiguration(stateProvider, target, radius, halfHeight.getAsInt());
    }
}