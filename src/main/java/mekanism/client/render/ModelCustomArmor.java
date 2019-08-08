package mekanism.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelArmoredJetpack;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.model.ModelGasMask;
import mekanism.client.model.ModelJetpack;
import mekanism.client.model.ModelScubaTank;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelCustomArmor extends BipedModel<LivingEntity> {

    public static ModelCustomArmor INSTANCE = new ModelCustomArmor();

    public static GlowArmor GLOW_BIG = new GlowArmor(1.0F);
    public static GlowArmor GLOW_SMALL = new GlowArmor(0.5F);

    public static Minecraft minecraft = Minecraft.getInstance();

    public ArmorModel modelType;

    public ModelCustomArmor() {
        resetPart(bipedHead, 0, 0, 0);
        resetPart(bipedBody, 0, 0, 0);
        resetPart(bipedRightArm, 5, 2, 0);
        resetPart(bipedLeftArm, -5, 2, 0);
        resetPart(bipedRightLeg, 0, 0, 0);
        resetPart(bipedLeftLeg, 0, 0, 0);

        bipedHeadwear.cubeList.clear();
    }

    public static boolean useModel(ArmorModel type, RendererModel partRender, ModelCustomArmor biped) {
        if (type.armorSlot == 0) {
            return partRender == biped.bipedHead;
        } else if (type.armorSlot == 1) {
            return partRender == biped.bipedBody;
        } else if (type.armorSlot == 3) {
            return partRender == biped.bipedLeftLeg || partRender == biped.bipedRightLeg;
        }
        return false;
    }

    public static BipedModel getGlow(EquipmentSlotType index) {
        BipedModel biped = index != EquipmentSlotType.LEGS ? GLOW_BIG : GLOW_SMALL;

        biped.bipedHead.showModel = index == EquipmentSlotType.HEAD;
        biped.bipedHeadwear.showModel = index == EquipmentSlotType.HEAD;
        biped.bipedBody.showModel = index == EquipmentSlotType.CHEST || index == EquipmentSlotType.LEGS;
        biped.bipedRightArm.showModel = index == EquipmentSlotType.CHEST;
        biped.bipedLeftArm.showModel = index == EquipmentSlotType.CHEST;
        biped.bipedRightLeg.showModel = index == EquipmentSlotType.LEGS || index == EquipmentSlotType.FEET;
        biped.bipedLeftLeg.showModel = index == EquipmentSlotType.LEGS || index == EquipmentSlotType.FEET;
        return biped;
    }

    public void init(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float size) {
        reset();

        isSneak = entity.isSneaking();
        isSitting = entity.isRiding();
        isChild = entity.isChild();

        if (modelType.armorSlot == 0) {
            bipedHead.isHidden = false;
            bipedHead.showModel = true;
        } else if (modelType.armorSlot == 1) {
            bipedBody.isHidden = false;
            bipedBody.showModel = true;
        } else if (modelType.armorSlot == 3) {
            bipedLeftLeg.isHidden = false;
            bipedLeftLeg.showModel = true;
            bipedRightLeg.isHidden = false;
            bipedRightLeg.showModel = true;
        }

        setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, size);
    }

    public void reset() {
        bipedHead.isHidden = true;
        bipedBody.isHidden = true;
        bipedRightArm.isHidden = true;
        bipedLeftArm.isHidden = true;
        bipedRightLeg.isHidden = true;
        bipedLeftLeg.isHidden = true;

        bipedHead.showModel = false;
        bipedBody.showModel = false;
        bipedRightArm.showModel = false;
        bipedLeftArm.showModel = false;
        bipedRightLeg.showModel = false;
        bipedLeftLeg.showModel = false;
    }

    public void resetPart(RendererModel renderer, float x, float y, float z) {
        renderer.cubeList.clear();
        ModelCustom model = new ModelCustom(this, renderer);
        renderer.addChild(model);
        setOffset(renderer, x, y, z);
    }

    public void setOffset(RendererModel renderer, float x, float y, float z) {
        renderer.offsetX = x;
        renderer.offsetY = y;
        renderer.offsetZ = z;
    }

    @Override
    public void render(@Nonnull LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        init(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }

    public enum ArmorModel {
        JETPACK(1, MekanismUtils.getResource(ResourceType.RENDER, "Jetpack.png")),
        ARMOREDJETPACK(1, MekanismUtils.getResource(ResourceType.RENDER, "Jetpack.png")),
        SCUBATANK(1, MekanismUtils.getResource(ResourceType.RENDER, "ScubaSet.png")),
        GASMASK(0, MekanismUtils.getResource(ResourceType.RENDER, "ScubaSet.png")),
        FREERUNNERS(3, MekanismUtils.getResource(ResourceType.RENDER, "FreeRunners.png"));

        public static ModelJetpack jetpackModel = new ModelJetpack();
        public static ModelArmoredJetpack armoredJetpackModel = new ModelArmoredJetpack();
        public static ModelGasMask gasMaskModel = new ModelGasMask();
        public static ModelScubaTank scubaTankModel = new ModelScubaTank();
        public static ModelFreeRunners freeRunnersModel = new ModelFreeRunners();
        public int armorSlot;
        public ResourceLocation resource;

        ArmorModel(int i, ResourceLocation r) {
            armorSlot = i;
            resource = r;
        }
    }

    public static class GlowArmor extends BipedModel<LivingEntity> {

        public GlowArmor(float size) {
            super(size);
        }

        @Override
        public void render(@Nonnull LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            isSneak = entity.isSneaking();
            isSitting = entity.isRiding();
            isChild = entity.isChild();

            setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlowInfo glowInfo = MekanismRenderer.enableGlow();
            super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            MekanismRenderer.disableGlow(glowInfo);
        }
    }

    public class ModelCustom extends RendererModel {

        public ModelCustomArmor biped;
        public RendererModel partRender;

        public ModelCustom(ModelCustomArmor base, RendererModel renderer) {
            super(base);
            biped = base;
            partRender = renderer;
        }

        @Override
        public void render(float size) {
            if (ModelCustomArmor.this.modelType != null) {
                GlStateManager.pushMatrix();
                GlStateManager.translatef(0, 0, 0.06F);
                minecraft.textureManager.bindTexture(modelType.resource);
                if (useModel(biped.modelType, partRender, biped)) {
                    if (biped.modelType == ArmorModel.JETPACK) {
                        ArmorModel.jetpackModel.render(0.0625F);
                    } else if (biped.modelType == ArmorModel.ARMOREDJETPACK) {
                        ArmorModel.armoredJetpackModel.render(0.0625F);
                    } else if (biped.modelType == ArmorModel.SCUBATANK) {
                        ArmorModel.scubaTankModel.render(0.0625F);
                    } else if (biped.modelType == ArmorModel.GASMASK) {
                        GlStateManager.translatef(0, 0, -0.05F);
                        ArmorModel.gasMaskModel.render(0.0625F);
                    } else if (biped.modelType == ArmorModel.FREERUNNERS) {
                        GlStateManager.translatef(1.02F, 1.02F, 1.02F);
                        if (partRender == biped.bipedLeftLeg) {
                            GlStateManager.translatef(-0.1375F, -0.75F, -0.0625F);
                            ArmorModel.freeRunnersModel.renderLeft(0.0625F);
                        } else if (partRender == biped.bipedRightLeg) {
                            GlStateManager.translatef(0.1375F, -0.75F, -0.0625F);
                            ArmorModel.freeRunnersModel.renderRight(0.0625F);
                        }
                    }
                }
                GlStateManager.popMatrix();
            }
        }
    }
}