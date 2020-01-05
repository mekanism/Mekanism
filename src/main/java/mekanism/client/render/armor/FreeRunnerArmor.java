package mekanism.client.render.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.render.ModelCustomArmor;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class FreeRunnerArmor extends ModelCustomArmor {

    public static final FreeRunnerArmor FREE_RUNNERS = new FreeRunnerArmor(0.5F);
    private static final ModelFreeRunners model = new ModelFreeRunners();

    private FreeRunnerArmor(float size) {
        super(size);
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect) {
        if (isChild) {
            matrix.func_227860_a_();
            float f1 = 1.0F / field_228225_h_;
            matrix.func_227862_a_(f1, f1, f1);
            matrix.func_227861_a_(0.0D, field_228226_i_ / 16.0F, 0.0D);
            renderLeg(matrix, renderer, light, overlayLight, hasEffect, true);
            renderLeg(matrix, renderer, light, overlayLight, hasEffect, false);
            matrix.func_227865_b_();
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
        matrix.func_227860_a_();
        if (left) {
            bipedLeftLeg.func_228307_a_(matrix);
        } else {
            bipedRightLeg.func_228307_a_(matrix);
        }
        matrix.func_227861_a_(0, 0, 0.06);
        matrix.func_227862_a_(1.02F, 1.02F, 1.02F);
        matrix.func_227861_a_(left ? -0.1375 : 0.1375, -0.75, -0.0625);
        model.renderLeg(matrix, renderer, light, overlayLight, hasEffect, left);
        matrix.func_227865_b_();
    }
}