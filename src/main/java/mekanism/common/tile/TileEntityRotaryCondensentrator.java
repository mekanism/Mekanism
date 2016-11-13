package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import mekanism.api.MekanismConfig.usage;
import mekanism.api.Range4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mekanism.api.util.ListUtils;
import mekanism.common.Mekanism;
import mekanism.common.Upgrade;
import mekanism.common.Upgrade.IUpgradeInfoHandler;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITankManager;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class TileEntityRotaryCondensentrator extends TileEntityElectricBlock implements IActiveState, ISustainedData, IFluidHandlerWrapper, IGasHandler, ITubeConnection, IRedstoneControl, IUpgradeTile, IUpgradeInfoHandler, ITankManager, ISecurityTile
{
	public GasTank gasTank = new GasTank(MAX_FLUID);

	public FluidTank fluidTank = new FluidTank(MAX_FLUID);

	public static final int MAX_FLUID = 10000;

	/** 0: gas -> fluid; 1: fluid -> gas */
	public int mode;

	public int gasOutput = 256;
	
	public int updateDelay;

	public boolean isActive;

	public boolean clientActive;

	public double prevEnergy;

	public final double BASE_ENERGY_USAGE = usage.rotaryCondensentratorUsage;
	
	public double energyPerTick = BASE_ENERGY_USAGE;
	
	public double clientEnergyUsed;
	
	public TileComponentUpgrade upgradeComponent = new TileComponentUpgrade(this, 5);
	public TileComponentSecurity securityComponent = new TileComponentSecurity(this);

	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType = RedstoneControl.DISABLED;

	public TileEntityRotaryCondensentrator()
	{
		super("RotaryCondensentrator", BlockStateMachine.MachineType.ROTARY_CONDENSENTRATOR.baseEnergy);
		inventory = new ItemStack[6];
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(worldObj.isRemote)
		{
			if(updateDelay > 0)
			{
				updateDelay--;

				if(updateDelay == 0 && clientActive != isActive)
				{
					isActive = clientActive;
					MekanismUtils.updateBlock(worldObj, getPos());
				}
			}
		}

		if(!worldObj.isRemote)
		{
			if(updateDelay > 0)
			{
				updateDelay--;

				if(updateDelay == 0 && clientActive != isActive)
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
				}
			}

			ChargeUtils.discharge(4, this);

			if(mode == 0)
			{
				if(inventory[1] != null && (gasTank.getGas() == null || gasTank.getStored() < gasTank.getMaxGas()))
				{
					gasTank.receive(GasTransmission.removeGas(inventory[1], gasTank.getGasType(), gasTank.getNeeded()), true);
				}

				if(FluidContainerUtils.isFluidContainer(inventory[2]))
				{
					FluidContainerUtils.handleContainerItemFill(this, fluidTank, 2, 3);
				}

				if(getEnergy() >= energyPerTick && MekanismUtils.canFunction(this) && isValidGas(gasTank.getGas()) && (fluidTank.getFluid() == null || (fluidTank.getFluid().amount < MAX_FLUID && gasEquals(gasTank.getGas(), fluidTank.getFluid()))))
				{
					int operations = getUpgradedUsage();
					double prev = getEnergy();
					
					setActive(true);
					fluidTank.fill(new FluidStack(gasTank.getGas().getGas().getFluid(), operations), true);
					gasTank.draw(operations, true);
					setEnergy(getEnergy() - energyPerTick*operations);
					clientEnergyUsed = prev-getEnergy();
				}
				else {
					if(prevEnergy >= getEnergy())
					{
						setActive(false);
					}
				}
			}
			else if(mode == 1)
			{
				if(inventory[0] != null && gasTank.getGas() != null)
				{
					gasTank.draw(GasTransmission.addGas(inventory[0], gasTank.getGas()), true);
				}

				if(gasTank.getGas() != null)
				{
					GasStack toSend = new GasStack(gasTank.getGas().getGas(), Math.min(gasTank.getGas().amount, gasOutput));
					gasTank.draw(GasTransmission.emit(toSend, this, ListUtils.asList(MekanismUtils.getLeft(facing))), true);
				}

				if(FluidContainerUtils.isFluidContainer(inventory[2]))
				{
					FluidContainerUtils.handleContainerItemEmpty(this, fluidTank, 2, 3);
				}

				if(getEnergy() >= energyPerTick && MekanismUtils.canFunction(this) && isValidFluid(fluidTank.getFluid()) && (gasTank.getGas() == null || (gasTank.getStored() < MAX_FLUID && gasEquals(gasTank.getGas(), fluidTank.getFluid()))))
				{
					int operations = getUpgradedUsage();
					double prev = getEnergy();
					
					setActive(true);
					gasTank.receive(new GasStack(GasRegistry.getGas(fluidTank.getFluid().getFluid()), operations), true);
					fluidTank.drain(operations, true);
					setEnergy(getEnergy() - energyPerTick*operations);
					clientEnergyUsed = prev-getEnergy();
				}
				else {
					if(prevEnergy >= getEnergy())
					{
						setActive(false);
					}
				}
			}

			prevEnergy = getEnergy();
		}
	}
	
	public int getUpgradedUsage()
	{
		int possibleProcess = (int)Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED));
		
		if(mode == 0) //Gas to fluid
		{
			possibleProcess = Math.min(Math.min(gasTank.getStored(), fluidTank.getCapacity()-fluidTank.getFluidAmount()), possibleProcess);
		}
		else { //Fluid to gas
			possibleProcess = Math.min(Math.min(fluidTank.getFluidAmount(), gasTank.getNeeded()), possibleProcess);
		}
		
		possibleProcess = Math.min((int)(getEnergy()/energyPerTick), possibleProcess);
		
		return possibleProcess;
	}

	public boolean isValidGas(GasStack g)
	{
		return g != null && g.getGas().hasFluid();

	}

	public boolean gasEquals(GasStack gas, FluidStack fluid)
	{
		return fluid != null && gas != null && gas.getGas().hasFluid() && gas.getGas().getFluid() == fluid.getFluid();

	}

	public boolean isValidFluid(FluidStack f)
	{
		return f != null && GasRegistry.getGas(f.getFluid()) != null;

	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			int type = dataStream.readInt();

			if(type == 0)
			{
				mode = mode == 0 ? 1 : 0;
			}

			for(EntityPlayer player : playersUsing)
			{
				Mekanism.packetHandler.sendTo(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList<Object>())), (EntityPlayerMP)player);
			}

			return;
		}

		super.handlePacketData(dataStream);

		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			mode = dataStream.readInt();
			isActive = dataStream.readBoolean();
			controlType = RedstoneControl.values()[dataStream.readInt()];
			clientEnergyUsed = dataStream.readDouble();
	
			if(dataStream.readBoolean())
			{
				fluidTank.setFluid(new FluidStack(FluidRegistry.getFluid(ByteBufUtils.readUTF8String(dataStream)), dataStream.readInt()));
			}
			else {
				fluidTank.setFluid(null);
			}
	
			if(dataStream.readBoolean())
			{
				gasTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
			}
			else {
				gasTank.setGas(null);
			}
	
			if(updateDelay == 0 && clientActive != isActive)
			{
				updateDelay = general.UPDATE_DELAY;
				isActive = clientActive;
				MekanismUtils.updateBlock(worldObj, getPos());
			}
		}
	}

	@Override
	public ArrayList<Object> getNetworkedData(ArrayList<Object> data)
	{
		super.getNetworkedData(data);

		data.add(mode);
		data.add(isActive);
		data.add(controlType.ordinal());
		data.add(clientEnergyUsed);

		if(fluidTank.getFluid() != null)
		{
			data.add(true);
			data.add(FluidRegistry.getFluidName(fluidTank.getFluid()));
			data.add(fluidTank.getFluid().amount);
		}
		else {
			data.add(false);
		}

		if(gasTank.getGas() != null)
		{
			data.add(true);
			data.add(gasTank.getGas().getGas().getID());
			data.add(gasTank.getStored());
		}
		else {
			data.add(false);
		}

		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		mode = nbtTags.getInteger("mode");
		isActive = nbtTags.getBoolean("isActive");
		controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];

		gasTank.read(nbtTags.getCompoundTag("gasTank"));

		if(nbtTags.hasKey("fluidTank"))
		{
			fluidTank.readFromNBT(nbtTags.getCompoundTag("fluidTank"));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setInteger("mode", mode);
		nbtTags.setBoolean("isActive", isActive);
		nbtTags.setInteger("controlType", controlType.ordinal());
		nbtTags.setTag("gasTank", gasTank.write(new NBTTagCompound()));

		if(fluidTank.getFluid() != null)
		{
			nbtTags.setTag("fluidTank", fluidTank.writeToNBT(new NBTTagCompound()));
		}
		
		return nbtTags;
	}

	@Override
	public boolean canSetFacing(int i)
	{
		return i != 0 && i != 1;
	}

	@Override
	public void setActive(boolean active)
	{
		isActive = active;

		if(clientActive != active && updateDelay == 0)
		{
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList<Object>())), new Range4D(Coord4D.get(this)));

			updateDelay = 10;
			clientActive = active;
		}
	}

	@Override
	public boolean getActive()
	{
		return isActive;
	}

	@Override
	public boolean renderUpdate()
	{
		return false;
	}

	@Override
	public boolean lightUpdate()
	{
		return true;
	}

	@Override
	public boolean canTubeConnect(EnumFacing side)
	{
		return side == MekanismUtils.getLeft(facing);
	}

	@Override
	public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer)
	{
		return gasTank.receive(stack, doTransfer);
	}

	@Override
	public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer)
	{
		return gasTank.draw(amount, doTransfer);
	}

	@Override
	public boolean canDrawGas(EnumFacing side, Gas type)
	{
		return (mode == 1 && side == MekanismUtils.getLeft(facing)) && gasTank.canDraw(type);
	}

	@Override
	public boolean canReceiveGas(EnumFacing side, Gas type)
	{
		return (mode == 0 && side == MekanismUtils.getLeft(facing)) && gasTank.canReceive(type);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing side)
	{
		return capability == Capabilities.GAS_HANDLER_CAPABILITY || capability == Capabilities.TUBE_CONNECTION_CAPABILITY 
				|| capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, side);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing side)
	{
		if(capability == Capabilities.GAS_HANDLER_CAPABILITY || capability == Capabilities.TUBE_CONNECTION_CAPABILITY)
		{
			return (T)this;
		}
		
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
		{
			return (T)new FluidHandlerWrapper(this, side);
		}
		
		return super.getCapability(capability, side);
	}
	
	@Override
	public void writeSustainedData(ItemStack itemStack)
	{
		if(fluidTank.getFluid() != null)
		{
			ItemDataUtils.setCompound(itemStack, "fluidTank", fluidTank.getFluid().writeToNBT(new NBTTagCompound()));
		}
		
		if(gasTank.getGas() != null)
		{
			ItemDataUtils.setCompound(itemStack, "gasTank", gasTank.getGas().write(new NBTTagCompound()));
		}
	}
	
	@Override
	public void readSustainedData(ItemStack itemStack)
	{
		fluidTank.setFluid(FluidStack.loadFluidStackFromNBT(ItemDataUtils.getCompound(itemStack, "fluidTank")));
		gasTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "gasTank")));
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill)
	{
		if(canFill(from, resource.getFluid()))
		{
			return fluidTank.fill(resource, doFill);
		}

		return 0;
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
	{
		if(fluidTank.getFluid() != null && fluidTank.getFluid().getFluid() == resource.getFluid())
		{
			return drain(from, resource.amount, doDrain);
		}

		return null;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		return mode == 1 && from == MekanismUtils.getRight(facing) && (fluidTank.getFluid() == null ? isValidFluid(new FluidStack(fluid, 1)) : fluidTank.getFluid().getFluid() == fluid);
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		return mode == 0 && from == MekanismUtils.getRight(facing);
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		if(from == MekanismUtils.getRight(facing))
		{
			return new FluidTankInfo[] {fluidTank.getInfo()};
		}

		return PipeUtils.EMPTY;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		if(canDrain(from, null))
		{
			return fluidTank.drain(maxDrain, doDrain);
		}

		return null;
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
		MekanismUtils.saveChunk(this);
	}

	@Override
	public boolean canPulse()
	{
		return false;
	}
	
	@Override
	public TileComponentUpgrade getComponent() 
	{
		return upgradeComponent;
	}
	
	@Override
	public void recalculateUpgradables(Upgrade upgrade)
	{
		super.recalculateUpgradables(upgrade);

		switch(upgrade)
		{
			case ENERGY:
				maxEnergy = MekanismUtils.getMaxEnergy(this, BASE_MAX_ENERGY);
				energyPerTick = MekanismUtils.getBaseEnergyPerTick(this, BASE_ENERGY_USAGE);
			default:
				break;
		}
	}

	@Override
	public List<String> getInfo(Upgrade upgrade) 
	{
		return upgrade == Upgrade.SPEED ? upgrade.getExpScaledInfo(this) : upgrade.getMultScaledInfo(this);
	}
	
	@Override
	public Object[] getTanks() 
	{
		return new Object[] {gasTank, fluidTank};
	}
	
	@Override
	public TileComponentSecurity getSecurity()
	{
		return securityComponent;
	}
}
