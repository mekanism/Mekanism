package mekanism.common.tile;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.client.gui.GuiProgress.ProgressBar;
import mekanism.client.sound.IHasSound;
import mekanism.common.IActiveState;
import mekanism.common.IInvConfiguration;
import mekanism.common.IEjector;
import mekanism.common.IElectricMachine;
import mekanism.common.IRedstoneControl;
import mekanism.common.IUpgradeTile;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.SideData;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import com.google.common.io.ByteArrayDataInput;

import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

public abstract class TileEntityBasicMachine extends TileEntityElectricBlock implements IElectricMachine, IPeripheral, IActiveState, IInvConfiguration, IUpgradeTile, IHasSound, IRedstoneControl
{
	/** This machine's side configuration. */
	public byte[] sideConfig;

	/** An arraylist of SideData for this machine. */
	public ArrayList<SideData> sideOutputs = new ArrayList<SideData>();

	/** The bundled URL of this machine's sound effect */
	public String soundURL;

	/** How much energy this machine uses per tick. */
	public double ENERGY_PER_TICK;

	/** How many ticks this machine has operated for. */
	public int operatingTicks = 0;

	/** Ticks required to operate -- or smelt an item. */
	public int TICKS_REQUIRED;

	/** How many ticks must pass until this block's active state can sync with the client. */
	public int updateDelay;

	/** Whether or not this block is in it's active state. */
	public boolean isActive;

	/** The client's current active state. */
	public boolean clientActive;

	/** The GUI texture path for this machine. */
	public ResourceLocation guiLocation;

	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType = RedstoneControl.DISABLED;

	/** This machine's previous amount of energy. */
	public double prevEnergy;

	public TileComponentUpgrade upgradeComponent;
	public TileComponentEjector ejectorComponent;

	/**
	 * The foundation of all machines - a simple tile entity with a facing, active state, initialized state, sound effect, and animated texture.
	 * @param soundPath - location of the sound effect
	 * @param name - full name of this machine
	 * @param location - GUI texture path of this machine
	 * @param perTick - the energy this machine consumes every tick in it's active state
	 * @param ticksRequired - how many ticks it takes to run a cycle
	 * @param maxEnergy - how much energy this machine can store
	 */
	public TileEntityBasicMachine(String soundPath, String name, ResourceLocation location, double perTick, int ticksRequired, double maxEnergy)
	{
		super(name, maxEnergy);
		ENERGY_PER_TICK = perTick;
		TICKS_REQUIRED = ticksRequired;
		soundURL = soundPath;
		guiLocation = location;
		isActive = false;
	}

	public abstract ProgressBar getProgressType();

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(worldObj.isRemote)
		{
			Mekanism.proxy.registerSound(this);

			if(updateDelay > 0)
			{
				updateDelay--;

				if(updateDelay == 0 && clientActive != isActive)
				{
					isActive = clientActive;
					MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
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
					Mekanism.packetPipeline.sendToAll(new PacketTileEntity(Coord4D.get(this), getNetworkedData(new ArrayList())));
				}
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		operatingTicks = nbtTags.getInteger("operatingTicks");
		clientActive = isActive = nbtTags.getBoolean("isActive");
		controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];

		if(nbtTags.hasKey("sideDataStored"))
		{
			for(int i = 0; i < 6; i++)
			{
				sideConfig[i] = nbtTags.getByte("config"+i);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setInteger("operatingTicks", operatingTicks);
		nbtTags.setBoolean("isActive", isActive);
		nbtTags.setInteger("controlType", controlType.ordinal());

		nbtTags.setBoolean("sideDataStored", true);

		for(int i = 0; i < 6; i++)
		{
			nbtTags.setByte("config"+i, sideConfig[i]);
		}
	}

	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);

		operatingTicks = dataStream.readInt();
		clientActive = dataStream.readBoolean();
		controlType = RedstoneControl.values()[dataStream.readInt()];

		for(int i = 0; i < 6; i++)
		{
			sideConfig[i] = dataStream.readByte();
		}

		if(updateDelay == 0 && clientActive != isActive)
		{
			updateDelay = Mekanism.UPDATE_DELAY;
			isActive = clientActive;
			MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(operatingTicks);
		data.add(isActive);
		data.add(controlType.ordinal());
		data.add(sideConfig);

		return data;
	}

	@Override
	public void invalidate()
	{
		super.invalidate();

		if(worldObj.isRemote)
		{
			Mekanism.proxy.unregisterSound(this);
		}
	}

	/**
	 * Gets the scaled progress level for the GUI.
	 * @param i - multiplier
	 * @return
	 */
	public int getScaledProgress(int i)
	{
		return operatingTicks*i / MekanismUtils.getTicks(getSpeedMultiplier(), TICKS_REQUIRED);
	}

	/**
	 * Gets the scaled progress level for the GUI.
	 * @return
	 */
	public double getScaledProgress()
	{
		return ((double)operatingTicks) / ((double)MekanismUtils.getTicks(getSpeedMultiplier(), TICKS_REQUIRED));
	}

	@Override
	public double getMaxEnergy()
	{
		return MekanismUtils.getMaxEnergy(getEnergyMultiplier(), MAX_ELECTRICITY);
	}

	@Override
	public boolean getActive()
	{
		return isActive;
	}

	@Override
	public void setActive(boolean active)
	{
		isActive = active;

		if(clientActive != active && updateDelay == 0)
		{
			Mekanism.packetPipeline.sendToAll(new PacketTileEntity(Coord4D.get(this), getNetworkedData(new ArrayList())));

			updateDelay = 10;
			clientActive = active;
		}
	}

	@Override
	public String getType()
	{
		return getInventoryName();
	}

	@Override
	public boolean equals(IPeripheral other)
	{
		return this == other;
	}

	@Override
	public boolean canSetFacing(int facing)
	{
		return facing != 0 && facing != 1;
	}

	@Override
	public void attach(IComputerAccess computer) {}

	@Override
	public void detach(IComputerAccess computer) {}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return sideOutputs.get(sideConfig[MekanismUtils.getBaseOrientation(side, facing)]).availableSlots;
	}

	@Override
	public ArrayList<SideData> getSideData()
	{
		return sideOutputs;
	}

	@Override
	public byte[] getConfiguration()
	{
		return sideConfig;
	}

	@Override
	public int getOrientation()
	{
		return facing;
	}

	@Override
	public int getEnergyMultiplier(Object... data)
	{
		return upgradeComponent.energyMultiplier;
	}

	@Override
	public void setEnergyMultiplier(int multiplier, Object... data)
	{
		upgradeComponent.energyMultiplier = multiplier;
		MekanismUtils.saveChunk(this);
	}

	@Override
	public int getSpeedMultiplier(Object... data)
	{
		return upgradeComponent.speedMultiplier;
	}

	@Override
	public void setSpeedMultiplier(int multiplier, Object... data)
	{
		upgradeComponent.speedMultiplier = multiplier;
		MekanismUtils.saveChunk(this);
	}

	@Override
	public boolean supportsUpgrades(Object... data)
	{
		return true;
	}

	@Override
	public String getSoundPath()
	{
		return soundURL;
	}

	@Override
	public float getVolumeMultiplier()
	{
		return 1;
	}

	@Override
	public boolean renderUpdate()
	{
		return true;
	}

	@Override
	public boolean lightUpdate()
	{
		return true;
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
	public TileComponentUpgrade getComponent()
	{
		return upgradeComponent;
	}

	@Override
	public IEjector getEjector()
	{
		return ejectorComponent;
	}
}
