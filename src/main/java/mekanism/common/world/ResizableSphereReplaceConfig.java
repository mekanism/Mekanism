package mekanism.common.world;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.function.IntSupplier;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.SaltConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.feature.IFeatureConfig;

public class ResizableSphereReplaceConfig implements IFeatureConfig {

    public static final Codec<ResizableSphereReplaceConfig> CODEC = RecordCodecBuilder.create(builder -> builder.group(
          BlockState.CODEC.fieldOf("state").forGetter(config -> config.state)
    ).apply(builder, state -> new ResizableSphereReplaceConfig(state, MekanismConfig.world.salt)));

    public final BlockState state;
    public final IntSupplier baseRadius;
    public final IntSupplier spread;
    public final IntSupplier ySize;
    public final List<BlockState> targets;

    public ResizableSphereReplaceConfig(BlockState state, SaltConfig saltConfig) {
        this.state = state;
        this.baseRadius = saltConfig.baseRadius;
        this.spread = saltConfig.spread;
        this.ySize = saltConfig.ySize;
        this.targets = ImmutableList.of(Blocks.DIRT.getDefaultState(), Blocks.CLAY.getDefaultState(), this.state);
    }

    public int getRadius(Random rand) {
        int spread = this.spread.getAsInt();
        return spread == 0 ? baseRadius.getAsInt() : baseRadius.getAsInt() + rand.nextInt(spread + 1);
    }
}