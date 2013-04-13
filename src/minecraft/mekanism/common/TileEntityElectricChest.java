package mekanism.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.common.io.ByteArrayDataInput;

import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import universalelectricity.core.item.ElectricItemHelper;
import universalelectricity.core.item.IItemElectric;
import mekanism.api.InfusionType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityElectricChest extends TileEntityElectricBlock
{
	public String password = "";
	
	public final int MAX_ACCESS = 2400;
	
	public boolean authenticated = false;
	
	public boolean locked = false;
	
	public float lidAngle;
	
	public float prevLidAngle;
	
	public TileEntityElectricChest()
	{
		super("Electric Chest", 12000);
		inventory = new ItemStack[55];
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
	
		prevLidAngle = lidAngle;
	    float increment = 0.1F;

	    if((playersUsing > 0) && (lidAngle == 0.0F)) 
	    {
	    	worldObj.playSoundEffect(xCoord + 0.5F, yCoord + 0.5D, zCoord + 0.5F, "random.chestopen", 0.5F, (worldObj.rand.nextFloat()*0.1F) + 0.9F);
	    }

	    if((playersUsing == 0 && lidAngle > 0.0F) || (playersUsing > 0 && lidAngle < 1.0F))
	    {
	    	float angle = lidAngle;

	    	if(playersUsing > 0)
	    	{
	    		lidAngle += increment;
	    	}
	    	else {
	    		lidAngle -= increment;
	    	}

	    	if (lidAngle > 1.0F)
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
		
		if(inventory[54] != null)
		{
			if(electricityStored < MAX_ELECTRICITY)
			{
				setJoules(getJoules() + ElectricItemHelper.dechargeItem(inventory[54], getMaxJoules() - getJoules(), getVoltage()));
				
				if(Mekanism.hooks.IC2Loaded && inventory[54].getItem() instanceof IElectricItem)
				{
					IElectricItem item = (IElectricItem)inventory[54].getItem();
					if(item.canProvideEnergy(inventory[54]))
					{
						double gain = ElectricItem.discharge(inventory[54], (int)((MAX_ELECTRICITY - electricityStored)*Mekanism.TO_IC2), 3, false, false)*Mekanism.FROM_IC2;
						setJoules(electricityStored + gain);
					}
				}
			}
			if(inventory[54].itemID == Item.redstone.itemID && electricityStored+1000 <= MAX_ELECTRICITY)
			{
				setJoules(electricityStored + 1000);
				inventory[54].stackSize--;
				
	            if(inventory[54].stackSize <= 0)
	            {
	                inventory[54] = null;
	            }
			}
		}
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        authenticated = nbtTags.getBoolean("authenticated");
        locked = nbtTags.getBoolean("locked");
        password = nbtTags.getString("password");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setBoolean("authenticated", authenticated);
        nbtTags.setBoolean("locked", locked);
        nbtTags.setString("password", password);
    }
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		authenticated = dataStream.readBoolean();
		locked = dataStream.readBoolean();
		password = dataStream.readUTF();
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(authenticated);
		data.add(locked);
		data.add(password);
		return data;
	}
	
	public boolean canAccess()
	{
		return authenticated && (getJoules() == 0 || !locked);
	}
	
	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 54)
		{
			return (itemstack.getItem() instanceof IElectricItem && ((IElectricItem)itemstack.getItem()).canProvideEnergy(itemstack)) || 
					(itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).amperes != 0) || 
					itemstack.itemID == Item.redstone.itemID;
		}
		else {
			return true;
		}
	}
	
	public int getScaledEnergyLevel(int i)
	{
		return (int)(electricityStored*i / MAX_ELECTRICITY);
	}
	
	@Override
	public int[] getSizeInventorySide(int side) 
	{
		if(side == 0)
		{
			return new int[] {1};
		}
		else {
			int[] ret = new int[54];
			
			for(int i = 0; i <= 54; i++)
			{
				ret[i] = i;
			}
			
			return ret;
		}
	}

	@Override
	public boolean func_102008_b(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 54)
		{
			return (itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).getWatts() == 0) ||
					(itemstack.getItem() instanceof IElectricItem && ((IElectricItem)itemstack.getItem()).canProvideEnergy(itemstack) && 
							(!(itemstack.getItem() instanceof IItemElectric) || 
							((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).getWatts() == 0));
		}
		else {
			return true;
		}
	}
	
	@Override
	public int getStartInventorySide(ForgeDirection side) 
	{
        if(side == ForgeDirection.DOWN)
        {
        	return 54;
        }
        else {
        	return 0;
        }
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		if(side == ForgeDirection.DOWN)
		{
			return 1;
		}
		else {
			return 54;
		}
	}
	
	@Override
	public boolean wrenchCanRemove(EntityPlayer entityPlayer) 
	{
		return false;
	}
}
