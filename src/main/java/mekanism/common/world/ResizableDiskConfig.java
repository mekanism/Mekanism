package mekanism.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.IntSupplier;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.SaltConfig;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;

public class ResizableDiskConfig implements FeatureConfiguration {

    public static final Codec<ResizableDiskConfig> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            RuleBasedBlockStateProvider.CODEC.fieldOf("state_provider").forGetter(config -> config.stateProvider)
    ).apply(builder, stateProvider -> new ResizableDiskConfig(stateProvider, MekanismConfig.world.salt)));

    public final RuleBasedBlockStateProvider stateProvider;
    public final IntProvider radius;
    public final IntSupplier halfHeight;
    public final BlockPredicate target;

    public ResizableDiskConfig(RuleBasedBlockStateProvider stateProvider, SaltConfig saltConfig) {
        this.stateProvider = stateProvider;
        this.radius = ConfigurableUniformInt.SALT;
        this.halfHeight = saltConfig.halfHeight;
        this.target = BlockPredicate.matchesBlocks(Blocks.DIRT, Blocks.CLAY);
    }
}