package mekanism.common;

import mekanism.common.entity.EntityRobit;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class RobitAIFollow extends EntityAIBase
{
	/** The robit entity. */
	private EntityRobit theRobit;

	/** The robit's owner. */
	private EntityPlayer theOwner;

	/** The world the robit is located in. */
	private World theWorld;

	/** How fast the robit can travel. */
	private float moveSpeed;

	/** The robit's pathfinder. */
	private PathNavigateGround thePathfinder;

	/** The ticker for updates. */
	private int ticker;

	/** The distance between the owner the robit must be at in order for the protocol to begin. */
	private float maxDist;

	/** The distance between the owner the robit must reach before it stops the protocol. */
	private float minDist;

	private float oldWaterCost;

	public RobitAIFollow(EntityRobit entityRobit, float speed, float min, float max)
	{
		theRobit = entityRobit;
		theWorld = entityRobit.worldObj;
		moveSpeed = speed;
		thePathfinder = entityRobit.getNavigator();
		minDist = min;
		maxDist = max;
		setMutexBits(3);
	}

	@Override
	public boolean shouldExecute()
	{
		EntityPlayer player = theRobit.getOwner();

		if(player == null)
		{
			return false;
		}
		else if(theRobit.worldObj.provider.getDimension() != player.worldObj.provider.getDimension())
		{
			return false;
		}
		else if(!theRobit.getFollowing())
		{
			//Still looks up at the player if on chargepad or not following

			theRobit.getLookHelper().setLookPositionWithEntity(player, 6.0F, theRobit.getVerticalFaceSpeed()/10);
			return false;
		}
		else if(theRobit.getDistanceSqToEntity(player) < (minDist * minDist))
		{
			return false;
		}
		else if(theRobit.getEnergy() == 0)
		{
			return false;
		}
		else {
			theOwner = player;
			return true;
		}
	}

	@Override
	public boolean continueExecuting()
	{
		return !thePathfinder.noPath() && theRobit.getDistanceSqToEntity(theOwner) > (maxDist * maxDist) && theRobit.getFollowing() && theRobit.getEnergy() > 0 && theOwner.worldObj.provider.getDimension() == theRobit.worldObj.provider.getDimension();
	}

	@Override
	public void startExecuting()
	{
		ticker = 0;
		oldWaterCost = theRobit.getPathPriority(PathNodeType.WATER);
        theRobit.setPathPriority(PathNodeType.WATER, 0.0F);
	}

	@Override
	public void resetTask()
	{
		theOwner = null;
		thePathfinder.clearPathEntity();
        theRobit.setPathPriority(PathNodeType.WATER, oldWaterCost);
	}

	@Override
	public void updateTask()
	{
		theRobit.getLookHelper().setLookPositionWithEntity(theOwner, 6.0F, theRobit.getVerticalFaceSpeed()/10);

		if(theRobit.getFollowing())
		{
			if(--ticker <= 0)
			{
				ticker = 10;

				if(!thePathfinder.tryMoveToEntityLiving(theOwner, moveSpeed))
				{
					if(theRobit.getDistanceSqToEntity(theOwner) >= 144.0D)
					{
						int x = MathHelper.floor_double(theOwner.posX) - 2;
						int y = MathHelper.floor_double(theOwner.getEntityBoundingBox().minY);
						int z = MathHelper.floor_double(theOwner.posZ) - 2;

						for(int l = 0; l <= 4; ++l)
						{
							for(int i1 = 0; i1 <= 4; ++i1)
							{
								BlockPos pos = new BlockPos(x+l, y, z+i1);
								BlockPos under = new BlockPos(x + l, y - 1, z + i1);
								
								if((l < 1 || i1 < 1 || l > 3 || i1 > 3) && theWorld.getBlockState(under).isSideSolid(theWorld, under, EnumFacing.UP) && isEmptyBlock(pos) && isEmptyBlock(new BlockPos(x + l, y + 1, z + i1)))
								{
									theRobit.setLocationAndAngles((x + l) + 0.5F, y, (z + i1) + 0.5F, theRobit.rotationYaw, theRobit.rotationPitch);
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
	
	private boolean isEmptyBlock(BlockPos pos)
    {
        IBlockState iblockstate = theWorld.getBlockState(pos);
        Block block = iblockstate.getBlock();
        
        return block == Blocks.AIR ? true : !iblockstate.isFullCube();
    }
}
