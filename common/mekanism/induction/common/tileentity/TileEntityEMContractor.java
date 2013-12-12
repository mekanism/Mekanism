package mekanism.induction.common.tileentity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.tileentity.TileEntityBasicBlock;
import mekanism.common.util.InventoryUtils;
import mekanism.induction.common.MekanismInduction;
import mekanism.induction.common.PathfinderEMContractor;
import mekanism.induction.common.ThreadEMPathfinding;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockVine;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.IFluidBlock;
import universalelectricity.core.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityEMContractor extends TileEntityBasicBlock
{
	public static int MAX_REACH = 40;
	public static int PUSH_DELAY = 5;
	public static double MAX_SPEED = .2;
	public static double ACCELERATION = .02;

	private int pushDelay;

	private AxisAlignedBB operationBounds;
	private AxisAlignedBB suckBounds;

	/**
	 * true = suck, false = push
	 */
	public boolean suck = true;

	private ThreadEMPathfinding thread;
	private PathfinderEMContractor pathfinder;
	private Set<EntityItem> pathfindingTrackers = new HashSet<EntityItem>();
	private TileEntityEMContractor linked;
	private int lastCalcTime = 0;

	/** Color of beam */
	private int dyeID = TileEntityTesla.DEFAULT_COLOR;
	private Object3D tempLinkVector;

	@Override
	public void onUpdate()
	{
		if(ticker == 1)
		{
			updateBounds();
		}

		pushDelay = Math.max(0, pushDelay - 1);

		if(tempLinkVector != null)
		{
			if(tempLinkVector.getTileEntity(worldObj) instanceof TileEntityEMContractor)
			{
				setLink((TileEntityEMContractor)tempLinkVector.getTileEntity(worldObj), true);
			}

			tempLinkVector = null;
		}

		if(canFunction())
		{
			TileEntity inventoryTile = getLatched();
			IInventory inventory = (IInventory)inventoryTile;

			if(!suck && pushDelay == 0)
			{
				ItemStack retrieved = InventoryUtils.takeTopItemFromInventory(inventory, ForgeDirection.OPPOSITES[getFacing()]);

				if(retrieved != null)
				{
					EntityItem item = getItemWithPosition(retrieved);

					if(!worldObj.isRemote)
					{
						worldObj.spawnEntityInWorld(item);
					}

					pushDelay = PUSH_DELAY;
				}
			}
			else if(suck)
			{
				if(suckBounds != null)
				{
					if(!worldObj.isRemote)
					{
						for(EntityItem item : (List<EntityItem>)worldObj.getEntitiesWithinAABB(EntityItem.class, suckBounds))
						{
							ItemStack remains = InventoryUtils.putStackInInventory(inventory, item.getEntityItem(), ForgeDirection.OPPOSITES[getFacing()], false);
	
							if(remains == null)
							{
								item.setDead();
							}
							else {
								item.setEntityItemStack(remains);
							}
						}
					}
				}
			}

			if(thread != null)
			{
				PathfinderEMContractor newPath = thread.getPath();

				if(newPath != null)
				{
					pathfinder = newPath;
					thread = null;
				}
			}

			final int renderFrequency = MekanismInduction.proxy.isFancy() ? 1 + worldObj.rand.nextInt(2) : 10 + worldObj.rand.nextInt(2);
			final boolean renderBeam = ticker % renderFrequency == 0 && hasLink() && linked.suck != suck;

			if(hasLink())
			{
				if(!suck)
				{
					if(renderBeam)
					{
						MekanismInduction.proxy.renderElectricShock(worldObj, new Vector3(this).translate(0.5), new Vector3(this).translate(new Vector3(getFacing())).translate(0.5), MekanismInduction.DYE_COLORS[dyeID], false);
					}
					
					//Push entity along path.
					if(pathfinder != null)
					{
						for(int i = 0; i < pathfinder.results.size(); i++)
						{
							Object3D result = pathfinder.results.get(i);

							if(TileEntityEMContractor.canBePath(worldObj, result))
							{
								if(i - 1 >= 0)
								{
									Object3D prevResult = pathfinder.results.get(i - 1);

									Object3D difference = prevResult.difference(result);
									final ForgeDirection direction = toForge(difference);

									if(renderBeam)
									{
										MekanismInduction.proxy.renderElectricShock(worldObj, toVec(prevResult).translate(0.5), toVec(result).translate(0.5), MekanismInduction.DYE_COLORS[dyeID], false);
									}

									AxisAlignedBB bounds = AxisAlignedBB.getAABBPool().getAABB(result.xCoord, result.yCoord, result.zCoord, result.xCoord + 1, result.yCoord + 1, result.zCoord + 1);
									List<EntityItem> entities = worldObj.getEntitiesWithinAABB(EntityItem.class, bounds);

									for(EntityItem entityItem : entities)
									{
										moveEntity(entityItem, direction, result);
									}
								}

							}
							else {
								updatePath();
								break;
							}
						}
					}
					else {
						updatePath();
					}
				}
				else {
					if(renderBeam)
					{
						MekanismInduction.proxy.renderElectricShock(worldObj, new Vector3(this).translate(0.5), new Vector3(this).translate(new Vector3(getFacing())).translate(0.5), MekanismInduction.DYE_COLORS[dyeID], false);
					}
	
					pathfinder = null;
	
					Object3D searchVec = Object3D.get(this).getFromSide(ForgeDirection.getOrientation(getFacing()));
					AxisAlignedBB searchBounds = AxisAlignedBB.getAABBPool().getAABB(searchVec.xCoord, searchVec.yCoord, searchVec.zCoord, searchVec.xCoord + 1, searchVec.yCoord + 1, searchVec.zCoord + 1);
	
					if(searchBounds != null)
					{
						for(EntityItem entityItem : (List<EntityItem>)worldObj.getEntitiesWithinAABB(EntityItem.class, searchBounds))
						{
							if(renderBeam)
							{
								MekanismInduction.proxy.renderElectricShock(worldObj, new Vector3(this).translate(0.5), new Vector3(entityItem), MekanismInduction.DYE_COLORS[dyeID], false);
							}
	
							moveEntity(entityItem, ForgeDirection.getOrientation(getFacing()), Object3D.get(this));
						}
					}
				}
			}
			else if(!hasLink())
			{
				for(EntityItem entityItem : (List<EntityItem>)worldObj.getEntitiesWithinAABB(EntityItem.class, operationBounds))
				{
					moveEntity(entityItem, ForgeDirection.getOrientation(getFacing()), Object3D.get(this));
				}
			}
			
			if(linked != null && linked.isInvalid())
			{
				linked = null;
			}
	
			lastCalcTime--;
		}
	}
	
	private static Vector3 toVec(Object3D obj)
	{
		return new Vector3(obj.xCoord, obj.yCoord, obj.zCoord);
	}
	
	private static ForgeDirection toForge(Object3D obj)
	{
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			if(side.offsetX == obj.xCoord && side.offsetY == obj.yCoord && side.offsetZ == obj.zCoord)
			{
				return side;
			}
		}
		
		return ForgeDirection.UNKNOWN;
	}

	public static boolean canBePath(World world, Object3D position)
	{
		Block block = Block.blocksList[position.getBlockId(world)];
		return block == null || (block instanceof BlockSnow || block instanceof BlockVine || block instanceof BlockLadder || ((block instanceof BlockFluid || block instanceof IFluidBlock) && block.blockID != Block.lavaMoving.blockID && block.blockID != Block.lavaStill.blockID));
	}
	
	private boolean hasLink()
	{
		return linked != null && !linked.isInvalid() && linked.linked == this;
	}

	private void moveEntity(EntityItem entityItem, ForgeDirection direction, Object3D lockVector)
	{
		switch(direction)
		{
			case DOWN:
				entityItem.setPosition(lockVector.xCoord + 0.5, entityItem.posY, lockVector.zCoord + 0.5);

				entityItem.motionX = 0;
				entityItem.motionZ = 0;

				if(!suck)
				{
					entityItem.motionY = Math.max(-MAX_SPEED, entityItem.motionY - ACCELERATION);
				}
				else {
					entityItem.motionY = Math.min(MAX_SPEED, entityItem.motionY + .04 + ACCELERATION);
				}

				break;
			case UP:
				entityItem.setPosition(lockVector.xCoord + 0.5, entityItem.posY, lockVector.zCoord + 0.5);

				entityItem.motionX = 0;
				entityItem.motionZ = 0;

				if(!suck)
				{
					entityItem.motionY = Math.min(MAX_SPEED, entityItem.motionY + .04 + ACCELERATION);
				}
				else {
					entityItem.motionY = Math.max(-MAX_SPEED, entityItem.motionY - ACCELERATION);
				}

				break;
			case NORTH:
				entityItem.setPosition(lockVector.xCoord + 0.5, lockVector.yCoord + 0.5, entityItem.posZ);

				entityItem.motionX = 0;
				entityItem.motionY = 0;

				if(!suck)
				{
					entityItem.motionZ = Math.max(-MAX_SPEED, entityItem.motionZ - ACCELERATION);
				}
				else {
					entityItem.motionZ = Math.min(MAX_SPEED, entityItem.motionZ + ACCELERATION);
				}

				break;
			case SOUTH:
				entityItem.setPosition(lockVector.xCoord + 0.5, lockVector.yCoord + 0.5, entityItem.posZ);

				entityItem.motionX = 0;
				entityItem.motionY = 0;

				if(!suck)
				{
					entityItem.motionZ = Math.min(MAX_SPEED, entityItem.motionZ + ACCELERATION);
				}
				else {
					entityItem.motionZ = Math.max(-MAX_SPEED, entityItem.motionZ - ACCELERATION);
				}

				break;
			case WEST:
				entityItem.setPosition(entityItem.posX, lockVector.yCoord + 0.5, lockVector.zCoord + 0.5);

				entityItem.motionY = 0;
				entityItem.motionZ = 0;

				if(!suck)
				{
					entityItem.motionX = Math.max(-MAX_SPEED, entityItem.motionX - ACCELERATION);
				}
				else {
					entityItem.motionX = Math.min(MAX_SPEED, entityItem.motionX + ACCELERATION);
				}

				break;
			case EAST:
				entityItem.setPosition(entityItem.posX, lockVector.yCoord + 0.5, lockVector.zCoord + 0.5);

				entityItem.motionY = 0;
				entityItem.motionZ = 0;

				if(!suck)
				{
					entityItem.motionX = Math.min(MAX_SPEED, entityItem.motionX + ACCELERATION);
				}
				else {
					entityItem.motionX = Math.max(-MAX_SPEED, entityItem.motionX - ACCELERATION);
				}

				break;
			default:
				break;
		}

		entityItem.ticksExisted = 1;
		entityItem.isAirBorne = true;
		entityItem.delayBeforeCanPickup = 1;
		entityItem.age = Math.max(entityItem.age - 1, 0);
	}

	private EntityItem getItemWithPosition(ItemStack toSend)
	{
		EntityItem item = null;

		switch(ForgeDirection.getOrientation(getFacing()))
		{
			case DOWN:
				item = new EntityItem(worldObj, xCoord + 0.5, yCoord - 0.2, zCoord + 0.5, toSend);
				break;
			case UP:
				item = new EntityItem(worldObj, xCoord + 0.5, yCoord + 1.2, zCoord + 0.5, toSend);
				break;
			case NORTH:
				item = new EntityItem(worldObj, xCoord + 0.5, yCoord + 0.5, zCoord - 0.2, toSend);
				break;
			case SOUTH:
				item = new EntityItem(worldObj, xCoord + 0.5, yCoord + 0.5, zCoord + 1.2, toSend);
				break;
			case WEST:
				item = new EntityItem(worldObj, xCoord - 0.2, yCoord + 0.5, zCoord + 0.5, toSend);
				break;
			case EAST:
				item = new EntityItem(worldObj, xCoord + 1.2, yCoord + 0.5, zCoord + 0.5, toSend);
				break;
			default:
				break;
		}

		item.motionX = 0;
		item.motionY = 0;
		item.motionZ = 0;

		return item;
	}

	public void updateBounds()
	{
		switch(ForgeDirection.getOrientation(getFacing()))
		{
			case DOWN:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord, Math.max(yCoord - MAX_REACH, 1), zCoord, xCoord + 1, yCoord, zCoord + 1);
				suckBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord - 0.1, zCoord, xCoord + 1, yCoord, zCoord + 1);
				break;
			case UP:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord + 1, zCoord, xCoord + 1, Math.min(yCoord + 1 + MAX_REACH, 255), zCoord + 1);
				suckBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord + 1, zCoord, xCoord + 1, yCoord + 1.1, zCoord + 1);
				break;
			case NORTH:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord - MAX_REACH, xCoord + 1, yCoord + 1, zCoord);
				suckBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord - 0.1, xCoord + 1, yCoord + 1, zCoord);
				break;
			case SOUTH:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord + 1, xCoord + 1, yCoord + 1, zCoord + 1 + MAX_REACH);
				suckBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord + 1, xCoord + 1, yCoord + 1, zCoord + 1.1);
				break;
			case WEST:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord - MAX_REACH, yCoord, zCoord, xCoord, yCoord + 1, zCoord + 1);
				suckBounds = AxisAlignedBB.getBoundingBox(xCoord - 0.1, yCoord, zCoord, xCoord, yCoord + 1, zCoord + 1);
				break;
			case EAST:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord + 1, yCoord, zCoord, xCoord + 1 + MAX_REACH, yCoord + 1, zCoord + 1);
				suckBounds = AxisAlignedBB.getBoundingBox(xCoord + 1, yCoord, zCoord, xCoord + 1.1, yCoord + 1, zCoord + 1);
				break;
			default:
				break;
		}
	}

	public boolean isLatched()
	{
		return getLatched() != null;
	}

	public TileEntity getLatched()
	{
		ForgeDirection side = ForgeDirection.getOrientation(getFacing()).getOpposite();

		TileEntity tile = worldObj.getBlockTileEntity(xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ);

		if(tile instanceof IInventory)
		{
			return tile;
		}

		return null;
	}

	public void incrementFacing()
	{
		setFacing((short)(facing == 5 ? 0 : facing+1));
	}

	public boolean canFunction()
	{
		return isLatched() && !worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);
		
		suck = nbtTags.getBoolean("suck");
		dyeID = nbtTags.getInteger("dyeID");
		
		if(nbtTags.hasKey("link"))
		{
			tempLinkVector = Object3D.read(nbtTags.getCompoundTag("link"));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);
		
		nbtTags.setBoolean("suck", suck);
		nbtTags.setInteger("dyeID", dyeID);

		if(linked != null)
		{
			nbtTags.setCompoundTag("link", Object3D.get(linked).write(new NBTTagCompound()));
		}
	}

	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		
		suck = dataStream.readBoolean();
		dyeID = dataStream.readInt();

		if(dataStream.readBoolean())
		{
			tempLinkVector = new Object3D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
		}

		worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
		updateBounds();
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(suck);
		data.add(dyeID);
		
		if(linked != null)
		{
			data.add(true);
			
			data.add(linked.xCoord);
			data.add(linked.yCoord);
			data.add(linked.zCoord);
		}
		else {
			data.add(false);
		}
		
		return data;
	}

	/**
	 * Link between two TileEntities, do pathfinding operation.
	 */
	public void setLink(TileEntityEMContractor tileEntity, boolean setOpponent)
	{
		if(linked != null && setOpponent)
		{
			linked.setLink(null, false);
		}

		linked = tileEntity;

		if(setOpponent)
		{
			linked.setLink(this, false);
		}

		updatePath();
	}

	public void updatePath()
	{
		if(thread == null && linked != null && lastCalcTime <= 0)
		{
			pathfinder = null;
			
			Object3D start = Object3D.get(this).getFromSide(ForgeDirection.getOrientation(getFacing()));
			Object3D target = Object3D.get(linked).getFromSide(ForgeDirection.getOrientation(linked.getFacing()));

			if(start.distanceTo(target) < MekanismInduction.MAX_CONTRACTOR_DISTANCE)
			{
				if(TileEntityEMContractor.canBePath(worldObj, start) && TileEntityEMContractor.canBePath(worldObj, target))
				{
					thread = new ThreadEMPathfinding(new PathfinderEMContractor(worldObj, target), start);
					thread.start();
					lastCalcTime = 40;
				}
			}
		}
	}

	public void setDye(int dye)
	{
		dyeID = dye;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
}
