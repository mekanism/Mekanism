package mekanism.generators.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelTurbine extends Model {

    private static float BLADE_ROTATE = 0.418879F;

    private final ModelRenderer rod;
    private final ModelRenderer extension_north;
    private final ModelRenderer blade_north;
    private final ModelRenderer extension_south;
    private final ModelRenderer extension_west;
    private final ModelRenderer extension_east;
    private final ModelRenderer blade_south;
    private final ModelRenderer blade_east;
    private final ModelRenderer blade_west;

    public ModelTurbine() {
        //TODO: 1.15 Check if this is the proper render type to use
        super(RenderType::func_228634_a_);
        textureWidth = 64;
        textureHeight = 64;
        extension_south = new ModelRenderer(this, 0, 0);
        extension_south.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_south.func_228301_a_(-1.0F, 0.0F, 1.0F, 2, 1, 3, 0.0F);
        setRotateAngle(extension_south, 0.0F, 0.0F, -BLADE_ROTATE);
        extension_west = new ModelRenderer(this, 0, 4);
        extension_west.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_west.func_228301_a_(-4.0F, 0.0F, -1.0F, 3, 1, 2, 0.0F);
        setRotateAngle(extension_west, BLADE_ROTATE, 0.0F, 0.0F);
        blade_east = new ModelRenderer(this, 10, 5);
        blade_east.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_east.func_228301_a_(4.0F, 0.0F, -1.5F, 4, 1, 3, 0.0F);
        setRotateAngle(blade_east, -BLADE_ROTATE, 0.0F, 0.0F);
        blade_north = new ModelRenderer(this, 10, 0);
        blade_north.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_north.func_228301_a_(-1.5F, 0.0F, -8.0F, 3, 1, 4, 0.0F);
        setRotateAngle(blade_north, 0.0F, 0.0F, BLADE_ROTATE);
        extension_east = new ModelRenderer(this, 0, 4);
        extension_east.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_east.func_228301_a_(1.0F, 0.0F, -1.0F, 3, 1, 2, 0.0F);
        setRotateAngle(extension_east, -BLADE_ROTATE, 0.0F, 0.0F);
        rod = new ModelRenderer(this, 0, 44);
        rod.setRotationPoint(-2.0F, 8.0F, -2.0F);
        rod.func_228301_a_(0.0F, 0.0F, 0.0F, 4, 16, 4, 0.0F);
        blade_south = new ModelRenderer(this, 10, 0);
        blade_south.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_south.func_228301_a_(-1.5F, 0.0F, 4.0F, 3, 1, 4, 0.0F);
        setRotateAngle(blade_south, 0.0F, 0.0F, -BLADE_ROTATE);
        extension_north = new ModelRenderer(this, 0, 0);
        extension_north.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_north.func_228301_a_(-1.0F, 0.0F, -4.0F, 2, 1, 3, 0.0F);
        setRotateAngle(extension_north, 0.0F, 0.0F, BLADE_ROTATE);
        blade_west = new ModelRenderer(this, 10, 5);
        blade_west.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_west.func_228301_a_(-8.0F, 0.0F, -1.5F, 4, 1, 3, 0.0F);
        setRotateAngle(blade_west, BLADE_ROTATE, 0.0F, 0.0F);
    }

    @Override
    public void func_225598_a_(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int otherLight, float red, float green, float blue, float alpha) {
        //public void render(float size, int index) {
        RenderSystem.pushMatrix();
        RenderSystem.rotatef(index * 5, 0, 1, 0);
        extension_south.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        extension_west.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        extension_east.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        extension_north.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        float scale = index * 0.5F;
        float widthDiv = 16;
        renderBlade(blade_west, size, scale, scale / widthDiv, -0.25F, 0.0F);
        renderBlade(blade_east, size, scale, scale / widthDiv, 0.25F, 0.0F);
        renderBlade(blade_north, size, scale / widthDiv, scale, 0.0F, -0.25F);
        renderBlade(blade_south, size, scale / widthDiv, scale, 0.0F, 0.25F);
        RenderSystem.popMatrix();
    }

    private void renderBlade(ModelRenderer blade, float size, float scaleX, float scaleZ, float transX, float transZ) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef(transX, 0, transZ);
        RenderSystem.scalef(1.0F + scaleX, 1.0F, 1.0F + scaleZ);
        RenderSystem.translatef(-transX, 0, -transZ);
        blade.render(size);
        RenderSystem.popMatrix();
    }

    public void setRotateAngle(ModelRenderer ModelRenderer, float x, float y, float z) {
        ModelRenderer.rotateAngleX = x;
        ModelRenderer.rotateAngleY = y;
        ModelRenderer.rotateAngleZ = z;
    }
}