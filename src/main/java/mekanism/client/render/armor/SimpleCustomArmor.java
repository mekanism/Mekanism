package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.MekanismJavaModel.FoilRendering;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public abstract class SimpleCustomArmor<MODEL> implements ICustomArmor, ResourceManagerReloadListener {

    @Nullable
    protected MODEL model;
    @Nullable
    protected MODEL babyModel;

    protected SimpleCustomArmor() {
    }

    @Override
    public <STATE extends HumanoidRenderState> void render(HumanoidModel<STATE> baseModel, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords,
          STATE state, boolean isBaby, ItemStack stack) {
        ModelPart humanoidPart = humanoidPart(baseModel);
        MODEL model = state.isBaby ? this.babyModel : this.model;
        if (model != null) {
            poseStack.pushPose();
            humanoidPart.translateAndRotate(poseStack);
            poseStack.translate(0, 0, getZOffset());
            collect(poseStack, nodeCollector, model, lightCoords, OverlayTexture.NO_OVERLAY, FoilRendering.ARMOR.foil(stack.hasFoil()), state.outlineColor);
            poseStack.popPose();
        }
    }

    @Override
    public <STATE extends HumanoidRenderState> boolean isVisible(HumanoidModel<STATE> baseModel, STATE state) {
        return humanoidPart(baseModel).visible;
    }

    protected abstract void collect(PoseStack poseStack, SubmitNodeCollector collector, MODEL model, int light, int overlayLight, FoilRendering foil, int outlineColor);

    protected abstract ModelPart humanoidPart(HumanoidModel<?> baseModel);

    protected abstract double getZOffset();
}