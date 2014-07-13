package mekanism.common.entity;

import java.util.UUID;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class EntityBabySkeleton extends EntitySkeleton
{
    private static final UUID babySpeedBoostUUID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    private static final AttributeModifier babySpeedBoostModifier = new AttributeModifier(babySpeedBoostUUID, "Baby speed boost", 0.5D, 1);
    
	public EntityBabySkeleton(World world) 
	{
		super(world);
		setChild(true);
	}
	
	@Override
	protected void entityInit()
	{
		super.entityInit();
		
        getDataWatcher().addObject(12, new Byte((byte)0));
	}
	
    public void setChild(boolean child)
    {
        getDataWatcher().updateObject(12, Byte.valueOf((byte)(child ? 1 : 0)));

        if(worldObj != null && !worldObj.isRemote)
        {
            IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
            iattributeinstance.removeModifier(babySpeedBoostModifier);

            if(child)
            {
                iattributeinstance.applyModifier(babySpeedBoostModifier);
            }
        }

        updateChildSize(child);
    }
    
    @Override
    public boolean isChild()
    {
        return getDataWatcher().getWatchableObjectByte(12) == 1;
    }
    
    @Override
    protected int getExperiencePoints(EntityPlayer p_70693_1_)
    {
        if(isChild())
        {
            experienceValue = (int)((float)experienceValue * 2.5F);
        }

        return super.getExperiencePoints(p_70693_1_);
    }
    
    public void updateChildSize(boolean child)
    {
        updateSize(child ? 0.5F : 1.0F);
    }

    protected final void updateSize(float size)
    {
        super.setSize(size, size+0.4F);
    }
}
