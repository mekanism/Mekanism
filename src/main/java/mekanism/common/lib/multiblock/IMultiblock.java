package mekanism.common.lib.multiblock;

import java.util.UUID;
import mekanism.common.lib.multiblock.UpdateProtocol.FormationResult;

public interface IMultiblock<T extends MultiblockData> extends IMultiblockBase {

    T createMultiblock();

    default T getMultiblock() {
        return (T) IMultiblockBase.super.getMultiblockData();
    }

    UpdateProtocol<T> getProtocol();

    @Override
    T getDefaultData();

    FormationResult runUpdate(UpdateType updateRequested, Cuboid cuboid);

    MultiblockManager<T> getManager();

    UUID getCacheID();

    MultiblockCache<T> getCache();

    void resetCache();
}