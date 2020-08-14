package mekanism.generators.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.client.model.MekanismJavaModel;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class ModelTurbine extends MekanismJavaModel {

    private static final ResourceLocation TURBINE_TEXTURE = MekanismGenerators.rl("render/turbine.png");
    private static final float BLADE_ROTATE = 0.418879F;

    private final RenderType RENDER_TYPE = getRenderType(TURBINE_TEXTURE);

    private final ModelRenderer extension_north;
    private final ModelRenderer blade_north;
    private final ModelRenderer extension_south;
    private final ModelRenderer extension_west;
    private final ModelRenderer extension_east;
    private final ModelRenderer blade_south;
    private final ModelRenderer blade_east;
    private final ModelRenderer blade_west;

    public ModelTurbine() {
        super(RenderType::getEntitySolid);
        textureWidth = 64;
        textureHeight = 64;
        extension_south = new ModelRenderer(this, 0, 0);
        extension_south.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_south.addBox(-1.0F, 0.0F, 1.0F, 2, 1, 3, 0.0F);
        setRotation(extension_south, 0.0F, 0.0F, -BLADE_ROTATE);
        extension_west = new ModelRenderer(this, 0, 4);
        extension_west.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_west.addBox(-4.0F, 0.0F, -1.0F, 3, 1, 2, 0.0F);
        setRotation(extension_west, BLADE_ROTATE, 0.0F, 0.0F);
        blade_east = new ModelRenderer(this, 10, 5);
        blade_east.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_east.addBox(4.0F, 0.0F, -1.5F, 4, 1, 3, 0.0F);
        setRotation(blade_east, -BLADE_ROTATE, 0.0F, 0.0F);
        blade_north = new ModelRenderer(this, 10, 0);
        blade_north.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_north.addBox(-1.5F, 0.0F, -8.0F, 3, 1, 4, 0.0F);
        setRotation(blade_north, 0.0F, 0.0F, BLADE_ROTATE);
        extension_east = new ModelRenderer(this, 0, 4);
        extension_east.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_east.addBox(1.0F, 0.0F, -1.0F, 3, 1, 2, 0.0F);
        setRotation(extension_east, -BLADE_ROTATE, 0.0F, 0.0F);
        blade_south = new ModelRenderer(this, 10, 0);
        blade_south.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_south.addBox(-1.5F, 0.0F, 4.0F, 3, 1, 4, 0.0F);
        setRotation(blade_south, 0.0F, 0.0F, -BLADE_ROTATE);
        extension_north = new ModelRenderer(this, 0, 0);
        extension_north.setRotationPoint(0.0F, 20.0F, 0.0F);
        extension_north.addBox(-1.0F, 0.0F, -4.0F, 2, 1, 3, 0.0F);
        setRotation(extension_north, 0.0F, 0.0F, BLADE_ROTATE);
        blade_west = new ModelRenderer(this, 10, 5);
        blade_west.setRotationPoint(0.0F, 20.0F, 0.0F);
        blade_west.addBox(-8.0F, 0.0F, -1.5F, 4, 1, 3, 0.0F);
        setRotation(blade_west, BLADE_ROTATE, 0.0F, 0.0F);
    }

    public IVertexBuilder getBuffer(@Nonnull IRenderTypeBuffer renderer) {
        return renderer.getBuffer(RENDER_TYPE);
    }

    public void render(@Nonnull MatrixStack matrix, IVertexBuilder buffer, int light, int overlayLight, int index) {
        matrix.push();
        matrix.rotate(Vector3f.YP.rotationDegrees(index * 5));
        render(matrix, buffer, light, overlayLight, 1, 1, 1, 1);
        float scale = index * 0.5F;
        float widthDiv = 16;
        renderBlade(matrix, buffer, light, overlayLight, blade_west, scale, scale / widthDiv, -0.25, 0);
        renderBlade(matrix, buffer, light, overlayLight, blade_east, scale, scale / widthDiv, 0.25, 0);
        renderBlade(matrix, buffer, light, overlayLight, blade_north, scale / widthDiv, scale, 0, -0.25);
        renderBlade(matrix, buffer, light, overlayLight, blade_south, scale / widthDiv, scale, 0, 0.25);
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
}