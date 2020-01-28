package mekanism.common.base;

//TODO: Go through and fix anything that might have gotten broken when optimizing the TPS
// somethings like the teleporter and laser amplifier may not be properly caching as well as the logistical sorter
public interface IComparatorSupport {

    default boolean supportsComparator() {
        return true;
    }

    int getRedstoneLevel();

    int getCurrentRedstoneLevel();
}