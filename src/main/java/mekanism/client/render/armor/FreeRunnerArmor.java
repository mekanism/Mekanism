package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelArmoredFreeRunners;
import mekanism.client.model.ModelFreeRunners;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class FreeRunnerArmor implements ICustomArmor, ResourceManagerReloadListener {

    public static final FreeRunnerArmor FREE_RUNNERS = new FreeRunnerArmor(false);
    public static final FreeRunnerArmor ARMORED_FREE_RUNNERS = new FreeRunnerArmor(true);

    private final boolean armored;
    private ModelFreeRunners model;

    private FreeRunnerArmor(boolean armored) {
        this.armored = armored;
    }

    @Override
    public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {
        if (armored) {
            model = new ModelArmoredFreeRunners(Minecraft.getInstance().getEntityModels());
        } else {
            model = new ModelFreeRunners(Minecraft.getInstance().getEntityModels());
        }
    }

    @Override
    public void render(HumanoidModel<? extends LivingEntity> baseModel, @Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer,
          int light, int overlayLight, float partialTicks, boolean hasEffect, LivingEntity entity, ItemStack stack) {
        if (baseModel.young) {
            matrix.pushPose();
            float f1 = 1.0F / baseModel.babyBodyScale;
            matrix.scale(f1, f1, f1);
            matrix.translate(0.0D, baseModel.bodyYOffset / 16.0F, 0.0D);
            renderLeg(baseModel, matrix, renderer, light, overlayLight, hasEffect, true);
            renderLeg(baseModel, matrix, renderer, light, overlayLight, hasEffect, false);
            matrix.popPose();
        } else {
            renderLeg(baseModel, matrix, renderer, light, overlayLight, hasEffect, true);
            renderLeg(baseModel, matrix, renderer, light, overlayLight, hasEffect, false);
        }
    }

    private void renderLeg(HumanoidModel<? extends LivingEntity> baseModel, @Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light,
          int overlayLight, boolean hasEffect, boolean left) {
        if (left && !baseModel.leftLeg.visible || !left && !baseModel.rightLeg.visible) {
            //If the model isn't meant to be shown don't bother rendering it
            return;
        }
        matrix.pushPose();
        if (left) {
            baseModel.leftLeg.translateAndRotate(matrix);
        } else {
            baseModel.rightLeg.translateAndRotate(matrix);
        }
        matrix.translate(0, 0, 0.06);
        matrix.scale(1.02F, 1.02F, 1.02F);
        matrix.translate(left ? -0.1375 : 0.1375, -0.75, -0.0625);
        model.renderLeg(matrix, renderer, light, overlayLight, hasEffect, left);
        matrix.popPose();
    }
}