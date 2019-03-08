package mekanism.common.entity;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Pos3D;
import mekanism.common.MekanismSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRedstone;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class EntityBalloon extends Entity implements IEntityAdditionalSpawnData
{
	public EnumColor color = EnumColor.DARK_BLUE;

	public Coord4D latched;
	public EntityLivingBase latchedEntity;

	/* server-only */
	public boolean hasCachedEntity;

	public UUID cachedEntityUUID;
	
    private static final DataParameter<Byte> IS_LATCHED = EntityDataManager.createKey(EntityBalloon.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> LATCHED_X = EntityDataManager.createKey(EntityBalloon.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LATCHED_Y = EntityDataManager.createKey(EntityBalloon.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LATCHED_Z = EntityDataManager.createKey(EntityBalloon.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LATCHED_ID = EntityDataManager.createKey(EntityBalloon.class, DataSerializers.VARINT);

	public EntityBalloon(World world)
	{
		super(world);

		ignoreFrustumCheck = true;
		preventEntitySpawning = true;
		setPosition(posX + 0.5F, posY + 3F, posZ + 0.5F);
		setSize(0.25F, 0.25F);
		motionY = 0.04;

		dataManager.register(IS_LATCHED, (byte)0);
		dataManager.register(LATCHED_X, 0);
		dataManager.register(LATCHED_Y, 0);
		dataManager.register(LATCHED_Z, 0);
		dataManager.register(LATCHED_ID, -1);
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
		this(entity.world);

		latchedEntity = entity;
		setPosition(latchedEntity.posX, latchedEntity.posY + latchedEntity.height + 1.7F, latchedEntity.posZ);

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		color = c;

		dataManager.set(IS_LATCHED, (byte)2);
		dataManager.set(LATCHED_ID, entity.getEntityId());
	}

	public EntityBalloon(World world, Coord4D obj, EnumColor c)
	{
		this(world);

		latched = obj;
		setPosition(latched.x + 0.5F, latched.y + 1.9F, latched.z + 0.5F);

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		color = c;

		dataManager.set(IS_LATCHED, (byte)1);
		dataManager.set(LATCHED_X, latched != null ? latched.x : 0); /* Latched X */
		dataManager.set(LATCHED_Y, latched != null ? latched.y : 0); /* Latched Y */
		dataManager.set(LATCHED_Z, latched != null ? latched.z : 0); /* Latched Z */
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

		if(world.isRemote)
		{
			if(dataManager.get(IS_LATCHED) == 1)
			{
				latched = new Coord4D(dataManager.get(LATCHED_X), dataManager.get(LATCHED_Y), dataManager.get(LATCHED_Z), world.provider.getDimension());
			}
			else {
				latched = null;
			}

			if(dataManager.get(IS_LATCHED) == 2)
			{
				latchedEntity = (EntityLivingBase)world.getEntityByID(dataManager.get(LATCHED_ID));
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
				byte isLatched;
				if (latched != null)
				{
					isLatched = (byte) 1;
				}
				else if (latchedEntity != null)
				{
					isLatched = (byte) 2;
				}
				else
				{
					isLatched = (byte) 0;
				}

				dataManager.set(IS_LATCHED, isLatched);
				dataManager.set(LATCHED_X, latched != null ? latched.x : 0);
				dataManager.set(LATCHED_Y, latched != null ? latched.y : 0);
				dataManager.set(LATCHED_Z, latched != null ? latched.z : 0);
				dataManager.set(LATCHED_ID, latchedEntity != null ? latchedEntity.getEntityId() : -1);
			}
		}

		if(!world.isRemote)
		{
			if(latched != null && (latched.exists(world) && latched.isAirBlock(world)))
			{
				latched = null;

				dataManager.set(IS_LATCHED, (byte)0);
			}

			if(latchedEntity != null && (latchedEntity.getHealth() <= 0 || latchedEntity.isDead || !world.loadedEntityList.contains(latchedEntity)))
			{
				latchedEntity = null;

				dataManager.set(IS_LATCHED, (byte)0);
			}
		}

		if(!isLatched())
		{
			motionY = Math.min(motionY*1.02F, 0.2F);

			move(MoverType.SELF, motionX, motionY, motionZ);

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
			if(posi.getY() < 256 && !world.isAirBlock(posi))
			{
				return posi.getY()+1+(entity instanceof EntityPlayer ? 1 : 0);
			}
		}

		return -1;
	}

	private void findCachedEntity()
	{
		for(Object obj : world.loadedEntityList)
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
		playSound(MekanismSounds.POP, 1, 1);

		if(world.isRemote)
		{
			for(int i = 0; i < 10; i++)
			{
				try {
					doParticle();
				} catch(Throwable ignored) {}
			}
		}

		setDead();
	}

	@SideOnly(Side.CLIENT)
	private void doParticle()
	{
		Pos3D pos = new Pos3D(posX + (rand.nextFloat()*.6 - 0.3), posY + (rand.nextFloat()*.6 - 0.3), posZ + (rand.nextFloat()*.6 - 0.3));

		Particle fx = new ParticleRedstone.Factory().createParticle(0, world, pos.x, pos.y, pos.z, 0, 0, 0);
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
	protected void readEntityFromNBT(@Nonnull NBTTagCompound nbtTags)
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
	protected void writeEntityToNBT(@Nonnull NBTTagCompound nbtTags)
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
			latchedEntity = (EntityLivingBase)world.getEntityByID(data.readInt());
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
	public boolean attackEntityFrom(@Nonnull DamageSource dmgSource, float damage)
	{
		if(isEntityInvulnerable(dmgSource))
		{
			return false;
		}
		else {
			markVelocityChanged();

			if(dmgSource != DamageSource.MAGIC && dmgSource != DamageSource.DROWN && dmgSource != DamageSource.FALL)
			{
				pop();
				return true;
			}

			return false;
		}
	}

	public boolean isLatched()
	{
		if(!world.isRemote)
		{
			return latched != null || latchedEntity != null;
		}
		else {
			return dataManager.get(IS_LATCHED) > 0;
		}
	}

	public boolean isLatchedToEntity()
	{
		return dataManager.get(IS_LATCHED) == 2 && latchedEntity != null;
	}
}
