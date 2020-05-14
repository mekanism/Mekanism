package mekanism.common.multiblock;

public interface IMultiblock<T extends MultiblockData> extends IMultiblockBase {

    @Override
    T getMultiblock();

    void markUpdated();
}