package mekanism.additions.common.entity.baby;

import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.network.datasync.DataParameter;

public interface IBabyEntity {

    //COPY of ZombieEntity BABY_SPEED_BOOST_ID and BABY_SPEED_BOOST
    UUID babySpeedBoostUUID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    AttributeModifier babySpeedBoostModifier = new AttributeModifier(babySpeedBoostUUID, "Baby speed boost", 0.5D, Operation.MULTIPLY_BASE);

    default void setChild(DataParameter<Boolean> childParameter, boolean child) {
        LivingEntity entity = (LivingEntity) this;
        entity.getDataManager().set(childParameter, child);
        if (entity.world != null && !entity.world.isRemote) {
            ModifiableAttributeInstance attributeInstance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attributeInstance != null) {
                attributeInstance.removeModifier(babySpeedBoostModifier);
                if (child) {
                    attributeInstance.applyNonPersistentModifier(babySpeedBoostModifier);
                }
            }
        }
    }
}