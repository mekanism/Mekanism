package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;

public interface IWireFrameRenderer {

    void renderWireFrame(TileEntity tile, float partialTick, MatrixStack matrix, IVertexBuilder buffer, float red, float green, float blue, float alpha);

    default boolean hasSelectionBox(BlockState state) {
        return true;
    }

    default boolean isCombined() {
        return false;
    }
}