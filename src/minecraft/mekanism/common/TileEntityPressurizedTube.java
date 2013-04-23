package mekanism.common;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.api.EnumGas;
import mekanism.api.IPressurizedTube;
import mekanism.api.ITubeConnection;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityPressurizedTube extends TileEntity implements IPressurizedTube, ITubeConnection
{
	/** The gas currently displayed in this tube. */
	public EnumGas refGas = null;
	
	/** The scale of the gas (0F -> 1F) currently inside this tube. */
	public float gasScale;
	
	@Override
	public void updateEntity()
	{
		if(worldObj.isRemote)
		{
			if(gasScale > 0)
			{
				gasScale -= .01;
			}
			else {
				refGas = null;
			}
		}
	}
	
	@Override
	public boolean canTransferGas(TileEntity fromTile)
	{
		return worldObj.getBlockPowerInput(xCoord, yCoord, zCoord) == 0;
	}
	
	@Override
	public void onTransfer(EnumGas type)
	{
		if(type == refGas)
		{
			gasScale = Math.min(1, gasScale+.02F);
		}
		else if(refGas == null)
		{
			refGas = type;
			gasScale += Math.min(1, gasScale+.02F);
		}
	}
	
	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return true;
	}
	
	@Override
	public boolean canUpdate()
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
