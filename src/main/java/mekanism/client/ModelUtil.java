package mekanism.client;

import java.util.List;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

public class ModelUtil {
    public static final Matrix4f IDENTITY = new Matrix4f();
    public static final BlockDisplayContext BLOCK_DISPLAY_NO_CONTEXT = BlockDisplayContext.create();

    public static Vector3fc[] computeExtents(BlockRegistryObject<?, ?> blockRegistryObject) {
        BlockModelRenderState state = new BlockModelRenderState();
        Minecraft.getInstance().getBlockModelResolver().update(state, blockRegistryObject.defaultState(), BLOCK_DISPLAY_NO_CONTEXT);
        List<BakedQuad> bakedQuads = state.setupModel(IDENTITY, false).stream().flatMap(part -> part.getQuads(null).stream()).toList();
        return CuboidItemModelWrapper.computeExtents(bakedQuads);
    }
}
