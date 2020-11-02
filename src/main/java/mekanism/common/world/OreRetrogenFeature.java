package mekanism.common.world;

import net.minecraft.world.gen.Heightmap;

public class OreRetrogenFeature extends ResizableOreFeature {

    @Override
    protected Heightmap.Type getHeightmapType() {
        //Use OCEAN_FLOOR instead of OCEAN_FLOOR_WG as the chunks are already generated
        return Heightmap.Type.OCEAN_FLOOR;
    }
}