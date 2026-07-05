package mekanism.client;

import java.util.List;
import java.util.Map;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.client.Minecraft;
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
import org.joml.Vector3fc;
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

    public static ContextMap transmitterVisibility(ResolvedModel resolvedModel, Map<String, Boolean> visibilityMap) {
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
}
