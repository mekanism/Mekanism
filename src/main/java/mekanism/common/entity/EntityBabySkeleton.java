package mekanism.common.entity;

import java.util.UUID;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public class EntityBabySkeleton extends EntitySkeleton {

    private static final UUID babySpeedBoostUUID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    private static final AttributeModifier babySpeedBoostModifier = new AttributeModifier(babySpeedBoostUUID, "Baby speed boost", 0.5D, 1);

    private static final DataParameter<Boolean> IS_CHILD = EntityDataManager.createKey(EntityBabySkeleton.class, DataSerializers.BOOLEAN);

    public EntityBabySkeleton(World world) {
        super(world);
        dataManager.register(IS_CHILD, false);
        setChild(true);
    }

    @Override
    public boolean isChild() {
        return dataManager.get(IS_CHILD);
    }

    public void setChild(boolean child) {
        dataManager.set(IS_CHILD, child);
        if (world != null && !world.isRemote) {
            IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
            iattributeinstance.removeModifier(babySpeedBoostModifier);
            if (child) {
                iattributeinstance.applyModifier(babySpeedBoostModifier);
            }
        }
        updateChildSize(child);
    }

    @Override
    protected int getExperiencePoints(EntityPlayer p_70693_1_) {
        if (isChild()) {
            experienceValue = (int) ((float) experienceValue * 2.5F);
        }
        return super.getExperiencePoints(p_70693_1_);
    }

    public void updateChildSize(boolean child) {
        updateSize(child ? 0.5F : 1.0F);
    }

    protected final void updateSize(float size) {
        super.setSize(size, size + 0.4F);
    }

    //copied from base entity, as abstractskeleton overrides it
    public float getEyeHeight() {
        return this.height * 0.85F;
    }
}