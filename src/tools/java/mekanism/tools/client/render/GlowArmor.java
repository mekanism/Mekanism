package mekanism.tools.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;

public class GlowArmor extends Model {

    private static final GlowArmor WRAPPER = new GlowArmor();
    private HumanoidModel<?> base;

    private GlowArmor() {
        super(RenderType::entityCutoutNoCull);
    }

    public static GlowArmor wrap(HumanoidModel<?> base) {
        WRAPPER.base = base;
        return WRAPPER;
    }

    @Override
    public void renderToBuffer(@Nonnull PoseStack matrix, @Nonnull VertexConsumer vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        if (base != null) {
            //Make it render at full brightness
            base.renderToBuffer(matrix, vertexBuilder, MekanismRenderer.FULL_LIGHT, overlayLight, red, green, blue, alpha);
        }
    }
}