package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.MekanismConfig.usage;
import mekanism.common.Upgrade;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISustainedTank;
import mekanism.common.base.ITankManager;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.Method;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")
public class TileEntityElectricPump extends TileEntityElectricBlock implements IFluidHandler, ISustainedTank, IConfigurable, IRedstoneControl, IUpgradeTile, ITankManager, IPeripheral
{
	/** This pump's tank */
	public FluidTank fluidTank = new FluidTank(10000);

	/** The nodes that have full sources near them or in them */
	public Set<Coord4D> recurringNodes = new HashSet<Coord4D>();

	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType = RedstoneControl.DISABLED;
	
	public TileComponentUpgrade upgradeComponent = new TileComponentUpgrade(this, 3);

	public TileEntityElectricPump()
	{
		super("ElectricPump", 10000);
		inventory = new ItemStack[4];
		
		upgradeComponent.clearSupportedTypes();
		upgradeComponent.setSupported(Upgrade.FILTER);
	}

	@Override
	public void onUpdate()
	{
		if(!worldObj.isRemote)
		{
			ChargeUtils.discharge(2, this);
	
			if(inventory[0] != null)
			{
				if(fluidTank.getFluid() != null && fluidTank.getFluid().amount >= FluidContainerRegistry.BUCKET_VOLUME)
				{
					if(inventory[0].getItem() instanceof IFluidContainerItem)
					{
						int prev = fluidTank.getFluidAmount();
						
						fluidTank.drain(FluidContainerUtils.insertFluid(fluidTank, inventory[0]), true);
						
						if(prev == fluidTank.getFluidAmount() || fluidTank.getFluidAmount() == 0)
						{
							if(inventory[1] == null)
							{
								inventory[1] = inventory[0].copy();
								inventory[0] = null;
								
								markDirty();
							}
						}
					}
					else if(FluidContainerRegistry.isEmptyContainer(inventory[0]))
					{
						ItemStack tempStack = FluidContainerRegistry.fillFluidContainer(fluidTank.getFluid(), inventory[0]);
	
						if(tempStack != null)
						{
							if(inventory[1] == null)
							{
								fluidTank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
	
								inventory[1] = tempStack;
								inventory[0].stackSize--;
	
								if(inventory[0].stackSize <= 0)
								{
									inventory[0] = null;
								}
	
								markDirty();
							}
							else if(tempStack.isItemEqual(inventory[1]) && tempStack.getMaxStackSize() > inventory[1].stackSize)
							{
								fluidTank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
	
								inventory[1].stackSize++;
								inventory[0].stackSize--;
	
								if(inventory[0].stackSize <= 0)
								{
									inventory[0] = null;
								}
	
								markDirty();
							}
						}
					}
				}
			}
		}

		if(!worldObj.isRemote && ticker % 20 == 0)
		{
			if(MekanismUtils.canFunction(this))
			{
				if(getEnergy() >= usage.electricPumpUsage && (fluidTank.getFluid() == null || fluidTank.getFluid().amount + FluidContainerRegistry.BUCKET_VOLUME <= fluidTank.getCapacity()))
				{
					suck(true);
				}
			}
			else {
				ticker--;
			}
		}

		super.onUpdate();

		if(fluidTank.getFluid() != null)
		{
			for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tileEntity = Coord4D.get(this).getFromSide(orientation).getTileEntity(worldObj);

				if(tileEntity instanceof IFluidHandler)
				{
					FluidStack toDrain = new FluidStack(fluidTank.getFluid(), Math.min(100, fluidTank.getFluidAmount()));
					fluidTank.drain(((IFluidHandler)tileEntity).fill(orientation.getOpposite(), toDrain, true), true);

					if(fluidTank.getFluid() == null || fluidTank.getFluid().amount <= 0)
					{
						break;
					}
				}
			}
		}
	}
	
	public boolean hasFilter()
	{
		return upgradeComponent.getInstalledTypes().contains(Upgrade.FILTER);
	}

	public boolean suck(boolean take)
	{
		List<Coord4D> tempPumpList = Arrays.asList(recurringNodes.toArray(new Coord4D[recurringNodes.size()]));
		Collections.shuffle(tempPumpList);

		//First see if there are any fluid blocks touching the pump - if so, sucks and adds the location to the recurring list
		for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
		{
			Coord4D wrapper = Coord4D.get(this).getFromSide(orientation);

			if(MekanismUtils.isFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
			{
				if(fluidTank.getFluid() == null || MekanismUtils.getFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord, hasFilter()).isFluidEqual(fluidTank.getFluid()))
				{
					if(take)
					{
						setEnergy(getEnergy() - usage.electricPumpUsage);
						recurringNodes.add(wrapper.clone());
						fluidTank.fill(MekanismUtils.getFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord, hasFilter()), true);
						worldObj.setBlockToAir(wrapper.xCoord, wrapper.yCoord, wrapper.zCoord);
					}

					return true;
				}
			}
		}

		//Finally, go over the recurring list of nodes and see if there is a fluid block available to suck - if not, will iterate around the recurring block, attempt to suck, 
		//and then add the adjacent block to the recurring list
		for(Coord4D wrapper : tempPumpList)
		{
			if(MekanismUtils.isFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
			{
				if(fluidTank.getFluid() == null || MekanismUtils.getFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord, hasFilter()).isFluidEqual(fluidTank.getFluid()))
				{
					if(take)
					{
						setEnergy(getEnergy() - usage.electricPumpUsage);
						fluidTank.fill(MekanismUtils.getFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord, hasFilter()), true);
						worldObj.setBlockToAir(wrapper.xCoord, wrapper.yCoord, wrapper.zCoord);
					}

					return true;
				}
			}

			//Add all the blocks surrounding this recurring node to the recurring node list
			for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
			{
				Coord4D side = wrapper.getFromSide(orientation);

				if(Coord4D.get(this).distanceTo(side) <= 80)
				{
					if(MekanismUtils.isFluid(worldObj, side.xCoord, side.yCoord, side.zCoord))
					{
						if(fluidTank.getFluid() == null || MekanismUtils.getFluid(worldObj, side.xCoord, side.yCoord, side.zCoord, hasFilter()).isFluidEqual(fluidTank.getFluid()))
						{
							if(take)
							{
								setEnergy(getEnergy() - usage.electricPumpUsage);
								recurringNodes.add(side);
								fluidTank.fill(MekanismUtils.getFluid(worldObj, side.xCoord, side.yCoord, side.zCoord, hasFilter()), true);
								worldObj.setBlockToAir(side.xCoord, side.yCoord, side.zCoord);
							}

							return true;
						}
					}
				}
			}

			recurringNodes.remove(wrapper);
		}

		return false;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);

		if(dataStream.readInt() == 1)
		{
			fluidTank.setFluid(new FluidStack(dataStream.readInt(), dataStream.readInt()));
		}
		else {
			fluidTank.setFluid(null);
		}
		
		controlType = RedstoneControl.values()[dataStream.readInt()];

		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		if(fluidTank.getFluid() != null)
		{
			data.add(1);
			data.add(fluidTank.getFluid().fluidID);
			data.add(fluidTank.getFluid().amount);
		}
		else {
			data.add(0);
		}
		
		data.add(controlType.ordinal());

		return data;
	}

	public int getScaledFluidLevel(int i)
	{
		return fluidTank.getFluid() != null ? fluidTank.getFluid().amount*i / 10000 : 0;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		if(fluidTank.getFluid() != null)
		{
			nbtTags.setTag("fluidTank", fluidTank.writeToNBT(new NBTTagCompound()));
		}

		nbtTags.setInteger("controlType", controlType.ordinal());

		NBTTagList recurringList = new NBTTagList();

		for(Coord4D wrapper : recurringNodes)
		{
			NBTTagCompound tagCompound = new NBTTagCompound();
			wrapper.write(tagCompound);
			recurringList.appendTag(tagCompound);
		}

		if(recurringList.tagCount() != 0)
		{
			nbtTags.setTag("recurringNodes", recurringList);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		if(nbtTags.hasKey("fluidTank"))
		{
			fluidTank.readFromNBT(nbtTags.getCompoundTag("fluidTank"));
		}

		if(nbtTags.hasKey("controlType"))
		{
			controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
		}

		if(nbtTags.hasKey("recurringNodes"))
		{
			NBTTagList tagList = nbtTags.getTagList("recurringNodes", NBT.TAG_COMPOUND);

			for(int i = 0; i < tagList.tagCount(); i++)
			{
				recurringNodes.add(Coord4D.read((NBTTagCompound)tagList.getCompoundTagAt(i)));
			}
		}
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 1)
		{
			return false;
		}
		else if(slotID == 0)
		{
			return FluidContainerRegistry.isEmptyContainer(itemstack);
		}
		else if(slotID == 2)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}

		return true;
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 2)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}
		else if(slotID == 1)
		{
			return true;
		}

		return false;
	}

	@Override
	public EnumSet<ForgeDirection> getConsumingSides()
	{
		return EnumSet.of(ForgeDirection.getOrientation(facing).getOpposite());
	}

	@Override
	public boolean canSetFacing(int side)
	{
		return side != 0 && side != 1;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(side == 1)
		{
			return new int[] {0};
		}
		else if(side == 0)
		{
			return new int[] {1};
		}
		else {
			return new int[] {2};
		}
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection direction)
	{
		if(direction == ForgeDirection.getOrientation(1))
		{
			return new FluidTankInfo[] {fluidTank.getInfo()};
		}

		return PipeUtils.EMPTY;
	}

	@Override
	public void setFluidStack(FluidStack fluidStack, Object... data)
	{
		fluidTank.setFluid(fluidStack);
	}

	@Override
	public FluidStack getFluidStack(Object... data)
	{
		return fluidTank.getFluid();
	}

	@Override
	public boolean hasTank(Object... data)
	{
		return true;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if(fluidTank.getFluid() != null && fluidTank.getFluid().getFluid() == resource.getFluid() && from == ForgeDirection.getOrientation(1))
		{
			return drain(from, resource.amount, doDrain);
		}

		return null;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if(from == ForgeDirection.getOrientation(1))
		{
			return fluidTank.drain(maxDrain, doDrain);
		}

		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return from == ForgeDirection.getOrientation(1);
	}

	@Override
	public boolean onSneakRightClick(EntityPlayer player, int side)
	{
		recurringNodes.clear();

		player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + MekanismUtils.localize("tooltip.configurator.pumpReset")));

		return true;
	}

	@Override
	public boolean onRightClick(EntityPlayer player, int side)
	{
		return false;
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
		return true;
	}

	@Override
	public TileComponentUpgrade getComponent() 
	{
		return upgradeComponent;
	}
	
	@Override
	public Object[] getTanks() 
	{
		return new Object[] {fluidTank};
	}

	@Override
	@Method(modid = "ComputerCraft")
	public String getType()
	{
		return getInventoryName();
	}

	@Override
	@Method(modid = "ComputerCraft")
	public String[] getMethodNames()
	{
		return new String[] {"reset"};
	}

	@Override
	@Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException
	{
		switch(method)
		{
			case 0:
				recurringNodes.clear();
				return new Object[] {"Pump calculation reset."};
			default:
				return new Object[] {"Unknown command."};
		}
	}

	@Override
	@Method(modid = "ComputerCraft")
	public void attach(IComputerAccess computer) {}

	@Override
	@Method(modid = "ComputerCraft")
	public void detach(IComputerAccess computer) {}

	@Override
	@Method(modid = "ComputerCraft")
	public boolean equals(IPeripheral other)
	{
		return this == other;
	}
}
