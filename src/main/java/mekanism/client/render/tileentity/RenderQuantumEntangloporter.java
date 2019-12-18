package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelQuantumEntangloporter;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class RenderQuantumEntangloporter extends MekanismTileEntityRenderer<TileEntityQuantumEntangloporter> {

    private ModelQuantumEntangloporter model = new ModelQuantumEntangloporter();

    @Override
    public void func_225616_a_(@Nonnull TileEntityQuantumEntangloporter tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        //TODO: 1.15
        /*RenderSystem.pushMatrix();
        RenderSystem.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "quantum_entangloporter.png"));
        MekanismRenderer.rotate(tile.getDirection(), 0, 180, 90, 270);
        RenderSystem.rotatef(180, 0, 0, 1);
        model.render(0.0625F, field_228858_b_.textureManager, false);
        RenderSystem.popMatrix();
        MekanismRenderer.machineRenderer().func_225616_a_(tile, partialTick, matrix, renderer, light, otherLight);*/
    }
}