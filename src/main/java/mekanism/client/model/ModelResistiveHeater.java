package mekanism.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelResistiveHeater extends Model {

    private static final ResourceLocation HEATER_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "resistive_heater.png");
    private static final ResourceLocation OVERLAY_ON = MekanismUtils.getResource(ResourceType.RENDER, "resistive_heater_overlay_on.png");
    private static final ResourceLocation OVERLAY_OFF = MekanismUtils.getResource(ResourceType.RENDER, "resistive_heater_overlay_off.png");
    private static final RenderType RENDER_TYPE_ON = MekanismRenderType.mekStandard(OVERLAY_ON);
    private static final RenderType RENDER_TYPE_OFF = MekanismRenderType.mekStandard(OVERLAY_OFF);

    private final RenderType RENDER_TYPE = getRenderType(HEATER_TEXTURE);

    private final ModelRenderer wallLeft;
    private final ModelRenderer base;
    private final ModelRenderer fin10;
    private final ModelRenderer portRight;
    private final ModelRenderer fin9;
    private final ModelRenderer fin2;
    private final ModelRenderer bar2;
    private final ModelRenderer fin4;
    private final ModelRenderer fin3;
    private final ModelRenderer fin6;
    private final ModelRenderer center;
    private final ModelRenderer fin8;
    private final ModelRenderer fin7;
    private final ModelRenderer fin5;
    private final ModelRenderer fin1;
    private final ModelRenderer bar1;
    private final ModelRenderer bar4;
    private final ModelRenderer bar3;
    private final ModelRenderer wallRight;
    private final ModelRenderer portLeft;

    public ModelResistiveHeater() {
        super(RenderType::entitySolid);
        textureWidth = 128;
        textureHeight = 64;

        wallLeft = new ModelRenderer(this, 0, 23);
        wallLeft.mirror = true;
        wallLeft.addBox(0F, 0F, 0F, 3, 9, 16, false);
        wallLeft.setRotationPoint(5F, 8F, -8F);
        wallLeft.setTextureSize(128, 64);
        setRotation(wallLeft, 0F, 0F, 0F);
        base = new ModelRenderer(this, 0, 0);
        base.addBox(0F, 0F, 0F, 16, 7, 16, false);
        base.setRotationPoint(-8F, 17F, -8F);
        base.setTextureSize(128, 64);
        base.mirror = true;
        setRotation(base, 0F, 0F, 0F);
        fin10 = new ModelRenderer(this, 38, 38);
        fin10.mirror = true;
        fin10.addBox(0F, 0F, 0F, 10, 9, 1, false);
        fin10.setRotationPoint(-5F, 8.5F, 6.5F);
        fin10.setTextureSize(128, 64);
        setRotation(fin10, 0F, 0F, 0F);
        portRight = new ModelRenderer(this, 48, 0);
        portRight.addBox(0F, 0F, 0F, 1, 8, 8, false);
        portRight.setRotationPoint(-8.01F, 12F, -4F);
        portRight.setTextureSize(128, 64);
        portRight.mirror = true;
        setRotation(portRight, 0F, 0F, 0F);
        fin9 = new ModelRenderer(this, 0, 48);
        fin9.mirror = true;
        fin9.addBox(0F, 0F, 0F, 10, 9, 1, false);
        fin9.setRotationPoint(-5F, 8.5F, 5F);
        fin9.setTextureSize(128, 64);
        setRotation(fin9, 0F, 0F, 0F);
        fin2 = new ModelRenderer(this, 0, 48);
        fin2.addBox(0F, 0F, 0F, 10, 9, 1, false);
        fin2.setRotationPoint(-5F, 8.5F, -6F);
        fin2.setTextureSize(128, 64);
        fin2.mirror = true;
        setRotation(fin2, 0F, 0F, 0F);
        bar2 = new ModelRenderer(this, 36, 23);
        bar2.addBox(0F, 0F, 0F, 1, 1, 13, false);
        bar2.setRotationPoint(-2F, 9.5F, -6.5F);
        bar2.setTextureSize(128, 64);
        bar2.mirror = true;
        setRotation(bar2, 0F, 0F, 0F);
        fin4 = new ModelRenderer(this, 0, 48);
        fin4.addBox(0F, 0F, 0F, 10, 9, 1, false);
        fin4.setRotationPoint(-5F, 8.5F, -3F);
        fin4.setTextureSize(128, 64);
        fin4.mirror = true;
        setRotation(fin4, 0F, 0F, 0F);
        fin3 = new ModelRenderer(this, 0, 48);
        fin3.mirror = true;
        fin3.addBox(0F, 0F, 0F, 10, 9, 1, false);
        fin3.setRotationPoint(-5F, 8.5F, -4.5F);
        fin3.setTextureSize(128, 64);
        setRotation(fin3, 0F, 0F, 0F);
        fin6 = new ModelRenderer(this, 0, 48);
        fin6.addBox(0F, 0F, 0F, 10, 9, 1, false);
        fin6.setRotationPoint(-5F, 8.5F, 0.5F);
        fin6.setTextureSize(128, 64);
        fin6.mirror = true;
        setRotation(fin6, 0F, 0F, 0F);
        center = new ModelRenderer(this, 0, 0);
        center.addBox(0F, 0F, 0F, 6, 6, 1, false);
        center.setRotationPoint(-3F, 11.5F, -0.5F);
        center.setTextureSize(128, 64);
        center.mirror = true;
        setRotation(center, 0F, 0F, 0F);
        fin8 = new ModelRenderer(this, 0, 48);
        fin8.addBox(0F, 0F, 0F, 10, 9, 1, false);
        fin8.setRotationPoint(-5F, 8.5F, 3.5F);
        fin8.setTextureSize(128, 64);
        fin8.mirror = true;
        setRotation(fin8, 0F, 0F, 0F);
        fin7 = new ModelRenderer(this, 0, 48);
        fin7.mirror = true;
        fin7.addBox(0F, 0F, 0F, 10, 9, 1, false);
        fin7.setRotationPoint(-5F, 8.5F, 2F);
        fin7.setTextureSize(128, 64);
        setRotation(fin7, 0F, 0F, 0F);
        fin5 = new ModelRenderer(this, 0, 48);
        fin5.mirror = true;
        fin5.addBox(0F, 0F, 0F, 10, 9, 1, false);
        fin5.setRotationPoint(-5F, 8.5F, -1.5F);
        fin5.setTextureSize(128, 64);
        setRotation(fin5, 0F, 0F, 0F);
        fin1 = new ModelRenderer(this, 22, 48);
        fin1.addBox(0F, 0F, 0F, 10, 9, 1, false);
        fin1.setRotationPoint(-5F, 8.5F, -7.5F);
        fin1.setTextureSize(128, 64);
        fin1.mirror = true;
        setRotation(fin1, 0F, 0F, 0F);
        bar1 = new ModelRenderer(this, 36, 23);
        bar1.addBox(0F, 0F, 0F, 1, 1, 13, false);
        bar1.setRotationPoint(-4F, 9.5F, -6.5F);
        bar1.setTextureSize(128, 64);
        bar1.mirror = true;
        setRotation(bar1, 0F, 0F, 0F);
        bar4 = new ModelRenderer(this, 36, 23);
        bar4.addBox(0F, 0F, 0F, 1, 1, 13, false);
        bar4.setRotationPoint(3F, 9.5F, -6.5F);
        bar4.setTextureSize(128, 64);
        bar4.mirror = true;
        setRotation(bar4, 0F, 0F, 0F);
        bar3 = new ModelRenderer(this, 36, 23);
        bar3.addBox(0F, 0F, 0F, 1, 1, 13, false);
        bar3.setRotationPoint(1F, 9.5F, -6.5F);
        bar3.setTextureSize(128, 64);
        bar3.mirror = true;
        setRotation(bar3, 0F, 0F, 0F);
        wallRight = new ModelRenderer(this, 0, 23);
        wallRight.addBox(0F, 0F, 0F, 3, 9, 16, false);
        wallRight.setRotationPoint(-8F, 8F, -8F);
        wallRight.setTextureSize(128, 64);
        wallRight.mirror = true;
        setRotation(wallRight, 0F, 0F, 0F);
        portLeft = new ModelRenderer(this, 48, 0);
        portLeft.addBox(0F, 0F, 0F, 1, 8, 8, false);
        portLeft.setRotationPoint(7.01F, 12F, -4F);
        portLeft.setTextureSize(128, 64);
        portLeft.mirror = true;
        setRotation(portLeft, 0F, 0F, 0F);
    }

    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean on) {
        render(matrix, renderer.getBuffer(RENDER_TYPE), light, overlayLight, 1, 1, 1, 1);
        matrix.push();
        matrix.scale(1.001F, 1.001F, 1.001F);
        matrix.translate(0, -0.0011, 0);
        render(matrix, renderer.getBuffer(on ? RENDER_TYPE_ON : RENDER_TYPE_OFF), MekanismRenderer.FULL_LIGHT, overlayLight, 1, 1, 1, 1);
        matrix.pop();
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue,
          float alpha) {
        wallLeft.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        base.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fin10.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        portRight.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fin9.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fin2.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        bar2.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fin4.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fin3.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fin6.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        center.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fin8.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fin7.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fin5.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fin1.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        bar1.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        bar4.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        bar3.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        wallRight.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        portLeft.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}