package mekanism.common.world;

import net.minecraft.world.level.levelgen.Heightmap;

public class OreRetrogenFeature extends ResizableOreFeature {

    @Override
    protected Heightmap.Types getHeightmapType() {
        //Use OCEAN_FLOOR instead of OCEAN_FLOOR_WG as the chunks are already generated
        return Heightmap.Types.OCEAN_FLOOR;
    }
}