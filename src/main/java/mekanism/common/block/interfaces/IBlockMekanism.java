package mekanism.common.block.interfaces;

import net.minecraft.util.EnumFacing;

public interface IBlockMekanism {

    default boolean hasDescription() {
        return false;
    }

    //TODO: This should only have to be implemented if hasDescription is true
    default String getDescription() {
        return "";
    }

    default boolean hasActiveTexture() {
        return false;
    }

    boolean canRotateTo(EnumFacing side);

    boolean hasRotations();
}