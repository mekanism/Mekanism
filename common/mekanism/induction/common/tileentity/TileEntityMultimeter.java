/**
 * 
 */
package mekanism.induction.common.tileentity;

import ic2.api.tile.IEnergyStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.common.ITileNetwork;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketTileEntity;
import mekanism.induction.common.MekanismInduction;
import mekanism.induction.common.MultimeterEventHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.block.IConductor;
import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.IElectricalStorage;
import universalelectricity.core.grid.IElectricityNetwork;
import universalelectricity.prefab.tile.IRotatable;
import universalelectricity.prefab.tile.TileEntityAdvanced;
import universalelectricity.prefab.tile.TileEntityElectrical;
import buildcraft.api.power.IPowerReceptor;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * Block that detects power.
 * 
 * @author Calclavia
 * 
 */
public class TileEntityMultimeter extends TileEntityAdvanced implements ITileNetwork, IConnector, IRotatable
{
	public Set<EntityPlayer> playersUsing = new HashSet<EntityPlayer>();

	public enum DetectMode
	{
		NONE("None"), LESS_THAN("Less Than"), LESS_THAN_EQUAL("Less Than or Equal"),
		EQUAL("Equal"), GREATER_THAN("Greater Than or Equal"), GREATER_THAN_EQUAL("Greater Than");

		public String display;

		private DetectMode(String display)
		{
			this.display = display;
		}
	}

	private DetectMode detectMode = DetectMode.NONE;
	private float peakDetection;
	private float energyLimit;
	private float detectedEnergy;
	private float detectedAverageEnergy;
	public boolean redstoneOn;

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{
			if (this.ticks % 20 == 0)
			{
				float prevDetectedEnergy = this.detectedEnergy;
				this.updateDetection(this.doGetDetectedEnergy());

				boolean outputRedstone = false;

				switch (detectMode)
				{
					default:
						break;
					case EQUAL:
						outputRedstone = this.detectedEnergy == this.energyLimit;
						break;
					case GREATER_THAN:
						outputRedstone = this.detectedEnergy > this.energyLimit;
						break;
					case GREATER_THAN_EQUAL:
						outputRedstone = this.detectedEnergy >= this.energyLimit;
						break;
					case LESS_THAN:
						outputRedstone = this.detectedEnergy < this.energyLimit;
						break;
					case LESS_THAN_EQUAL:
						outputRedstone = this.detectedEnergy <= this.energyLimit;
						break;
				}

				if (outputRedstone != this.redstoneOn)
				{
					this.redstoneOn = outputRedstone;
					this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, MekanismInduction.Multimeter.blockID);
				}

				if (prevDetectedEnergy != this.detectedEnergy)
				{
					this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
				}
			}
		}

		if (!this.worldObj.isRemote)
		{
			for (EntityPlayer player : this.playersUsing)
			{
				PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())), player);
			}
		}
	}

	@Override
	public void handlePacketData(ByteArrayDataInput input)
	{
		switch (input.readByte())
		{
			default:
				this.detectMode = DetectMode.values()[input.readByte()];
				this.detectedEnergy = input.readFloat();
				this.energyLimit = input.readFloat();
				break;
			case 2:
				this.toggleMode();
				break;
			case 3:
				this.energyLimit = input.readFloat();
				break;
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add((byte)1);
		data.add((byte)detectMode.ordinal());
		data.add(detectedEnergy);
		data.add(energyLimit);
		
		return data;
	}
	
	@Override
	public void validate()
	{
		super.validate();
		
		if(worldObj.isRemote)
		{
			PacketHandler.sendPacket(Transmission.SERVER, new PacketDataRequest().setParams(Object3D.get(this)));
		}
	}

	public float doGetDetectedEnergy()
	{
		ForgeDirection direction = this.getDirection();
		TileEntity tileEntity = this.worldObj.getBlockTileEntity(this.xCoord + direction.offsetX, this.yCoord + direction.offsetY, this.zCoord + direction.offsetZ);
		return getDetectedEnergy(direction.getOpposite(), tileEntity);
	}

	public static float getDetectedEnergy(ForgeDirection side, TileEntity tileEntity)
	{
		if (tileEntity instanceof TileEntityElectrical)
		{
			return ((TileEntityElectrical) tileEntity).getEnergyStored();
		}
		else if (tileEntity instanceof IElectricalStorage)
		{
			return ((IElectricalStorage) tileEntity).getEnergyStored();
		}
		else if (tileEntity instanceof IConductor)
		{
			IElectricityNetwork network = ((IConductor) tileEntity).getNetwork();

			if (MultimeterEventHandler.getCache(tileEntity.worldObj).containsKey(network) && MultimeterEventHandler.getCache(tileEntity.worldObj).get(network) instanceof Float)
			{
				return MultimeterEventHandler.getCache(tileEntity.worldObj).get(network);
			}
		}
		else if (tileEntity instanceof IEnergyStorage)
		{
			return ((IEnergyStorage) tileEntity).getStored();
		}
		else if (tileEntity instanceof IEnergyStorage)
		{
			return ((IEnergyStorage) tileEntity).getStored();
		}
		else if (tileEntity instanceof IPowerReceptor)
		{
			if (((IPowerReceptor) tileEntity).getPowerReceiver(side) != null)
			{
				return ((IPowerReceptor) tileEntity).getPowerReceiver(side).getEnergyStored();
			}
		}

		return 0;
	}

	public void updateDetection(float detected)
	{
		this.detectedEnergy = detected;
		this.detectedAverageEnergy = (detectedAverageEnergy + this.detectedEnergy) / 2;
		this.peakDetection = Math.max(peakDetection, this.detectedEnergy);
	}

	public float getDetectedEnergy()
	{
		return this.detectedEnergy;
	}

	public float getAverageDetectedEnergy()
	{
		return this.detectedAverageEnergy;
	}

	public void toggleMode()
	{
		this.detectMode = DetectMode.values()[(this.detectMode.ordinal() + 1) % DetectMode.values().length];
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.detectMode = DetectMode.values()[nbt.getInteger("detectMode")];
		this.energyLimit = nbt.getFloat("energyLimit");
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("detectMode", this.detectMode.ordinal());
		nbt.setFloat("energyLimit", this.energyLimit);
	}

	public DetectMode getMode()
	{
		return this.detectMode;
	}

	public float getLimit()
	{
		return this.energyLimit;
	}

	/**
	 * @return
	 */
	public float getPeak()
	{
		return this.peakDetection;
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return direction == this.getDirection();
	}

	@Override
	public ForgeDirection getDirection()
	{
		return ForgeDirection.getOrientation(this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord));
	}

	@Override
	public void setDirection(ForgeDirection direction)
	{
		this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, direction.ordinal(), 3);
	}

}
