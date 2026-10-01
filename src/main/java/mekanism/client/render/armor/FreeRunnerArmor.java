package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.MekanismJavaModel.FoilRendering;
import mekanism.client.model.ModelArmoredFreeRunners;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.model.ModelFreeRunners.FreeRunnerRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class FreeRunnerArmor implements ICustomArmor, ResourceManagerReloadListener {

    public static final FreeRunnerArmor FREE_RUNNERS = new FreeRunnerArmor(false);
    public static final FreeRunnerArmor ARMORED_FREE_RUNNERS = new FreeRunnerArmor(true);

    private final boolean armored;
    @Nullable
    private ModelFreeRunners model;
    @Nullable
    private ModelFreeRunners babyModel;

    private FreeRunnerArmor(boolean armored) {
        this.armored = armored;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
        if (armored) {
            model = new ModelArmoredFreeRunners(modelSet);
            babyModel = new ModelArmoredFreeRunners(modelSet.bakeLayer(ModelArmoredFreeRunners.ARMORED_FREE_RUNNER_BABY_LAYER));
        } else {
            model = new ModelFreeRunners(modelSet);
            babyModel = new ModelFreeRunners(modelSet.bakeLayer(ModelFreeRunners.FREE_RUNNER_BABY_LAYER));
        }
    }

    @Override
    public <STATE extends HumanoidRenderState> void render(HumanoidModel<STATE> baseModel, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords,
          STATE state, ItemStack stack) {
        //If the model isn't meant to be shown don't bother rendering anything
        if (!baseModel.leftLeg.visible && !baseModel.rightLeg.visible) {
            return;
        }
        ModelFreeRunners model = state.isBaby ? this.babyModel : this.model;
        if (model != null) {
            FoilRendering foil = FoilRendering.ARMOR.foil(stack.hasFoil());
            tryRenderLeg(poseStack, nodeCollector, lightCoords, foil, state.outlineColor, model, baseModel.leftLeg, FreeRunnerRenderState.LEFT_ONLY);
            tryRenderLeg(poseStack, nodeCollector, lightCoords, foil, state.outlineColor, model, baseModel.rightLeg, FreeRunnerRenderState.RIGHT_ONLY);
        }
    }

    //TODO - 26.3: Fix rendering
    private void tryRenderLeg(PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords, FoilRendering foil, int outlineColor, ModelFreeRunners model,
          ModelPart leg, FreeRunnerRenderState renderState) {
        if (leg.visible) {
            poseStack.pushPose();
            leg.translateAndRotate(poseStack);
            poseStack.translate(0, 0, 0.06);
            poseStack.scale(1.02F, 1.02F, 1.02F);
            poseStack.translate(-0.1375, -0.75, -0.0625);
            model.collect(renderState, poseStack, nodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, foil, outlineColor);
            poseStack.popPose();
        }
    }
}