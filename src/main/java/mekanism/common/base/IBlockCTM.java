package mekanism.common.base;

import net.minecraft.world.IBlockAccess;

public interface IBlockCTM
{
	//public CTMData getCTMData(IBlockAccess world, int x, int y, int z, int meta);
	
	public boolean shouldRenderBlock(IBlockAccess world, int x, int y, int z, int meta);
}
