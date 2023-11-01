package mekanism.additions.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.additions.client.model.ModelBabyEnderman;
import mekanism.additions.common.entity.baby.EntityBabyEnderman;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public class BabyEndermanHeldBlockLayer extends RenderLayer<EntityBabyEnderman, ModelBabyEnderman> {

    public BabyEndermanHeldBlockLayer(RenderLayerParent<EntityBabyEnderman, ModelBabyEnderman> renderer) {
        super(renderer);
    }

    @Override
    public void render(@NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, EntityBabyEnderman enderman, float limbSwing, float limbSwingAmount,
          float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        BlockState blockstate = enderman.getCarriedBlock();
        if (blockstate != null) {
            matrix.pushPose();
            matrix.translate(0, 0.6875, -0.75);
            matrix.mulPose(Axis.XP.rotationDegrees(20));
            matrix.mulPose(Axis.YP.rotationDegrees(45));
            matrix.translate(0.25, 0.1875, 0.25);
            //Modify scale of block to be 3/4 of what it is for the adult enderman
            float scale = 0.375F;
            matrix.scale(-scale, -scale, scale);
            matrix.mulPose(Axis.YP.rotationDegrees(90));
            //Adjust the position of the block to actually look more like it is in the enderman's hands
            matrix.translate(0, -1, 0.25);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockstate, matrix, renderer, light, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
            matrix.popPose();
        }
    }
}