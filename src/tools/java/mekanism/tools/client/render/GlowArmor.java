package mekanism.tools.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.LightCoordsUtil;

public class GlowArmor<STATE extends HumanoidRenderState> extends Model<STATE> {

    private final HumanoidModel<STATE> base;

    public GlowArmor(HumanoidModel<STATE> base) {
        super(base.root(), base.renderType());
        this.base = base;
    }

    @Override
    public void setupAnim(STATE state) {
        base.setupAnim(state);
    }

    @Override
    public final void renderToBuffer(PoseStack matrix, VertexConsumer vertexBuilder, int light, int overlayLight, int color) {
        //Make it render at full brightness
        base.renderToBuffer(matrix, vertexBuilder, LightCoordsUtil.FULL_BRIGHT, overlayLight, color);
    }
}