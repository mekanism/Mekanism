package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;

public abstract class MekanismJavaModel extends Model {

    public MekanismJavaModel(Function<ResourceLocation, RenderType> renderType) {
        super(renderType);
    }

    protected static VertexConsumer getVertexConsumer(@Nonnull MultiBufferSource renderer, @Nonnull RenderType renderType, boolean hasEffect) {
        return ItemRenderer.getFoilBufferDirect(renderer, renderType, false, hasEffect);
    }

    protected static void setRotation(ModelPart model, float x, float y, float z) {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }

    protected static void renderPartsToBuffer(List<ModelPart> parts, PoseStack poseStack, @Nonnull VertexConsumer vertexConsumer, int light, int overlayLight,
          float red, float green, float blue, float alpha) {
        for (ModelPart part : parts) {
            part.render(poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
        }
    }

    protected static List<ModelPart> getRenderableParts(ModelPart root, ModelPartData... modelPartData) {
        List<ModelPart> parts = new ArrayList<>(modelPartData.length);
        for (ModelPartData partData : modelPartData) {
            parts.add(partData.getFromRoot(root));
        }
        return parts;
    }

    protected static LayerDefinition createLayerDefinition(int textureWidth, int textureHeight, ModelPartData... parts) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        for (ModelPartData part : parts) {
            part.addToDefinition(partdefinition);
        }
        return LayerDefinition.create(meshdefinition, textureWidth, textureHeight);
    }

    protected static void renderPartsAsWireFrame(List<ModelPart> parts, PoseStack poseStack, @Nonnull VertexConsumer vertexConsumer, float red, float green,
          float blue, float alpha) {
        for (ModelPart part : parts) {
            renderWireFrame(part, poseStack, vertexConsumer, red, green, blue, alpha);
        }
    }

    public static void renderWireFrame(ModelPart part, PoseStack matrix, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha) {
        if (part.visible) {
            part.visit(matrix, (pose, name, cubeIndex, cube) -> {
                Matrix4f matrix4f = pose.pose();
                for (ModelPart.Polygon quad : cube.polygons) {
                    Vector3f normal = quad.normal.copy();
                    normal.transform(pose.normal());
                    float normalX = normal.x();
                    float normalY = normal.y();
                    float normalZ = normal.z();
                    Vector4f vertex = getVertex(matrix4f, quad.vertices[0]);
                    Vector4f vertex2 = getVertex(matrix4f, quad.vertices[1]);
                    Vector4f vertex3 = getVertex(matrix4f, quad.vertices[2]);
                    Vector4f vertex4 = getVertex(matrix4f, quad.vertices[3]);
                    vertexConsumer.vertex(vertex.x(), vertex.y(), vertex.z()).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
                    vertexConsumer.vertex(vertex2.x(), vertex2.y(), vertex2.z()).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();

                    vertexConsumer.vertex(vertex3.x(), vertex3.y(), vertex3.z()).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
                    vertexConsumer.vertex(vertex4.x(), vertex4.y(), vertex4.z()).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
                    //Vertices missing from base implementation
                    vertexConsumer.vertex(vertex2.x(), vertex2.y(), vertex2.z()).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
                    vertexConsumer.vertex(vertex3.x(), vertex3.y(), vertex3.z()).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();

                    vertexConsumer.vertex(vertex.x(), vertex.y(), vertex.z()).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
                    vertexConsumer.vertex(vertex4.x(), vertex4.y(), vertex4.z()).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
                }
            });
        }
    }

    private static Vector4f getVertex(Matrix4f matrix4f, ModelPart.Vertex vertex) {
        Vector4f vector4f = new Vector4f(vertex.pos.x() / 16F, vertex.pos.y() / 16F, vertex.pos.z() / 16F, 1);
        vector4f.transform(matrix4f);
        return vector4f;
    }
}