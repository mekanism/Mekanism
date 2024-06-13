package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.lib.Outlines.Line;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Cube;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public abstract class MekanismJavaModel extends Model {

    public MekanismJavaModel(Function<ResourceLocation, RenderType> renderType) {
        super(renderType);
    }

    protected static VertexConsumer getVertexConsumer(@NotNull MultiBufferSource renderer, @NotNull RenderType renderType, boolean hasEffect) {
        return ItemRenderer.getFoilBufferDirect(renderer, renderType, false, hasEffect);
    }

    protected static void setRotation(ModelPart model, float x, float y, float z) {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }

    protected static void renderPartsToBuffer(List<ModelPart> parts, PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int light, int overlayLight, int argb) {
        for (ModelPart part : parts) {
            part.render(poseStack, vertexConsumer, light, overlayLight, argb);
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

    protected static void renderPartsAsWireFrame(List<ModelPart> parts, PoseStack poseStack, @NotNull VertexConsumer vertexConsumer) {
        //tmp variables to avoid allocating for each model part
        Vector4f pos = new Vector4f();
        Vector3f normal = new Vector3f();
        Vector3f v0 = new Vector3f();
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f v3 = new Vector3f();
        for (ModelPart part : parts) {
            visit(part, poseStack, vertexConsumer, pos, normal, v0, v1, v2, v3);
        }
    }

    //Simplified version of ModelPart#visit that also avoids capturing lambdas
    private static void visit(ModelPart part, PoseStack poseStack, VertexConsumer vertexConsumer,
          //Variables that are just used to skip allocating extra times
          Vector4f pos, Vector3f normal, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3) {
        if (part.visible) {
            if (!part.isEmpty() || !part.children.isEmpty()) {
                poseStack.pushPose();
                part.translateAndRotate(poseStack);
                visitAndRender(part.cubes, poseStack, vertexConsumer, pos, normal, v0, v1, v2, v3);
                for (ModelPart child : part.children.values()) {
                    visit(child, poseStack, vertexConsumer, pos, normal, v0, v1, v2, v3);
                }
                poseStack.popPose();
            }
        }
    }

    private static void visitAndRender(List<ModelPart.Cube> cubes, PoseStack matrix, VertexConsumer buffer,
          //Variables that are just used to skip allocating extra times
          Vector4f pos, Vector3f normal, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3) {
        Matrix4f pose = matrix.last().pose();
        Matrix3f poseNormal = matrix.last().normal();
        Set<Line> lines = new HashSet<>();
        for (Cube cube : cubes) {
            for (ModelPart.Polygon quad : cube.polygons) {
                quad.vertices[0].pos.div(16, v0);
                quad.vertices[1].pos.div(16, v1);
                quad.vertices[2].pos.div(16, v2);
                quad.vertices[3].pos.div(16, v3);
                lines.add(Line.from(v0, v1));
                lines.add(Line.from(v1, v2));
                lines.add(Line.from(v2, v3));
                lines.add(Line.from(v3, v0));
            }
        }
        RenderTickHandler.renderVertexWireFrame(lines, buffer, pose, poseNormal, pos, normal);
    }
}