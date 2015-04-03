package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.Mekanism;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityGasTank extends TileEntityContainerBlock implements IGasHandler, ITubeConnection, IRedstoneControl
{
	public enum GasMode
	{
		IDLE,
		DUMPING,
		DUMPING_EXCESS
	}

	/** The type of gas stored in this tank. */
	public GasTank gasTank = new GasTank(MAX_GAS);

	public static final int MAX_GAS = 96000;

	/** How fast this tank can output gas. */
	public int output = 256;

	public GasMode dumping;

	public int currentGasAmount;

	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType;

	public TileEntityGasTank()
	{
		super("GasTank");
		
		inventory = new ItemStack[2];
		dumping = GasMode.IDLE;
		controlType = RedstoneControl.DISABLED;
	}

	@Override
	public void onUpdate()
	{
		if(inventory[0] != null && gasTank.getGas() != null)
		{
			gasTank.draw(GasTransmission.addGas(inventory[0], gasTank.getGas()), true);
		}

		if(inventory[1] != null && (gasTank.getGas() == null || gasTank.getGas().amount < gasTank.getMaxGas()))
		{
			gasTank.receive(GasTransmission.removeGas(inventory[1], gasTank.getGasType(), gasTank.getNeeded()), true);
		}

		if(!worldObj.isRemote && gasTank.getGas() != null && MekanismUtils.canFunction(this) && dumping != GasMode.DUMPING)
		{
			GasStack toSend = new GasStack(gasTank.getGas().getGas(), Math.min(gasTank.getStored(), output));

			TileEntity tileEntity = Coord4D.get(this).getFromSide(ForgeDirection.getOrientation(facing)).getTileEntity(worldObj);

			if(tileEntity instanceof IGasHandler)
			{
				if(((IGasHandler)tileEntity).canReceiveGas(ForgeDirection.getOrientation(facing).getOpposite(), gasTank.getGas().getGas()))
				{
					gasTank.draw(((IGasHandler)tileEntity).receiveGas(ForgeDirection.getOrientation(facing).getOpposite(), toSend, true), true);
				}
			}
		}

		if(!worldObj.isRemote && dumping == GasMode.DUMPING)
		{
			gasTank.draw(8, true);
		}

		if(!worldObj.isRemote && dumping == GasMode.DUMPING_EXCESS && gasTank.getNeeded() < output)
		{
			gasTank.draw(output-gasTank.getNeeded(), true);
		}
		
		if(!worldObj.isRemote)
		{
			int newGasAmount = gasTank.getStored();
			
			if(newGasAmount != currentGasAmount)
			{
				markDirty();
				currentGasAmount = newGasAmount;
			}
		}
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 1)
		{
			return (itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).getGas(itemstack) == null);
		}
		else if(slotID == 0)
		{
			return (itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).getGas(itemstack) != null &&
			    ((IGasItem)itemstack.getItem()).getGas(itemstack).amount == ((IGasItem)itemstack.getItem()).getMaxGas(itemstack));
		}

		return false;
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return itemstack.getItem() instanceof IGasItem && (gasTank.getGas() == null || ((IGasItem)itemstack.getItem()).canReceiveGas(itemstack, gasTank.getGas().getGas()));
		}
		else if(slotID == 1)
		{
			return itemstack.getItem() instanceof IGasItem && (gasTank.getGas() == null || ((IGasItem)itemstack.getItem()).canProvideGas(itemstack, gasTank.getGas().getGas()));
		}

		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return side == 1 ? new int[]{0} : new int[]{1};
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack, boolean doTransfer)
	{
		return gasTank.receive(stack, doTransfer);
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack)
	{
		return receiveGas(side, stack, true);
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount, boolean doTransfer)
	{
		return null;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		return drawGas(side, amount, true);
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return gasTank.canDraw(type);
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		if(side != ForgeDirection.getOrientation(facing))
		{
			return gasTank.canReceive(type);
		}

		return false;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(!worldObj.isRemote)
		{
			int type = dataStream.readInt();

			if(type == 0)
			{
				int index = (dumping.ordinal() + 1)%dumping.values().length;
				dumping = GasMode.values()[index];
			}

			for(EntityPlayer player : playersUsing)
			{
				Mekanism.packetHandler.sendTo(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), (EntityPlayerMP)player);
			}

			return;
		}

		super.handlePacketData(dataStream);

		if(dataStream.readBoolean())
		{
			gasTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			gasTank.setGas(null);
		}

		dumping = GasMode.values()[dataStream.readInt()];
		controlType = RedstoneControl.values()[dataStream.readInt()];

		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		gasTank.read(nbtTags.getCompoundTag("gasTank"));
		dumping = GasMode.values()[nbtTags.getInteger("dumping")];
		
		controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setTag("gasTank", gasTank.write(new NBTTagCompound()));
		nbtTags.setInteger("dumping", dumping.ordinal());
		nbtTags.setInteger("controlType", controlType.ordinal());
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		if(gasTank.getGas() != null)
		{
			data.add(true);
			data.add(gasTank.getGas().getGas().getID());
			data.add(gasTank.getStored());
		}
		else {
			data.add(false);
		}

		data.add(dumping.ordinal());
		data.add(controlType.ordinal());

		return data;
	}

	@Override
	public boolean canSetFacing(int side)
	{
		return side != 0 && side != 1;
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return true;
	}

	public int getRedstoneLevel()
	{
		int stored = gasTank.getStored();
		
		if(stored == 0) 
		{
			return 0;
		}
		
		return MathHelper.floor_float((float)stored / (float)MAX_GAS * 14.0f + 1.0f);
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
	}

	@Override
	public boolean canPulse()
	{
		return false;
	}
}
