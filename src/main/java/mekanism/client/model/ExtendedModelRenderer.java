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
        if (visible) {
            if (!cubes.isEmpty() || !children.isEmpty()) {
                matrix.pushPose();
                translateAndRotate(matrix);
                MatrixStack.Entry matrixEntry = matrix.last();
                Matrix4f matrix4f = matrixEntry.pose();
                Matrix3f matrix3f = matrixEntry.normal();
                for (ModelRenderer.ModelBox box : cubes) {
                    for (ModelRenderer.TexturedQuad quad : box.polygons) {
                        Vector3f normal = quad.normal.copy();
                        normal.transform(matrix3f);
                        float normalX = normal.x();
                        float normalY = normal.y();
                        float normalZ = normal.z();
                        Vector4f vertex = getVertex(matrix4f, quad.vertices[0]);
                        Vector4f vertex2 = getVertex(matrix4f, quad.vertices[1]);
                        Vector4f vertex3 = getVertex(matrix4f, quad.vertices[2]);
                        Vector4f vertex4 = getVertex(matrix4f, quad.vertices[3]);
                        buffer.vertex(vertex.x(), vertex.y(), vertex.z()).normal(normalX, normalY, normalZ).color(red, green, blue, alpha).endVertex();
                        buffer.vertex(vertex2.x(), vertex2.y(), vertex2.z()).normal(normalX, normalY, normalZ).color(red, green, blue, alpha).endVertex();

                        buffer.vertex(vertex3.x(), vertex3.y(), vertex3.z()).normal(normalX, normalY, normalZ).color(red, green, blue, alpha).endVertex();
                        buffer.vertex(vertex4.x(), vertex4.y(), vertex4.z()).normal(normalX, normalY, normalZ).color(red, green, blue, alpha).endVertex();
                        //Vertices missing from base implementation
                        buffer.vertex(vertex2.x(), vertex2.y(), vertex2.z()).normal(normalX, normalY, normalZ).color(red, green, blue, alpha).endVertex();
                        buffer.vertex(vertex3.x(), vertex3.y(), vertex3.z()).normal(normalX, normalY, normalZ).color(red, green, blue, alpha).endVertex();

                        buffer.vertex(vertex.x(), vertex.y(), vertex.z()).normal(normalX, normalY, normalZ).color(red, green, blue, alpha).endVertex();
                        buffer.vertex(vertex4.x(), vertex4.y(), vertex4.z()).normal(normalX, normalY, normalZ).color(red, green, blue, alpha).endVertex();
                    }
                }

                for (ModelRenderer modelrenderer : children) {
                    if (modelrenderer instanceof ExtendedModelRenderer) {
                        ((ExtendedModelRenderer) modelrenderer).renderWireFrame(matrix, buffer, red, green, blue, alpha);
                    } else {
                        modelrenderer.render(matrix, buffer, MekanismRenderer.FULL_LIGHT, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
                    }
                }
                matrix.popPose();
            }
        }
    }

    private static Vector4f getVertex(Matrix4f matrix4f, ModelRenderer.PositionTextureVertex vertex) {
        Vector4f vector4f = new Vector4f(vertex.pos.x() / 16F, vertex.pos.y() / 16F, vertex.pos.z() / 16F, 1);
        vector4f.transform(matrix4f);
        return vector4f;
    }
}