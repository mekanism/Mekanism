package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.MekanismJavaModel.FoilRendering;
import mekanism.client.model.ModelScubaTank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;

public class ScubaTankArmor extends SimpleCustomArmor<ModelScubaTank> {

    public static final ScubaTankArmor SCUBA_TANK = new ScubaTankArmor();

    private ScubaTankArmor() {
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
        model = new ModelScubaTank(modelSet);
        babyModel = new ModelScubaTank(modelSet.bakeLayer(ModelScubaTank.TANK_BABY_LAYER));
    }

    @Override
    protected void collect(PoseStack poseStack, SubmitNodeCollector collector, ModelScubaTank model, int light, int overlayLight, FoilRendering foil, int outlineColor) {
        collector.submitModel(model, Unit.INSTANCE, poseStack, model.RENDER_TYPE, light, overlayLight, outlineColor);
        if (foil != FoilRendering.NONE) {
            collector.order(1).submitModel(model, Unit.INSTANCE, poseStack, foil.renderType(), light, overlayLight, EntityRenderState.NO_OUTLINE);
        }
    }

    @Override
    protected ModelPart humanoidPart(HumanoidModel<?> baseModel) {
        return baseModel.body;
    }

    @Override
    protected double getZOffset() {
        return 0.06;
    }
}