package mekanism.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelFreeRunners extends Model {

    private static final ResourceLocation FREE_RUNNER_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "free_runners.png");
    private final RenderType RENDER_TYPE = func_228282_a_(FREE_RUNNER_TEXTURE);

    private final ModelRenderer SpringL;
    private final ModelRenderer SpringR;
    private final ModelRenderer BraceL;
    private final ModelRenderer BraceR;
    private final ModelRenderer SupportL;
    private final ModelRenderer SupportR;

    public ModelFreeRunners() {
        super(RenderType::entitySolid);
        textureWidth = 64;
        textureHeight = 32;

        SpringL = new ModelRenderer(this, 8, 0);
        SpringL.addBox(1.5F, 18F, 0F, 1, 6, 1, false);
        SpringL.setRotationPoint(0F, 0F, 0F);
        SpringL.setTextureSize(64, 32);
        SpringL.mirror = true;
        setRotation(SpringL, 0.1047198F, 0F, 0F);
        SpringR = new ModelRenderer(this, 8, 0);
        SpringR.addBox(-2.5F, 18F, 0F, 1, 6, 1, false);
        SpringR.setRotationPoint(0F, 0F, 0F);
        SpringR.setTextureSize(64, 32);
        SpringR.mirror = true;
        setRotation(SpringR, 0.1047198F, 0F, 0F);
        SpringR.mirror = false;
        BraceL = new ModelRenderer(this, 12, 0);
        BraceL.addBox(0.2F, 18F, -0.8F, 4, 2, 3, false);
        BraceL.setRotationPoint(0F, 0F, 0F);
        BraceL.setTextureSize(64, 32);
        BraceL.mirror = true;
        setRotation(BraceL, 0F, 0F, 0F);
        BraceR = new ModelRenderer(this, 12, 0);
        BraceR.addBox(-4.2F, 18F, -0.8F, 4, 2, 3, false);
        BraceR.setRotationPoint(0F, 0F, 0F);
        BraceR.setTextureSize(64, 32);
        BraceR.mirror = true;
        setRotation(BraceR, 0F, 0F, 0F);
        SupportL = new ModelRenderer(this, 0, 0);
        SupportL.addBox(1F, 16.5F, -4.2F, 2, 4, 2, false);
        SupportL.setRotationPoint(0F, 0F, 0F);
        SupportL.setTextureSize(64, 32);
        SupportL.mirror = true;
        setRotation(SupportL, 0.296706F, 0F, 0F);
        SupportR = new ModelRenderer(this, 0, 0);
        SupportR.addBox(-3F, 16.5F, -4.2F, 2, 4, 2, false);
        SupportR.setRotationPoint(0F, 0F, 0F);
        SupportR.setTextureSize(64, 32);
        SupportR.mirror = true;
        setRotation(SupportR, 0.296706F, 0F, 0F);
        SupportR.mirror = false;
    }

    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect) {
        render(matrix, getVertexBuilder(renderer, hasEffect), light, overlayLight, 1, 1, 1, 1);
    }

    private IVertexBuilder getVertexBuilder(@Nonnull IRenderTypeBuffer renderer, boolean hasEffect) {
        return ItemRenderer.func_229113_a_(renderer, RENDER_TYPE, false, hasEffect);
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        SpringL.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        BraceL.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        SupportL.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        SpringR.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        BraceR.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        SupportR.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }

    public void renderLeg(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect, boolean left) {
        IVertexBuilder vertexBuilder = getVertexBuilder(renderer, hasEffect);
        float red = 1;
        float green = 1;
        float blue = 1;
        float alpha = 1;
        if (left) {
            SpringL.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            BraceL.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            SupportL.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        } else {
            SpringR.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            BraceR.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
            SupportR.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        }
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}