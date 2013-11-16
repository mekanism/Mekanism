/**
 * 
 */
package mekanism.induction.common.tileentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.api.induction.ITesla;
import mekanism.common.ITileNetwork;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketTileEntity;
import mekanism.induction.common.MekanismInduction;
import mekanism.induction.common.TeslaGrid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.compatibility.TileEntityUniversalElectrical;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;

/**
 * The Tesla TileEntity.
 * 
 * - Redstone (Prevent Output Toggle) - Right click (Prevent Input Toggle)
 * 
 * @author Calclavia
 * 
 */
public class TileEntityTesla extends TileEntityUniversalElectrical implements ITesla, ITileNetwork
{
	public final static int DEFAULT_COLOR = 12;
	public final float TRANSFER_CAP = 10;
	private int dyeID = DEFAULT_COLOR;

	private boolean canReceive = true;
	private boolean attackEntities = true;

	/** Client side to do sparks */
	private boolean doTransfer = true;

	/** Prevents transfer loops */
	private final Set<TileEntityTesla> outputBlacklist = new HashSet<TileEntityTesla>();
	private final Set<TileEntityTesla> connectedTeslas = new HashSet<TileEntityTesla>();

	/**
	 * Caching
	 */
	private TileEntityTesla topCache = null;
	private TileEntityTesla controlCache = null;

	/**
	 * Quantum Tesla
	 */
	public Vector3 linked;
	public int linkDim;

	/**
	 * Client
	 */
	private int zapCounter = 0;
	private boolean isLinkedClient;

	@Override
	public void initiate()
	{
		super.initiate();
		TeslaGrid.instance().register(this);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		boolean doPacketUpdate = this.getEnergyStored() > 0;

		/**
		 * Only transfer if it is the bottom controlling Tesla tower.
		 */
		if (this.isController())
		{
			this.produce();

			if (this.ticks % (5 + this.worldObj.rand.nextInt(2)) == 0 && ((this.worldObj.isRemote && this.doTransfer) || (this.getEnergyStored() > 0 && !this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord))))
			{
				final TileEntityTesla topTesla = this.getTopTelsa();
				final Vector3 topTeslaVector = new Vector3(topTesla);

				/**
				 * Quantum transportation.
				 */
				if (this.linked != null || this.isLinkedClient)
				{
					if (!this.worldObj.isRemote)
					{
						World dimWorld = MinecraftServer.getServer().worldServerForDimension(this.linkDim);

						if (dimWorld != null)
						{
							TileEntity transferTile = this.linked.getTileEntity(dimWorld);

							if (transferTile instanceof TileEntityTesla && !transferTile.isInvalid())
							{
								this.transfer(((TileEntityTesla) transferTile), Math.min(this.getProvide(ForgeDirection.UNKNOWN), TRANSFER_CAP));

								if (this.zapCounter % 5 == 0 && MekanismInduction.SOUND_FXS)
								{
									this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, MekanismInduction.PREFIX + "electricshock", this.getEnergyStored() / 25, 1.3f - 0.5f * (this.dyeID / 16f));
								}
							}
						}
					}
					else
					{
						MekanismInduction.proxy.renderElectricShock(this.worldObj, topTeslaVector.clone().translate(0.5), topTeslaVector.clone().translate(new Vector3(0.5, Double.POSITIVE_INFINITY, 0.5)), false);
					}
				}
				else
				{

					List<ITesla> transferTeslaCoils = new ArrayList<ITesla>();

					for (ITesla tesla : TeslaGrid.instance().get())
					{
						if (new Vector3((TileEntity) tesla).distance(new Vector3(this)) < this.getRange())
						{
							/**
							 * Make sure Tesla is not part of this tower.
							 */
							if (!this.connectedTeslas.contains(tesla) && tesla.canReceive(this))
							{
								if (tesla instanceof TileEntityTesla)
								{
									if (((TileEntityTesla) tesla).getHeight() <= 1)
									{
										continue;
									}

									tesla = ((TileEntityTesla) tesla).getControllingTelsa();
								}

								transferTeslaCoils.add(tesla);
							}
						}
					}

					/**
					 * Sort by distance.
					 */
					Collections.sort(transferTeslaCoils, new Comparator()
					{
						public int compare(ITesla o1, ITesla o2)
						{
							double distance1 = new Vector3(topTesla).distance(new Vector3((TileEntity) o1));
							double distance2 = new Vector3(topTesla).distance(new Vector3((TileEntity) o2));

							if (distance1 < distance2)
							{
								return 1;
							}
							else if (distance1 > distance2)
							{
								return -1;
							}

							return 0;
						}

						@Override
						public int compare(Object obj, Object obj1)
						{
							return compare((ITesla) obj, (ITesla) obj1);
						}
					});

					if (transferTeslaCoils.size() > 0)
					{
						float transferEnergy = this.getEnergyStored() / transferTeslaCoils.size();
						int count = 0;
						boolean sentPacket = false;
						for (ITesla tesla : transferTeslaCoils)
						{
							if (this.zapCounter % 5 == 0 && MekanismInduction.SOUND_FXS)
							{
								this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, MekanismInduction.PREFIX + "electricshock", this.getEnergyStored() / 25, 1.3f - 0.5f * (this.dyeID / 16f));
							}

							Vector3 targetVector = new Vector3((TileEntity) tesla);

							if (tesla instanceof TileEntityTesla)
							{
								((TileEntityTesla) tesla).getControllingTelsa().outputBlacklist.add(this);
								targetVector = new Vector3(((TileEntityTesla) tesla).getTopTelsa());
							}

							double distance = topTeslaVector.distance(targetVector);
							MekanismInduction.proxy.renderElectricShock(this.worldObj, new Vector3(topTesla).translate(new Vector3(0.5)), targetVector.translate(new Vector3(0.5)), (float) MekanismInduction.DYE_COLORS[this.dyeID].x, (float) MekanismInduction.DYE_COLORS[this.dyeID].y, (float) MekanismInduction.DYE_COLORS[this.dyeID].z);

							this.transfer(tesla, Math.min(transferEnergy, TRANSFER_CAP));

							if (!sentPacket && transferEnergy > 0)
							{
								this.sendPacket(3);
							}

							if (this.attackEntities && this.zapCounter % 5 == 0)
							{
								MovingObjectPosition mop = topTeslaVector.clone().translate(0.5).rayTraceEntities(this.worldObj, targetVector.clone().translate(0.5));

								if (mop != null && mop.entityHit != null)
								{
									if (mop.entityHit instanceof EntityLivingBase)
									{
										mop.entityHit.attackEntityFrom(DamageSource.magic, 4);
										MekanismInduction.proxy.renderElectricShock(this.worldObj, new Vector3(topTesla).clone().translate(0.5), new Vector3(mop.entityHit));
									}
								}
							}

							if (count++ > 1)
							{
								break;
							}
						}
					}
				}

				this.zapCounter++;
				this.outputBlacklist.clear();

				this.doTransfer = false;
			}

			if (!this.worldObj.isRemote && this.getEnergyStored() > 0 != doPacketUpdate)
			{
				this.sendPacket(2);
			}
		}

		this.clearCache();
	}

	private void transfer(ITesla tesla, float transferEnergy)
	{
		if (transferEnergy > 0)
		{
			tesla.transfer(transferEnergy * (1 - (this.worldObj.rand.nextFloat() * 0.1f)), true);
			this.transfer(-transferEnergy, true);
		}
	}

	@Override
	public float receiveElectricity(ElectricityPack receive, boolean doReceive)
	{
		return super.receiveElectricity(receive, doReceive);
	}

	@Override
	public boolean canReceive(TileEntity tileEntity)
	{
		return this.canReceive && !this.outputBlacklist.contains(tileEntity) && this.getRequest(ForgeDirection.UNKNOWN) > 0;
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return this.isController();
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add((byte)1);
		data.add(getEnergyStored());
		data.add(dyeID);
		data.add(canReceive);
		data.add(attackEntities);
		data.add(linked != null);
		
		return data;
	}

	public ArrayList getEnergyPacket()
	{
		ArrayList data = new ArrayList();
		data.add((byte)2);
		data.add(getEnergyStored());
		
		return data;
	}

	/**
	 * Do Tesla Beam.
	 */
	public ArrayList getTeslaPacket()
	{
		ArrayList data = new ArrayList();
		data.add((byte)3);
		
		return data;
	}

	public void sendPacket(int id)
	{
		switch (id)
		{
			case 1:
				PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())));
				break;
			case 2:
				PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getEnergyPacket()));
				break;
			case 3:
				PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getTeslaPacket()));
				break;
		}
	}

	@Override
	public void handlePacketData(ByteArrayDataInput input)
	{
		switch (input.readByte())
		{
			case 1:
				this.setEnergyStored(input.readFloat());
				this.dyeID = input.readInt();
				this.canReceive = input.readBoolean();
				this.attackEntities = input.readBoolean();
				this.isLinkedClient = input.readBoolean();
				break;
			case 2:
				this.setEnergyStored(input.readFloat());
				break;
			case 3:
				this.doTransfer = true;
		}
	}

	private boolean isController()
	{
		return this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord) == 0;
	}

	private void clearCache()
	{
		this.topCache = null;
		this.controlCache = null;
	}

	@Override
	public float transfer(float transferEnergy, boolean doTransfer)
	{
		if (isController() || this.getControllingTelsa() == this)
		{
			if (doTransfer)
			{
				this.receiveElectricity(transferEnergy, true);
			}

			this.sendPacket(2);
			return transferEnergy;
		}
		else
		{
			if (this.getEnergyStored() > 0)
			{
				transferEnergy += this.getEnergyStored();
				this.setEnergyStored(0);
			}

			return this.getControllingTelsa().transfer(transferEnergy, doTransfer);
		}
	}

	public int getRange()
	{
		return Math.min(4 * (this.getHeight() - 1), 50);
	}

	public void updatePositionStatus()
	{
		boolean isTop = new Vector3(this).translate(new Vector3(0, 1, 0)).getTileEntity(this.worldObj) instanceof TileEntityTesla;
		boolean isBottom = new Vector3(this).translate(new Vector3(0, -1, 0)).getTileEntity(this.worldObj) instanceof TileEntityTesla;

		if (isTop && isBottom)
		{
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 1, 3);
		}
		else if (isBottom)
		{
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 2, 3);
		}
		else
		{
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 0, 3);
		}
	}

	/**
	 * Called only on bottom.
	 * 
	 * @return The highest Tesla coil in this tower.
	 */
	public TileEntityTesla getTopTelsa()
	{
		if (this.topCache != null)
		{
			return this.topCache;
		}

		this.connectedTeslas.clear();
		Vector3 checkPosition = new Vector3(this);
		TileEntityTesla returnTile = this;

		while (true)
		{
			TileEntity t = checkPosition.getTileEntity(this.worldObj);

			if (t instanceof TileEntityTesla)
			{
				this.connectedTeslas.add((TileEntityTesla) t);
				returnTile = (TileEntityTesla) t;
			}
			else
			{
				break;
			}

			checkPosition.y++;
		}

		this.topCache = returnTile;
		return returnTile;
	}

	/**
	 * For non-controlling Tesla to use.
	 * 
	 * @return
	 */
	public TileEntityTesla getControllingTelsa()
	{
		if (this.controlCache != null)
		{
			return this.controlCache;
		}

		Vector3 checkPosition = new Vector3(this);
		TileEntityTesla returnTile = this;

		while (true)
		{
			TileEntity t = checkPosition.getTileEntity(this.worldObj);

			if (t instanceof TileEntityTesla)
			{
				returnTile = (TileEntityTesla) t;
			}
			else
			{
				break;
			}

			checkPosition.y--;
		}

		this.controlCache = returnTile;
		return returnTile;
	}

	/**
	 * Called only on bottom.
	 * 
	 * @return The highest Tesla coil in this tower.
	 */
	public int getHeight()
	{
		this.connectedTeslas.clear();
		int y = 0;

		while (true)
		{
			TileEntity t = new Vector3(this).translate(new Vector3(0, y, 0)).getTileEntity(this.worldObj);

			if (t instanceof TileEntityTesla)
			{
				this.connectedTeslas.add((TileEntityTesla) t);
				y++;
			}
			else
			{
				break;
			}

		}

		return y;
	}

	@Override
	public void invalidate()
	{
		TeslaGrid.instance().unregister(this);
		super.invalidate();
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

	public void setDye(int id)
	{
		this.dyeID = id;
		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
	}

	public boolean toggleReceive()
	{
		return this.canReceive = !this.canReceive;
	}

	public boolean toggleEntityAttack()
	{
		boolean returnBool = this.attackEntities = !this.attackEntities;
		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		return returnBool;
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.dyeID = nbt.getInteger("dyeID");
		this.canReceive = nbt.getBoolean("canReceive");
		this.attackEntities = nbt.getBoolean("attackEntities");

		if (nbt.hasKey("link_x") && nbt.hasKey("link_y") && nbt.hasKey("link_z"))
		{
			this.linked = new Vector3(nbt.getInteger("link_x"), nbt.getInteger("link_y"), nbt.getInteger("link_z"));
			this.linkDim = nbt.getInteger("linkDim");
		}
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("dyeID", this.dyeID);
		nbt.setBoolean("canReceive", this.canReceive);
		nbt.setBoolean("attackEntities", this.attackEntities);

		if (this.linked != null)
		{
			nbt.setInteger("link_x", (int) this.linked.x);
			nbt.setInteger("link_y", (int) this.linked.y);
			nbt.setInteger("link_z", (int) this.linked.z);
			nbt.setInteger("linkDim", this.linkDim);
		}
	}

	public void setLink(Vector3 vector3, int dimID, boolean setOpponent)
	{
		if (!this.worldObj.isRemote)
		{
			World otherWorld = MinecraftServer.getServer().worldServerForDimension(this.linkDim);

			if (setOpponent && this.linked != null && otherWorld != null)
			{
				TileEntity tileEntity = this.linked.getTileEntity(otherWorld);

				if (tileEntity instanceof TileEntityTesla)
				{
					((TileEntityTesla) tileEntity).setLink(null, this.linkDim, false);
				}
			}

			this.linked = vector3;
			this.linkDim = dimID;
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);

			World newOtherWorld = MinecraftServer.getServer().worldServerForDimension(this.linkDim);

			if (setOpponent && newOtherWorld != null && this.linked != null)
			{
				TileEntity tileEntity = this.linked.getTileEntity(newOtherWorld);

				if (tileEntity instanceof TileEntityTesla)
				{
					((TileEntityTesla) tileEntity).setLink(new Vector3(this), this.worldObj.provider.dimensionId, false);
				}
			}
		}
	}

	@Override
	public float getRequest(ForgeDirection direction)
	{
		if (direction != ForgeDirection.DOWN)
		{
			return this.getMaxEnergyStored() - this.getEnergyStored();
		}

		return 0;
	}

	@Override
	public float getProvide(ForgeDirection direction)
	{
		if (this.isController() && direction == ForgeDirection.DOWN)
		{
			return this.getEnergyStored();
		}

		return 0;
	}

	@Override
	public float getMaxEnergyStored()
	{
		return TRANSFER_CAP;
	}

	@Override
	public EnumSet<ForgeDirection> getInputDirections()
	{
		EnumSet input = EnumSet.allOf(ForgeDirection.class);
		input.remove(ForgeDirection.DOWN);
		return input;
	}

	@Override
	public EnumSet<ForgeDirection> getOutputDirections()
	{
		return EnumSet.of(ForgeDirection.DOWN);
	}
}
