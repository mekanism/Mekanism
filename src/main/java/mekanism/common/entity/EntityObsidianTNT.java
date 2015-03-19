package mekanism.common.entity;

import mekanism.api.MekanismConfig.general;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityObsidianTNT extends Entity
{
	/** How long the fuse is */
	public int fuse;

	/** Whether or not the TNT has exploded */
	private boolean hasExploded = false;

	public EntityObsidianTNT(World world)
	{
		super(world);
		fuse = 0;
		preventEntitySpawning = true;
		setSize(0.98F, 0.98F);
		yOffset = height / 2.0F;
	}

	public EntityObsidianTNT(World world, double x, double y, double z)
	{
		this(world);

		setPosition(x, y, z);

		float randPi = (float)(Math.random()*Math.PI*2);

		motionX = -(Math.sin(randPi))*0.02F;
		motionY = 0.2;
		motionZ = -(Math.cos(randPi))*0.02F;

		fuse = general.obsidianTNTDelay;

		prevPosX = x;
		prevPosY = y;
		prevPosZ = z;
	}

	@Override
	protected void entityInit() {}

	@Override
	protected boolean canTriggerWalking()
	{
		return false;
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return !isDead;
	}

	@Override
	public boolean canBePushed()
	{
		return true;
	}

	@Override
	public void onUpdate()
	{
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		motionY -= 0.04;

		moveEntity(motionX, motionY, motionZ);

		motionX *= 0.98;
		motionY *= 0.98;
		motionZ *= 0.98;

		if(onGround)
		{
			motionX *= 0.7;
			motionZ *= 0.7;
			motionY *= -0.5;
		}

		if(fuse-- <= 0)
		{
			if(!worldObj.isRemote)
			{
				setDead();
				explode();
			}
			else {
				if(hasExploded)
				{
					setDead();
				}
				else {
					worldObj.spawnParticle("lava", posX, posY + 0.5, posZ, 0, 0, 0);
				}
			}
		}
		else {
			worldObj.spawnParticle("lava", posX, posY + 0.5, posZ, 0, 0, 0);
		}
	}

	private void explode()
	{
		worldObj.createExplosion(null, posX, posY, posZ, general.obsidianTNTBlastRadius, true);
		hasExploded = true;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbtTags)
	{
		nbtTags.setByte("Fuse", (byte)fuse);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbtTags)
	{
		fuse = nbtTags.getByte("Fuse");
	}

	@Override
	public float getShadowSize()
	{
		return 0;
	}
}
