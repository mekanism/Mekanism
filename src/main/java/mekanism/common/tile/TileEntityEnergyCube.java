package mekanism.common.tile;

import java.util.ArrayList;
import java.util.EnumSet;

import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.CableUtils;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.Method;

import io.netty.buffer.ByteBuf;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")
public class TileEntityEnergyCube extends TileEntityElectricBlock implements IPeripheral, IRedstoneControl
{
	/** This Energy Cube's tier. */
	public EnergyCubeTier tier = EnergyCubeTier.BASIC;

	/** The redstone level this Energy Cube is outputting at. */
	public int currentRedstoneLevel;

	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType;

	public int prevScale;

	/**
	 * A block used to store and transfer electricity.
	 */
	public TileEntityEnergyCube()
	{
		super("EnergyCube", 0);

		inventory = new ItemStack[2];
		controlType = RedstoneControl.DISABLED;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(!worldObj.isRemote)
		{
			ChargeUtils.charge(0, this);
			ChargeUtils.discharge(1, this);
	
			if(MekanismUtils.canFunction(this))
			{
				CableUtils.emit(this);
			}
			
			int newScale = getScaledEnergyLevel(20);
	
			if(newScale != prevScale)
			{
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
			}
	
			prevScale = newScale;
		}
	}

	@Override
	public String getInventoryName()
	{
		return MekanismUtils.localize("tile.EnergyCube" + tier.getBaseTier().getName() + ".name");
	}

	@Override
	public double getMaxOutput()
	{
		return tier.output;
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return ChargeUtils.canBeCharged(itemstack);
		}
		else if(slotID == 1)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}

		return true;
	}

	@Override
	public EnumSet<ForgeDirection> getConsumingSides()
	{
		EnumSet set = EnumSet.allOf(ForgeDirection.class);
		set.removeAll(getOutputtingSides());
		set.remove(ForgeDirection.UNKNOWN);

		return set;
	}

	@Override
	public EnumSet<ForgeDirection> getOutputtingSides()
	{
		return EnumSet.of(ForgeDirection.getOrientation(facing));
	}

	@Override
	public boolean canSetFacing(int side)
	{
		return true;
	}

	@Override
	public double getMaxEnergy()
	{
		return tier.maxEnergy;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return side <= 1 ? new int[] {0} : new int[] {1};
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 1)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}
		else if(slotID == 0)
		{
			return ChargeUtils.canBeOutputted(itemstack, true);
		}

		return false;
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
		return new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded"};
	}

	@Override
	@Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException
	{
		switch(method)
		{
			case 0:
				return new Object[] {getEnergy()};
			case 1:
				return new Object[] {tier.output};
			case 2:
				return new Object[] {getMaxEnergy()};
			case 3:
				return new Object[] {(getMaxEnergy()-getEnergy())};
			default:
				Mekanism.logger.error("Attempted to call unknown method with computer ID " + computer.getID());
				return null;
		}
	}

	@Override
	@Method(modid = "ComputerCraft")
	public boolean equals(IPeripheral other)
	{
		return this == other;
	}

	@Override
	@Method(modid = "ComputerCraft")
	public void attach(IComputerAccess computer) {}

	@Override
	@Method(modid = "ComputerCraft")
	public void detach(IComputerAccess computer) {}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		tier = EnergyCubeTier.getFromName(PacketHandler.readString(dataStream));

		super.handlePacketData(dataStream);

		controlType = RedstoneControl.values()[dataStream.readInt()];

		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(tier.getBaseTier().getName());

		super.getNetworkedData(data);

		data.add(controlType.ordinal());

		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		tier = EnergyCubeTier.getFromName(nbtTags.getString("tier"));
		controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setString("tier", tier.getBaseTier().getName());
		nbtTags.setInteger("controlType", controlType.ordinal());
	}

	@Override
	public void setEnergy(double energy)
	{
		if(tier == EnergyCubeTier.CREATIVE && energy != Integer.MAX_VALUE)
		{
			return;
		}
		
		super.setEnergy(energy);

		int newRedstoneLevel = getRedstoneLevel();

		if(newRedstoneLevel != currentRedstoneLevel)
		{
			markDirty();
			currentRedstoneLevel = newRedstoneLevel;
		}
	}

	public int getRedstoneLevel()
	{
		double fractionFull = getEnergy()/getMaxEnergy();
		return MathHelper.floor_float((float)(fractionFull * 14.0F)) + (fractionFull > 0 ? 1 : 0);
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
