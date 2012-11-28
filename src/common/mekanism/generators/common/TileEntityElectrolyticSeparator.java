package mekanism.generators.common;

import ic2.api.Direction;
import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import ic2.api.IEnergySink;
import universalelectricity.core.UniversalElectricity;
import universalelectricity.core.electricity.ElectricInfo;
import universalelectricity.core.implement.IElectricityReceiver;
import universalelectricity.core.implement.IItemElectric;
import universalelectricity.core.implement.IJouleStorage;
import universalelectricity.core.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;
import mekanism.api.EnumGas;
import mekanism.api.IGasAcceptor;
import mekanism.api.IGasStorage;
import mekanism.api.IStorageTank;
import mekanism.api.ITileNetwork;
import mekanism.common.LiquidSlot;
import mekanism.common.PacketHandler;
import mekanism.common.TileEntityElectricBlock;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;

public class TileEntityElectrolyticSeparator extends TileEntityElectricBlock implements IGasStorage, IEnergySink, IJouleStorage, IElectricityReceiver, ITankContainer, IPeripheral
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
		
		if(inventory[3] != null && electricityStored < MAX_ELECTRICITY)
		{
			if(inventory[3].getItem() instanceof IItemElectric)
			{
				IItemElectric electricItem = (IItemElectric)inventory[3].getItem();

				if (electricItem.canProduceElectricity())
				{
					double joulesReceived = electricItem.onUse(electricItem.getMaxJoules(inventory[3]) * 0.005, inventory[3]);
					setJoules(electricityStored + joulesReceived);
				}
			}
			else if(inventory[3].getItem() instanceof IElectricItem)
			{
				IElectricItem item = (IElectricItem)inventory[3].getItem();
				if(item.canProvideEnergy())
				{
					double gain = ElectricItem.discharge(inventory[3], (int)((MAX_ELECTRICITY - electricityStored)*UniversalElectricity.TO_IC2_RATIO), 3, false, false)*UniversalElectricity.IC2_RATIO;
					setJoules(electricityStored + gain);
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
			waterSlot.setLiquid(waterSlot.liquidStored - 10);
			setJoules(electricityStored - 4);
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
		return (int)(electricityStored*i / MAX_ELECTRICITY);
	}
	
	@Override
	public void handlePacketData(INetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		if(!worldObj.isRemote)
		{
			try {
				outputType = EnumGas.getFromName(dataStream.readUTF());
			} catch (Exception e)
			{
				System.out.println("[Mekanism] Error while handling tile entity packet.");
				e.printStackTrace();
			}
			return;
		}
		
		try {
			facing = dataStream.readInt();
			electricityStored = dataStream.readDouble();
			waterSlot.liquidStored = dataStream.readInt();
			oxygenStored = dataStream.readInt();
			hydrogenStored = dataStream.readInt();
			outputType = EnumGas.getFromName(dataStream.readUTF());
			worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
		} catch (Exception e)
		{
			System.out.println("[Mekanism] Error while handling tile entity packet.");
			e.printStackTrace();
		}
	}

	@Override
	public void sendPacket() 
	{
		PacketHandler.sendTileEntityPacketToClients(this, 0, facing, electricityStored, waterSlot.liquidStored, oxygenStored, hydrogenStored, outputType.name);
	}

	@Override
	public void sendPacketWithRange() 
	{
		PacketHandler.sendTileEntityPacketToClients(this, 50, facing, electricityStored, waterSlot.liquidStored, oxygenStored, hydrogenStored, outputType.name);
	}
	
	/**
	 * Set this block's energy to a new amount.
	 * @param energy - new amount of energy
	 */
	public void setEnergy(int energy)
	{
		electricityStored = Math.max(Math.min(energy, MAX_ELECTRICITY), 0);
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
		return MAX_ELECTRICITY*UniversalElectricity.IC2_RATIO;
	}
	
	@Override
	public double getJoules(Object... data) 
	{
		return electricityStored*UniversalElectricity.IC2_RATIO;
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
		return electricityStored < MAX_ELECTRICITY ? ElectricInfo.getWatts((16)*UniversalElectricity.IC2_RATIO) : 0;
	}
	
	@Override
	public void onReceive(Object sender, double amps, double voltage, ForgeDirection side) 
	{
		double electricityToReceive = ElectricInfo.getJoules(amps, voltage);
		double electricityNeeded = MAX_ELECTRICITY - electricityStored;
		double electricityToStore = 0;
		
		if(electricityToReceive <= electricityNeeded)
		{
			electricityToStore = electricityToReceive;
		}
		else if(electricityToReceive > electricityNeeded)
		{
			electricityToStore = electricityNeeded;
		}
		setJoules(electricityStored + electricityToStore);
	}
	
	@Override
	public boolean demandsEnergy() 
	{
		return electricityStored < MAX_ELECTRICITY;
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
    	
    	return (int)(rejects*UniversalElectricity.TO_IC2_RATIO);
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
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("hydrogenStored", hydrogenStored);
        nbtTags.setInteger("oxygenStored", oxygenStored);
        nbtTags.setInteger("waterStored", waterSlot.liquidStored);
        nbtTags.setString("outputType", outputType.name);
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
	public void attach(IComputerAccess computer, String computerSide) {}

	@Override
	public void detach(IComputerAccess computer) {}
}
