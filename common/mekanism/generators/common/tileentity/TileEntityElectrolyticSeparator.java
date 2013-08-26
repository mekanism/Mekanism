package mekanism.generators.common.tileentity;

import ic2.api.energy.tile.IEnergySink;

import java.util.ArrayList;
import java.util.Random;

import mekanism.api.IStorageTank;
import mekanism.api.Object3D;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.gas.EnumGas;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasAcceptor;
import mekanism.api.gas.IGasStorage;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.ISustainedTank;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.tileentity.TileEntityElectricBlock;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.block.BlockGenerator.GeneratorType;
import mekanism.generators.common.network.PacketElectrolyticSeparatorParticle;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class TileEntityElectrolyticSeparator extends TileEntityElectricBlock implements IGasStorage, IEnergySink, IFluidHandler, IPeripheral, ITubeConnection, IStrictEnergyAcceptor, ISustainedTank
{
	/** This separator's water slot. */
	public FluidTank waterTank = new FluidTank(24000);
	
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
		super("Electrolytic Separator", GeneratorType.ELECTROLYTIC_SEPARATOR.maxEnergy);
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
				FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(inventory[0]);
				
				if(fluid != null && fluid.getFluid() == FluidRegistry.WATER)
				{
					if(waterTank.getFluid() == null || waterTank.getFluid().amount+fluid.amount <= waterTank.getCapacity())
					{
						waterTank.fill(fluid, true);
						
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
			
			if(oxygenStored < MAX_GAS && hydrogenStored < MAX_GAS && waterTank.getFluid() != null && waterTank.getFluid().amount-2 >= 0 && electricityStored-100 > 0)
			{
				waterTank.drain(2, true);
				setEnergy(electricityStored - MekanismGenerators.electrolyticSeparatorUsage);
				setGas(EnumGas.OXYGEN, oxygenStored + 1);
				setGas(EnumGas.HYDROGEN, hydrogenStored + 2);
			}
			
			if(outputType != EnumGas.NONE && getGas(outputType) > 0)
			{
				setGas(outputType, getGas(outputType) - (Math.min(getGas(outputType), output) - GasTransmission.emitGasToNetwork(outputType, Math.min(getGas(outputType), output), this, ForgeDirection.getOrientation(facing))));
				
				TileEntity tileEntity = Object3D.get(this).getFromSide(ForgeDirection.getOrientation(facing)).getTileEntity(worldObj);
				
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
					PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketElectrolyticSeparatorParticle().setParams(this), Object3D.get(this), 40D);
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
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 3)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}
		else if(slotID == 0)
		{
			return FluidContainerRegistry.isEmptyContainer(itemstack);
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
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return FluidContainerRegistry.getFluidForFilledItem(itemstack) != null && FluidContainerRegistry.getFluidForFilledItem(itemstack).getFluid() == FluidRegistry.WATER;
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
			return ChargeUtils.canBeDischarged(itemstack);
		}
		
		return true;
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
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
		return waterTank.getFluid() != null ? waterTank.getFluid().amount*i / waterTank.getCapacity() : 0;
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
			waterTank.setFluid(new FluidStack(FluidRegistry.WATER, amount));
		}
		else {
			waterTank.setFluid(null);
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
		
		if(waterTank.getFluid() != null)
		{
			data.add(waterTank.getFluid().amount);
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
	public double demandedEnergyUnits() 
	{
		return (MAX_ELECTRICITY - electricityStored)*Mekanism.TO_IC2;
	}
	
	@Override
	public int getMaxSafeInput()
	{
		return 2048;
	}

	@Override
    public double injectEnergyUnits(ForgeDirection direction, double i)
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
    	
    	return rejects*Mekanism.TO_IC2;
    }
	
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return direction != ForgeDirection.getOrientation(facing);
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
        
        if(waterTank.getFluid() != null)
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
				return new Object[] {waterTank.getFluid() != null ? waterTank.getFluid().amount : 0};
			case 5:
				return new Object[] {waterTank.getFluid() != null ? (waterTank.getCapacity()-waterTank.getFluid().amount) : 0};
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
	public void setFluidStack(FluidStack fluidStack, Object... data) 
	{
		waterTank.setFluid(fluidStack);
	}

	@Override
	public FluidStack getFluidStack(Object... data) 
	{
		return waterTank.getFluid();
	}

	@Override
	public boolean hasTank(Object... data) 
	{
		return true;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) 
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) 
	{
		return fluid == FluidRegistry.WATER;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) 
	{
		return false;
	}
	
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) 
	{
		if(resource.getFluid() == FluidRegistry.WATER)
		{
			return waterTank.fill(resource, doFill);
		}
		
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) 
	{
		return new FluidTankInfo[] {waterTank.getInfo()};
	}
}
