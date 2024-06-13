package mekanism.tools.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;

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
    public void renderToBuffer(@NotNull PoseStack matrix, @NotNull VertexConsumer vertexBuilder, int light, int overlayLight, int color) {
        if (base != null) {
            //Make it render at full brightness
            base.renderToBuffer(matrix, vertexBuilder, LightTexture.FULL_BRIGHT, overlayLight, color);
        }
    }
}