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
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ScubaTankArmor implements ICustomArmor, ResourceManagerReloadListener {

    public static final ScubaTankArmor SCUBA_TANK = new ScubaTankArmor();

    @Nullable
    private ModelScubaTank model;

    private ScubaTankArmor() {
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        model = new ModelScubaTank(Minecraft.getInstance().getEntityModels());
    }

    @Override
    public <STATE extends HumanoidRenderState> void render(HumanoidModel<STATE> baseModel, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords,
          STATE state, ItemStack stack) {
        if (model == null || !baseModel.body.visible) {
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
        //TODO - 26.2 foil rendering? Not actually enchantable by default
        nodeCollector.submitModel(this.model, Unit.INSTANCE, poseStack, model.RENDER_TYPE, lightCoords, OverlayTexture.NO_OVERLAY, 0, null);
        poseStack.popPose();
    }
}