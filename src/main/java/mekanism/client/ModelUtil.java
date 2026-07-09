package mekanism.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.client.render.outline.Outlines;
import mekanism.client.render.outline.Outlines.Line;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Cube;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.NeoForgeModelProperties;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

public class ModelUtil {

    private ModelUtil() {
    }

    public static final Matrix4f IDENTITY = new Matrix4f();
    public static final BlockDisplayContext BLOCK_DISPLAY_NO_CONTEXT = BlockDisplayContext.create();

    public static Vector3fc[] computeExtents(BlockRegistryObject<?, ?> blockRegistryObject) {
        BlockModelRenderState state = new BlockModelRenderState();
        Minecraft.getInstance().getBlockModelResolver().update(state, blockRegistryObject.defaultState(), BLOCK_DISPLAY_NO_CONTEXT);
        List<BakedQuad> bakedQuads = state.setupModel(IDENTITY, false).stream().flatMap(part -> part.getQuads(null).stream()).toList();
        return CuboidItemModelWrapper.computeExtents(bakedQuads);
    }

    public static ContextMap partVisibility(ResolvedModel resolvedModel, Map<String, Boolean> visibilityMap) {
        ContextMap.Builder builder = new ContextMap.Builder();
        fillAdditionalProperties(resolvedModel, builder);
        builder.withParameter(NeoForgeModelProperties.PART_VISIBILITY, visibilityMap);
        return builder.create(ContextKeySet.EMPTY);
    }

    /// copied from Neo [net.neoforged.neoforge.client.extensions.ResolvedModelExtension] as it's private
    private static void fillAdditionalProperties(@Nullable ResolvedModel model, ContextMap.Builder propertiesBuilder) {
        if (model != null) {
            fillAdditionalProperties(model.parent(), propertiesBuilder);
            //noinspection OverrideOnly
            model.wrapped().fillAdditionalProperties(propertiesBuilder);
        }
    }

    public static TextureSlots makeTextureSlots(ResolvedModel resolvedModel, String slot, Identifier texture) {
        return makeTextureSlots(resolvedModel, resolvedModel, slot, texture);
    }

    /// from [ResolvedModel#findTopTextureSlots(ResolvedModel)]
    public static TextureSlots makeTextureSlots(ResolvedModel resolvedModel, ModelDebugName debugName, String slot, Identifier texture) {
        ResolvedModel current = resolvedModel;
        TextureSlots.Resolver resolver;
        for (resolver = new TextureSlots.Resolver(); current != null; current = current.parent()) {
            resolver.addLast(current.wrapped().textureSlots());
        }
        resolver.addLast(new TextureSlots.Data.Builder()
              .addTexture(slot, new Material(texture))
              .build()
        );
        return resolver.resolve(debugName);
    }

    public static Set<Line> getPartsAsWireFrame(List<ModelPart> parts) {
        Set<Line> lines = new HashSet<>();
        //tmp variables to avoid allocating for each model part
        Vector4f pos = new Vector4f();
        Vector3f v0 = new Vector3f();
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f v3 = new Vector3f();
        PoseStack poseStack = new PoseStack();
        for (ModelPart part : parts) {
            visit(part, poseStack, v0, v1, v2, v3, pos, lines);
        }
        return lines;
    }

    //Simplified version of ModelPart#visit that also avoids capturing lambdas
    private static void visit(ModelPart part, PoseStack poseStack, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, Vector4f pos, Set<Line> lines) {
        if (part.visible) {
            if (!part.isEmpty() || !part.children.isEmpty()) {
                poseStack.pushPose();
                part.translateAndRotate(poseStack);
                visitAndRender(part.cubes, poseStack.last().pose(), v0, v1, v2, v3, pos, lines);
                for (ModelPart child : part.children.values()) {
                    visit(child, poseStack, v0, v1, v2, v3, pos, lines);
                }
                poseStack.popPose();
            }
        }
    }

    private static void visitAndRender(List<Cube> cubes, Matrix4f pose, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, Vector4f pos, Set<Line> lines) {
        for (Cube cube : cubes) {
            for (ModelPart.Polygon quad : cube.polygons) {
                setVectorFromVertex(quad.vertices()[0], pose, pos, v0);
                setVectorFromVertex(quad.vertices()[1], pose, pos, v1);
                setVectorFromVertex(quad.vertices()[2], pose, pos, v2);
                setVectorFromVertex(quad.vertices()[3], pose, pos, v3);
                Outlines.addQuad(lines, v0, v1, v2, v3);
            }
        }
    }

    private static void setVectorFromVertex(ModelPart.Vertex vertex, Matrix4f pose, Vector4f pos, Vector3f vector) {
        pos.set(vertex.worldX(), vertex.worldY(), vertex.worldZ(), 1);
        pose.transform(pos);
        vector.set(pos);
    }
}
