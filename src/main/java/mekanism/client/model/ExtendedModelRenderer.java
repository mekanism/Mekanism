package mekanism.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

public class ExtendedModelRenderer extends ModelRenderer {

    public ExtendedModelRenderer(Model model, int texOffX, int texOffY) {
        super(model, texOffX, texOffY);
    }

    public void render(MatrixStack matrix, IVertexBuilder buffer, int light, int overlayLight, float red, float green, float blue, float alpha, boolean wireFrame) {
        if (wireFrame) {
            renderWireFrame(matrix, buffer, red, green, blue, alpha);
        } else {
            render(matrix, buffer, light, overlayLight, red, green, blue, alpha);
        }
    }

    public void renderWireFrame(MatrixStack matrix, IVertexBuilder buffer, float red, float green, float blue, float alpha) {
        if (showModel) {
            if (!cubeList.isEmpty() || !childModels.isEmpty()) {
                matrix.push();
                translateRotate(matrix);
                MatrixStack.Entry matrixEntry = matrix.getLast();
                Matrix4f matrix4f = matrixEntry.getMatrix();
                Matrix3f matrix3f = matrixEntry.getNormal();
                for (ModelRenderer.ModelBox box : cubeList) {
                    for (ModelRenderer.TexturedQuad quad : box.quads) {
                        Vector3f normal = quad.normal.copy();
                        normal.transform(matrix3f);
                        float normalX = normal.getX();
                        float normalY = normal.getY();
                        float normalZ = normal.getZ();
                        Vector4f vertex = getVertex(matrix4f, quad.vertexPositions[0]);
                        Vector4f vertex2 = getVertex(matrix4f, quad.vertexPositions[1]);
                        Vector4f vertex3 = getVertex(matrix4f, quad.vertexPositions[2]);
                        Vector4f vertex4 = getVertex(matrix4f, quad.vertexPositions[3]);
                        buffer.pos(vertex.getX(), vertex.getY(), vertex.getZ()).normal(normalX, normalY, normalZ).color(red, green, blue, alpha).endVertex();
                        buffer.pos(vertex2.getX(), vertex2.getY(), vertex2.getZ()).normal(normalX, normalY, normalZ).color(red, green, blue, alpha).endVertex();

                        buffer.pos(vertex3.getX(), vertex3.getY(), vertex3.getZ()).normal(normalX, normalY, normalZ).color(red, green, blue, alpha).endVertex();
                        buffer.pos(vertex4.getX(), vertex4.getY(), vertex4.getZ()).normal(normalX, normalY, normalZ).color(red, green, blue, alpha).endVertex();
                        //Vertices missing from base implementation
                        buffer.pos(vertex2.getX(), vertex2.getY(), vertex2.getZ()).normal(normalX, normalY, normalZ).color(red, green, blue, alpha).endVertex();
                        buffer.pos(vertex3.getX(), vertex3.getY(), vertex3.getZ()).normal(normalX, normalY, normalZ).color(red, green, blue, alpha).endVertex();

                        buffer.pos(vertex.getX(), vertex.getY(), vertex.getZ()).normal(normalX, normalY, normalZ).color(red, green, blue, alpha).endVertex();
                        buffer.pos(vertex4.getX(), vertex4.getY(), vertex4.getZ()).normal(normalX, normalY, normalZ).color(red, green, blue, alpha).endVertex();
                    }
                }

                for (ModelRenderer modelrenderer : childModels) {
                    if (modelrenderer instanceof ExtendedModelRenderer) {
                        ((ExtendedModelRenderer) modelrenderer).renderWireFrame(matrix, buffer, red, green, blue, alpha);
                    } else {
                        modelrenderer.render(matrix, buffer, MekanismRenderer.FULL_LIGHT, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
                    }
                }
                matrix.pop();
            }
        }
    }

    private static Vector4f getVertex(Matrix4f matrix4f, ModelRenderer.PositionTextureVertex vertex) {
        Vector4f vector4f = new Vector4f(vertex.position.getX() / 16F, vertex.position.getY() / 16F, vertex.position.getZ() / 16F, 1);
        vector4f.transform(matrix4f);
        return vector4f;
    }
}