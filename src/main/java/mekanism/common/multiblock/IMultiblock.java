package mekanism.common.multiblock;

public interface IMultiblock<T extends MultiblockData> extends IMultiblockBase {

    void setMultiblock(T multiblock);
}