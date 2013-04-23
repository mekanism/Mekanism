package mekanism.generators.common;

import java.util.ArrayList;

import universalelectricity.core.item.ElectricItemHelper;
import universalelectricity.core.item.IItemElectric;

import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import mekanism.api.EnumGas;
import mekanism.api.IStorageTank;
import mekanism.common.ChargeUtils;
import mekanism.common.Mekanism;
import mekanism.common.MekanismUtils;
import mekanism.generators.common.BlockGenerator.GeneratorType;
import micdoodle8.mods.galacticraft.API.ISolarLevel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;

public class TileEntitySolarGenerator extends TileEntityGenerator
{
	/** Whether or not this generator sees the sun. */
	public boolean seesSun = false;
	
	/** How fast this tile entity generates energy. */
	public int GENERATION_RATE;
	
	public TileEntitySolarGenerator()
	{
		super("Solar Generator", 96000, 80);
		GENERATION_RATE = 40;
		inventory = new ItemStack[1];
	}
	
	public TileEntitySolarGenerator(String name, int maxEnergy, int output, int generation)
	{
		super(name, maxEnergy, output);
		GENERATION_RATE = generation;
		inventory = new ItemStack[1];
	}
	
	@Override
	public int[] getSizeInventorySide(int side)
	{
		return new int[] {0};
	}
	
	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		return 1;
	}
	
	@Override
	public boolean canSetFacing(int facing)
	{
		return facing != 0 && facing != 1;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		ChargeUtils.charge(0, this);
		
		if(!worldObj.isRemote)
		{
			if(worldObj.isDaytime() && !worldObj.isRaining() && !worldObj.isThundering() && !worldObj.provider.hasNoSky && worldObj.canBlockSeeTheSky(xCoord, yCoord+1, zCoord))
			{
				seesSun = true;
			}
			else {
				seesSun = false;
			}
			
			if(worldObj.getBlockId(xCoord, yCoord+1, zCoord) == MekanismGenerators.generatorID && worldObj.getBlockMetadata(xCoord, yCoord+1, zCoord) == GeneratorType.SOLAR_GENERATOR.meta)
			{
				seesSun = false;
			}
		}
		
		if(canOperate())
		{
			setEnergy(electricityStored + getEnvironmentBoost());
		}
	}
	
	@Override
	public boolean func_102008_b(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 0)
		{
			return (itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getReceiveRequest(itemstack).getWatts() == 0) ||
					(itemstack.getItem() instanceof IElectricItem && (!(itemstack.getItem() instanceof IItemElectric) || 
							((IItemElectric)itemstack.getItem()).getReceiveRequest(itemstack).getWatts() == 0));
		}
		
		return false;
	}
	
	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return itemstack.getItem() instanceof IElectricItem || 
					(itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getReceiveRequest(itemstack).amperes != 0);
		}
		
		return true;
	}
	
	@Override
	public boolean canOperate()
	{
		return electricityStored < MAX_ELECTRICITY && seesSun;
	}
	
	@Override
	public int getEnvironmentBoost()
	{
		return seesSun ? (GENERATION_RATE*(worldObj.provider instanceof ISolarLevel ? (int)((ISolarLevel)worldObj.provider).getSolorEnergyMultiplier() : 1)) : 0;
	}

	@Override
	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getSeesSun"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				return new Object[] {output};
			case 2:
				return new Object[] {MAX_ELECTRICITY};
			case 3:
				return new Object[] {(MAX_ELECTRICITY-electricityStored)};
			case 4:
				return new Object[] {seesSun};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return null;
		}
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		seesSun = dataStream.readBoolean();
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(seesSun);
		return data;
	}
}
