package mekanism.generators.common.tileentity;

import java.util.ArrayList;

import mekanism.api.Object3D;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.block.BlockGenerator.GeneratorType;
import micdoodle8.mods.galacticraft.API.ISolarLevel;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;

public class TileEntitySolarGenerator extends TileEntityGenerator
{
	/** Whether or not this generator sees the sun. */
	public boolean seesSun = false;
	
	/** How fast this tile entity generates energy. */
	public double GENERATION_RATE;
	
	public TileEntitySolarGenerator()
	{
		super("Solar Generator", 96000, MekanismGenerators.solarGeneration*2);
		GENERATION_RATE = MekanismGenerators.solarGeneration;
		inventory = new ItemStack[1];
	}
	
	public TileEntitySolarGenerator(String name, double maxEnergy, double output, double generation)
	{
		super(name, maxEnergy, output);
		GENERATION_RATE = generation;
		inventory = new ItemStack[1];
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return new int[] {0};
	}
	
	@Override
	public float getVolumeMultiplier()
	{
		return 0.05F;
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
			
			for(int y = yCoord+1; y < 256; y++)
			{
				Object3D obj = new Object3D(xCoord, y, zCoord);
				Block block = Block.blocksList[obj.getBlockId(worldObj)];
				
				if(block != null)
				{
					if(block.isOpaqueCube() || block.blockID == MekanismGenerators.generatorID && obj.getMetadata(worldObj) == GeneratorType.SOLAR_GENERATOR.meta)
					{
						seesSun = false;
						break;
					}
				}
			}
			
			if(canOperate())
			{
				setActive(true);
				setEnergy(electricityStored + getEnvironmentBoost());
			}
			else {
				setActive(false);
			}
		}
	}
	
	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 0)
		{
			return ChargeUtils.canBeOutputted(itemstack, true);
		}
		
		return false;
	}
	
	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return ChargeUtils.canBeCharged(itemstack);
		}
		
		return true;
	}
	
	@Override
	public boolean canOperate()
	{
		return electricityStored < MAX_ELECTRICITY && seesSun && MekanismUtils.canFunction(this);
	}
	
	@Override
	public double getEnvironmentBoost()
	{
		return seesSun ? (GENERATION_RATE*(worldObj.provider instanceof ISolarLevel ? (int)((ISolarLevel)worldObj.provider).getSolarEnergyMultiplier() : 1)) : 0;
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
	
	@Override
	public boolean hasVisual()
	{
		return false;
	}
}
