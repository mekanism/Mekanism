package mekanism.common.block;

public interface IBlockMekanism {

    default boolean hasDescription() {
        return false;
    }

    default boolean hasActiveTexture() {
        return false;
    }
}