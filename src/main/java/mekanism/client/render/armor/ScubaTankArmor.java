package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.ModelScubaTank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ScubaTankArmor implements ICustomArmor, ResourceManagerReloadListener {

    public static final ScubaTankArmor SCUBA_TANK = new ScubaTankArmor();

    private ModelScubaTank model;

    private ScubaTankArmor() {
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        model = new ModelScubaTank(Minecraft.getInstance().getEntityModels());
    }

    @Override
    public <STATE extends HumanoidRenderState> void render(HumanoidModel<STATE> baseModel, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords,
          STATE state, ItemStack stack) {
        if (!baseModel.body.visible) {
            //If the body model shouldn't show don't bother displaying it
            return;
        }
        poseStack.pushPose();
        if (state.isBaby) {
            float f1 = 1.0F / BABY_MODEL_TRANSFORM.babyBodyScale();
            poseStack.scale(f1, f1, f1);
            poseStack.translate(0.0D, BABY_MODEL_TRANSFORM.bodyYOffset() / 16.0F, 0.0D);
        }
        baseModel.body.translateAndRotate(poseStack);
        poseStack.translate(0, 0, 0.06);
        this.model.collect(poseStack, nodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, stack.hasFoil());
        poseStack.popPose();
    }
}