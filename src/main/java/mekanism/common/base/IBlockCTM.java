package mekanism.common.base;

import mekanism.common.CTMData;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface IBlockCTM
{
	public CTMData getCTMData(IBlockAccess world, int x, int y, int z, int meta);
}
