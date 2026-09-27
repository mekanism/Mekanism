package mekanism.additions.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CarriedBlockLayer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;

public class BabyEndermanCarriedBlockLayer extends CarriedBlockLayer {

    public BabyEndermanCarriedBlockLayer(RenderLayerParent<EndermanRenderState, EndermanModel<EndermanRenderState>> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, EndermanRenderState state, float yRot, float xRot) {
        BlockModelRenderState carriedBlock = state.carriedBlock;
        if (!carriedBlock.isEmpty()) {
            poseStack.pushPose();
            //Slightly shift the initial translation compared to what vanilla does for the carried block layer
            poseStack.translate(0.0F, 0.3125F, 0.25F);
            super.submit(poseStack, submitNodeCollector, lightCoords, state, yRot, xRot);
            poseStack.popPose();
        }
    }
}