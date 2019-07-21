package mekanism.common.block.interfaces;

import net.minecraft.util.EnumFacing;

public interface IRotatableBlock {

    boolean canRotateTo(EnumFacing side);
}