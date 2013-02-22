package mekanism.common;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.api.IPressurizedTube;
import mekanism.api.ITubeConnection;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityPressurizedTube extends TileEntity implements IPressurizedTube, ITubeConnection
{
	@Override
	public boolean canTransferGas()
	{
		return true;
	}
	
	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}
}
