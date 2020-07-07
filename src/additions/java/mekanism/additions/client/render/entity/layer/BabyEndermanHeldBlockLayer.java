package mekanism.additions.client.render.entity.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.additions.client.model.ModelBabyEnderman;
import mekanism.additions.common.entity.baby.EntityBabyEnderman;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.math.vector.Vector3f;

public class BabyEndermanHeldBlockLayer extends LayerRenderer<EntityBabyEnderman, ModelBabyEnderman> {

    public BabyEndermanHeldBlockLayer(IEntityRenderer<EntityBabyEnderman, ModelBabyEnderman> renderer) {
        super(renderer);
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, EntityBabyEnderman enderman, float limbSwing, float limbSwingAmount,
          float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        BlockState blockstate = enderman.getHeldBlockState();
        if (blockstate != null) {
            matrix.push();
            matrix.translate(0, 0.6875, -0.75);
            matrix.rotate(Vector3f.XP.rotationDegrees(20));
            matrix.rotate(Vector3f.YP.rotationDegrees(45));
            matrix.translate(0.25, 0.1875, 0.25);
            //Modify scale of block to be 3/4 of what it is for the adult enderman
            float scale = 0.375F;
            matrix.scale(-scale, -scale, scale);
            matrix.rotate(Vector3f.YP.rotationDegrees(90));
            //Adjust the position of the block to actually look more like it is in the enderman's hands
            matrix.translate(0, -1, 0.25);
            Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(blockstate, matrix, renderer, light, OverlayTexture.NO_OVERLAY);
            matrix.pop();
        }
    }
}