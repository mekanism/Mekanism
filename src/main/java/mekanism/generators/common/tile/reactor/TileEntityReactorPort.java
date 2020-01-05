package mekanism.generators.common.tile.reactor;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.EnumSet;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.IHeatTransfer;
import mekanism.api.Range4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mekanism.api.reactor.IReactorBlock;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.CableUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.item.ItemHohlraum;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

public class TileEntityReactorPort extends TileEntityReactorBlock implements IFluidHandler, IGasHandler, ITubeConnection, IHeatTransfer, IConfigurable
{
	public boolean fluidEject;
	
	public TileEntityReactorPort()
	{
		super("name", 1);
		
		inventory = new ItemStack[0];
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		fluidEject = nbtTags.getBoolean("fluidEject");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setBoolean("fluidEject", fluidEject);
	}

	@Override
	public boolean isFrame()
	{
		return false;
	}

	@Override
	public void onUpdate()
	{
		if(changed)
		{
			worldObj.func_147453_f(xCoord, yCoord, zCoord, getBlockType());
		}
		
		super.onUpdate();

		if(!worldObj.isRemote)
		{
			CableUtils.emit(this);
			
			if(fluidEject && getReactor() != null && getReactor().getSteamTank().getFluid() != null)
			{
				IFluidTank tank = getReactor().getSteamTank();
				
				for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
				{
					TileEntity tile = Coord4D.get(this).getFromSide(side).getTileEntity(worldObj);
					
					if(tile instanceof IFluidHandler && !(tile instanceof TileEntityReactorPort))
					{
						if(((IFluidHandler)tile).canFill(side.getOpposite(), tank.getFluid().getFluid()))
						{
							tank.drain(((IFluidHandler)tile).fill(side.getOpposite(), tank.getFluid(), true), true);
						}
					}
				}
			}
		}
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(resource.getFluid() == FluidRegistry.WATER && getReactor() != null && !fluidEject)
		{
			return getReactor().getWaterTank().fill(resource, doFill);
		}
		
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if(resource.getFluid() == FluidRegistry.getFluid("steam") && getReactor() != null)
		{
			getReactor().getSteamTank().drain(resource.amount, doDrain);
		}
		
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if(getReactor() != null)
		{
			return getReactor().getSteamTank().drain(maxDrain, doDrain);
		}
		
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return (getReactor() != null && fluid == FluidRegistry.WATER && !fluidEject);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return (getReactor() != null && fluid == FluidRegistry.getFluid("steam"));
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if(getReactor() == null)
		{
			return new FluidTankInfo[0];
		}
		
		return new FluidTankInfo[] {getReactor().getWaterTank().getInfo(), getReactor().getSteamTank().getInfo()};
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack, boolean doTransfer)
	{
		if(getReactor() != null)
		{
			if(stack.getGas() == GasRegistry.getGas("deuterium"))
			{
				return getReactor().getDeuteriumTank().receive(stack, doTransfer);
			}
			else if(stack.getGas() == GasRegistry.getGas("tritium"))
			{
				return getReactor().getTritiumTank().receive(stack, doTransfer);
			}
			else if(stack.getGas() == GasRegistry.getGas("fusionFuelDT"))
			{
				return getReactor().getFuelTank().receive(stack, doTransfer);
			}
		}
		
		return 0;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack)
	{
		return receiveGas(side, stack, true);
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount, boolean doTransfer)
	{
		if(getReactor() != null)
		{
			if(getReactor().getSteamTank().getFluidAmount() > 0)
			{
				return new GasStack(GasRegistry.getGas("steam"), getReactor().getSteamTank().drain(amount, doTransfer).amount);
			}
		}

		return null;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		return drawGas(side, amount, true);
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return (type == GasRegistry.getGas("deuterium") || type == GasRegistry.getGas("tritium") || type == GasRegistry.getGas("fusionFuelDT"));
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return (type == GasRegistry.getGas("steam"));
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return getReactor() != null;
	}

	@Override
	public boolean canOutputTo(ForgeDirection side)
	{
		return true;
	}

	@Override
	public double getEnergy()
	{
		if(getReactor() == null)
		{
			return 0;
		}
		else {
			return getReactor().getBufferedEnergy();
		}
	}

	@Override
	public void setEnergy(double energy)
	{
		if(getReactor() != null)
		{
			getReactor().setBufferedEnergy(energy);
		}
	}

	@Override
	public double getMaxEnergy()
	{
		if(getReactor() == null)
		{
			return 0;
		}
		else {
			return getReactor().getBufferSize();
		}
	}

	@Override
	public EnumSet<ForgeDirection> getOutputtingSides()
	{
		EnumSet set = EnumSet.allOf(ForgeDirection.class);
		set.remove(ForgeDirection.UNKNOWN);
		
		return set;
	}

	@Override
	public EnumSet<ForgeDirection> getConsumingSides()
	{
		return EnumSet.noneOf(ForgeDirection.class);
	}

	@Override
	public double getMaxOutput()
	{
		return 1000000000;
	}

	@Override
	public double getTemp()
	{
		if(getReactor() != null)
		{
			return getReactor().getTemp();
		}
		
		return 0;
	}

	@Override
	public double getInverseConductionCoefficient()
	{
		return 5;
	}

	@Override
	public double getInsulationCoefficient(ForgeDirection side)
	{
		if(getReactor() != null)
		{
			return getReactor().getInsulationCoefficient(side);
		}
		
		return 0;
	}

	@Override
	public void transferHeatTo(double heat)
	{
		if(getReactor() != null)
		{
			getReactor().transferHeatTo(heat);
		}
	}

	@Override
	public double[] simulateHeat()
	{
		return HeatUtils.simulate(this);
	}

	@Override
	public double applyTemperatureChange()
	{
		if(getReactor() != null)
		{
			return getReactor().applyTemperatureChange();
		}
		
		return 0;
	}

	@Override
	public boolean canConnectHeat(ForgeDirection side)
	{
		return getReactor() != null;
	}

	@Override
	public IHeatTransfer getAdjacent(ForgeDirection side)
	{
		TileEntity adj = Coord4D.get(this).getFromSide(side).getTileEntity(worldObj);
		
		if(adj instanceof IHeatTransfer && !(adj instanceof IReactorBlock))
		{
			return (IHeatTransfer)adj;
		}
		
		return null;
	}
	
	@Override
	public ItemStack getStackInSlot(int slotID)
	{
		return getReactor() != null && getReactor().isFormed() ? getReactor().getInventory()[slotID] : null;
	}
	
	@Override
	public int getSizeInventory()
	{
		return getReactor() != null && getReactor().isFormed() ? 1 : 0;
	}

	@Override
	public void setInventorySlotContents(int slotID, ItemStack itemstack)
	{
		if(getReactor() != null && getReactor().isFormed())
		{
			getReactor().getInventory()[slotID] = itemstack;

			if(itemstack != null && itemstack.stackSize > getInventoryStackLimit())
			{
				itemstack.stackSize = getInventoryStackLimit();
			}
		}
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return getReactor() != null && getReactor().isFormed() ? new int[] {0} : InventoryUtils.EMPTY;
	}
	
	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(getReactor() != null && getReactor().isFormed() && itemstack.getItem() instanceof ItemHohlraum)
		{
			ItemHohlraum hohlraum = (ItemHohlraum)itemstack.getItem();
			
			return hohlraum.getGas(itemstack) != null && hohlraum.getGas(itemstack).amount == hohlraum.getMaxGas(itemstack);
		}
		
		return false;
	}
	
	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(getReactor() != null && getReactor().isFormed() && itemstack.getItem() instanceof ItemHohlraum)
		{
			ItemHohlraum hohlraum = (ItemHohlraum)itemstack.getItem();
			
			return hohlraum.getGas(itemstack) == null;
		}
		
		return false;
	}
	
	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);
		
		if(worldObj.isRemote)
		{
			fluidEject = dataStream.readBoolean();
			
			MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(fluidEject);
		
		return data;
	}

	@Override
	public boolean onSneakRightClick(EntityPlayer player, int side)
	{
		if(!worldObj.isRemote)
		{
			fluidEject = !fluidEject;
			String modeText = " " + (fluidEject ? EnumColor.DARK_RED : EnumColor.DARK_GREEN) + LangUtils.transOutputInput(fluidEject) + ".";
			player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + LangUtils.localize("tooltip.configurator.reactorPortEject") + modeText));
			
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
			markDirty();
		}
		
		return true;
	}

	@Override
	public boolean onRightClick(EntityPlayer player, int side)
	{
		return false;
	}
}