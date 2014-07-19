package mekanism.common.entity;

import io.netty.buffer.ByteBuf;

import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.Pos3D;
import mekanism.common.util.StackUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityFlame extends Entity implements IEntityAdditionalSpawnData
{
	public static final int LIFESPAN = 60;
	public static final int DAMAGE = 4;
	
	public DamageSource flamethrowerDamage = (new DamageSource("flamethrower")).setFireDamage().setProjectile();
	
	public Entity owner = null;
	
	public EntityFlame(World world)
	{
		super(world);
		
		setSize(0.5F, 0.5F);
	}
	
	public EntityFlame(EntityPlayer player)
	{
		this(player.worldObj);
		
		Pos3D playerPos = new Pos3D(player).translate(0, 1.6, 0);
		Pos3D flameVec = new Pos3D(1, 1, 1);
		
		flameVec.multiply(new Pos3D(player.getLook(90)));
		flameVec.rotateYaw(6);
		
		Pos3D mergedVec = playerPos.clone().translate(flameVec);
		setPosition(mergedVec.xPos, mergedVec.yPos, mergedVec.zPos);
		
		Pos3D motion = new Pos3D(0.2, 0.2, 0.2);
		motion.multiply(new Pos3D(player.getLookVec()));
		
		setHeading(motion);
		
		motionX = motion.xPos;
		motionY = motion.yPos;
		motionZ = motion.zPos;
		
		owner = player;
	}
	
    public void setHeading(Pos3D motion)
    {
        float d = MathHelper.sqrt_double((motion.xPos * motion.xPos) + (motion.zPos * motion.zPos));
        
        prevRotationYaw = rotationYaw = (float)(Math.atan2(motion.xPos, motion.zPos) * 180.0D / Math.PI);
        prevRotationPitch = rotationPitch = (float)(Math.atan2(motion.yPos, d) * 180.0D / Math.PI);
    }
	
	@Override
	public void onUpdate()
	{
		if(isDead)
		{
			return;
		}
		
		ticksExisted++;
		
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        
        prevRotationPitch = rotationPitch;
        prevRotationYaw = rotationYaw;
        
        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        
        if(rand.nextInt(4) == 0)
        {
        	calculateVector();
        }
        
		if(ticksExisted > LIFESPAN)
		{
			setDead();
			return;
		}
	}
	
	private void calculateVector()
	{
		Vec3 localVec = Vec3.createVectorHelper(posX, posY, posZ);
        Vec3 motionVec = Vec3.createVectorHelper(posX + motionX, posY + motionY, posZ + motionZ);
        MovingObjectPosition mop = worldObj.func_147447_a(localVec, motionVec, true, true, false);
        localVec = Vec3.createVectorHelper(posX, posY, posZ);
        motionVec = Vec3.createVectorHelper(posX + motionX, posY + motionY, posZ + motionZ);

        if(mop != null)
        {
            motionVec = Vec3.createVectorHelper(mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
        }

        Entity entity = null;
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
        double entityDist = 0.0D;
        int i;

        for(Entity entity1 : (List<Entity>)list)
        {
            if((entity1 instanceof EntityItem || entity1.canBeCollidedWith()) && (entity1 != owner || ticksExisted >= 5))
            {
                float boundsScale = 0.3F;
                AxisAlignedBB newBounds = entity1.boundingBox.expand((double)boundsScale, (double)boundsScale, (double)boundsScale);
                MovingObjectPosition movingobjectposition1 = newBounds.calculateIntercept(localVec, motionVec);

                if(movingobjectposition1 != null)
                {
                    double dist = localVec.distanceTo(movingobjectposition1.hitVec);

                    if(dist < entityDist || entityDist == 0)
                    {
                        entity = entity1;
                        entityDist = dist;
                    }
                }
            }
        }

        if(entity != null)
        {
            mop = new MovingObjectPosition(entity);
        }

        if(mop != null && mop.entityHit instanceof EntityPlayer)
        {
            EntityPlayer entityplayer = (EntityPlayer)mop.entityHit;

            if(entityplayer.capabilities.disableDamage || owner instanceof EntityPlayer && !((EntityPlayer)owner).canAttackPlayer(entityplayer))
            {
                mop = null;
            }
        }

        if(mop != null)
        {
            if(mop.entityHit != null)
            {
            	if(mop.entityHit instanceof EntityItem)
            	{
            		if(mop.entityHit.ticksExisted > 40)
            		{
            			if(!smeltItem((EntityItem)mop.entityHit))
            			{
            				burn(mop.entityHit);
            			}
            		}
            	}
            	else {
            		burn(mop.entityHit);
            	}
            }
            else {
                Block block = worldObj.getBlock(mop.blockX, mop.blockY, mop.blockZ);
                int meta = worldObj.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ);
                
                smeltBlock(new Coord4D(mop.blockX, mop.blockY, mop.blockZ));
                
                System.out.println(block);
            }
            
            setDead();
        }
	}
	
	private boolean smeltItem(EntityItem item)
	{
		ItemStack result = FurnaceRecipes.smelting().getSmeltingResult(item.getEntityItem());
		
		if(result != null)
		{
			item.setEntityItemStack(StackUtils.size(result, item.getEntityItem().stackSize));
			item.ticksExisted = 0;
			
			spawnParticlesAt(new Pos3D(item));
			
			return true;
		}
		
		return false;
	}
	
	private boolean smeltBlock(Coord4D block)
	{
		ItemStack result = FurnaceRecipes.smelting().getSmeltingResult(block.getStack(worldObj));
		
		if(result != null)
		{
			if(!worldObj.isRemote)
			{
				Block b = block.getBlock(worldObj);
				int meta = block.getMetadata(worldObj);
				
				if(Block.getBlockFromItem(result.getItem()) != Blocks.air)
				{
					worldObj.setBlock(block.xCoord, block.yCoord, block.zCoord, Block.getBlockFromItem(result.getItem()), result.getItemDamage(), 3);
				}
				else {
					worldObj.setBlockToAir(block.xCoord, block.yCoord, block.zCoord);
					
					EntityItem item = new EntityItem(worldObj, block.xCoord + 0.5, block.yCoord + 0.5, block.zCoord + 0.5, result.copy());
					worldObj.spawnEntityInWorld(item);
				}
				
				worldObj.playAuxSFXAtEntity(null, 2001, block.xCoord, block.yCoord, block.zCoord, Block.getIdFromBlock(b) + (meta << 12));
			}
			
			spawnParticlesAt(new Pos3D(block).translate(0.5, 0.5, 0.5));
			
			return true;
		}
		
		return false;
	}
	
	private void burn(Entity entity)
	{
    	entity.setFire(200);
        entity.attackEntityFrom(flamethrowerDamage, DAMAGE);
	}
	
	private void spawnParticlesAt(Pos3D pos)
	{
		for(int i = 0; i < 10; i++)
		{
			worldObj.spawnParticle("smoke", pos.xPos + (rand.nextFloat()-0.5), pos.yPos + (rand.nextFloat()-0.5), pos.zPos + (rand.nextFloat()-0.5), 0, 0, 0);
		}
	}

	@Override
	protected void entityInit() {}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbtTags) {}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbtTags) {}

	@Override
	public void writeSpawnData(ByteBuf dataStream) {}

	@Override
	public void readSpawnData(ByteBuf dataStream) {}
}
