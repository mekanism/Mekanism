package mekanism.tools.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;

public class GlowArmor extends BipedModel<LivingEntity> {

    private static final GlowArmor BIG = new GlowArmor(1.0F);
    private static final GlowArmor SMALL = new GlowArmor(0.5F);

    private GlowArmor(float size) {
        super(size);
    }

    @Override
    public void renderToBuffer(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        //Make it render at full brightness
        super.renderToBuffer(matrix, vertexBuilder, MekanismRenderer.FULL_LIGHT, overlayLight, red, green, blue, alpha);
    }

    public static BipedModel<LivingEntity> getGlow(EquipmentSlotType index) {
        BipedModel<LivingEntity> biped = index == EquipmentSlotType.LEGS ? SMALL : BIG;
        biped.head.visible = index == EquipmentSlotType.HEAD;
        biped.hat.visible = index == EquipmentSlotType.HEAD;
        biped.body.visible = index == EquipmentSlotType.CHEST || index == EquipmentSlotType.LEGS;
        biped.rightArm.visible = index == EquipmentSlotType.CHEST;
        biped.leftArm.visible = index == EquipmentSlotType.CHEST;
        biped.rightLeg.visible = index == EquipmentSlotType.LEGS || index == EquipmentSlotType.FEET;
        biped.leftLeg.visible = index == EquipmentSlotType.LEGS || index == EquipmentSlotType.FEET;
        return biped;
    }
}