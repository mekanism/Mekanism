package mekanism.common;

import java.util.Iterator;
import java.util.List;

import mekanism.common.entity.EntityRobit;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/*
 * 	Written by pixlepix (I'm in mekanism! Yay!)
 *	Boilerplate copied from RobitAIFollow
 */
public class RobitAIPickup extends EntityAIBase
{
	/** The robit entity. */
	private EntityRobit theRobit;

	/** The world the robit is located in. */
	private World theWorld;

	/** How fast the robit can travel. */
	private float moveSpeed;

	/** The robit's pathfinder. */
	private PathNavigate thePathfinder;

	/** The ticker for updates. */
	private int ticker;

	/** Whether or not this robit avoids water. */
	private boolean avoidWater;
	private EntityItem closest;

	public RobitAIPickup(EntityRobit entityRobit, float speed)
	{
		theRobit = entityRobit;
		theWorld = entityRobit.worldObj;
		moveSpeed = speed;
		thePathfinder = entityRobit.getNavigator();
		setMutexBits(3);
	}

	@Override
	public boolean shouldExecute()
	{
		if(!theRobit.getDropPickup())
		{
			return false;
		}
		if(closest != null && closest.getDistanceSqToEntity(closest) > 100 && thePathfinder.getPathToXYZ(closest.posX, closest.posY, closest.posZ) != null)
		{
			return true;
		}

		List items = theRobit.worldObj.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(theRobit.posX-10, theRobit.posY-10, theRobit.posZ-10, theRobit.posX+10, theRobit.posY+10, theRobit.posZ+10));
		Iterator iter = items.iterator();
		//Cached for slight performance
		double closestDistance = -1;

		while(iter.hasNext())
		{
			EntityItem entity = (EntityItem)iter.next();

			double distance = theRobit.getDistanceToEntity(entity);

			if(distance <= 10)
			{
				if(closestDistance == -1 || distance < closestDistance)
				{
					if(thePathfinder.getPathToXYZ(entity.posX, entity.posY, entity.posZ) != null)
					{
						closest = entity;
						closestDistance = distance;
					}
				}
			}
		}

		if(closest == null)
		{
			//No valid items
			return false;
		}

		return true;

	}

	@Override
	public boolean continueExecuting()
	{
		return !closest.isDead && !thePathfinder.noPath() && theRobit.getDistanceSqToEntity(closest) > 100 && theRobit.getFollowing() && theRobit.getEnergy() > 0 && closest.worldObj.provider.dimensionId == theRobit.worldObj.provider.dimensionId;
	}

	@Override
	public void startExecuting()
	{
		ticker = 0;
		avoidWater = theRobit.getNavigator().getAvoidsWater();
		theRobit.getNavigator().setAvoidsWater(false);
	}

	@Override
	public void resetTask()
	{
		thePathfinder.clearPathEntity();
		theRobit.getNavigator().setAvoidsWater(avoidWater);
	}

	@Override
	public void updateTask()
	{
		theRobit.getLookHelper().setLookPositionWithEntity(closest, 6.0F, theRobit.getVerticalFaceSpeed()/10);

		if(!theRobit.getDropPickup())
		{
			return;
		}

		if(--ticker <= 0)
		{
			ticker = 10;

			if(!thePathfinder.tryMoveToEntityLiving(closest, moveSpeed))
			{
				if(theRobit.getDistanceSqToEntity(closest) >= 144.0D)
				{
					int x = MathHelper.floor_double(closest.posX) - 2;
					int y = MathHelper.floor_double(closest.posZ) - 2;
					int z = MathHelper.floor_double(closest.boundingBox.minY);

					for(int l = 0; l <= 4; ++l)
					{
						for(int i1 = 0; i1 <= 4; ++i1)
						{
							if((l < 1 || i1 < 1 || l > 3 || i1 > 3) && theWorld.doesBlockHaveSolidTopSurface(theWorld, x + l, z - 1, y + i1) && !theWorld.getBlock(x + l, z, y + i1).isNormalCube() && !theWorld.getBlock(x + l, z + 1, y + i1).isNormalCube())
							{
								theRobit.setLocationAndAngles((x + l) + 0.5F, z, (y + i1) + 0.5F, theRobit.rotationYaw, theRobit.rotationPitch);
								thePathfinder.clearPathEntity();
								return;
							}
						}
					}
				}

			}
		}
	}
}