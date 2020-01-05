package mekanism.client.render.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelScubaTank;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class ScubaTankArmor extends CustomArmor {

    public static final ScubaTankArmor SCUBA_TANK = new ScubaTankArmor(0.5F);
    private static final ModelScubaTank model = new ModelScubaTank();

    private ScubaTankArmor(float size) {
        super(size);
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect) {
        if (!bipedBody.showModel) {
            //If the body model shouldn't show don't bother displaying it
            return;
        }
        if (isChild) {
            matrix.func_227860_a_();
            float f1 = 1.0F / field_228225_h_;
            matrix.func_227862_a_(f1, f1, f1);
            matrix.func_227861_a_(0.0D, field_228226_i_ / 16.0F, 0.0D);
            renderTank(matrix, renderer, light, overlayLight, hasEffect);
            matrix.func_227865_b_();
        } else {
            renderTank(matrix, renderer, light, overlayLight, hasEffect);
        }
    }

    private void renderTank(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect) {
        matrix.func_227860_a_();
        bipedBody.func_228307_a_(matrix);
        matrix.func_227861_a_(0, 0, 0.06);
        model.render(matrix, renderer, light, overlayLight, hasEffect);
        matrix.func_227865_b_();
    }
}