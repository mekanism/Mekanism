package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelWindGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class RenderWindGenerator extends MekanismTileEntityRenderer<TileEntityWindGenerator> {

    private static final ResourceLocation GENERATOR_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "wind_generator.png");
    private static final ModelWindGenerator model = new ModelWindGenerator();
    private static final RenderType RENDER_TYPE = model.func_228282_a_(GENERATOR_TEXTURE);

    @Override
    public void func_225616_a_(@Nonnull TileEntityWindGenerator tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light,
          int otherLight) {
        matrix.func_227860_a_();
        matrix.func_227861_a_(0.5F, 1.5F, 0.5F);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.func_227863_a_(Vector3f.field_229183_f_.func_229187_a_(180));
        double angle = tile.getAngle();
        if (tile.getActive()) {
            angle = (tile.getAngle() + ((tile.getPos().getY() + 4F) / TileEntityWindGenerator.SPEED_SCALED) * partialTick) % 360;
        }
        IVertexBuilder vertexBuilder = renderer.getBuffer(RENDER_TYPE);
        model.render(matrix, vertexBuilder, angle, light, OverlayTexture.field_229196_a_, 1, 1, 1, 1);
        matrix.func_227865_b_();
    }

    @Override
    public boolean isGlobalRenderer(TileEntityWindGenerator tile) {
        return true;
    }
}