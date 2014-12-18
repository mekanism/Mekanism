package mekanism.common.base;

import mekanism.common.CTMData;

import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IBlockCTM
{
	public CTMData getCTMData(IBlockAccess world, BlockPos pos, int meta);
}
