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
import mekanism.common.ISustainedTank;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.ChargeUtils;
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
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityElectricPump extends TileEntityElectricBlock implements IFluidHandler, ISustainedTank, IConfigurable
{
	/** This pump's tank */
	public FluidTank fluidTank;

	/** The nodes that have full sources near them or in them */
	public Set<Coord4D> recurringNodes = new HashSet<Coord4D>();

	/** The nodes that have already been sucked up, but are held on to in order to remove dead blocks */
	public Set<Coord4D> cleaningNodes = new HashSet<Coord4D>();

	public TileEntityElectricPump()
	{
		super("ElectricPump", 10000);
		fluidTank = new FluidTank(10000);
		inventory = new ItemStack[3];
	}

	@Override
	public void onUpdate()
	{
		ChargeUtils.discharge(2, this);

		if(inventory[0] != null)
		{
			if(fluidTank.getFluid() != null && fluidTank.getFluid().amount >= FluidContainerRegistry.BUCKET_VOLUME)
			{
				if(FluidContainerRegistry.isEmptyContainer(inventory[0]))
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

		if(!worldObj.isRemote && worldObj.getWorldTime() % 20 == 0)
		{
			if(getEnergy() >= Mekanism.electricPumpUsage && (fluidTank.getFluid() == null || fluidTank.getFluid().amount+FluidContainerRegistry.BUCKET_VOLUME <= 10000))
			{
				if(suck(true))
				{
					Mekanism.packetHandler.sendToAllAround(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), Coord4D.get(this).getTargetPoint(50D));
				}

				clean(true);
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

	public boolean suck(boolean take)
	{
		List<Coord4D> tempPumpList = Arrays.asList(recurringNodes.toArray(new Coord4D[recurringNodes.size()]));
		Collections.shuffle(tempPumpList);

		for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
		{
			Coord4D wrapper = Coord4D.get(this).getFromSide(orientation);

			if(MekanismUtils.isFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
			{
				if(fluidTank.getFluid() == null || MekanismUtils.getFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord).isFluidEqual(fluidTank.getFluid()))
				{
					if(take)
					{
						setEnergy(getEnergy() - Mekanism.electricPumpUsage);
						recurringNodes.add(wrapper.clone());
						fluidTank.fill(MekanismUtils.getFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord), true);
						worldObj.setBlockToAir(wrapper.xCoord, wrapper.yCoord, wrapper.zCoord);
					}

					return true;
				}
			}
		}

		for(Coord4D wrapper : cleaningNodes)
		{
			if(MekanismUtils.isFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
			{
				if(fluidTank.getFluid() != null && MekanismUtils.getFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord).isFluidEqual(fluidTank.getFluid()))
				{
					if(take)
					{
						setEnergy(getEnergy() - Mekanism.electricPumpUsage);
						fluidTank.fill(MekanismUtils.getFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord), true);
						worldObj.setBlockToAir(wrapper.xCoord, wrapper.yCoord, wrapper.zCoord);
					}

					return true;
				}
			}
		}

		for(Coord4D wrapper : tempPumpList)
		{
			if(MekanismUtils.isFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
			{
				if(fluidTank.getFluid() == null || MekanismUtils.getFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord).isFluidEqual(fluidTank.getFluid()))
				{
					if(take)
					{
						setEnergy(getEnergy() - Mekanism.electricPumpUsage);
						fluidTank.fill(MekanismUtils.getFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord), true);
						worldObj.setBlockToAir(wrapper.xCoord, wrapper.yCoord, wrapper.zCoord);
					}

					return true;
				}
			}

			for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
			{
				Coord4D side = wrapper.getFromSide(orientation);

				if(Coord4D.get(this).distanceTo(side) <= 80)
				{
					if(MekanismUtils.isFluid(worldObj, side.xCoord, side.yCoord, side.zCoord))
					{
						if(fluidTank.getFluid() == null || MekanismUtils.getFluid(worldObj, side.xCoord, side.yCoord, side.zCoord).isFluidEqual(fluidTank.getFluid()))
						{
							if(take)
							{
								setEnergy(getEnergy() - Mekanism.electricPumpUsage);
								recurringNodes.add(side);
								fluidTank.fill(MekanismUtils.getFluid(worldObj, side.xCoord, side.yCoord, side.zCoord), true);
								worldObj.setBlockToAir(side.xCoord, side.yCoord, side.zCoord);
							}

							return true;
						}
					}
				}
			}

			cleaningNodes.add(wrapper);
			recurringNodes.remove(wrapper);
		}

		return false;
	}

	public boolean clean(boolean take)
	{
		boolean took = false;

		if(!worldObj.isRemote)
		{
			for(Coord4D wrapper : cleaningNodes)
			{
				if(MekanismUtils.isDeadFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
				{
					if(fluidTank.getFluid() != null && MekanismUtils.getFluidId(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord) == fluidTank.getFluid().fluidID)
					{
						took = true;

						if(take)
						{
							worldObj.setBlockToAir(wrapper.xCoord, wrapper.yCoord, wrapper.zCoord);
						}
					}
				}
			}

			for(Coord4D wrapper : recurringNodes)
			{
				if(MekanismUtils.isDeadFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
				{
					if(fluidTank.getFluid() != null && MekanismUtils.getFluidId(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord) == fluidTank.getFluid().fluidID)
					{
						took = true;

						if(take)
						{
							worldObj.setBlockToAir(wrapper.xCoord, wrapper.yCoord, wrapper.zCoord);
						}
					}
				}
			}

			for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
			{
				Coord4D wrapper = Coord4D.get(this).getFromSide(orientation);

				if(MekanismUtils.isDeadFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
				{
					if(fluidTank.getFluid() != null && MekanismUtils.getFluidId(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord) == fluidTank.getFluid().fluidID)
					{
						took = true;

						if(take)
						{
							worldObj.setBlockToAir(wrapper.xCoord, wrapper.yCoord, wrapper.zCoord);
						}
					}
				}
			}
		}

		return took;
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

		NBTTagList cleaningList = new NBTTagList();

		for(Coord4D obj : cleaningNodes)
		{
			cleaningList.appendTag(obj.write(new NBTTagCompound()));
		}

		if(cleaningList.tagCount() != 0)
		{
			nbtTags.setTag("cleaningNodes", cleaningList);
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

		if(nbtTags.hasKey("recurringNodes"))
		{
			NBTTagList tagList = nbtTags.getTagList("recurringNodes", NBT.TAG_COMPOUND);

			for(int i = 0; i < tagList.tagCount(); i++)
			{
				recurringNodes.add(Coord4D.read((NBTTagCompound)tagList.getCompoundTagAt(i)));
			}
		}

		if(nbtTags.hasKey("cleaningNodes"))
		{
			NBTTagList tagList = nbtTags.getTagList("cleaningNodes", NBT.TAG_COMPOUND);

			for(int i = 0; i < tagList.tagCount(); i++)
			{
				cleaningNodes.add(Coord4D.read((NBTTagCompound)tagList.getCompoundTagAt(i)));
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
	protected EnumSet<ForgeDirection> getConsumingSides()
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
		cleaningNodes.clear();

		player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + MekanismUtils.localize("tooltip.configurator.pumpReset")));

		return true;
	}

	@Override
	public boolean onRightClick(EntityPlayer player, int side)
	{
		return false;
	}
}
