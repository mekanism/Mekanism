package mekanism.generators.common.tileentity;

import java.util.ArrayList;
import java.util.Random;

import mekanism.api.Object3D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasAcceptor;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.ISustainedTank;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tileentity.TileEntityElectricBlock;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.block.BlockGenerator.GeneratorType;
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
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;

public class TileEntityElectrolyticSeparator extends TileEntityElectricBlock implements IFluidHandler, IPeripheral, ITubeConnection, ISustainedTank
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
	public Gas outputType;
	
	/** Type type of gas this block is dumping. */
	public Gas dumpType;

	public TileEntityElectrolyticSeparator()
	{
		super("ElectrolyticSeparator", GeneratorType.ELECTROLYTIC_SEPARATOR.maxEnergy);
		inventory = new ItemStack[4];
		outputType = GasRegistry.getGas("oxygen");
		dumpType = null;
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
						
						if(inventory[0].getItem().hasContainerItem())
						{
							inventory[0] = inventory[0].getItem().getContainerItemStack(inventory[0]);
						}
						else {
							inventory[0].stackSize--;
						}
						
						if(inventory[0].stackSize == 0)
						{
							inventory[0] = null;
						}
					}
				}
			}
			
			if(!worldObj.isRemote)
			{
				if(inventory[1] != null && hydrogenStored > 0)
				{
					hydrogenStored -= GasTransmission.addGas(inventory[1], new GasStack(GasRegistry.getGas("hydrogen"), hydrogenStored));
					MekanismUtils.saveChunk(this);
				}
				
				if(inventory[2] != null && oxygenStored > 0)
				{
					hydrogenStored -= GasTransmission.addGas(inventory[2], new GasStack(GasRegistry.getGas("oxygen"), oxygenStored));
					MekanismUtils.saveChunk(this);
				}
			}
			
			if(oxygenStored < MAX_GAS && hydrogenStored < MAX_GAS && waterTank.getFluid() != null && waterTank.getFluid().amount-2 >= 0 && getEnergy()-100 > 0)
			{
				waterTank.drain(2, true);
				setEnergy(getEnergy() - MekanismGenerators.electrolyticSeparatorUsage);
				setStored(GasRegistry.getGas("oxygen"), oxygenStored + 1);
				setStored(GasRegistry.getGas("hydrogen"), hydrogenStored + 2);
			}
			
			if(outputType != null && getStored(outputType) > 0)
			{
				GasStack toSend = new GasStack(outputType, Math.min(getStored(outputType), output));
				setStored(outputType, getStored(outputType) - GasTransmission.emitGasToNetwork(toSend, this, ForgeDirection.getOrientation(facing)));
				
				TileEntity tileEntity = Object3D.get(this).getFromSide(ForgeDirection.getOrientation(facing)).getTileEntity(worldObj);
				
				if(tileEntity instanceof IGasAcceptor)
				{
					if(((IGasAcceptor)tileEntity).canReceiveGas(ForgeDirection.getOrientation(facing).getOpposite(), outputType))
					{
						int added = ((IGasAcceptor)tileEntity).receiveGas(new GasStack(outputType, Math.min(getStored(outputType), output)));
						
						setStored(outputType, getStored(outputType) - added);
					}
				}
			}
			
			if(dumpType != null && getStored(dumpType) > 0)
			{
				setStored(dumpType, (getStored(dumpType) - 8));
				
				if(worldObj.rand.nextInt(3) == 2)
				{
					PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Object3D.get(this), getParticlePacket(new ArrayList())), Object3D.get(this), 40D);
				}
			}
		}
	}
	
	public int getStored(Gas gas)
	{
		if(gas == GasRegistry.getGas("oxygen"))
		{
			return oxygenStored;
		}
		else if(gas == GasRegistry.getGas("hydrogen"))
		{
			return hydrogenStored;
		}
		
		return 0;
	}
	
	public void setStored(Gas type, int amount)
	{
		if(type == GasRegistry.getGas("hydrogen"))
		{
			hydrogenStored = Math.max(Math.min(amount, MAX_GAS), 0);
		}
		else if(type == GasRegistry.getGas("oxygen"))
		{
			oxygenStored = Math.max(Math.min(amount, MAX_GAS), 0);
		}
		
		MekanismUtils.saveChunk(this);
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
		else if(slotID == 1 || slotID == 2)
		{
			return itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).getGas(itemstack) != null && 
					((IGasItem)itemstack.getItem()).getGas(itemstack).amount == ((IGasItem)itemstack.getItem()).getMaxGas(itemstack);
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
			return itemstack.getItem() instanceof IGasItem && (((IGasItem)itemstack.getItem()).getGas(itemstack) == null || ((IGasItem)itemstack.getItem()).getGas(itemstack).getGas() == GasRegistry.getGas("hydrogen"));
		}
		else if(slotID == 2)
		{
			return itemstack.getItem() instanceof IGasItem && (((IGasItem)itemstack.getItem()).getGas(itemstack) == null || ((IGasItem)itemstack.getItem()).getGas(itemstack).getGas() == GasRegistry.getGas("oxygen"));
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
				outputType = GasRegistry.getGas(dataStream.readInt());
			}
			else if(type == 1)
			{
				dumpType = GasRegistry.getGas(dataStream.readInt());
			}
			
			return;
		}
		
		super.handlePacketData(dataStream);
		
		int type = dataStream.readInt();
		
		if(type == 0)
		{
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
			outputType = GasRegistry.getGas(dataStream.readInt());
			dumpType = GasRegistry.getGas(dataStream.readInt());
		}
		else if(type == 1)
		{
			spawnParticle();
		}
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(0);
		if(waterTank.getFluid() != null)
		{
			data.add(waterTank.getFluid().amount);
		}
		else {
			data.add(0);
		}
		
		data.add(oxygenStored);
		data.add(hydrogenStored);
		data.add(GasRegistry.getGasID(outputType));
		data.add(GasRegistry.getGasID(dumpType));
		
		return data;
	}
	
	public ArrayList getParticlePacket(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(1);
		return data;
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
        
        try {
	        outputType = Gas.readFromNBT(nbtTags.getCompoundTag("outputType"));
	        dumpType = Gas.readFromNBT(nbtTags.getCompoundTag("dumpType"));
        } catch(Exception e) {} //TODO remove next major release
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
        
        if(outputType != null)
        {
        	nbtTags.setCompoundTag("outputType", outputType.write(new NBTTagCompound()));
        }
        
        if(dumpType != null)
        {
        	nbtTags.setCompoundTag("dumpType", dumpType.write(new NBTTagCompound()));
        }
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
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception 
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
