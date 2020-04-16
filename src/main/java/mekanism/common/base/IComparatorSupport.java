package mekanism.common.base;

public interface IComparatorSupport {

    default boolean supportsComparator() {
        return true;
    }

    int getRedstoneLevel();

    int getCurrentRedstoneLevel();
}