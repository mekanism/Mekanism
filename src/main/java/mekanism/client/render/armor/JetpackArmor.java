package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.ModelArmoredJetpack;
import mekanism.client.model.ModelJetpack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class JetpackArmor implements ICustomArmor, ResourceManagerReloadListener {

    public static final JetpackArmor JETPACK = new JetpackArmor(false);
    public static final JetpackArmor ARMORED_JETPACK = new JetpackArmor(true);

    private final boolean armored;
    @Nullable
    private ModelJetpack model;

    private JetpackArmor(boolean armored) {
        this.armored = armored;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        if (armored) {
            model = new ModelArmoredJetpack(Minecraft.getInstance().getEntityModels());
        } else {
            model = new ModelJetpack(Minecraft.getInstance().getEntityModels());
        }
    }

    @Override
    public <STATE extends HumanoidRenderState> void render(HumanoidModel<STATE> baseModel, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords,
          STATE state, ItemStack stack) {
        if (model == null || !baseModel.body.visible) {
            //If the body model shouldn't show don't bother displaying it
            return;
        }
        poseStack.pushPose();
        baseModel.body.translateAndRotate(poseStack);
        poseStack.translate(0, 0, 0.06);
        model.collect(poseStack, nodeCollector, lightCoords, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}