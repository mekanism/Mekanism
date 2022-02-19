package mekanism.client.render.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelScubaTank;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class ScubaTankArmor extends CustomArmor {

    public static final ScubaTankArmor SCUBA_TANK = new ScubaTankArmor(0.5F);
    private static final ModelScubaTank model = new ModelScubaTank();

    private ScubaTankArmor(float size) {
        super(size);
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, float partialTicks, boolean hasEffect,
          LivingEntity entity, ItemStack stack) {
        if (!body.visible) {
            //If the body model shouldn't show don't bother displaying it
            return;
        }
        if (young) {
            matrix.pushPose();
            float f1 = 1.0F / babyBodyScale;
            matrix.scale(f1, f1, f1);
            matrix.translate(0.0D, bodyYOffset / 16.0F, 0.0D);
            renderTank(matrix, renderer, light, overlayLight, hasEffect);
            matrix.popPose();
        } else {
            renderTank(matrix, renderer, light, overlayLight, hasEffect);
        }
    }

    private void renderTank(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect) {
        matrix.pushPose();
        body.translateAndRotate(matrix);
        matrix.translate(0, 0, 0.06);
        model.render(matrix, renderer, light, overlayLight, hasEffect);
        matrix.popPose();
    }
}