package mekanism.common.lib.multiblock;

import java.util.UUID;

public interface IMultiblock<T extends MultiblockData> extends IMultiblockBase {

    T createMultiblock();

    default T getMultiblock() {
        return (T) IMultiblockBase.super.getMultiblockData();
    }

    UpdateProtocol<T> getProtocol();

    @Override
    T getDefaultData();

    MultiblockManager<T> getManager();

    UUID getCacheID();

    MultiblockCache<T> getCache();

    void resetCache();

    default IStructureValidator validateStructure() {
        return new CuboidStructureValidator(getStructure());
    }
}