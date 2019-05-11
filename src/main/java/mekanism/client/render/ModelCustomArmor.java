package mekanism.client.render;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelArmoredJetpack;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.model.ModelGasMask;
import mekanism.client.model.ModelJetpack;
import mekanism.client.model.ModelScubaTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelCustomArmor extends ModelBiped {

    public static ModelCustomArmor INSTANCE = new ModelCustomArmor();

    public static GlowArmor GLOW_BIG = new GlowArmor(1.0F);
    public static GlowArmor GLOW_SMALL = new GlowArmor(0.5F);

    public static Minecraft mc = Minecraft.getMinecraft();

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

    public static boolean useModel(ArmorModel type, ModelRenderer partRender, ModelCustomArmor biped) {
        if (type.armorSlot == 0) {
            return partRender == biped.bipedHead;
        } else if (type.armorSlot == 1) {
            return partRender == biped.bipedBody;
        } else if (type.armorSlot == 3) {
            return partRender == biped.bipedLeftLeg || partRender == biped.bipedRightLeg;
        }
        return false;
    }

    public static ModelBiped getGlow(EntityEquipmentSlot index) {
        ModelBiped biped = index != EntityEquipmentSlot.LEGS ? GLOW_BIG : GLOW_SMALL;

        biped.bipedHead.showModel = index == EntityEquipmentSlot.HEAD;
        biped.bipedHeadwear.showModel = index == EntityEquipmentSlot.HEAD;
        biped.bipedBody.showModel = index == EntityEquipmentSlot.CHEST || index == EntityEquipmentSlot.LEGS;
        biped.bipedRightArm.showModel = index == EntityEquipmentSlot.CHEST;
        biped.bipedLeftArm.showModel = index == EntityEquipmentSlot.CHEST;
        biped.bipedRightLeg.showModel = index == EntityEquipmentSlot.LEGS || index == EntityEquipmentSlot.FEET;
        biped.bipedLeftLeg.showModel = index == EntityEquipmentSlot.LEGS || index == EntityEquipmentSlot.FEET;
        return biped;
    }

    public void init(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float size) {
        reset();

        isSneak = entity.isSneaking();
        isRiding = entity.isRiding();

        if (entity instanceof EntityLivingBase) {
            isChild = ((EntityLivingBase) entity).isChild();
        }

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

        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, size, entity);
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

    public void resetPart(ModelRenderer renderer, float x, float y, float z) {
        renderer.cubeList.clear();
        ModelCustom model = new ModelCustom(this, renderer);
        renderer.addChild(model);
        setOffset(renderer, x, y, z);
    }

    public void setOffset(ModelRenderer renderer, float x, float y, float z) {
        renderer.offsetX = x;
        renderer.offsetY = y;
        renderer.offsetZ = z;
    }

    @Override
    public void render(@Nonnull Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
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

    public static class GlowArmor extends ModelBiped {

        public GlowArmor(float size) {
            super(size);
        }

        @Override
        public void render(@Nonnull Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            isSneak = entity.isSneaking();
            isRiding = entity.isRiding();
            if (entity instanceof EntityLivingBase) {
                isChild = ((EntityLivingBase) entity).isChild();
            }

            setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
            MekanismRenderer.glowOn();
            super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            MekanismRenderer.glowOff();
        }
    }

    public class ModelCustom extends ModelRenderer {

        public ModelCustomArmor biped;
        public ModelRenderer partRender;

        public ModelCustom(ModelCustomArmor base, ModelRenderer renderer) {
            super(base);
            biped = base;
            partRender = renderer;
        }

        @Override
        public void render(float size) {
            if (ModelCustomArmor.this.modelType != null) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0, 0, 0.06F);
                mc.renderEngine.bindTexture(modelType.resource);
                if (useModel(biped.modelType, partRender, biped)) {
                    if (biped.modelType == ArmorModel.JETPACK) {
                        ArmorModel.jetpackModel.render(0.0625F);
                    } else if (biped.modelType == ArmorModel.ARMOREDJETPACK) {
                        ArmorModel.armoredJetpackModel.render(0.0625F);
                    } else if (biped.modelType == ArmorModel.SCUBATANK) {
                        ArmorModel.scubaTankModel.render(0.0625F);
                    } else if (biped.modelType == ArmorModel.GASMASK) {
                        GlStateManager.translate(0, 0, -0.05F);
                        ArmorModel.gasMaskModel.render(0.0625F);
                    } else if (biped.modelType == ArmorModel.FREERUNNERS) {
                        GlStateManager.scale(1.02F, 1.02F, 1.02F);
                        if (partRender == biped.bipedLeftLeg) {
                            GlStateManager.translate(-0.1375F, -0.75F, -0.0625F);
                            ArmorModel.freeRunnersModel.renderLeft(0.0625F);
                        } else if (partRender == biped.bipedRightLeg) {
                            GlStateManager.translate(0.1375F, -0.75F, -0.0625F);
                            ArmorModel.freeRunnersModel.renderRight(0.0625F);
                        }
                    }
                }
                GlStateManager.popMatrix();
            }
        }
    }
}