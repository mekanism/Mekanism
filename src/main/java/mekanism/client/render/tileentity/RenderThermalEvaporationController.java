package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nonnull;
import mekanism.client.render.FluidRenderer;
import mekanism.client.render.FluidRenderer.RenderData;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.AtlasTexture;

public class RenderThermalEvaporationController extends MekanismTileEntityRenderer<TileEntityThermalEvaporationController> {

    @Override
    public void func_225616_a_(@Nonnull TileEntityThermalEvaporationController tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        if (tile.structured && tile.height - 2 >= 1 && tile.inputTank.getFluidAmount() > 0) {
            RenderData data = new RenderData();
            data.location = tile.getRenderLocation();
            data.height = tile.height - 2;
            //TODO: If we ever allow different width for the evap controller then update this length and width
            data.length = 2;
            data.width = 2;
            data.fluidType = tile.inputTank.getFluid();
            bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            RenderSystem.pushMatrix();
            RenderSystem.enableCull();
            RenderSystem.enableBlend();
            RenderSystem.disableLighting();
            RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            setLightmapDisabled(true);
            FluidRenderer.translateToOrigin(data.location);
            float fluidScale = (float) tile.inputTank.getFluidAmount() / (float) tile.getMaxFluid();
            GlowInfo glowInfo = MekanismRenderer.enableGlow(data.fluidType);
            MekanismRenderer.color(data.fluidType, fluidScale);
            if (data.fluidType.getFluid().getAttributes().isGaseous(data.fluidType)) {
                FluidRenderer.getTankDisplay(data).render();
            } else {
                //Render the proper height
                FluidRenderer.getTankDisplay(data, Math.min(1, fluidScale)).render();
            }
            MekanismRenderer.resetColor();
            MekanismRenderer.disableGlow(glowInfo);
            setLightmapDisabled(false);
            RenderSystem.enableLighting();
            RenderSystem.disableBlend();
            RenderSystem.disableCull();
            RenderSystem.popMatrix();
        }
    }

    @Override
    public boolean isGlobalRenderer(TileEntityThermalEvaporationController tile) {
        return tile.structured && tile.height - 2 >= 1 && tile.inputTank.getFluidAmount() > 0;
    }
}