package mekanism.common.base;

//TODO: Move a bunch of this stuff upwards towards TileEntityMekanism in terms of keeping track of current redstone values??
public interface IComparatorSupport {

    //TODO: Evaluate if this logic can be moved into the Block itself
    int getRedstoneLevel();
}