package mekanism.additions.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class BabyEndermanHeldBlockLayer extends RenderLayer<EndermanRenderState, EndermanModel<EndermanRenderState>> {

    public BabyEndermanHeldBlockLayer(RenderLayerParent<EndermanRenderState, EndermanModel<EndermanRenderState>> renderer) {
        super(renderer);
    }

    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, EndermanRenderState state, float yRot, float xRot) {
        BlockModelRenderState carriedBlock = state.carriedBlock;
        if (!carriedBlock.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.6875F, -0.75F);
            poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.translate(0.25F, 0.1875F, 0.25F);
            //Modify scale of block to be 3/4 of what it is for the adult enderman
            float scale = 0.375F;
            poseStack.scale(-scale, -scale, scale);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            carriedBlock.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
    }
}