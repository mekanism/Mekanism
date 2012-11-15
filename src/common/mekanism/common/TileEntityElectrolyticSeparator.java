package mekanism.common;

import ic2.api.Direction;
import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import ic2.api.IEnergySink;
import universalelectricity.core.UniversalElectricity;
import universalelectricity.core.Vector3;
import universalelectricity.electricity.ElectricInfo;
import universalelectricity.implement.IElectricityReceiver;
import universalelectricity.implement.IItemElectric;
import universalelectricity.implement.IJouleStorage;

import buildcraft.api.core.Orientations;
import buildcraft.api.liquids.ILiquidTank;
import buildcraft.api.liquids.ITankContainer;
import buildcraft.api.liquids.LiquidStack;
import buildcraft.api.liquids.LiquidTank;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;
import mekanism.api.IEnergizedItem;
import mekanism.api.IEnergyAcceptor;
import mekanism.api.IGasAcceptor;
import mekanism.api.IGasStorage;
import mekanism.api.IStorageTank;
import mekanism.api.IStorageTank.EnumGas;
import mekanism.api.ITileNetwork;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityElectrolyticSeparator extends TileEntityElectricBlock implements IGasStorage, IEnergySink, IJouleStorage, IElectricityReceiver, IEnergyAcceptor, ITankContainer, IPeripheral
{
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

	public TileEntityElectrolyticSeparator()
	{
		super("Electrolytic Seperator", 9600);
		inventory = new ItemStack[4];
		outputType = EnumGas.HYDROGEN;
	}
	
	@Override
	public void onUpdate()
	{
		if(hydrogenStored > MAX_GAS)
		{
			hydrogenStored = MAX_GAS;
		}
		
		if(oxygenStored > MAX_GAS)
		{
			oxygenStored = MAX_GAS;
		}
		
		if(inventory[3] != null && energyStored < MAX_ENERGY)
		{
			if(inventory[3].getItem() instanceof IEnergizedItem)
			{
				IEnergizedItem item = (IEnergizedItem)inventory[3].getItem();
				
				if(item.canBeDischarged())
				{
					int received = 0;
					int energyNeeded = MAX_ENERGY - energyStored;
					if(item.getRate() <= energyNeeded)
					{
						received = item.discharge(inventory[3], item.getRate());
					}
					else if(item.getRate() > energyNeeded)
					{
						received = item.discharge(inventory[3], energyNeeded);
					}
					
					setEnergy(energyStored + received);
				}
			}
			else if(inventory[3].getItem() instanceof IElectricItem)
			{
				IElectricItem item = (IElectricItem)inventory[3].getItem();
				if(item.canProvideEnergy())
				{
					int gain = ElectricItem.discharge(inventory[3], MAX_ENERGY - energyStored, 3, false, false);
					setEnergy(energyStored + gain);
				}
			}
		}
		
		if(inventory[0] != null && waterSlot.liquidStored < waterSlot.MAX_LIQUID)
		{
			if(inventory[0].itemID == Item.bucketWater.shiftedIndex)
			{
				inventory[0] = new ItemStack(Item.bucketEmpty, 1);
				waterSlot.setLiquid(waterSlot.liquidStored + 1000);
			}
		}
		
		if(!worldObj.isRemote)
		{
			if(inventory[1] != null && hydrogenStored < MAX_GAS)
			{
				if(inventory[1].getItem() instanceof IStorageTank)
				{
					if(((IStorageTank)inventory[1].getItem()).gasType() == EnumGas.HYDROGEN)
					{
						IStorageTank item = (IStorageTank)inventory[1].getItem();
						
						if(item.canReceiveGas())
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
							
							int rejects = item.addGas(inventory[1], sendingGas);
							setGas(EnumGas.HYDROGEN, hydrogenStored - (sendingGas - rejects));
						}
					}
				}
			}
			
			if(inventory[2] != null && oxygenStored < MAX_GAS)
			{
				if(inventory[2].getItem() instanceof IStorageTank)
				{
					if(((IStorageTank)inventory[2].getItem()).gasType() == EnumGas.OXYGEN)
					{
						IStorageTank item = (IStorageTank)inventory[2].getItem();
						
						if(item.canReceiveGas())
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
							
							int rejects = item.addGas(inventory[2], sendingGas);
							setGas(EnumGas.OXYGEN, oxygenStored - (sendingGas - rejects));
						}
					}
				}
			}
		}
		
		if(oxygenStored < MAX_GAS && hydrogenStored < MAX_GAS && waterSlot.liquidStored-2 >= 0 && energyStored-4 > 0)
		{
			waterSlot.setLiquid(waterSlot.liquidStored - 10);
			setEnergy(energyStored - 4);
			setGas(EnumGas.OXYGEN, oxygenStored + 1);
			setGas(EnumGas.HYDROGEN, hydrogenStored + 1);
		}
		
		if(hydrogenStored > 0 && !worldObj.isRemote)
		{
			TileEntity tileEntity = Vector3.getTileEntityFromSide(worldObj, Vector3.get(this), ForgeDirection.getOrientation(facing));
			
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
		return energyStored*i / MAX_ENERGY;
	}
	
	@Override
	public void handlePacketData(INetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try {
			facing = dataStream.readInt();
			energyStored = dataStream.readInt();
			waterSlot.liquidStored = dataStream.readInt();
			oxygenStored = dataStream.readInt();
			hydrogenStored = dataStream.readInt();
			worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
		} catch (Exception e)
		{
			System.out.println("[Mekanism] Error while handling tile entity packet.");
			e.printStackTrace();
		}
	}

	@Override
	public void sendPacket() 
	{
		PacketHandler.sendTileEntityPacket(this, facing, energyStored, waterSlot.liquidStored, oxygenStored, hydrogenStored);
	}

	@Override
	public void sendPacketWithRange() 
	{
		PacketHandler.sendTileEntityPacketWithRange(this, 50, facing, energyStored, waterSlot.liquidStored, oxygenStored, hydrogenStored);
	}
	
	/**
	 * Set this block's energy to a new amount.
	 * @param energy - new amount of energy
	 */
	public void setEnergy(int energy)
	{
		energyStored = Math.max(Math.min(energy, MAX_ENERGY), 0);
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
	public int transferToAcceptor(int amount)
	{
    	int rejects = 0;
    	int neededEnergy = MAX_ENERGY-energyStored;
    	if(amount <= neededEnergy)
    	{
    		energyStored += amount;
    	}
    	else if(amount > neededEnergy)
    	{
    		energyStored += neededEnergy;
    		rejects = amount-neededEnergy;
    	}
    	
    	return rejects;
	}
	
	@Override
	public boolean canReceive(ForgeDirection side)
	{
		return true;
	}
	
	@Override
	public double getMaxJoules() 
	{
		return MAX_ENERGY*UniversalElectricity.IC2_RATIO;
	}
	
	@Override
	public double getJoules(Object... data) 
	{
		return energyStored*UniversalElectricity.IC2_RATIO;
	}

	@Override
	public void setJoules(double joules, Object... data) 
	{
		setEnergy((int)(joules*UniversalElectricity.TO_IC2_RATIO));
	}
	
	@Override
	public boolean canConnect(ForgeDirection side) 
	{
		return true;
	}
	
	@Override
	public boolean canReceiveFromSide(ForgeDirection side) 
	{
		return side != ForgeDirection.getOrientation(facing);
	}
	
	@Override
	public double getVoltage() 
	{
		return 120;
	}
	
	@Override
	public double wattRequest() 
	{
		return energyStored < MAX_ENERGY ? ElectricInfo.getWatts((16)*UniversalElectricity.IC2_RATIO) : 0;
	}
	
	@Override
	public void onReceive(TileEntity sender, double amps, double voltage, ForgeDirection side) 
	{
		int energyToReceive = (int)(ElectricInfo.getJoules(amps, voltage)*UniversalElectricity.TO_IC2_RATIO);
		int energyNeeded = MAX_ENERGY - energyStored;
		int energyToStore = 0;
		
		if(energyToReceive <= energyNeeded)
		{
			energyToStore = energyToReceive;
		}
		else if(energyToReceive > energyNeeded)
		{
			energyToStore = energyNeeded;
		}
		setEnergy(energyStored + energyToStore);
	}
	
	@Override
	public boolean demandsEnergy() 
	{
		return energyStored < MAX_ENERGY;
	}

	@Override
    public int injectEnergy(Direction direction, int i)
    {
    	int rejects = 0;
    	int neededEnergy = MAX_ENERGY-energyStored;
    	if(i <= neededEnergy)
    	{
    		energyStored += i;
    	}
    	else if(i > neededEnergy)
    	{
    		energyStored += neededEnergy;
    		rejects = i-neededEnergy;
    	}
    	
    	return rejects;
    }
	
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction)
	{
		return direction.toForgeDirection() != ForgeDirection.getOrientation(facing);
	}

	@Override
	public int fill(Orientations from, LiquidStack resource, boolean doFill) 
	{
		if(from.toDirection() != ForgeDirection.getOrientation(facing))
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
	public LiquidStack drain(Orientations from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public ILiquidTank[] getTanks() 
	{
		return new ILiquidTank[] {new LiquidTank(waterSlot.liquidID, waterSlot.liquidStored, waterSlot.MAX_LIQUID)};
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        hydrogenStored = nbtTags.getInteger("hydrogenStored");
        oxygenStored = nbtTags.getInteger("oxygenStored");
        waterSlot.liquidStored = nbtTags.getInteger("waterStored");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("hydrogenStored", hydrogenStored);
        nbtTags.setInteger("oxygenStored", oxygenStored);
        nbtTags.setInteger("waterStored", waterSlot.liquidStored);
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
				return new Object[] {energyStored};
			case 1:
				return new Object[] {output};
			case 2:
				return new Object[] {MAX_ENERGY};
			case 3:
				return new Object[] {(MAX_ENERGY-energyStored)};
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
	public void attach(IComputerAccess computer, String computerSide) {}

	@Override
	public void detach(IComputerAccess computer) {}
}
