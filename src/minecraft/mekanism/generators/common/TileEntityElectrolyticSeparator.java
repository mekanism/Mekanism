package mekanism.generators.common;

import ic2.api.Direction;
import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import ic2.api.energy.tile.IEnergySink;

import java.util.ArrayList;
import java.util.EnumSet;

import mekanism.api.EnumGas;
import mekanism.api.IGasAcceptor;
import mekanism.api.IGasStorage;
import mekanism.api.IStorageTank;
import mekanism.api.ITubeConnection;
import mekanism.common.LiquidSlot;
import mekanism.common.Mekanism;
import mekanism.common.MekanismUtils;
import mekanism.common.PacketHandler;
import mekanism.common.TileEntityElectricBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerData;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;
import universalelectricity.core.electricity.ElectricityConnections;
import universalelectricity.core.implement.IConductor;
import universalelectricity.core.implement.IItemElectric;
import universalelectricity.core.implement.IJouleStorage;
import universalelectricity.core.implement.IVoltage;
import universalelectricity.core.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class TileEntityElectrolyticSeparator extends TileEntityElectricBlock implements IGasStorage, IEnergySink, IJouleStorage, IVoltage, ITankContainer, IPeripheral, ITubeConnection
{
	/** This separator's water slot. */
	public LiquidSlot waterSlot = new LiquidSlot(24000, 9);
	
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
		super("Electrolytic Seperator", 9600);
		ElectricityConnections.registerConnector(this, EnumSet.allOf(ForgeDirection.class));
		inventory = new ItemStack[4];
		outputType = EnumGas.HYDROGEN;
		dumpType = EnumGas.NONE;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(powerProvider != null)
		{
			int received = (int)(powerProvider.useEnergy(0, (float)((MAX_ELECTRICITY-electricityStored)*Mekanism.TO_BC), true)*Mekanism.FROM_BC);
			setJoules(electricityStored + received);
		}
		
		if(hydrogenStored > MAX_GAS)
		{
			hydrogenStored = MAX_GAS;
		}
		
		if(oxygenStored > MAX_GAS)
		{
			oxygenStored = MAX_GAS;
		}
		
		if(!worldObj.isRemote)
		{
			for(ForgeDirection direction : ForgeDirection.values())
			{
				if(direction != ForgeDirection.getOrientation(facing))
				{
					TileEntity tileEntity = Vector3.getTileEntityFromSide(worldObj, new Vector3(this), direction);
					if(tileEntity != null)
					{
						if(tileEntity instanceof IConductor)
						{
							if(electricityStored < MAX_ELECTRICITY)
							{
								double electricityNeeded = MAX_ELECTRICITY - electricityStored;
								((IConductor)tileEntity).getNetwork().startRequesting(this, electricityNeeded, electricityNeeded >= getVoltage() ? getVoltage() : electricityNeeded);
								setJoules(electricityStored + ((IConductor)tileEntity).getNetwork().consumeElectricity(this).getWatts());
							}
							else if(electricityStored >= MAX_ELECTRICITY)
							{
								((IConductor)tileEntity).getNetwork().stopRequesting(this);
							}
						}
					}
				}
			}
		}
		
		if(inventory[3] != null && electricityStored < MAX_ELECTRICITY)
		{
			if(inventory[3].getItem() instanceof IItemElectric)
			{
				IItemElectric electricItem = (IItemElectric)inventory[3].getItem();

				if (electricItem.canProduceElectricity())
				{
					double joulesNeeded = MAX_ELECTRICITY-electricityStored;
					double joulesReceived = electricItem.onUse(Math.min(electricItem.getMaxJoules(inventory[3])*0.005, joulesNeeded), inventory[3]);
					setJoules(electricityStored + joulesReceived);
				}
			}
			else if(inventory[3].getItem() instanceof IElectricItem)
			{
				IElectricItem item = (IElectricItem)inventory[3].getItem();
				if(item.canProvideEnergy())
				{
					double gain = ElectricItem.discharge(inventory[3], (int)((MAX_ELECTRICITY - electricityStored)*Mekanism.TO_IC2), 3, false, false)*Mekanism.FROM_IC2;
					setJoules(electricityStored + gain);
				}
			}
		}
		
		if(inventory[0] != null)
		{
			LiquidStack liquid = LiquidContainerRegistry.getLiquidForFilledItem(inventory[0]);
			
			if(liquid != null && liquid.itemID == Block.waterStill.blockID)
			{
				if(waterSlot.liquidStored+liquid.amount <= waterSlot.MAX_LIQUID)
				{
					waterSlot.setLiquid(waterSlot.liquidStored + liquid.amount);
					
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
		
		if(oxygenStored < MAX_GAS && hydrogenStored < MAX_GAS && waterSlot.liquidStored-2 >= 0 && electricityStored-4 > 0)
		{
			waterSlot.setLiquid(waterSlot.liquidStored - 2);
			setJoules(electricityStored - 10);
			setGas(EnumGas.OXYGEN, oxygenStored + 1);
			setGas(EnumGas.HYDROGEN, hydrogenStored + 2);
		}
		
		if(outputType != EnumGas.NONE && getGas(outputType) > 0 && !worldObj.isRemote)
		{
			setGas(outputType, getGas(outputType) - (Math.min(getGas(outputType), output) - MekanismUtils.emitGasToNetwork(outputType, Math.min(getGas(outputType), output), this, ForgeDirection.getOrientation(facing))));
			
			TileEntity tileEntity = Vector3.getTileEntityFromSide(worldObj, new Vector3(this), ForgeDirection.getOrientation(facing));
			
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
		
		if(dumpType != EnumGas.NONE  && getGas(dumpType) > 0)
		{
			setGas(dumpType, (getGas(dumpType) - 8));
			spawnParticle();
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
		return waterSlot.liquidStored*i / waterSlot.MAX_LIQUID;
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
		waterSlot.liquidStored = dataStream.readInt();
		oxygenStored = dataStream.readInt();
		hydrogenStored = dataStream.readInt();
		outputType = EnumGas.getFromName(dataStream.readUTF());
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(waterSlot.liquidStored);
		data.add(oxygenStored);
		data.add(hydrogenStored);
		data.add(outputType.name);
		data.add(dumpType.name);
		return data;
	}
	
	@Override
	public void setGas(EnumGas type, int amount)
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
	public int getGas(EnumGas type)
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
	public double getMaxJoules(Object... data) 
	{
		return MAX_ELECTRICITY;
	}
	
	@Override
	public double getJoules(Object... data) 
	{
		return electricityStored;
	}

	@Override
	public void setJoules(double joules, Object... data) 
	{
		electricityStored = Math.max(Math.min(joules, getMaxJoules()), 0);
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
		if(from != ForgeDirection.getOrientation(facing))
		{
			if(resource.itemID == Block.waterStill.blockID)
			{
				int waterTransfer = 0;
				int waterNeeded = waterSlot.MAX_LIQUID - waterSlot.liquidStored;
				int attemptTransfer = resource.amount;
				
				if(attemptTransfer <= waterNeeded)
				{
					waterTransfer = attemptTransfer;
				}
				else {
					waterTransfer = waterNeeded;
				}
				
				if(doFill)
				{
					waterSlot.setLiquid(waterSlot.liquidStored + waterTransfer);
				}
				
				return waterTransfer;
			}
		}
		
		return 0;
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill)
	{
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
		return new ILiquidTank[] {new LiquidTank(waterSlot.liquidID, waterSlot.liquidStored, waterSlot.MAX_LIQUID)};
	}
	
	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type)
	{
		return null;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        hydrogenStored = nbtTags.getInteger("hydrogenStored");
        oxygenStored = nbtTags.getInteger("oxygenStored");
        waterSlot.liquidStored = nbtTags.getInteger("waterStored");
        outputType = EnumGas.getFromName(nbtTags.getString("outputType"));
        dumpType = EnumGas.getFromName(nbtTags.getString("dumpType"));
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("hydrogenStored", hydrogenStored);
        nbtTags.setInteger("oxygenStored", oxygenStored);
        nbtTags.setInteger("waterStored", waterSlot.liquidStored);
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
				return new Object[] {waterSlot.liquidStored};
			case 5:
				return new Object[] {(waterSlot.MAX_LIQUID-waterSlot.liquidStored)};
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
	public double getVoltage(Object... data) 
	{
		return 120;
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return side == ForgeDirection.getOrientation(facing);
	}
}
