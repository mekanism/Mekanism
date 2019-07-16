package mekanism.common.world;

import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.DimensionType;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

/**
 * Dummy object for providing something to CraftingManager#findMatchingRecipe during startup, to prevent in-dev non-null assertions from breaking things
 */
public class DummyWorld extends World {

    public DummyWorld() {
        super(new DummySaveHandler(), new DummyWorldInfo(), new WorldProvider() {
            @Override
            public DimensionType getDimensionType() {
                return DimensionType.OVERWORLD;
            }
        }, new Profiler(), false);
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return false;
    }

    public static class DummyWorldInfo extends WorldInfo {}

    public static class DummySaveHandler implements ISaveHandler {

        @Nullable
        @Override
        public WorldInfo loadWorldInfo() {
            return null;
        }

        @Override
        public void checkSessionLock() throws MinecraftException {

        }

        @Override
        public IChunkLoader getChunkLoader(WorldProvider provider) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound) {

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
}
