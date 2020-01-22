package mekanism.generators.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelTurbine extends Model {

    private static final ResourceLocation TURBINE_TEXTURE = MekanismGenerators.rl("render/turbine.png");
    private static float BLADE_ROTATE = 0.418879F;

    private final RenderType RENDER_TYPE = func_228282_a_(TURBINE_TEXTURE);

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
        super(RenderType::entitySolid);
        textureWidth = 64;
        textureHeight = 64;
        extension_south = new ModelRenderer(this, 0, 0);
        extension_south.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_south.addBox(-1.0F, 0.0F, 1.0F, 2, 1, 3, 0.0F);
        setRotateAngle(extension_south, 0.0F, 0.0F, -BLADE_ROTATE);
        extension_west = new ModelRenderer(this, 0, 4);
        extension_west.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_west.addBox(-4.0F, 0.0F, -1.0F, 3, 1, 2, 0.0F);
        setRotateAngle(extension_west, BLADE_ROTATE, 0.0F, 0.0F);
        blade_east = new ModelRenderer(this, 10, 5);
        blade_east.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_east.addBox(4.0F, 0.0F, -1.5F, 4, 1, 3, 0.0F);
        setRotateAngle(blade_east, -BLADE_ROTATE, 0.0F, 0.0F);
        blade_north = new ModelRenderer(this, 10, 0);
        blade_north.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_north.addBox(-1.5F, 0.0F, -8.0F, 3, 1, 4, 0.0F);
        setRotateAngle(blade_north, 0.0F, 0.0F, BLADE_ROTATE);
        extension_east = new ModelRenderer(this, 0, 4);
        extension_east.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_east.addBox(1.0F, 0.0F, -1.0F, 3, 1, 2, 0.0F);
        setRotateAngle(extension_east, -BLADE_ROTATE, 0.0F, 0.0F);
        rod = new ModelRenderer(this, 0, 44);
        rod.setRotationPoint(-2.0F, 8.0F, -2.0F);
        rod.addBox(0.0F, 0.0F, 0.0F, 4, 16, 4, 0.0F);
        blade_south = new ModelRenderer(this, 10, 0);
        blade_south.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_south.addBox(-1.5F, 0.0F, 4.0F, 3, 1, 4, 0.0F);
        setRotateAngle(blade_south, 0.0F, 0.0F, -BLADE_ROTATE);
        extension_north = new ModelRenderer(this, 0, 0);
        extension_north.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_north.addBox(-1.0F, 0.0F, -4.0F, 2, 1, 3, 0.0F);
        setRotateAngle(extension_north, 0.0F, 0.0F, BLADE_ROTATE);
        blade_west = new ModelRenderer(this, 10, 5);
        blade_west.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_west.addBox(-8.0F, 0.0F, -1.5F, 4, 1, 3, 0.0F);
        setRotateAngle(blade_west, BLADE_ROTATE, 0.0F, 0.0F);
    }

    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, int index) {
        matrix.push();
        matrix.rotate(Vector3f.field_229181_d_.func_229187_a_(index * 5));
        float scale = index * 0.5F;
        float widthDiv = 16;
        IVertexBuilder vertexBuilder = renderer.getBuffer(RENDER_TYPE);
        renderBlade(matrix, vertexBuilder, light, overlayLight, blade_west, scale, scale / widthDiv, -0.25, 0);
        renderBlade(matrix, vertexBuilder, light, overlayLight, blade_east, scale, scale / widthDiv, 0.25, 0);
        renderBlade(matrix, vertexBuilder, light, overlayLight, blade_north, scale / widthDiv, scale, 0, -0.25);
        renderBlade(matrix, vertexBuilder, light, overlayLight, blade_south, scale / widthDiv, scale, 0, 0.25);
        matrix.pop();
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue,
          float alpha) {
        extension_south.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        extension_west.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        extension_east.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        extension_north.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }

    private void renderBlade(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, ModelRenderer blade, float scaleX,
          float scaleZ, double transX, double transZ) {
        matrix.push();
        matrix.translate(transX, 0, transZ);
        matrix.scale(1.0F + scaleX, 1.0F, 1.0F + scaleZ);
        matrix.translate(-transX, 0, -transZ);
        blade.render(matrix, vertexBuilder, light, overlayLight, 1, 1, 1, 1);
        matrix.pop();
    }

    public void setRotateAngle(ModelRenderer ModelRenderer, float x, float y, float z) {
        ModelRenderer.rotateAngleX = x;
        ModelRenderer.rotateAngleY = y;
        ModelRenderer.rotateAngleZ = z;
    }
}