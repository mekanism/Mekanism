package mekanism.common.world;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.placement.SimplePlacement;

public class AdjustableCountPlacement extends SimplePlacement<AdjustableSpreadConfig> {

    public AdjustableCountPlacement(Codec<AdjustableSpreadConfig> codec) {
        super(codec);
    }

    @Nonnull
    @Override
    public Stream<BlockPos> getPositions(@Nonnull Random random, @Nonnull AdjustableSpreadConfig config, @Nonnull BlockPos pos) {
        return IntStream.range(0, config.getSpread(random)).mapToObj(count -> pos);
    }
}