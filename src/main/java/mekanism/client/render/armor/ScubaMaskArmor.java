package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.MekanismJavaModel.FoilRendering;
import mekanism.client.model.ModelScubaMask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.server.packs.resources.ResourceManager;

public class ScubaMaskArmor extends SimpleCustomArmor<ModelScubaMask> {

    public static final ScubaMaskArmor SCUBA_MASK = new ScubaMaskArmor();

    private ScubaMaskArmor() {
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
        model = new ModelScubaMask(modelSet);
        babyModel = new ModelScubaMask(modelSet.bakeLayer(ModelScubaMask.MASK_BABY_LAYER));
    }

    @Override
    protected void collect(PoseStack poseStack, SubmitNodeCollector collector, ModelScubaMask model, int light, int overlayLight, FoilRendering foil, int outlineColor) {
        model.collect(poseStack, collector, light, overlayLight, foil, outlineColor);
    }

    @Override
    protected ModelPart humanoidPart(HumanoidModel<?> baseModel) {
        return baseModel.head;
    }

    @Override
    protected double getZOffset() {
        return 0.01;
    }
}