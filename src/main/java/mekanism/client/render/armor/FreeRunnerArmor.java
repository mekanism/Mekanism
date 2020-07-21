package mekanism.client.render.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelFreeRunners;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class FreeRunnerArmor extends CustomArmor {

    public static final FreeRunnerArmor FREE_RUNNERS = new FreeRunnerArmor(0.5F);
    private static final ModelFreeRunners model = new ModelFreeRunners();

    private FreeRunnerArmor(float size) {
        super(size);
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect, LivingEntity entity,
          ItemStack stack) {
        if (isChild) {
            matrix.push();
            float f1 = 1.0F / childBodyScale;
            matrix.scale(f1, f1, f1);
            matrix.translate(0.0D, childBodyOffsetY / 16.0F, 0.0D);
            renderLeg(matrix, renderer, light, overlayLight, hasEffect, true);
            renderLeg(matrix, renderer, light, overlayLight, hasEffect, false);
            matrix.pop();
        } else {
            renderLeg(matrix, renderer, light, overlayLight, hasEffect, true);
            renderLeg(matrix, renderer, light, overlayLight, hasEffect, false);
        }
    }

    private void renderLeg(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect, boolean left) {
        if (left && !bipedLeftLeg.showModel || !left && !bipedRightLeg.showModel) {
            //If the model isn't meant to be shown don't bother rendering it
            return;
        }
        matrix.push();
        if (left) {
            bipedLeftLeg.translateRotate(matrix);
        } else {
            bipedRightLeg.translateRotate(matrix);
        }
        matrix.translate(0, 0, 0.06);
        matrix.scale(1.02F, 1.02F, 1.02F);
        matrix.translate(left ? -0.1375 : 0.1375, -0.75, -0.0625);
        model.renderLeg(matrix, renderer, light, overlayLight, hasEffect, left);
        matrix.pop();
    }
}