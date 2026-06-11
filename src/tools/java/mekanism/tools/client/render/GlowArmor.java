package mekanism.tools.client.render;
//TODO - 26.1 glow armor model
/*
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.LightCoordsUtil;

public class GlowArmor extends Model {

    private static final GlowArmor WRAPPER = new GlowArmor();
    private HumanoidModel<?> base;

    private GlowArmor() {
        super(RenderTypes::entityCutoutNoCull);
    }

    public static GlowArmor wrap(HumanoidModel<?> base) {
        WRAPPER.base = base;
        return WRAPPER;
    }

    @Override
    public void renderToBuffer(PoseStack matrix, VertexConsumer vertexBuilder, int light, int overlayLight, int color) {
        if (base != null) {
            //Make it render at full brightness
            base.renderToBuffer(matrix, vertexBuilder, LightCoordsUtil.FULL_BRIGHT, overlayLight, color);
        }
    }
}*/