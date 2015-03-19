package mekanism.common.base;

import mekanism.common.CTMData;

import net.minecraft.world.IBlockAccess;

public interface IBlockCTM
{
	public CTMData getCTMData(IBlockAccess world, int x, int y, int z, int meta);
}
