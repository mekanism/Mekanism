package mekanism.client.render.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelGasMask;
import mekanism.client.render.ModelCustomArmor;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class GasMaskArmor extends ModelCustomArmor {

    public static final GasMaskArmor GAS_MASK = new GasMaskArmor(0.5F);
    private static final ModelGasMask model = new ModelGasMask();

    private GasMaskArmor(float size) {
        super(size);
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect) {
        if (!bipedHead.showModel) {
            //If the head model shouldn't show don't bother displaying it
            return;
        }
        if (isChild) {
            matrix.func_227860_a_();
            if (field_228221_a_) {
                float f = 1.5F / field_228224_g_;
                matrix.func_227862_a_(f, f, f);
            }
            matrix.func_227861_a_(0.0D, field_228222_b_ / 16.0F, field_228223_f_ / 16.0F);
            renderMask(matrix, renderer, light, overlayLight, hasEffect);
            matrix.func_227865_b_();
        } else {
            renderMask(matrix, renderer, light, overlayLight, hasEffect);
        }
    }

    private void renderMask(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect) {
        matrix.func_227860_a_();
        bipedHead.func_228307_a_(matrix);
        matrix.func_227861_a_(0, 0, 0.01);
        model.render(matrix, renderer, light, overlayLight, hasEffect);
        matrix.func_227865_b_();
    }
}