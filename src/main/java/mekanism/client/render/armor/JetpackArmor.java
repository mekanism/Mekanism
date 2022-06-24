package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.ModelArmoredJetpack;
import mekanism.client.model.ModelJetpack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class JetpackArmor implements ICustomArmor, ResourceManagerReloadListener {

    public static final JetpackArmor JETPACK = new JetpackArmor(false);
    public static final JetpackArmor ARMORED_JETPACK = new JetpackArmor(true);

    private final boolean armored;
    private ModelJetpack model;

    private JetpackArmor(boolean armored) {
        this.armored = armored;
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        if (armored) {
            model = new ModelArmoredJetpack(Minecraft.getInstance().getEntityModels());
        } else {
            model = new ModelJetpack(Minecraft.getInstance().getEntityModels());
        }
    }

    @Override
    public void render(HumanoidModel<? extends LivingEntity> baseModel, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer,
          int light, int overlayLight, float partialTicks, boolean hasEffect, LivingEntity entity, ItemStack stack) {
        if (!baseModel.body.visible) {
            //If the body model shouldn't show don't bother displaying it
            return;
        }
        if (baseModel.young) {
            matrix.pushPose();
            float f1 = 1.0F / baseModel.babyBodyScale;
            matrix.scale(f1, f1, f1);
            matrix.translate(0.0D, baseModel.bodyYOffset / 16.0F, 0.0D);
            renderJetpack(baseModel, matrix, renderer, light, overlayLight, hasEffect);
            matrix.popPose();
        } else {
            renderJetpack(baseModel, matrix, renderer, light, overlayLight, hasEffect);
        }
    }

    private void renderJetpack(HumanoidModel<? extends LivingEntity> baseModel, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light,
          int overlayLight, boolean hasEffect) {
        matrix.pushPose();
        baseModel.body.translateAndRotate(matrix);
        matrix.translate(0, 0, 0.06);
        model.render(matrix, renderer, light, overlayLight, hasEffect);
        matrix.popPose();
    }
}