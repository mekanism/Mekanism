package mekanism.generators.common;

import ic2.api.Direction;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.item.IElectricItem;

import java.util.ArrayList;
import java.util.Random;

import mekanism.api.EnumGas;
import mekanism.api.GasTransmission;
import mekanism.api.IGasAcceptor;
import mekanism.api.IGasStorage;
import mekanism.api.IStorageTank;
import mekanism.api.IStrictEnergyAcceptor;
import mekanism.api.ITubeConnection;
import mekanism.common.ChargeUtils;
import mekanism.common.ISustainedTank;
import mekanism.common.Mekanism;
import mekanism.common.MekanismUtils;
import mekanism.common.PacketHandler;
import mekanism.common.TileEntityElectricBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;
import universalelectricity.core.item.IItemElectric;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class TileEntityElectrolyticSeparator extends TileEntityElectricBlock implements IGasStorage, IEnergySink, ITankContainer, IPeripheral, ITubeConnection, IStrictEnergyAcceptor, ISustainedTank
{
	/** This separator's water slot. */
	public LiquidTank waterTank = new LiquidTank(24000);
	
	/** The maximum amount of gas this block can store. */
	public int MAX_GAS = 2400;
	
	/** The amount of oxygen this block is storing. */
	public int oxygenStored;
	
	/** The amount of hydrogen this block is storing. */
	public int hydrogenStored;
	
	/** How fast this block can output gas. */
	public int output = 16;
	
	/** The type of gas this block is outputting. */
	public EnumGas outputType;
	
	/** Type type of gas this block is dumping. */
	public EnumGas dumpType;

	public TileEntityElectrolyticSeparator()
	{
		super("Electrolytic Separator", 20000);
		inventory = new ItemStack[4];
		outputType = EnumGas.HYDROGEN;
		dumpType = EnumGas.NONE;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!worldObj.isRemote)
		{
			ChargeUtils.discharge(3, this);
			
			if(inventory[0] != null)
			{
				LiquidStack liquid = LiquidContainerRegistry.getLiquidForFilledItem(inventory[0]);
				
				if(liquid != null && liquid.itemID == Block.waterStill.blockID)
				{
					if(waterTank.getLiquid() == null || waterTank.getLiquid().amount+liquid.amount <= waterTank.getCapacity())
					{
						waterTank.fill(liquid, true);
						
						if(inventory[0].isItemEqual(new ItemStack(Item.bucketWater)))
						{
							inventory[0] = new ItemStack(Item.bucketEmpty);
						}
						else {
							inventory[0].stackSize--;
							
							if(inventory[0].stackSize == 0)
							{
								inventory[0] = null;
							}
						}
					}
				}
			}
			
			if(!worldObj.isRemote)
			{
				if(inventory[1] != null && hydrogenStored > 0)
				{
					if(inventory[1].getItem() instanceof IStorageTank)
					{
						if(((IStorageTank)inventory[1].getItem()).getGasType(inventory[1]) == EnumGas.HYDROGEN || ((IStorageTank)inventory[1].getItem()).getGasType(inventory[1]) == EnumGas.NONE)
						{
							IStorageTank item = (IStorageTank)inventory[1].getItem();
							
							if(item.canReceiveGas(inventory[1], EnumGas.HYDROGEN))
							{
								int sendingGas = 0;
								
								if(item.getRate() <= hydrogenStored)
								{
									sendingGas = item.getRate();
								}
								else if(item.getRate() > hydrogenStored)
								{
									sendingGas = hydrogenStored;
								}
								
								int rejects = item.addGas(inventory[1], EnumGas.HYDROGEN, sendingGas);
								setGas(EnumGas.HYDROGEN, hydrogenStored - (sendingGas - rejects));
							}
						}
					}
				}
				
				if(inventory[2] != null && oxygenStored > 0)
				{
					if(inventory[2].getItem() instanceof IStorageTank)
					{
						if(((IStorageTank)inventory[2].getItem()).getGasType(inventory[2]) == EnumGas.OXYGEN || ((IStorageTank)inventory[2].getItem()).getGasType(inventory[2]) == EnumGas.NONE)
						{
							IStorageTank item = (IStorageTank)inventory[2].getItem();
							
							if(item.canReceiveGas(inventory[2], EnumGas.OXYGEN))
							{
								int sendingGas = 0;
								
								if(item.getRate() <= oxygenStored)
								{
									sendingGas = item.getRate();
								}
								else if(item.getRate() > oxygenStored)
								{
									sendingGas = oxygenStored;
								}
								
								int rejects = item.addGas(inventory[2], EnumGas.OXYGEN, sendingGas);
								setGas(EnumGas.OXYGEN, oxygenStored - (sendingGas - rejects));
							}
						}
					}
				}
			}
			
			if(oxygenStored < MAX_GAS && hydrogenStored < MAX_GAS && waterTank.getLiquid() != null && waterTank.getLiquid().amount-2 >= 0 && electricityStored-100 > 0)
			{
				waterTank.drain(2, true);
				setEnergy(electricityStored - 10);
				setGas(EnumGas.OXYGEN, oxygenStored + 1);
				setGas(EnumGas.HYDROGEN, hydrogenStored + 2);
			}
			
			if(outputType != EnumGas.NONE && getGas(outputType) > 0)
			{
				setGas(outputType, getGas(outputType) - (Math.min(getGas(outputType), output) - GasTransmission.emitGasToNetwork(outputType, Math.min(getGas(outputType), output), this, ForgeDirection.getOrientation(facing))));
				
				TileEntity tileEntity = VectorHelper.getTileEntityFromSide(worldObj, new Vector3(this), ForgeDirection.getOrientation(facing));
				
				if(tileEntity instanceof IGasAcceptor)
				{
					if(((IGasAcceptor)tileEntity).canReceiveGas(ForgeDirection.getOrientation(facing).getOpposite(), outputType))
					{
						int sendingGas = 0;
						if(getGas(outputType) >= output)
						{
							sendingGas = output;
						}
						else if(getGas(outputType) < output)
						{
							sendingGas = getGas(outputType);
						}
						
						int rejects = ((IGasAcceptor)tileEntity).transferGasToAcceptor(sendingGas, outputType);
						
						setGas(outputType, getGas(outputType) - (sendingGas - rejects));
					}
				}
			}
			
			if(dumpType != EnumGas.NONE && getGas(dumpType) > 0)
			{
				setGas(dumpType, (getGas(dumpType) - 8));
				
				if(new Random().nextInt(3) == 2)
				{
					PacketHandler.sendElectrolyticSeparatorParticle(this);
				}
			}
		}
	}
	
	public void spawnParticle()
	{
		switch(facing)
		{
			case 3:
				worldObj.spawnParticle("smoke", xCoord+0.1, yCoord+1, zCoord+0.25, 0.0D, 0.0D, 0.0D);
				break;
			case 4:
				worldObj.spawnParticle("smoke", xCoord+0.75, yCoord+1, zCoord+0.1, 0.0D, 0.0D, 0.0D);
				break;
			case 2:
				worldObj.spawnParticle("smoke", xCoord+0.9, yCoord+1, zCoord+0.75, 0.0D, 0.0D, 0.0D);
				break;
			case 5:
				worldObj.spawnParticle("smoke", xCoord+0.25, yCoord+1, zCoord+0.9, 0.0D, 0.0D, 0.0D);
				break;
		}
	}
	
	@Override
	public boolean func_102008_b(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 3)
		{
			return (itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).getWatts() > 0) ||
					(itemstack.getItem() instanceof IElectricItem && ((IElectricItem)itemstack.getItem()).canProvideEnergy(itemstack) && 
							(!(itemstack.getItem() instanceof IItemElectric) || 
							((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).getWatts() > 0));
		}
		else if(slotID == 0)
		{
			return LiquidContainerRegistry.isEmptyContainer(itemstack);
		}
		else if(slotID == 1)
		{
			return itemstack.getItem() instanceof IStorageTank && ((IStorageTank)itemstack.getItem()).getGas(EnumGas.HYDROGEN, itemstack) == ((IStorageTank)itemstack.getItem()).getMaxGas(EnumGas.HYDROGEN, itemstack);
		}
		else if(slotID == 2)
		{
			return itemstack.getItem() instanceof IStorageTank && ((IStorageTank)itemstack.getItem()).getGas(EnumGas.OXYGEN, itemstack) == ((IStorageTank)itemstack.getItem()).getMaxGas(EnumGas.HYDROGEN, itemstack);
		}
		
		return false;
	}
	
	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return LiquidContainerRegistry.getLiquidForFilledItem(itemstack) != null && LiquidContainerRegistry.getLiquidForFilledItem(itemstack).itemID == Block.waterStill.blockID;
		}
		else if(slotID == 1)
		{
			return itemstack.getItem() instanceof IStorageTank && ((IStorageTank)itemstack.getItem()).getGasType(itemstack) == EnumGas.HYDROGEN || ((IStorageTank)itemstack.getItem()).getGasType(itemstack) == EnumGas.NONE;
		}
		else if(slotID == 2)
		{
			return itemstack.getItem() instanceof IStorageTank && ((IStorageTank)itemstack.getItem()).getGasType(itemstack) == EnumGas.OXYGEN || ((IStorageTank)itemstack.getItem()).getGasType(itemstack) == EnumGas.NONE;
		}
		else if(slotID == 3)
		{
			return (itemstack.getItem() instanceof IElectricItem && ((IElectricItem)itemstack.getItem()).canProvideEnergy(itemstack)) || 
					(itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).amperes != 0) || 
					itemstack.itemID == Item.redstone.itemID;
		}
		return true;
	}
	
	@Override
	public int[] getSizeInventorySide(int side)
	{
		if(ForgeDirection.getOrientation(side) == MekanismUtils.getLeft(facing))
		{
			return new int[] {3};
		}
		else if(side == facing || ForgeDirection.getOrientation(side) == ForgeDirection.getOrientation(facing).getOpposite())
		{
			return new int[] {1, 2};
		}
		
		return new int[] {0};
	}
	
	@Override
	public int getStartInventorySide(ForgeDirection side) 
	{
		if(side == MekanismUtils.getLeft(facing))
		{
			return 3;
		}
		else if(side == ForgeDirection.getOrientation(facing) || side == ForgeDirection.getOrientation(facing).getOpposite())
		{
			return 1;
		}
		
		return 0;
	}
	
	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		if(side == ForgeDirection.getOrientation(facing) || side == ForgeDirection.getOrientation(facing).getOpposite())
		{
			return 2;
		}
		
		return 1;
	}
	
	@Override
	public double transferEnergyToAcceptor(double amount)
	{
    	double rejects = 0;
    	double neededElectricity = MAX_ELECTRICITY-electricityStored;
    	
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
	
	/**
	 * Gets the scaled hydrogen level for the GUI.
	 * @param i - multiplier
	 * @return
	 */
	public int getScaledHydrogenLevel(int i)
	{
		return hydrogenStored*i / MAX_GAS;
	}
	
	/**
	 * Gets the scaled oxygen level for the GUI.
	 * @param i - multiplier
	 * @return
	 */
	public int getScaledOxygenLevel(int i)
	{
		return oxygenStored*i / MAX_GAS;
	}
	
	/**
	 * Gets the scaled water level for the GUI.
	 * @param i - multiplier
	 * @return
	 */
	public int getScaledWaterLevel(int i)
	{
		return waterTank.getLiquid() != null ? waterTank.getLiquid().amount*i / waterTank.getCapacity() : 0;
	}
	
	/**
	 * Gets the scaled energy level for the GUI.
	 * @param i - multiplier
	 * @return
	 */
	public int getScaledEnergyLevel(int i)
	{
		return (int)(electricityStored*i / MAX_ELECTRICITY);
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		if(!worldObj.isRemote)
		{
			byte type = dataStream.readByte();
			
			if(type == 0)
			{
				outputType = EnumGas.getFromName(dataStream.readUTF());
				return;
			}
			else if(type == 1)
			{
				dumpType = EnumGas.getFromName(dataStream.readUTF());
				return;
			}
		}
		
		super.handlePacketData(dataStream);
		
		int amount = dataStream.readInt();
		if(amount != 0)
		{
			waterTank.setLiquid(new LiquidStack(Block.waterStill.blockID, amount, 0));
		}
		
		oxygenStored = dataStream.readInt();
		hydrogenStored = dataStream.readInt();
		outputType = EnumGas.getFromName(dataStream.readUTF());
		dumpType = EnumGas.getFromName(dataStream.readUTF());
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		if(waterTank.getLiquid() != null)
		{
			data.add(waterTank.getLiquid().amount);
		}
		else {
			data.add(0);
		}
		
		data.add(oxygenStored);
		data.add(hydrogenStored);
		data.add(outputType.name);
		data.add(dumpType.name);
		return data;
	}
	
	@Override
	public int getMaxGas(EnumGas type, Object... data)
	{
		return MAX_GAS;
	}
	
	@Override
	public void setGas(EnumGas type, int amount, Object... data)
	{
		if(type == EnumGas.HYDROGEN)
		{
			hydrogenStored = Math.max(Math.min(amount, MAX_GAS), 0);
		}
		else if(type == EnumGas.OXYGEN)
		{
			oxygenStored = Math.max(Math.min(amount, MAX_GAS), 0);
		}
	}
	
	@Override
	public int getGas(EnumGas type, Object... data)
	{
		if(type == EnumGas.HYDROGEN)
		{
			return hydrogenStored;
		}
		else if(type == EnumGas.OXYGEN)
		{
			return oxygenStored;
		}
		
		return 0;
	}
	
	@Override
	public int demandsEnergy() 
	{
		return (int)((MAX_ELECTRICITY - electricityStored)*Mekanism.TO_IC2);
	}
	
	@Override
	public int getMaxSafeInput()
	{
		return 2048;
	}

	@Override
    public int injectEnergy(Direction direction, int i)
    {
    	double rejects = 0;
    	double neededEnergy = MAX_ELECTRICITY-electricityStored;
    	if(i <= neededEnergy)
    	{
    		electricityStored += i;
    	}
    	else if(i > neededEnergy)
    	{
    		electricityStored += neededEnergy;
    		rejects = i-neededEnergy;
    	}
    	
    	return (int)(rejects*Mekanism.TO_IC2);
    }
	
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction)
	{
		return direction.toForgeDirection() != ForgeDirection.getOrientation(facing);
	}

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) 
	{
		return fill(0, resource, doFill);
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill)
	{
		if(resource.itemID == Block.waterStill.blockID && tankIndex == 0)
		{
			return waterTank.fill(resource, doFill);
		}
		
		return 0;
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction) 
	{
		return new ILiquidTank[] {waterTank};
	}
	
	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type)
	{
		return waterTank;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        hydrogenStored = nbtTags.getInteger("hydrogenStored");
        oxygenStored = nbtTags.getInteger("oxygenStored");
        
        if(nbtTags.hasKey("waterTank"))
        {
        	waterTank.readFromNBT(nbtTags.getCompoundTag("waterTank"));
        }
        
        outputType = EnumGas.getFromName(nbtTags.getString("outputType"));
        dumpType = EnumGas.getFromName(nbtTags.getString("dumpType"));
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("hydrogenStored", hydrogenStored);
        nbtTags.setInteger("oxygenStored", oxygenStored);
        
        if(waterTank.getLiquid() != null)
        {
        	nbtTags.setTag("waterTank", waterTank.writeToNBT(new NBTTagCompound()));
        }
        
        nbtTags.setString("outputType", outputType.name);
        nbtTags.setString("dumpType", dumpType.name);
    }

	@Override
	public String getType() 
	{
		return getInvName();
	}

	@Override
	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getWater", "getWaterNeeded", "getHydrogen", "getHydrogenNeeded", "getOxygen", "getOxygenNeeded"};
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
				return new Object[] {waterTank.getLiquid() != null ? waterTank.getLiquid().amount : 0};
			case 5:
				return new Object[] {waterTank.getLiquid() != null ? (waterTank.getCapacity()-waterTank.getLiquid().amount) : 0};
			case 6:
				return new Object[] {hydrogenStored};
			case 7:
				return new Object[] {MAX_GAS-hydrogenStored};
			case 8:
				return new Object[] {oxygenStored};
			case 9:
				return new Object[] {MAX_GAS-oxygenStored};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return new Object[] {"Unknown command."};
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
	public boolean canTubeConnect(ForgeDirection side)
	{
		return side == ForgeDirection.getOrientation(facing);
	}
	
	@Override
	public void setLiquidStack(LiquidStack liquidStack, Object... data) 
	{
		waterTank.setLiquid(liquidStack);
	}

	@Override
	public LiquidStack getLiquidStack(Object... data) 
	{
		return waterTank.getLiquid();
	}

	@Override
	public boolean hasTank(Object... data) 
	{
		return true;
	}
}
