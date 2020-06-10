package mekanism.common.lib.multiblock;

import java.util.UUID;

public interface IMultiblock<T extends MultiblockData> extends IMultiblockBase {

    T createMultiblock();

    default T getMultiblock() {
        return (T) IMultiblockBase.super.getMultiblockData(getManager());
    }

    @Override
    T getDefaultData();

    MultiblockManager<T> getManager();

    UUID getCacheID();

    MultiblockCache<T> getCache();

    void resetCache();

    void setCache(MultiblockCache<T> cache);

    boolean isMaster();

    Structure getStructure();

    void setStructure(Structure structure);

    @Override
    default void setStructure(MultiblockManager<?> manager, Structure structure) {
        if (manager == getManager()) {
            setStructure(structure);
        }
    }

    @Override
    default Structure getStructure(MultiblockManager<?> manager) {
        if (manager == getManager()) {
            return getStructure();
        }
        return null;
    }

    @Override
    default boolean hasStructure(Structure structure) {
        return getStructure() == structure;
    }

    default boolean hasCache() {
        return getCache() != null;
    }

    default FormationProtocol<T> createFormationProtocol() {
        return new FormationProtocol<>(this, getStructure());
    }
}