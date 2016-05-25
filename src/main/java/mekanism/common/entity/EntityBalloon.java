package mekanism.common.entity;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Pos3D;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityBalloon extends Entity implements IEntityAdditionalSpawnData
{
	public EnumColor color = EnumColor.DARK_BLUE;

	public Coord4D latched;
	public EntityLivingBase latchedEntity;

	/* server-only */
	public boolean hasCachedEntity;

	public UUID cachedEntityUUID;

	public EntityBalloon(World world)
	{
		super(world);

		ignoreFrustumCheck = true;
		preventEntitySpawning = true;
		setPosition(posX + 0.5F, posY + 3F, posZ + 0.5F);
		setSize(0.25F, 0.25F);
		motionY = 0.04;

		dataWatcher.addObject(5, (byte)0); /* Is latched */
		dataWatcher.addObject(6, 0); /* Latched X */
		dataWatcher.addObject(7, 0); /* Latched Y */
		dataWatcher.addObject(8, 0); /* Latched Z */
		dataWatcher.addObject(9, -1); /* Latched entity ID */
	}

	public EntityBalloon(World world, double x, double y, double z, EnumColor c)
	{
		this(world);

		setPosition(x + 0.5F, y + 3F, z + 0.5F);

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		color = c;
	}

	public EntityBalloon(EntityLivingBase entity, EnumColor c)
	{
		this(entity.worldObj);

		latchedEntity = entity;
		setPosition(latchedEntity.posX, latchedEntity.posY + latchedEntity.height + 1.7F, latchedEntity.posZ);

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		color = c;

		dataWatcher.updateObject(5, (byte)2); /* Is latched */
		dataWatcher.updateObject(6, 0); /* Latched X */
		dataWatcher.updateObject(7, 0); /* Latched Y */
		dataWatcher.updateObject(8, 0); /* Latched Z */
		dataWatcher.updateObject(9, entity.getEntityId()); /* Latched entity ID */
	}

	public EntityBalloon(World world, Coord4D obj, EnumColor c)
	{
		this(world);

		latched = obj;
		setPosition(latched.xCoord + 0.5F, latched.yCoord + 1.9F, latched.zCoord + 0.5F);

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		color = c;

		dataWatcher.updateObject(5, (byte)1); /* Is latched */
		dataWatcher.updateObject(6, latched != null ? latched.xCoord : 0); /* Latched X */
		dataWatcher.updateObject(7, latched != null ? latched.yCoord : 0); /* Latched Y */
		dataWatcher.updateObject(8, latched != null ? latched.zCoord : 0); /* Latched Z */
		dataWatcher.updateObject(9, -1); /* Latched entity ID */
	}

	@Override
	public void onUpdate()
	{
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		if(posY > 255)
		{
			pop();
			return;
		}

		if(worldObj.isRemote)
		{
			if(dataWatcher.getWatchableObjectByte(5) == 1)
			{
				latched = new Coord4D(dataWatcher.getWatchableObjectInt(6), dataWatcher.getWatchableObjectInt(7), dataWatcher.getWatchableObjectInt(8), worldObj.provider.getDimensionId());
			}
			else {
				latched = null;
			}

			if(dataWatcher.getWatchableObjectByte(5) == 2)
			{
				latchedEntity = (EntityLivingBase)worldObj.getEntityByID(dataWatcher.getWatchableObjectInt(9));
			}
			else {
				latchedEntity = null;
			}
		}
		else {
			if(hasCachedEntity)
			{
				findCachedEntity();
				cachedEntityUUID = null;
				hasCachedEntity = false;
			}

			if(ticksExisted == 1)
			{
				dataWatcher.updateObject(5, new Byte(latched != null ? (byte)1 : (latchedEntity != null ? (byte)2 : (byte)0))); /* Is latched */
				dataWatcher.updateObject(6, new Integer(latched != null ? latched.xCoord : 0)); /* Latched X */
				dataWatcher.updateObject(7, new Integer(latched != null ? latched.yCoord : 0)); /* Latched Y */
				dataWatcher.updateObject(8, new Integer(latched != null ? latched.zCoord : 0)); /* Latched Z */
				dataWatcher.updateObject(9, new Integer(latchedEntity != null ? latchedEntity.getEntityId() : -1)); /* Latched entity ID */
			}
		}

		if(!worldObj.isRemote)
		{
			if(latched != null && (latched.exists(worldObj) && latched.isAirBlock(worldObj)))
			{
				latched = null;

				dataWatcher.updateObject(5, (byte)0); /* Is latched */
			}

			if(latchedEntity != null && (latchedEntity.getHealth() <= 0 || latchedEntity.isDead || !worldObj.loadedEntityList.contains(latchedEntity)))
			{
				latchedEntity = null;

				dataWatcher.updateObject(5, (byte)0); /* Is latched */
			}
		}

		if(!isLatched())
		{
			motionY = Math.min(motionY*1.02F, 0.2F);

			moveEntity(motionX, motionY, motionZ);

			motionX *= 0.98;
			motionZ *= 0.98;

			if(onGround)
			{
				motionX *= 0.7;
				motionZ *= 0.7;
			}

			if(motionY == 0)
			{
				motionY = 0.04;
			}
		}
		else if(latched != null)
		{
			motionX = 0;
			motionY = 0;
			motionZ = 0;
		}
		else if(latchedEntity != null && latchedEntity.getHealth() > 0)
		{
			int floor = getFloor(latchedEntity);

			if(latchedEntity.posY-(floor+1) < -0.1)
			{
				latchedEntity.motionY = Math.max(0.04, latchedEntity.motionY*1.015);
			}
			else if(latchedEntity.posY-(floor+1) > 0.1)
			{
				latchedEntity.motionY = Math.min(-0.04, latchedEntity.motionY*1.015);
			}
			else {
				latchedEntity.motionY = 0;
			}

			setPosition(latchedEntity.posX, latchedEntity.posY + getAddedHeight(), latchedEntity.posZ);
		}
	}
	
	public double getAddedHeight()
	{
		return latchedEntity.height + 0.8;
	}

	private int getFloor(EntityLivingBase entity)
	{
		BlockPos pos = new BlockPos(entity);

		for(BlockPos posi = pos; posi.getY() > 0; posi = posi.down())
		{
			if(posi.getY() < 256 && !worldObj.isAirBlock(posi))
			{
				return posi.getY()+1+(entity instanceof EntityPlayer ? 1 : 0);
			}
		}

		return -1;
	}

	private void findCachedEntity()
	{
		for(Object obj : worldObj.loadedEntityList)
		{
			if(obj instanceof EntityLivingBase)
			{
				EntityLivingBase entity = (EntityLivingBase)obj;

				if(entity.getUniqueID().equals(cachedEntityUUID))
				{
					latchedEntity = entity;
				}
			}
		}
	}

	private void pop()
	{
		worldObj.playSoundAtEntity(this, "mekanism:etc.Pop", 1, 1);

		if(worldObj.isRemote)
		{
			for(int i = 0; i < 10; i++)
			{
				try {
					doParticle();
				} catch(Throwable t) {}
			}
		}

		setDead();
	}

	@SideOnly(Side.CLIENT)
	private void doParticle()
	{
		Pos3D pos = new Pos3D(posX + (rand.nextFloat()*.6 - 0.3), posY + (rand.nextFloat()*.6 - 0.3), posZ + (rand.nextFloat()*.6 - 0.3));

		EntityFX fx = new EntityReddustFX.Factory().getEntityFX(0, worldObj, pos.xCoord, pos.yCoord, pos.zCoord, 0, 0, 0);
		fx.setRBGColorF(color.getColor(0), color.getColor(1), color.getColor(2));

		Minecraft.getMinecraft().effectRenderer.addEffect(fx);
	}

	@Override
	public boolean canBePushed()
	{
		return latched == null;
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return !isDead;
	}

	@Override
	protected boolean canTriggerWalking()
	{
		return false;
	}

	@Override
	protected void entityInit() {}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbtTags)
	{
		color = EnumColor.values()[nbtTags.getInteger("color")];

		if(nbtTags.hasKey("latched"))
		{
			latched = Coord4D.read(nbtTags.getCompoundTag("latched"));
		}

		if(nbtTags.hasKey("idMost"))
		{
			hasCachedEntity = true;
			cachedEntityUUID = new UUID(nbtTags.getLong("idMost"), nbtTags.getLong("idLeast"));
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbtTags)
	{
		nbtTags.setInteger("color", color.ordinal());

		if(latched != null)
		{
			nbtTags.setTag("latched", latched.write(new NBTTagCompound()));
		}

		if(latchedEntity != null)
		{
			nbtTags.setLong("idMost", latchedEntity.getUniqueID().getMostSignificantBits());
			nbtTags.setLong("idLeast", latchedEntity.getUniqueID().getLeastSignificantBits());
		}
	}

	@Override
	public boolean hitByEntity(Entity entity)
	{
		pop();
		return true;
	}

	@Override
	public void writeSpawnData(ByteBuf data)
	{
		data.writeDouble(posX);
		data.writeDouble(posY);
		data.writeDouble(posZ);

		data.writeInt(color.ordinal());

		if(latched != null)
		{
			data.writeByte((byte)1);

			latched.write(data);
		}
		else if(latchedEntity != null)
		{
			data.writeByte((byte)2);
			data.writeInt(latchedEntity.getEntityId());
		}
		else {
			data.writeByte((byte)0);
		}
	}

	@Override
	public void readSpawnData(ByteBuf data)
	{
		setPosition(data.readDouble(), data.readDouble(), data.readDouble());

		color = EnumColor.values()[data.readInt()];

		byte type = data.readByte();

		if(type == 1)
		{
			latched = Coord4D.read(data);
		}
		else if(type == 2)
		{
			latchedEntity = (EntityLivingBase)worldObj.getEntityByID(data.readInt());
		}
		else {
			latched = null;
		}
	}

	@Override
	public void setDead()
	{
		super.setDead();

		if(latchedEntity != null)
		{
			latchedEntity.isAirBorne = false;
		}
	}

	@Override
	public boolean isInRangeToRenderDist(double dist)
	{
		return dist <= 64;
	}

	@Override
	public boolean isInRangeToRender3d(double p_145770_1_, double p_145770_3_, double p_145770_5_)
	{
		return true;
	}

	@Override
	public boolean attackEntityFrom(DamageSource dmgSource, float damage)
	{
		if(isEntityInvulnerable(dmgSource))
		{
			return false;
		}
		else {
			setBeenAttacked();

			if(dmgSource != DamageSource.magic && dmgSource != DamageSource.drown && dmgSource != DamageSource.fall)
			{
				pop();
				return true;
			}

			return false;
		}
	}

	public boolean isLatched()
	{
		if(!worldObj.isRemote)
		{
			return latched != null || latchedEntity != null;
		}
		else {
			return dataWatcher.getWatchableObjectByte(5) > 0;
		}
	}

	public boolean isLatchedToEntity()
	{
		return dataWatcher.getWatchableObjectByte(5) == 2 && latchedEntity != null;
	}
}
