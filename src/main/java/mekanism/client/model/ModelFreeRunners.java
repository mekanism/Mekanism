package mekanism.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
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
        super(RenderType::func_228634_a_);
        textureWidth = 64;
        textureHeight = 32;

        SpringL = new ModelRenderer(this, 8, 0);
        SpringL.func_228304_a_(1.5F, 18F, 0F, 1, 6, 1, false);
        SpringL.setRotationPoint(0F, 0F, 0F);
        SpringL.setTextureSize(64, 32);
        SpringL.mirror = true;
        setRotation(SpringL, 0.1047198F, 0F, 0F);
        SpringR = new ModelRenderer(this, 8, 0);
        SpringR.func_228304_a_(-2.5F, 18F, 0F, 1, 6, 1, false);
        SpringR.setRotationPoint(0F, 0F, 0F);
        SpringR.setTextureSize(64, 32);
        SpringR.mirror = true;
        setRotation(SpringR, 0.1047198F, 0F, 0F);
        SpringR.mirror = false;
        BraceL = new ModelRenderer(this, 12, 0);
        BraceL.func_228304_a_(0.2F, 18F, -0.8F, 4, 2, 3, false);
        BraceL.setRotationPoint(0F, 0F, 0F);
        BraceL.setTextureSize(64, 32);
        BraceL.mirror = true;
        setRotation(BraceL, 0F, 0F, 0F);
        BraceR = new ModelRenderer(this, 12, 0);
        BraceR.func_228304_a_(-4.2F, 18F, -0.8F, 4, 2, 3, false);
        BraceR.setRotationPoint(0F, 0F, 0F);
        BraceR.setTextureSize(64, 32);
        BraceR.mirror = true;
        setRotation(BraceR, 0F, 0F, 0F);
        SupportL = new ModelRenderer(this, 0, 0);
        SupportL.func_228304_a_(1F, 16.5F, -4.2F, 2, 4, 2, false);
        SupportL.setRotationPoint(0F, 0F, 0F);
        SupportL.setTextureSize(64, 32);
        SupportL.mirror = true;
        setRotation(SupportL, 0.296706F, 0F, 0F);
        SupportR = new ModelRenderer(this, 0, 0);
        SupportR.func_228304_a_(-3F, 16.5F, -4.2F, 2, 4, 2, false);
        SupportR.setRotationPoint(0F, 0F, 0F);
        SupportR.setTextureSize(64, 32);
        SupportR.mirror = true;
        setRotation(SupportR, 0.296706F, 0F, 0F);
        SupportR.mirror = false;
    }

    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        func_225598_a_(matrix, renderer.getBuffer(RENDER_TYPE), light, otherLight, 1, 1, 1, 1);
    }

    @Override
    public void func_225598_a_(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int otherLight, float red, float green, float blue, float alpha) {
        renderLeft(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        renderRight(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
    }

    public void renderLeft(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int otherLight, float red, float green, float blue, float alpha) {
        SpringL.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        BraceL.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        SupportL.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
    }

    public void renderRight(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int otherLight, float red, float green, float blue, float alpha) {
        SpringR.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        BraceR.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        SupportR.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}