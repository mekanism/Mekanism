package mekanism.common.world;

import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * Dummy object for providing something to CraftingManager#findMatchingRecipe during startup, to prevent in-dev non-null assertions from breaking things
 */
public class DummyWorld extends World {

    public DummyWorld() {
        super(null, null, new WorldProvider() {
            @Override
            public DimensionType getDimensionType() {
                return DimensionType.OVERWORLD;
            }
        }, null, false);
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return false;
    }
}
