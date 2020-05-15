package mekanism.common.multiblock;

public interface IMultiblock<T extends MultiblockData> extends IMultiblockBase {

    void markUpdated();

    void setMultiblock(T multiblock);
}