package mekanism.common.world;

import com.mojang.serialization.Codec;
import javax.annotation.Nonnull;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.TopSolidOnce;

public class TopSolidRetrogenPlacement extends TopSolidOnce {

    public TopSolidRetrogenPlacement(Codec<NoPlacementConfig> configFactory) {
        super(configFactory);
    }

    @Nonnull
    @Override
    protected Heightmap.Type func_241858_a(@Nonnull NoPlacementConfig config) {
        return Heightmap.Type.OCEAN_FLOOR;
    }
}