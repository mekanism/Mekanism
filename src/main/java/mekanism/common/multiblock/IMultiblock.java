package mekanism.common.multiblock;

import java.util.UUID;

public interface IMultiblock<T extends MultiblockData> extends IMultiblockBase {

    void setMultiblock(T multiblock);

    MultiblockManager<T> getManager();

    UUID getCacheID();

    MultiblockCache<T> getCache();

    void resetCache();
}