package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelQuantumEntangloporter;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class RenderQuantumEntangloporter extends TileEntityRenderer<TileEntityQuantumEntangloporter> {

    private ModelQuantumEntangloporter model = new ModelQuantumEntangloporter();

    public RenderQuantumEntangloporter(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void func_225616_a_(@Nonnull TileEntityQuantumEntangloporter tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer,
          int light, int overlayLight) {
        matrix.func_227860_a_();
        matrix.func_227861_a_(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.func_227863_a_(Vector3f.field_229183_f_.func_229187_a_(180));
        model.render(matrix, renderer, light, overlayLight, false);
        matrix.func_227865_b_();
        MekanismRenderer.machineRenderer().func_225616_a_(tile, partialTick, matrix, renderer, light, overlayLight);
    }
}