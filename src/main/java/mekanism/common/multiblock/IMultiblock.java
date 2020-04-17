package mekanism.common.multiblock;

public interface IMultiblock<T extends MultiblockData<T>> extends IMultiblockBase {

    @Override
    T getMultiblockData();

    void markUpdated();
}