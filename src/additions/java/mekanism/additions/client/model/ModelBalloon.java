package mekanism.additions.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelBalloon extends Model {

    private final ModelRenderer Balloon2;
    private final ModelRenderer Balloon1;
    private final ModelRenderer Balloon3;
    private final ModelRenderer Balloonnub;
    private final ModelRenderer String;

    public ModelBalloon() {
        //TODO: 1.15 test
        super(RenderType::func_228634_a_);
        textureWidth = 64;
        textureHeight = 32;

        Balloon2 = new ModelRenderer(this, 0, 0);
        Balloon2.func_228304_a_(-2.5F, -2, -2, 5, 4, 4, false);
        Balloon2.setRotationPoint(0F, 0F, 0F);
        Balloon2.setTextureSize(64, 32);
        Balloon2.mirror = true;
        setRotation(Balloon2, 0F, 0F, 0F);
        Balloon1 = new ModelRenderer(this, 0, 8);
        Balloon1.func_228304_a_(-2F, -2F, -2.5F, 4, 4, 5, false);
        Balloon1.setRotationPoint(0F, 0F, 0F);
        Balloon1.setTextureSize(64, 32);
        Balloon1.mirror = true;
        setRotation(Balloon1, 0F, 0F, 0F);
        Balloon3 = new ModelRenderer(this, 18, 0);
        Balloon3.func_228304_a_(-2F, -2.5F, -2F, 4, 5, 4, false);
        Balloon3.setRotationPoint(0F, 0F, 0F);
        Balloon3.setTextureSize(64, 32);
        Balloon3.mirror = true;
        setRotation(Balloon3, 0F, 0F, 0F);
        Balloonnub = new ModelRenderer(this, 18, 9);
        Balloonnub.func_228304_a_(-0.5F, 2.5F, -0.5F, 1, 1, 1, false);
        Balloonnub.setRotationPoint(0F, 0F, 0F);
        Balloonnub.setTextureSize(64, 32);
        Balloonnub.mirror = true;
        setRotation(Balloonnub, 0F, 0F, 0F);
        String = new ModelRenderer(this, 34, 0);
        String.func_228304_a_(-0.5F, 3.5F, -0.5F, 1, 11, 1, false);
        String.setRotationPoint(0F, 0F, 0F);
        String.setTextureSize(64, 32);
        String.mirror = true;
        setRotation(String, 0F, 0F, 0F);
    }

    @Override
    public void func_225598_a_(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int otherLight, float red, float green, float blue, float alpha) {
        //public void render(float size, EnumColor color) {
        RenderSystem.pushMatrix();
        //MekanismRenderer.color(color);
        RenderSystem.color4f(red, green, blue, alpha);
        RenderSystem.scalef(1.5F, 1.5F, 1.5F);
        RenderSystem.translatef(0, -0.07F, 0);

        Balloon2.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        Balloon1.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        Balloon3.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        Balloonnub.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);

        MekanismRenderer.resetColor();
        RenderSystem.popMatrix();

        RenderSystem.pushMatrix();
        RenderSystem.scalef(0.2F, 1, 0.2F);
        String.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        RenderSystem.popMatrix();
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}