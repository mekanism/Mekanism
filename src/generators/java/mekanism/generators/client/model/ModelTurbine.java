package mekanism.generators.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelTurbine extends Model {

    private static final ResourceLocation TURBINE_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "turbine.png");
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

    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight, int index) {
        matrix.func_227860_a_();
        matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(index * 5));
        float scale = index * 0.5F;
        float widthDiv = 16;
        IVertexBuilder vertexBuilder = renderer.getBuffer(RENDER_TYPE);
        renderBlade(matrix, vertexBuilder, light, otherLight, blade_west, scale, scale / widthDiv, -0.25, 0);
        renderBlade(matrix, vertexBuilder, light, otherLight, blade_east, scale, scale / widthDiv, 0.25, 0);
        renderBlade(matrix, vertexBuilder, light, otherLight, blade_north, scale / widthDiv, scale, 0, -0.25);
        renderBlade(matrix, vertexBuilder, light, otherLight, blade_south, scale / widthDiv, scale, 0, 0.25);
        matrix.func_227865_b_();
    }

    @Override
    public void func_225598_a_(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int otherLight, float red, float green, float blue,
          float alpha) {
        extension_south.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        extension_west.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        extension_east.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        extension_north.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
    }

    private void renderBlade(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int otherLight, ModelRenderer blade, float scaleX,
          float scaleZ, double transX, double transZ) {
        matrix.func_227860_a_();
        matrix.func_227861_a_(transX, 0, transZ);
        matrix.func_227862_a_(1.0F + scaleX, 1.0F, 1.0F + scaleZ);
        matrix.func_227861_a_(-transX, 0, -transZ);
        blade.func_228309_a_(matrix, vertexBuilder, light, otherLight, 1, 1, 1, 1);
        matrix.func_227865_b_();
    }

    public void setRotateAngle(ModelRenderer ModelRenderer, float x, float y, float z) {
        ModelRenderer.rotateAngleX = x;
        ModelRenderer.rotateAngleY = y;
        ModelRenderer.rotateAngleZ = z;
    }
}