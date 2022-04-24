package mekanism.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.IntSupplier;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.SaltConfig;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class ResizableDiskConfig implements FeatureConfiguration {

    public static final Codec<ResizableDiskConfig> CODEC = RecordCodecBuilder.create(builder -> builder.group(
          BlockState.CODEC.fieldOf("state").forGetter(config -> config.state)
    ).apply(builder, state -> new ResizableDiskConfig(state, MekanismConfig.world.salt)));

    public final BlockState state;
    public final IntProvider radius;
    public final IntSupplier halfHeight;
    public final List<BlockState> targets;

    public ResizableDiskConfig(BlockState state, SaltConfig saltConfig) {
        this.state = state;
        this.radius = ConfigurableUniformInt.SALT;
        this.halfHeight = saltConfig.halfHeight;
        this.targets = List.of(Blocks.DIRT.defaultBlockState(), Blocks.CLAY.defaultBlockState(), this.state);
    }
}