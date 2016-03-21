package mekanism.common.tile;

import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class TileEntityPersonalChest extends TileEntityContainerBlock implements ISecurityTile
{
	public static int[] INV;

	public float lidAngle;

	public float prevLidAngle;
	
	public TileComponentSecurity securityComponent;

	public TileEntityPersonalChest()
	{
		super("PersonalChest");
		inventory = new ItemStack[54];
		
		securityComponent = new TileComponentSecurity(this);
	}

	@Override
	public void onUpdate()
	{
		prevLidAngle = lidAngle;
		float increment = 0.1F;

		if((playersUsing.size() > 0) && (lidAngle == 0.0F))
		{
			worldObj.playSoundEffect(xCoord + 0.5F, yCoord + 0.5D, zCoord + 0.5F, "random.chestopen", 0.5F, (worldObj.rand.nextFloat()*0.1F) + 0.9F);
		}

		if((playersUsing.size() == 0 && lidAngle > 0.0F) || (playersUsing.size() > 0 && lidAngle < 1.0F))
		{
			float angle = lidAngle;

			if(playersUsing.size() > 0)
			{
				lidAngle += increment;
			}
			else {
				lidAngle -= increment;
			}

			if(lidAngle > 1.0F)
			{
				lidAngle = 1.0F;
			}

			float split = 0.5F;

			if(lidAngle < split && angle >= split)
			{
				worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, "random.chestclosed", 0.5F, (worldObj.rand.nextFloat()*0.1F) + 0.9F);
			}

			if(lidAngle < 0.0F)
			{
				lidAngle = 0.0F;
			}
		}
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		return true;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(side == 0 || SecurityUtils.getSecurity(this) != SecurityMode.PUBLIC)
		{
			return InventoryUtils.EMPTY;
		}
		else {
			if(INV == null)
			{
				INV = new int[55];

				for(int i = 0; i < INV.length; i++)
				{
					INV[i] = i;
				}
			}

			return INV;
		}
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		return true;
	}

	@Override
	public boolean wrenchCanRemove(EntityPlayer entityPlayer)
	{
		return SecurityUtils.canAccess(entityPlayer, this);
	}

	@Override
	public boolean canSetFacing(int side)
	{
		return side != 0 && side != 1;
	}

	@Override
	public TileComponentSecurity getSecurity() 
	{
		return securityComponent;
	}
}
