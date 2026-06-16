package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.lib.Outlines.Line;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Cube;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

//TODO - 26.2: review if any of these can be converted back to regular java models - needs rendertype without texture & only single rendertype/light coords
public abstract class MekanismJavaModel<STATE extends @Nullable Object> /*extends Model<STATE>*/ {

    protected final ModelPart root;
    protected final List<ModelPart> allParts;

    public MekanismJavaModel(ModelPart root) {
        this.root = root;
        this.allParts = root.getAllParts();
    }

    //TODO - 26.2 outlines??
    public abstract void collect(STATE state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlayLight);

    public void setupAnim(STATE state) {
        this.resetPose();
    }

    public final void resetPose() {
        for (ModelPart part : this.allParts) {
            part.resetPose();
        }
    }

    public ModelPart root() {
        return root;
    }

    protected static void renderPartsToBuffer(List<ModelPart> parts, PoseStack poseStack, VertexConsumer vertexConsumer, int light, int overlayLight, int argb) {
        for (ModelPart part : parts) {
            part.render(poseStack, vertexConsumer, light, overlayLight, argb);
        }
    }

    protected static void collectParts(List<ModelPart> parts, PoseStack poseStack, RenderType renderType, SubmitNodeCollector collector, int light, int overlayLight, int argb, @Nullable TextureAtlasSprite sprite) {
        for (ModelPart part : parts) {
            //TODO - 26.2: Figure out how foil rendering works now as it no longer is passed to this
            collector.submitModelPart(part, poseStack, renderType, light, overlayLight, sprite, argb, null, 0);
        }
    }

    protected static List<ModelPart> getRenderableParts(ModelPart root, ModelPartData... modelPartData) {
        List<ModelPart> parts = new ArrayList<>(modelPartData.length);
        for (ModelPartData partData : modelPartData) {
            parts.add(partData.getFromRoot(root));
        }
        return parts;
    }

    public static LayerDefinition createLayerDefinition(int textureWidth, int textureHeight, ModelPartData... parts) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partDefinition = mesh.getRoot();
        for (ModelPartData part : parts) {
            part.addToDefinition(partDefinition);
        }
        return LayerDefinition.create(mesh, textureWidth, textureHeight);
    }

    public static void renderPartsAsWireFrame(List<ModelPart> parts, PoseStack poseStack, VertexConsumer vertexConsumer, boolean isHighContrast) {
        //tmp variables to avoid allocating for each model part
        Vector4f pos = new Vector4f();
        Vector3f normal = new Vector3f();
        Vector3f v0 = new Vector3f();
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f v3 = new Vector3f();
        for (ModelPart part : parts) {
            visit(part, poseStack, vertexConsumer, pos, normal, v0, v1, v2, v3, isHighContrast);
        }
    }

    //Simplified version of ModelPart#visit that also avoids capturing lambdas
    private static void visit(ModelPart part, PoseStack poseStack, VertexConsumer vertexConsumer,
          //Variables that are just used to skip allocating extra times
          Vector4f pos, Vector3f normal, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, boolean isHighContrast) {
        if (part.visible) {
            if (!part.isEmpty() || !part.children.isEmpty()) {
                poseStack.pushPose();
                part.translateAndRotate(poseStack);
                visitAndRender(part.cubes, poseStack, vertexConsumer, pos, normal, v0, v1, v2, v3, isHighContrast);
                for (ModelPart child : part.children.values()) {
                    visit(child, poseStack, vertexConsumer, pos, normal, v0, v1, v2, v3, isHighContrast);
                }
                poseStack.popPose();
            }
        }
    }

    private static void visitAndRender(List<Cube> cubes, PoseStack matrix, VertexConsumer buffer,
          //Variables that are just used to skip allocating extra times
          Vector4f pos, Vector3f normal, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, boolean isHighContrast) {
        Matrix4f pose = matrix.last().pose();
        Matrix3f poseNormal = matrix.last().normal();
        Set<Line> lines = new HashSet<>();
        for (Cube cube : cubes) {
            for (ModelPart.Polygon quad : cube.polygons) {
                setVectorFromVertex(quad.vertices()[0], v0);
                setVectorFromVertex(quad.vertices()[1], v1);
                setVectorFromVertex(quad.vertices()[2], v2);
                setVectorFromVertex(quad.vertices()[3], v3);
                lines.add(Line.from(v0, v1));
                lines.add(Line.from(v1, v2));
                lines.add(Line.from(v2, v3));
                lines.add(Line.from(v3, v0));
            }
        }
        RenderTickHandler.renderVertexWireFrame(lines, buffer, pose, poseNormal, pos, normal, isHighContrast);
    }

    private static void setVectorFromVertex(ModelPart.Vertex vertex, Vector3f vector) {
        vector.set(vertex.worldX(), vertex.worldY(), vertex.worldZ());
    }

    public abstract static class NoState extends MekanismJavaModel<@Nullable Void> {

        public NoState(ModelPart root) {
            super(root);
        }

        public void setupAnim() {
            setupAnim(null);
        }

        @Override
        public final void collect(@Nullable Void unused, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlayLight) {
            collect(poseStack, submitNodeCollector, light, overlayLight);
        }

        public abstract void collect(PoseStack poseStack, SubmitNodeCollector collector, int light, int overlayLight);
    }
}