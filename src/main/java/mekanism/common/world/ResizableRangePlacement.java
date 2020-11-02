package mekanism.common.world;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.placement.SimplePlacement;

public class ResizableRangePlacement extends SimplePlacement<ResizableTopSolidRangeConfig> {

    public ResizableRangePlacement(Codec<ResizableTopSolidRangeConfig> codec) {
        super(codec);
    }

    @Nonnull
    @Override
    public Stream<BlockPos> getPositions(@Nonnull Random random, @Nonnull ResizableTopSolidRangeConfig config, @Nonnull BlockPos pos) {
        int y = random.nextInt(config.maximum.getAsInt() - config.topOffset.getAsInt()) + config.bottomOffset.getAsInt();
        return Stream.of(new BlockPos(pos.getX(), y, pos.getZ()));
    }
}