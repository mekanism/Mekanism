package mekanism.induction.common.tileentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.induction.ITesla;
import mekanism.common.Object3D;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tileentity.TileEntityElectricBlock;
import mekanism.common.util.CableUtils;
import mekanism.induction.common.MekanismInduction;
import mekanism.induction.common.TeslaGrid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

/**
 * The Tesla TileEntity.
 * 
 * - Redstone (Prevent Output Toggle) - Right click (Prevent Input Toggle)
 * 
 * @author Calclavia
 * 
 */
public class TileEntityTesla extends TileEntityElectricBlock implements ITesla
{
	public static final int DEFAULT_COLOR = 12;
	public static final double TRANSFER_CAP = 10000;
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
	
	public TileEntityTesla()
	{
		super("Telsa", TRANSFER_CAP);
		inventory = new ItemStack[0];
	}
	
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		
		if(ticker == 1)
		{
			TeslaGrid.instance().register(this);
		}
		
		boolean doPacketUpdate = getEnergy() > 0;
		
		//Only transfer if it is the bottom controlling Tesla tower.
		if(isController())
		{
			CableUtils.emit(this);
			
			if(ticker % (5 + worldObj.rand.nextInt(2)) == 0 && ((worldObj.isRemote && doTransfer) || (getEnergy() > 0 && !worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))))
			{
				final TileEntityTesla topTesla = getTopTelsa();
				final Vector3 topTeslaVector = new Vector3(topTesla);
				
				//Quantum transportation.
				if(linked != null || isLinkedClient)
				{
					if(!worldObj.isRemote)
					{
						World dimWorld = MinecraftServer.getServer().worldServerForDimension(linkDim);
						
						if(dimWorld != null)
						{
							TileEntity transferTile = linked.getTileEntity(dimWorld);
							
							if(transferTile instanceof TileEntityTesla && !transferTile.isInvalid())
							{
								transfer(((TileEntityTesla)transferTile), getMaxEnergy()-getEnergy());
								
								if(zapCounter % 5 == 0)
								{
									worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, "mekanism:etc.Shock", (float)getEnergy() / 25000, 1.3f - 0.5f * (dyeID / 16f));
								}
							}
						}
					}
					else {
						MekanismInduction.proxy.renderElectricShock(worldObj, topTeslaVector.clone().translate(0.5), topTeslaVector.clone().translate(new Vector3(0.5, Double.POSITIVE_INFINITY, 0.5)), false);
					}
				}
				else {
					List<ITesla> transferTeslaCoils = new ArrayList<ITesla>();
					
					for(ITesla tesla : TeslaGrid.instance().get())
					{
						if(new Vector3((TileEntity)tesla).distance(new Vector3(this)) < getRange())
						{
							
							//Make sure Tesla is not part of this tower.
							if(!connectedTeslas.contains(tesla) && tesla.canReceive(this))
							{
								if(tesla instanceof TileEntityTesla)
								{
									if(((TileEntityTesla)tesla).getHeight() <= 1)
									{
										continue;
									}
									
									tesla = ((TileEntityTesla)tesla).getControllingTelsa();
								}
								
								transferTeslaCoils.add(tesla);
							}
						}
					}
					
					//Sort by distance.
					Collections.sort(transferTeslaCoils, new Comparator() {
						public int compare(ITesla o1, ITesla o2)
						{
							double distance1 = new Vector3(topTesla).distance(new Vector3((TileEntity)o1));
							double distance2 = new Vector3(topTesla).distance(new Vector3((TileEntity)o2));
							
							if(distance1 < distance2)
							{
								return 1;
							}
							else if(distance1 > distance2)
							{
								return -1;
							}
							
							return 0;
						}
						
						@Override
						public int compare(Object obj, Object obj1)
						{
							return compare((ITesla)obj, (ITesla)obj1);
						}
					});
					
					if(transferTeslaCoils.size() > 0)
					{
						double transferEnergy = getEnergy() / transferTeslaCoils.size();
						int count = 0;
						boolean sentPacket = false;
						
						for(ITesla tesla : transferTeslaCoils)
						{
							if(zapCounter % 5 == 0)
							{
								worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, "mekanism:etc.Shock", (float)getEnergy() / 25000, 1.3f - 0.5f * (dyeID / 16f));
							}
							
							Vector3 targetVector = new Vector3((TileEntity)tesla);
							
							if(tesla instanceof TileEntityTesla)
							{
								((TileEntityTesla)tesla).getControllingTelsa().outputBlacklist.add(this);
								targetVector = new Vector3(((TileEntityTesla)tesla).getTopTelsa());
							}
							
							double distance = topTeslaVector.distance(targetVector);
							
							MekanismInduction.proxy.renderElectricShock(worldObj, new Vector3(topTesla).translate(new Vector3(0.5)), targetVector.translate(new Vector3(0.5)), (float)MekanismInduction.DYE_COLORS[dyeID].x, (float)MekanismInduction.DYE_COLORS[dyeID].y,
									(float)MekanismInduction.DYE_COLORS[dyeID].z);
							
							transfer(tesla, Math.min(transferEnergy, TRANSFER_CAP));
							
							if(!sentPacket && transferEnergy > 0)
							{
								sendPacket(2);
							}
							
							if(attackEntities && zapCounter % 5 == 0)
							{
								MovingObjectPosition mop = topTeslaVector.clone().translate(0.5).rayTraceEntities(worldObj, targetVector.clone().translate(0.5));
								
								if(mop != null && mop.entityHit != null)
								{
									if(mop.entityHit instanceof EntityLivingBase)
									{
										mop.entityHit.attackEntityFrom(DamageSource.magic, 4);
										MekanismInduction.proxy.renderElectricShock(worldObj, new Vector3(topTesla).clone().translate(0.5), new Vector3(mop.entityHit));
									}
								}
							}
							
							if(count++ > 1)
							{
								break;
							}
						}
					}
				}
				
				zapCounter++;
				outputBlacklist.clear();
				
				doTransfer = false;
			}
			
			if(!worldObj.isRemote && getEnergy() > 0 != doPacketUpdate)
			{
				sendPacket(1);
			}
		}
		
		clearCache();
	}
	
	private void transfer(ITesla tesla, double transferEnergy)
	{
		transfer(-tesla.transfer(transferEnergy, true), true);
	}
	
	@Override
	public boolean canReceive(TileEntity tileEntity)
	{
		return canReceive && !outputBlacklist.contains(tileEntity) && getRequest(ForgeDirection.UNKNOWN) > 0;
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add((byte)1);
		data.add(dyeID);
		data.add(canReceive);
		data.add(attackEntities);
		data.add(linked != null);
		
		return data;
	}
	
	/**
	 * Do Tesla Beam.
	 */
	public ArrayList getTeslaPacket(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add((byte)2);
		
		return data;
	}
	
	public void sendPacket(int id)
	{
		switch(id)
		{
			case 1:
				PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())));
				break;
			case 2:
				PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getTeslaPacket(new ArrayList())));
				break;
		}
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		
		switch(dataStream.readByte())
		{
			case 1:
				dyeID = dataStream.readInt();
				canReceive = dataStream.readBoolean();
				attackEntities = dataStream.readBoolean();
				isLinkedClient = dataStream.readBoolean();
				break;
			case 2:
				doTransfer = true;
				break;
		}
	}
	
	private boolean isController()
	{
		return worldObj.getBlockMetadata(xCoord, yCoord, zCoord) == 0;
	}
	
	private void clearCache()
	{
		topCache = null;
		controlCache = null;
	}
	
	@Override
	public double transfer(double transferEnergy, boolean doTransfer)
	{
		if(isController() || getControllingTelsa() == this)
		{
			transferEnergy = Math.min(transferEnergy, getMaxEnergy()-getEnergy());
			
			if(doTransfer)
			{
				setEnergy(getEnergy() + transferEnergy);
			}
			
			sendPacket(1);
			return transferEnergy;
		}
		else {
			if(getEnergy() > 0)
			{
				transferEnergy += getEnergy();
				setEnergy(0);
			}
			
			return getControllingTelsa().transfer(transferEnergy, doTransfer);
		}
	}
	
	public int getRange()
	{
		return Math.min(4 * (getHeight() - 1), 50);
	}
	
	public void updatePositionStatus()
	{
		boolean isTop = new Vector3(this).translate(new Vector3(0, 1, 0)).getTileEntity(worldObj) instanceof TileEntityTesla;
		boolean isBottom = new Vector3(this).translate(new Vector3(0, -1, 0)).getTileEntity(worldObj) instanceof TileEntityTesla;
		
		if(isTop && isBottom)
		{
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 3);
		}
		else if(isBottom)
		{
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 2, 3);
		}
		else {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 3);
		}
	}
	
	/**
	 * Called only on bottom.
	 * 
	 * @return The highest Tesla coil in this tower.
	 */
	public TileEntityTesla getTopTelsa()
	{
		if(topCache != null)
		{
			return topCache;
		}
		
		connectedTeslas.clear();
		Vector3 checkPosition = new Vector3(this);
		TileEntityTesla returnTile = this;
		
		while(true)
		{
			TileEntity t = checkPosition.getTileEntity(worldObj);
			
			if(t instanceof TileEntityTesla)
			{
				connectedTeslas.add((TileEntityTesla)t);
				returnTile = (TileEntityTesla)t;
			}
			else {
				break;
			}
			
			checkPosition.y++;
		}
		
		topCache = returnTile;
		return returnTile;
	}
	
	/**
	 * For non-controlling Tesla to use.
	 * 
	 * @return
	 */
	public TileEntityTesla getControllingTelsa()
	{
		if(controlCache != null)
		{
			return controlCache;
		}
		
		Vector3 checkPosition = new Vector3(this);
		TileEntityTesla returnTile = this;
		
		while(true)
		{
			TileEntity t = checkPosition.getTileEntity(worldObj);
			
			if(t instanceof TileEntityTesla)
			{
				returnTile = (TileEntityTesla)t;
			}
			else {
				break;
			}
			
			checkPosition.y--;
		}
		
		controlCache = returnTile;
		return returnTile;
	}
	
	/**
	 * Called only on bottom.
	 * 
	 * @return The highest Tesla coil in this tower.
	 */
	public int getHeight()
	{
		connectedTeslas.clear();
		int y = 0;
		
		while(true)
		{
			TileEntity t = new Vector3(this).translate(new Vector3(0, y, 0)).getTileEntity(worldObj);
			
			if(t instanceof TileEntityTesla)
			{
				connectedTeslas.add((TileEntityTesla)t);
				y++;
			}
			else {
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
		dyeID = id;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	public boolean toggleReceive()
	{
		return canReceive = !canReceive;
	}
	
	public boolean toggleEntityAttack()
	{
		boolean returnBool = attackEntities = !attackEntities;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		return returnBool;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		dyeID = nbt.getInteger("dyeID");
		canReceive = nbt.getBoolean("canReceive");
		attackEntities = nbt.getBoolean("attackEntities");
		
		if(nbt.hasKey("link_x") && nbt.hasKey("link_y") && nbt.hasKey("link_z"))
		{
			linked = new Vector3(nbt.getInteger("link_x"), nbt.getInteger("link_y"), nbt.getInteger("link_z"));
			linkDim = nbt.getInteger("linkDim");
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("dyeID", dyeID);
		nbt.setBoolean("canReceive", canReceive);
		nbt.setBoolean("attackEntities", attackEntities);
		
		if(linked != null)
		{
			nbt.setInteger("link_x", (int)linked.x);
			nbt.setInteger("link_y", (int)linked.y);
			nbt.setInteger("link_z", (int)linked.z);
			nbt.setInteger("linkDim", linkDim);
		}
	}
	
	public void setLink(Vector3 vector3, int dimID, boolean setOpponent)
	{
		if(!worldObj.isRemote)
		{
			World otherWorld = MinecraftServer.getServer().worldServerForDimension(linkDim);
			
			if(setOpponent && linked != null && otherWorld != null)
			{
				TileEntity tileEntity = linked.getTileEntity(otherWorld);
				
				if(tileEntity instanceof TileEntityTesla)
				{
					((TileEntityTesla)tileEntity).setLink(null, linkDim, false);
				}
			}
			
			linked = vector3;
			linkDim = dimID;
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			
			World newOtherWorld = MinecraftServer.getServer().worldServerForDimension(linkDim);
			
			if(setOpponent && newOtherWorld != null && linked != null)
			{
				TileEntity tileEntity = linked.getTileEntity(newOtherWorld);
				
				if(tileEntity instanceof TileEntityTesla)
				{
					((TileEntityTesla)tileEntity).setLink(new Vector3(this), worldObj.provider.dimensionId, false);
				}
			}
		}
	}
	
	@Override
	public double getMaxOutput()
	{
		return TRANSFER_CAP;
	}
	
	@Override
	public EnumSet<ForgeDirection> getOutputtingSides()
	{
		return EnumSet.of(ForgeDirection.DOWN);
	}
}