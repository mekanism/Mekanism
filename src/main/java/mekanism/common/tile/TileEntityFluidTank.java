package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.IConfigurable;
import mekanism.api.MekanismConfig.general;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.FluidTankTier;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IFluidContainerManager;
import mekanism.common.base.ISustainedTank;
import mekanism.common.base.ITankManager;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityFluidTank extends TileEntityContainerBlock implements IActiveState, IConfigurable, IFluidHandler, ISustainedTank, IFluidContainerManager, ITankManager, ISecurityTile, ITierUpgradeable
{
	public boolean isActive;

	public boolean clientActive;
	
	public FluidTank fluidTank;
	
	public ContainerEditMode editMode = ContainerEditMode.BOTH;
	
	public FluidTankTier tier = FluidTankTier.BASIC;
	
	public int updateDelay;
	
	public int prevAmount;
	
	public int valve;
	public Fluid valveFluid;
	
	public float prevScale;
	
	public boolean needsPacket;
	
	public int currentRedstoneLevel;
	
	public TileComponentSecurity securityComponent = new TileComponentSecurity(this);
	
	public TileEntityFluidTank() 
	{
		super("FluidTank");
		
		fluidTank = new FluidTank(tier.storage);
		inventory = new ItemStack[2];
	}
	
	@Override
	public boolean upgrade(BaseTier upgradeTier)
	{
		if(upgradeTier.ordinal() != tier.ordinal()+1)
		{
			return false;
		}
		
		tier = FluidTankTier.values()[upgradeTier.ordinal()];
		fluidTank.setCapacity(tier.storage);
		
		Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
		markDirty();
		
		return true;
	}
	
	@Override
	public boolean canSetFacing(int facing)
	{
		return false;
	}

	@Override
	public void onUpdate() 
	{
		if(worldObj.isRemote)
		{
			if(updateDelay > 0)
			{
				updateDelay--;

				if(updateDelay == 0 && clientActive != isActive)
				{
					isActive = clientActive;
					MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
				}
			}
			
			float targetScale = (float)(fluidTank.getFluid() != null ? fluidTank.getFluid().amount : 0)/fluidTank.getCapacity();

			if(Math.abs(prevScale - targetScale) > 0.01)
			{
				prevScale = (9*prevScale + targetScale)/10;
			}
		}
		else {
			if(updateDelay > 0)
			{
				updateDelay--;

				if(updateDelay == 0 && clientActive != isActive)
				{
					needsPacket = true;
				}
			}
			
			if(valve > 0)
			{
				valve--;
				
				if(valve == 0)
				{
					valveFluid = null;
					needsPacket = true;
				}
			}
			
			if(fluidTank.getFluidAmount() != prevAmount)
			{
				MekanismUtils.saveChunk(this);
				needsPacket = true;
			}
			
			prevAmount = fluidTank.getFluidAmount();
			
			if(inventory[0] != null)
			{
				manageInventory();
			}
			
			if(isActive)
			{
				activeEmit();
			}
			
			int newRedstoneLevel = getRedstoneLevel();

			if(newRedstoneLevel != currentRedstoneLevel)
			{
				markDirty();
				currentRedstoneLevel = newRedstoneLevel;
			}
			
			if(needsPacket)
			{
				Mekanism.packetHandler.sendToAllAround(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), Coord4D.get(this).getTargetPoint(50));
			}
			
			needsPacket = false;
		}
	}
	
	@Override
	public String getInventoryName()
	{
		return LangUtils.localize("tile.FluidTank" + tier.getBaseTier().getName() + ".name");
	}
	
	private void activeEmit()
	{
		if(fluidTank.getFluid() != null)
		{
			TileEntity tileEntity = Coord4D.get(this).getFromSide(ForgeDirection.DOWN).getTileEntity(worldObj);

			if(tileEntity instanceof IFluidHandler)
			{
				FluidStack toDrain = new FluidStack(fluidTank.getFluid(), Math.min(tier.output, fluidTank.getFluidAmount()));
				fluidTank.drain(((IFluidHandler)tileEntity).fill(ForgeDirection.UP, toDrain, true), true);
			}
		}
	}
	
	private void manageInventory()
	{
		if(inventory[0] != null)
		{
			if(inventory[0].getItem() instanceof IFluidContainerItem)
			{
				if(editMode == ContainerEditMode.FILL && fluidTank.getFluidAmount() > 0)
				{
					FluidContainerUtils.handleContainerItemFill(this, fluidTank, 0, 1);
				}
				else if(editMode == ContainerEditMode.EMPTY)
				{
					FluidStack ret = FluidContainerUtils.handleContainerItemEmpty(this, inventory, fluidTank.getFluid(), getCurrentNeeded(), 0, 1, null);
					
					if(ret != null)
					{
						fluidTank.setFluid(PipeUtils.copy(ret, Math.min(fluidTank.getCapacity(), ret.amount)));
						
						int rejects = Math.max(0, ret.amount - fluidTank.getCapacity());
						
						if(rejects > 0)
						{
							pushUp(PipeUtils.copy(ret, rejects), true);
						}
					}
				}
			}
			else if(FluidContainerRegistry.isEmptyContainer(inventory[0]) && (editMode == ContainerEditMode.BOTH || editMode == ContainerEditMode.FILL))
			{
				FluidContainerUtils.handleRegistryItemFill(this, fluidTank, 0, 1);
			}
			else if(FluidContainerRegistry.isFilledContainer(inventory[0]) && (editMode == ContainerEditMode.BOTH || editMode == ContainerEditMode.EMPTY))
			{
				FluidStack ret = FluidContainerUtils.handleRegistryItemEmpty(this, inventory, fluidTank.getFluid(), getCurrentNeeded(), 0, 1, null);
				
				if(ret != null)
				{
					fluidTank.setFluid(PipeUtils.copy(ret, Math.min(fluidTank.getCapacity(), ret.amount)));
					
					int rejects = Math.max(0, ret.amount - fluidTank.getCapacity());
					
					if(rejects > 0)
					{
						pushUp(PipeUtils.copy(ret, rejects), true);
					}
				}
			}
		}
	}
	
	public int pushUp(FluidStack fluid, boolean doFill)
	{
		Coord4D up = Coord4D.get(this).getFromSide(ForgeDirection.UP);
		
		if(up.getTileEntity(worldObj) instanceof TileEntityFluidTank)
		{
			IFluidHandler handler = (IFluidHandler)up.getTileEntity(worldObj);
			
			if(handler.canFill(ForgeDirection.DOWN, fluid.getFluid()))
			{
				return handler.fill(ForgeDirection.DOWN, fluid, doFill);
			}
		}
		
		return 0;
	}
	
	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		return slotID == 1;
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			if(itemstack.getItem() instanceof IFluidContainerItem)
			{
				return true;
			}
			else if(FluidContainerRegistry.isFilledContainer(itemstack))
			{
				FluidStack stack = FluidContainerRegistry.getFluidForFilledItem(itemstack);
				
				if(fluidTank.getFluid() == null || fluidTank.getFluid().isFluidEqual(stack))
				{
					return editMode == ContainerEditMode.EMPTY || editMode == ContainerEditMode.BOTH;
				}
			}
			else if(FluidContainerRegistry.isEmptyContainer(itemstack))
			{
				return editMode == ContainerEditMode.FILL || editMode == ContainerEditMode.BOTH;
			}
		}

		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(side == 0)
		{
			return new int[] {1};
		}
		else if(side == 1)
		{
			return new int[] {0};
		}
		
		return InventoryUtils.EMPTY;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setInteger("tier", tier.ordinal());
		nbtTags.setBoolean("isActive", isActive);
		nbtTags.setInteger("editMode", editMode.ordinal());
		
		if(fluidTank.getFluid() != null)
		{
			nbtTags.setTag("fluidTank", fluidTank.writeToNBT(new NBTTagCompound()));
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		tier = FluidTankTier.values()[nbtTags.getInteger("tier")];
		clientActive = isActive = nbtTags.getBoolean("isActive");
		editMode = ContainerEditMode.values()[nbtTags.getInteger("editMode")];
		
		if(nbtTags.hasKey("fluidTank"))
		{
			fluidTank.setCapacity(tier.storage);
			fluidTank.readFromNBT(nbtTags.getCompoundTag("fluidTank"));
		}
	}
	
	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);

		if(worldObj.isRemote)
		{
			tier = FluidTankTier.values()[dataStream.readInt()];
			fluidTank.setCapacity(tier.storage);
			
			clientActive = dataStream.readBoolean();
			valve = dataStream.readInt();
			editMode = ContainerEditMode.values()[dataStream.readInt()];
			
			if(valve > 0)
			{
				valveFluid = FluidRegistry.getFluid(dataStream.readInt());
			}
			else {
				valveFluid = null;
			}
			
			if(dataStream.readInt() == 1)
			{
				fluidTank.setFluid(new FluidStack(FluidRegistry.getFluid(dataStream.readInt()), dataStream.readInt()));
			}
			else {
				fluidTank.setFluid(null);
			}
			
			if(updateDelay == 0 && clientActive != isActive)
			{
				updateDelay = general.UPDATE_DELAY;
				isActive = clientActive;
				MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
			}
		}
	}
	
	public int getRedstoneLevel()
	{
        double fractionFull = (float)fluidTank.getFluidAmount()/(float)fluidTank.getCapacity();
        return MathHelper.floor_float((float)(fractionFull * 14.0F)) + (fractionFull > 0 ? 1 : 0);
	}
	
	public int getCurrentNeeded()
	{
		int needed = fluidTank.getCapacity()-fluidTank.getFluidAmount();
		
		Coord4D top = Coord4D.get(this).getFromSide(ForgeDirection.UP);
		TileEntity topTile = top.getTileEntity(worldObj);
		
		if(topTile instanceof TileEntityFluidTank)
		{
			TileEntityFluidTank topTank = (TileEntityFluidTank)topTile;
			
			if(fluidTank.getFluid() != null && topTank.fluidTank.getFluid() != null)
			{
				if(fluidTank.getFluid().getFluid() != topTank.fluidTank.getFluid().getFluid())
				{
					return needed;
				}
			}
			
			needed += topTank.getCurrentNeeded();
		}
		
		return needed;
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(tier.ordinal());
		data.add(isActive);
		data.add(valve);
		data.add(editMode.ordinal());
		
		if(valve > 0)
		{
			data.add(valveFluid.getID());
		}
		
		if(fluidTank.getFluid() != null)
		{
			data.add(1);
			data.add(fluidTank.getFluid().getFluidID());
			data.add(fluidTank.getFluid().amount);
		}
		else {
			data.add(0);
		}
		
		return data;
	}
	
	@Override
	public void setActive(boolean active)
	{
		isActive = active;

		if(clientActive != active && updateDelay == 0)
		{
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));

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
	public boolean onSneakRightClick(EntityPlayer player, int side)
	{
		if(!worldObj.isRemote)
		{
			setActive(!getActive());
			worldObj.playSoundEffect(xCoord, yCoord, zCoord, "random.click", 0.3F, 1);
		}
		
		return true;
	}

	@Override
	public boolean onRightClick(EntityPlayer player, int side)
	{
		return false;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) 
	{
		if(resource != null && canFill(from, resource.getFluid()))
		{
			int filled = fluidTank.fill(resource, doFill);
			
			if(filled < resource.amount && !isActive)
			{
				filled += pushUp(PipeUtils.copy(resource, resource.amount-filled), doFill);
			}
			
			if(filled > 0 && from == ForgeDirection.UP)
			{
				if(valve == 0)
				{
					needsPacket = true;
				}
				
				valve = 20;
				valveFluid = resource.getFluid();
			}
			
			return filled;
		}
		
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) 
	{
		if(resource != null && canDrain(from, resource.getFluid()))
		{
			return fluidTank.drain(resource.amount, doDrain);
		}
		
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) 
	{
		if(canDrain(from, null))
		{
			return fluidTank.drain(maxDrain, doDrain);
		}
		
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) 
	{
		if(from == ForgeDirection.DOWN)
		{
			TileEntity tile = Coord4D.get(this).getFromSide(ForgeDirection.DOWN).getTileEntity(worldObj);
			
			if(isActive && !(tile instanceof TileEntityFluidTank))
			{
				return false;
			}
		}
		
		return fluidTank.getFluid() == null || fluidTank.getFluid().getFluid() == fluid;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		if(fluidTank != null)
		{
			if(fluid == null || fluidTank.getFluid() != null && fluidTank.getFluid().getFluid() == fluid)
			{
				if(isActive)
				{
					return from != ForgeDirection.DOWN;
				}
				
				return true;
			}
		}
		
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) 
	{
		return new FluidTankInfo[] {fluidTank.getInfo()};
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
	public ContainerEditMode getContainerEditMode() 
	{
		return editMode;
	}

	@Override
	public void setContainerEditMode(ContainerEditMode mode) 
	{
		editMode = mode;
	}
	
	@Override
	public Object[] getTanks() 
	{
		return new Object[] {fluidTank};
	}
	
	@Override
	public TileComponentSecurity getSecurity()
	{
		return securityComponent;
	}
}
