package mekanism.common.tileentity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;

import mekanism.common.IRedstoneControl;
import mekanism.common.Mekanism;
import mekanism.common.Object3D;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.CableUtils;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;

public class TileEntityEnergyCube extends TileEntityElectricBlock implements IPowerReceptor, IPeripheral, IRedstoneControl
{
	/** This Energy Cube's tier. */
	public EnergyCubeTier tier = EnergyCubeTier.BASIC;
	
	/** The redstone level this Energy Cube is outputting at. */
	public int currentRedstoneLevel;
	
	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType;
	
	public int prevScale;
	
	/**
	 * A block used to store and transfer electricity.
	 * @param energy - maximum energy this block can hold.
	 * @param i - output per tick this block can handle.
	 */
	public TileEntityEnergyCube()
	{
		super("EnergyCube", 0);
		
		inventory = new ItemStack[2];
		controlType = RedstoneControl.DISABLED;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		ChargeUtils.charge(0, this);
		ChargeUtils.discharge(1, this);
		
		if(MekanismUtils.canFunction(this))
		{
			CableUtils.emit(this);
		}
	}
	
	@Override
	public String getInvName()
	{
		return MekanismUtils.localize(getBlockType().getUnlocalizedName() + "." + tier.name + ".name");
	}
	
	@Override
	public double getMaxOutput()
	{
		return tier.OUTPUT;
	}
	
	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return ChargeUtils.canBeCharged(itemstack);
		}
		else if(slotID == 1)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}
		
		return true;
	}
	
	@Override
	protected EnumSet<ForgeDirection> getConsumingSides()
	{
		EnumSet set = EnumSet.allOf(ForgeDirection.class);
		set.remove(getOutputtingSides());
		
		return set;
	}
	
	@Override
	public EnumSet<ForgeDirection> getOutputtingSides()
	{
		return EnumSet.of(ForgeDirection.getOrientation(facing));
	}
	
	@Override
	public boolean canSetFacing(int side)
	{
		return true;
	}

	@Override
	public double getMaxEnergy() 
	{
		return tier.MAX_ELECTRICITY;
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return side == 1 ? new int[] {0} : new int[] {1};
	}
	
	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 1)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}
		else if(slotID == 0)
		{
			return ChargeUtils.canBeOutputted(itemstack, true);
		}
		
		return false;
	}

	@Override
	public float getVoltage() 
	{
		return (float)(tier.VOLTAGE*Mekanism.TO_UE);
	}

	@Override
	public String getType() 
	{
		return getInvName();
	}

	@Override
	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {getEnergy()};
			case 1:
				return new Object[] {tier.OUTPUT};
			case 2:
				return new Object[] {getMaxEnergy()};
			case 3:
				return new Object[] {(getMaxEnergy()-getEnergy())};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return null;
		}
	}

	@Override
	public boolean canAttachToSide(int side) 
	{
		return true;
	}

	@Override
	public void attach(IComputerAccess computer) {}

	@Override
	public void detach(IComputerAccess computer) {}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		tier = EnergyCubeTier.getFromName(dataStream.readUTF());
		
		super.handlePacketData(dataStream);
		
		controlType = RedstoneControl.values()[dataStream.readInt()];
		
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(tier.name);
		
		super.getNetworkedData(data);
		
		data.add(controlType.ordinal());
		
		return data;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        tier = EnergyCubeTier.getFromName(nbtTags.getString("tier"));
        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setString("tier", tier.name);
        nbtTags.setInteger("controlType", controlType.ordinal());
    }
	
	@Override
	public void setEnergy(double energy) 
	{
	    super.setEnergy(energy);
	    
	    int newRedstoneLevel = getRedstoneLevel();
	    
	    if(newRedstoneLevel != currentRedstoneLevel)
	    {
	        onInventoryChanged();
	        currentRedstoneLevel = newRedstoneLevel;
	    }
	    
	    if(!worldObj.isRemote)
	    {
		    int newScale = getScaledEnergyLevel(100);
		    
		    if(newScale != prevScale)
		    {
		    	PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())), Object3D.get(this), 50D);
		    }
		    
		    prevScale = newScale;
	    }
	}

	public int getRedstoneLevel()
	{
        double fractionFull = getEnergy()/getMaxEnergy();
        return MathHelper.floor_float((float)(fractionFull * 14.0F)) + (fractionFull > 0 ? 1 : 0);
	}
	
	@Override
	public RedstoneControl getControlType() 
	{
		return controlType;
	}

	@Override
	public void setControlType(RedstoneControl type) 
	{
		controlType = type;
	}
}
