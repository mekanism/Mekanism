package mekanism.common.tileentity;

import ic2.api.energy.tile.IEnergySink;

import java.util.ArrayList;

import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.common.Mekanism;
import mekanism.common.util.ChargeUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityElectricChest extends TileEntityElectricBlock implements IEnergySink, IStrictEnergyAcceptor
{
	public String password = "";
	
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
	    
	    ChargeUtils.discharge(54, this);
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
		return authenticated && (getEnergy() == 0 || !locked);
	}
	
	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 54)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}
		else {
			return true;
		}
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(int side) 
	{
		if(side == 0)
		{
			return new int[] {54};
		}
		else {
			int[] ret = new int[55];
			
			for(int i = 0; i <= ret.length; i++)
			{
				ret[i] = i;
			}
			
			return ret;
		}
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 54)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}
		else {
			return true;
		}
	}
	
	@Override
	public boolean wrenchCanRemove(EntityPlayer entityPlayer) 
	{
		return false;
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) 
	{
		return true;
	}

	@Override
	public double transferEnergyToAcceptor(double amount)
	{
    	double rejects = 0;
    	double neededElectricity = getMaxEnergy()-getEnergy();
    	
    	if(amount <= neededElectricity)
    	{
    		electricityStored += amount;
    	}
    	else {
    		electricityStored += neededElectricity;
    		rejects = amount-neededElectricity;
    	}
    	
    	return rejects;
	}

	@Override
	public boolean canReceiveEnergy(ForgeDirection side) 
	{
		return true;
	}

	@Override
	public double demandedEnergyUnits() 
	{
		return (getMaxEnergy() - getEnergy())*Mekanism.TO_IC2;
	}

	@Override
    public double injectEnergyUnits(ForgeDirection direction, double i)
    {
		double givenEnergy = i*Mekanism.FROM_IC2;
    	double rejects = 0;
    	double neededEnergy = getMaxEnergy()-getEnergy();
    	
    	if(givenEnergy < neededEnergy)
    	{
    		electricityStored += givenEnergy;
    	}
    	else if(givenEnergy > neededEnergy)
    	{
    		electricityStored += neededEnergy;
    		rejects = givenEnergy-neededEnergy;
    	}
    	
    	return rejects*Mekanism.TO_IC2;
    }

	@Override
	public int getMaxSafeInput() 
	{
		return 2048;
	}
}
