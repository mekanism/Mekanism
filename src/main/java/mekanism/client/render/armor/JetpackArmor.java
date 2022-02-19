package mekanism.client.render.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelArmoredJetpack;
import mekanism.client.model.ModelJetpack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class JetpackArmor extends CustomArmor {

    public static final JetpackArmor JETPACK = new JetpackArmor(0.5F, false);
    public static final JetpackArmor ARMORED_JETPACK = new JetpackArmor(0.5F, true);
    private static final ModelJetpack model = new ModelJetpack();
    private static final ModelArmoredJetpack armoredModel = new ModelArmoredJetpack();

    private final boolean armored;

    private JetpackArmor(float size, boolean armored) {
        super(size);
        this.armored = armored;
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
            renderJetpack(matrix, renderer, light, overlayLight, hasEffect);
            matrix.popPose();
        } else {
            renderJetpack(matrix, renderer, light, overlayLight, hasEffect);
        }
    }

    private void renderJetpack(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect) {
        matrix.pushPose();
        body.translateAndRotate(matrix);
        matrix.translate(0, 0, 0.06);
        if (armored) {
            armoredModel.render(matrix, renderer, light, overlayLight, hasEffect);
        } else {
            model.render(matrix, renderer, light, overlayLight, hasEffect);
        }
        matrix.popPose();
    }
}