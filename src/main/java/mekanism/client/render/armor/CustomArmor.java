package mekanism.client.render.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.item.ItemStack;

public abstract class CustomArmor extends BipedModel<LivingEntity> {

    protected CustomArmor(float size) {
        super(size);
    }

    public abstract void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect, LivingEntity entity, ItemStack stack);

    @Override
    public void setRotationAngles(@Nonnull LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity instanceof ArmorStandEntity) {
            //If the entity is an armor stand, use the rotations from ArmorStandArmorModel
            ArmorStandEntity armorStand = (ArmorStandEntity) entity;
            float multiplier = (float) Math.PI / 180F;
            this.bipedHead.rotateAngleX = multiplier * armorStand.getHeadRotation().getX();
            this.bipedHead.rotateAngleY = multiplier * armorStand.getHeadRotation().getY();
            this.bipedHead.rotateAngleZ = multiplier * armorStand.getHeadRotation().getZ();
            this.bipedHead.setRotationPoint(0.0F, 1.0F, 0.0F);
            this.bipedBody.rotateAngleX = multiplier * armorStand.getBodyRotation().getX();
            this.bipedBody.rotateAngleY = multiplier * armorStand.getBodyRotation().getY();
            this.bipedBody.rotateAngleZ = multiplier * armorStand.getBodyRotation().getZ();
            this.bipedLeftArm.rotateAngleX = multiplier * armorStand.getLeftArmRotation().getX();
            this.bipedLeftArm.rotateAngleY = multiplier * armorStand.getLeftArmRotation().getY();
            this.bipedLeftArm.rotateAngleZ = multiplier * armorStand.getLeftArmRotation().getZ();
            this.bipedRightArm.rotateAngleX = multiplier * armorStand.getRightArmRotation().getX();
            this.bipedRightArm.rotateAngleY = multiplier * armorStand.getRightArmRotation().getY();
            this.bipedRightArm.rotateAngleZ = multiplier * armorStand.getRightArmRotation().getZ();
            this.bipedLeftLeg.rotateAngleX = multiplier * armorStand.getLeftLegRotation().getX();
            this.bipedLeftLeg.rotateAngleY = multiplier * armorStand.getLeftLegRotation().getY();
            this.bipedLeftLeg.rotateAngleZ = multiplier * armorStand.getLeftLegRotation().getZ();
            this.bipedLeftLeg.setRotationPoint(1.9F, 11.0F, 0.0F);
            this.bipedRightLeg.rotateAngleX = multiplier * armorStand.getRightLegRotation().getX();
            this.bipedRightLeg.rotateAngleY = multiplier * armorStand.getRightLegRotation().getY();
            this.bipedRightLeg.rotateAngleZ = multiplier * armorStand.getRightLegRotation().getZ();
            this.bipedRightLeg.setRotationPoint(-1.9F, 11.0F, 0.0F);
            this.bipedHeadwear.copyModelAngles(this.bipedHead);
        } else {
            //Otherwise just use super to apply the proper rotations
            super.setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }
}