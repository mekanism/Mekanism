package mekanism.common.block;

import net.minecraft.util.EnumFacing;

public interface IBlockMekanism {

    default boolean hasDescription() {
        return false;
    }

    String getDescription();

    default boolean hasActiveTexture() {
        return false;
    }

    boolean canRotateTo(EnumFacing side);

    boolean hasRotations();
}