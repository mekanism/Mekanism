package mekanism.additions.common.entity.baby;

import java.util.UUID;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;

public interface IBabyEntity {

    //COPY of Zombie SPEED_MODIFIER_BABY_UUID and SPEED_MODIFIER_BABY
    UUID babySpeedBoostUUID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    AttributeModifier babySpeedBoostModifier = new AttributeModifier(babySpeedBoostUUID, "Baby speed boost", 0.5D, Operation.MULTIPLY_BASE);

    default void setChild(EntityDataAccessor<Boolean> childParameter, boolean child) {
        LivingEntity entity = (LivingEntity) this;
        entity.getEntityData().set(childParameter, child);
        if (entity.level != null && !entity.level.isClientSide) {
            AttributeInstance attributeInstance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attributeInstance != null) {
                attributeInstance.removeModifier(babySpeedBoostModifier);
                if (child) {
                    attributeInstance.addTransientModifier(babySpeedBoostModifier);
                }
            }
        }
    }
}