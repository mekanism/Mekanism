package mekanism.client.render.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelScubaMask;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class ScubaMaskArmor extends CustomArmor {

    public static final ScubaMaskArmor SCUBA_MASK = new ScubaMaskArmor(0.5F);
    private static final ModelScubaMask model = new ModelScubaMask();

    private ScubaMaskArmor(float size) {
        super(size);
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, float partialTicks, boolean hasEffect,
          LivingEntity entity, ItemStack stack) {
        if (!bipedHead.showModel) {
            //If the head model shouldn't show don't bother displaying it
            return;
        }
        if (isChild) {
            matrix.push();
            if (isChildHeadScaled) {
                float f = 1.5F / childHeadScale;
                matrix.scale(f, f, f);
            }
            matrix.translate(0.0D, childHeadOffsetY / 16.0F, childHeadOffsetZ / 16.0F);
            renderMask(matrix, renderer, light, overlayLight, hasEffect);
            matrix.pop();
        } else {
            renderMask(matrix, renderer, light, overlayLight, hasEffect);
        }
    }

    private void renderMask(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect) {
        matrix.push();
        bipedHead.translateRotate(matrix);
        matrix.translate(0, 0, 0.01);
        model.render(matrix, renderer, light, overlayLight, hasEffect);
        matrix.pop();
    }
}