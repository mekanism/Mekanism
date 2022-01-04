package mekanism.additions.common.entity.baby;

import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.network.syncher.EntityDataAccessor;

public interface IBabyEntity {

    //COPY of ZombieEntity BABY_SPEED_BOOST_ID and BABY_SPEED_BOOST
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