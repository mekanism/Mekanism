package mekanism.common.block.interfaces;

public interface IBlockMekanism {

    default boolean hasDescription() {
        return false;
    }

    //TODO: This should only have to be implemented if hasDescription is true
    //TODO: Interface of IBlockDescriptive
    default String getDescription() {
        return "";
    }
}