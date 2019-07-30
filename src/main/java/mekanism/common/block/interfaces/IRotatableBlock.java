package mekanism.common.block.interfaces;

import mekanism.common.block.states.IStateFacing;
import net.minecraft.util.EnumFacing;

public interface IRotatableBlock extends IStateFacing {

    boolean canRotateTo(EnumFacing side);
}