package mekanism.client.render.tileentity;

import mekanism.client.model.ModelQuantumEntangloporter;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderQuantumEntangloporter extends TileEntityRenderer<TileEntityQuantumEntangloporter> {

    private ModelQuantumEntangloporter model = new ModelQuantumEntangloporter();

    @Override
    public void render(TileEntityQuantumEntangloporter tileEntity, double x, double y, double z, float partialTick, int destroyStage) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "QuantumEntangloporter.png"));
        MekanismRenderer.rotate(tileEntity.getDirection(), 0, 180, 90, 270);
        GlStateManager.rotatef(180, 0, 0, 1);
        model.render(0.0625F, rendererDispatcher.textureManager, false);
        GlStateManager.popMatrix();
        MekanismRenderer.machineRenderer().render(tileEntity, x, y, z, partialTick, destroyStage);
    }
}