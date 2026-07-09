package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.MekanismJavaModel.FoilRendering;
import mekanism.client.model.ModelScubaMask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ScubaMaskArmor implements ICustomArmor, ResourceManagerReloadListener {

    public static final ScubaMaskArmor SCUBA_MASK = new ScubaMaskArmor();

    @Nullable
    private ModelScubaMask model;

    private ScubaMaskArmor() {
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        model = new ModelScubaMask(Minecraft.getInstance().getEntityModels());
    }

    @Override
    public <STATE extends HumanoidRenderState> void render(HumanoidModel<STATE> baseModel, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords,
          STATE state, ItemStack stack) {
        if (model == null || !baseModel.head.visible) {
            //If the head model shouldn't show don't bother displaying it
            return;
        }
        poseStack.pushPose();
        baseModel.head.translateAndRotate(poseStack);
        poseStack.translate(0, 0, 0.01);
        model.collect(poseStack, nodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, FoilRendering.ARMOR.foil(stack.hasFoil()), state.outlineColor);
        poseStack.popPose();
    }
}