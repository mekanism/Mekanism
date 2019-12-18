package mekanism.generators.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.lwjgl.opengl.GL11;

public class ModelBioGenerator extends Model {

    private final ModelRenderer base;
    private final ModelRenderer sideRight;
    private final ModelRenderer back;
    private final ModelRenderer bar;
    private final ModelRenderer glass;
    private final ModelRenderer sideLeft;

    public ModelBioGenerator() {
        //TODO: 1.15 Check if this is the proper render type to use
        super(RenderType::func_228634_a_);
        textureWidth = 64;
        textureHeight = 64;

        base = new ModelRenderer(this, 0, 0);
        base.func_228304_a_(0F, 0F, 0F, 16, 7, 16, false);
        base.setRotationPoint(-8F, 17F, -8F);
        base.setTextureSize(64, 64);
        base.mirror = true;
        setRotation(base, 0F, 0F, 0F);
        sideRight = new ModelRenderer(this, 0, 40);
        sideRight.func_228304_a_(0F, 0F, 0F, 3, 9, 8, false);
        sideRight.setRotationPoint(5F, 8F, -8F);
        sideRight.setTextureSize(64, 64);
        setRotation(sideRight, 0F, 0F, 0F);
        back = new ModelRenderer(this, 0, 23);
        back.func_228304_a_(0F, 0F, 0F, 16, 9, 8, false);
        back.setRotationPoint(-8F, 8F, 0F);
        back.setTextureSize(64, 64);
        back.mirror = true;
        setRotation(back, 0F, 0F, 0F);
        bar = new ModelRenderer(this, 0, 57);
        bar.func_228304_a_(0F, 0F, 0F, 10, 1, 1, false);
        bar.setRotationPoint(-5F, 8.5F, -7.5F);
        bar.setTextureSize(64, 64);
        bar.mirror = true;
        setRotation(bar, 0F, 0F, 0F);
        glass = new ModelRenderer(this, 22, 40);
        glass.func_228304_a_(0F, 0F, 0F, 12, 8, 7, false);
        glass.setRotationPoint(-6F, 9F, -7F);
        glass.setTextureSize(64, 64);
        glass.mirror = true;
        setRotation(glass, 0F, 0F, 0F);
        sideLeft = new ModelRenderer(this, 0, 40);
        sideLeft.func_228304_a_(0F, 0F, 0F, 3, 9, 8, false);
        sideLeft.setRotationPoint(-8F, 8F, -8F);
        sideLeft.setTextureSize(64, 64);
        sideLeft.mirror = true;
        setRotation(sideLeft, 0F, 0F, 0F);
    }

    @Override
    public void func_225598_a_(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int otherLight, float red, float green, float blue, float alpha) {
        base.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        sideRight.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        sideLeft.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        back.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        bar.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);

        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        RenderSystem.disableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        glass.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}