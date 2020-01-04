package mekanism.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelArmoredJetpack;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.model.ModelGasMask;
import mekanism.client.model.ModelJetpack;
import mekanism.client.model.ModelScubaTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

public class ModelCustomArmor extends BipedModel<LivingEntity> {

    public static ModelCustomArmor INSTANCE = new ModelCustomArmor();

    public ArmorModel modelType;

    public ModelCustomArmor() {
        super(0.5F);
        bipedHead.addChild(new ModelCustom(this, bipedHead));
        bipedBody.addChild(new ModelCustom(this, bipedBody));
        bipedRightArm.addChild(new ModelCustom(this, bipedRightArm));
        bipedLeftArm.addChild(new ModelCustom(this, bipedLeftArm));
        bipedRightLeg.addChild(new ModelCustom(this, bipedRightLeg));
        bipedLeftLeg.addChild(new ModelCustom(this, bipedLeftLeg));
    }

    private static boolean useModel(ArmorModel type, ModelRenderer partRender, ModelCustomArmor biped) {
        if (type.armorSlot == 0) {
            return partRender == biped.bipedHead;
        } else if (type.armorSlot == 1) {
            return partRender == biped.bipedBody;
        } else if (type.armorSlot == 3) {
            return partRender == biped.bipedLeftLeg || partRender == biped.bipedRightLeg;
        }
        return false;
    }

    @Override
    public void func_225597_a_(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        reset();
        if (modelType.armorSlot == 0) {
            bipedHead.showModel = true;
        } else if (modelType.armorSlot == 1) {
            bipedBody.showModel = true;
        } else if (modelType.armorSlot == 3) {
            bipedLeftLeg.showModel = true;
            bipedRightLeg.showModel = true;
        }
        super.func_225597_a_(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    public void reset() {
        bipedHead.showModel = false;
        bipedBody.showModel = false;
        bipedRightArm.showModel = false;
        bipedLeftArm.showModel = false;
        bipedRightLeg.showModel = false;
        bipedLeftLeg.showModel = false;
    }

    public enum ArmorModel {
        JETPACK(1, MekanismUtils.getResource(ResourceType.RENDER, "jetpack.png")),
        ARMOREDJETPACK(1, MekanismUtils.getResource(ResourceType.RENDER, "jetpack.png")),
        SCUBATANK(1, MekanismUtils.getResource(ResourceType.RENDER, "scuba_set.png")),
        GASMASK(0, MekanismUtils.getResource(ResourceType.RENDER, "scuba_set.png")),
        FREERUNNERS(3, MekanismUtils.getResource(ResourceType.RENDER, "free_runners.png"));

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

    public class ModelCustom extends ModelRenderer {

        public ModelCustomArmor biped;
        public ModelRenderer partRender;

        public ModelCustom(ModelCustomArmor base, ModelRenderer renderer) {
            super(base);
            biped = base;
            partRender = renderer;
        }

        @Override
        public void func_228309_a_(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue,
              float alpha) {
            if (ModelCustomArmor.this.modelType != null) {
                matrix.func_227860_a_();
                matrix.func_227861_a_(0, 0, 0.06);
                if (useModel(biped.modelType, partRender, biped)) {
                    //TODO: 1.15 - Fix this stuff being in the wrong layer/render type
                    // We probably will need to end up adding our own custom BipedArmorLayer so that we can give IRenderTypeBuffer
                    // to our models
                    if (biped.modelType == ArmorModel.JETPACK) {
                        ArmorModel.jetpackModel.func_225598_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
                        ArmorModel.jetpackModel.renderWings(matrix, vertexBuilder, MekanismRenderer.FULL_LIGHT, overlayLight, red, green, blue, 0.2F);
                    } else if (biped.modelType == ArmorModel.ARMOREDJETPACK) {
                        ArmorModel.armoredJetpackModel.func_225598_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
                        ArmorModel.armoredJetpackModel.renderWings(matrix, vertexBuilder, MekanismRenderer.FULL_LIGHT, overlayLight, red, green, blue, 0.2F);
                    } else if (biped.modelType == ArmorModel.SCUBATANK) {
                        ArmorModel.scubaTankModel.func_225598_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
                    } else if (biped.modelType == ArmorModel.GASMASK) {
                        matrix.func_227861_a_(0, 0, -0.05);
                        ArmorModel.gasMaskModel.func_225598_a_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
                        ArmorModel.gasMaskModel.renderGlass(matrix, vertexBuilder, MekanismRenderer.FULL_LIGHT, overlayLight, red, green, blue, 0.3F);
                    } else if (biped.modelType == ArmorModel.FREERUNNERS) {
                        matrix.func_227862_a_(1.02F, 1.02F, 1.02F);
                        if (partRender == biped.bipedLeftLeg) {
                            matrix.func_227861_a_(-0.1375, -0.75, -0.0625);
                            ArmorModel.freeRunnersModel.renderLeft(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
                        } else if (partRender == biped.bipedRightLeg) {
                            matrix.func_227861_a_(0.1375, -0.75, -0.0625);
                            ArmorModel.freeRunnersModel.renderRight(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
                        }
                    }
                }
                matrix.func_227865_b_();
            }
        }
    }
}