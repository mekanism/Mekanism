package mekanism.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelTransporterBox extends Model {

    private static final ResourceLocation BOX_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "transporter_box.png");
    private final RenderType RENDER_TYPE = getRenderType(BOX_TEXTURE);
    private final ModelRenderer box;

    public ModelTransporterBox() {
        super(RenderType::getEntityCutoutNoCull);
        textureWidth = 64;
        textureHeight = 64;

        box = new ModelRenderer(this, 0, 0);
        box.addBox(0F, 0F, 0F, 7, 7, 7, false);
        box.setRotationPoint(-3.5F, 0, -3.5F);
        box.setTextureSize(64, 64);
        box.mirror = true;
        setRotation(box, 0F, 0F, 0F);
    }

    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, float x, float y, float z, EnumColor color) {
        matrix.push();
        matrix.translate(x, y, z);
        render(matrix, renderer.getBuffer(RENDER_TYPE), MekanismRenderer.FULL_LIGHT, overlayLight, color.getColor(0), color.getColor(1), color.getColor(2), 1);
        matrix.pop();
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        box.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}