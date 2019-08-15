package mekanism.common.world;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;

/**
 * Dummy object for providing something to CraftingManager#findMatchingRecipe during startup, to prevent in-dev non-null assertions from breaking things
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
//TODO: Reimplement this if needed
public class DummyWorld {

}/* extends World {

    public DummyWorld() {
        super(new DummySaveHandler(), new DummyWorldInfo(), new Dimension() {
            @Override
            public DimensionType getDimensionType() {
                return DimensionType.OVERWORLD;
            }
        }, new Profiler(), false);
    }

    @Override
    protected AbstractChunkProvider createChunkProvider() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return false;
    }

    public static class DummyWorldInfo extends WorldInfo {}

    public static class DummySaveHandler implements SaveHandler {

        @Nullable
        @Override
        public WorldInfo loadWorldInfo() {
            return null;
        }

        @Override
        public void checkSessionLock() throws SessionLockException {

        }

        @Override
        public ChunkLoader getChunkLoader(Dimension provider) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void saveWorldInfoWithPlayer(WorldInfo worldInformation, CompoundNBT tagCompound) {

        }

        @Override
        public void saveWorldInfo(WorldInfo worldInformation) {

        }

        @Override
        public IPlayerFileData getPlayerNBTManager() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void flush() {

        }

        @Override
        public File getWorldDirectory() {
            throw new UnsupportedOperationException();
        }

        @Override
        public File getMapFileFromName(String mapName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TemplateManager getStructureTemplateManager() {
            throw new UnsupportedOperationException();
        }
    }
}*/