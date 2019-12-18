package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nonnull;
import mekanism.client.render.FluidRenderer;
import mekanism.client.render.FluidRenderer.RenderData;
import mekanism.client.render.FluidRenderer.ValveRenderData;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.tile.TileEntityDynamicTank;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.inventory.container.PlayerContainer;

public class RenderDynamicTank extends MekanismTileEntityRenderer<TileEntityDynamicTank> {

    @Override
    public void func_225616_a_(@Nonnull TileEntityDynamicTank tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        if (tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.fluidStored.getAmount() > 0) {
            RenderData data = new RenderData();
            data.location = tile.structure.renderLocation;
            data.height = tile.structure.volHeight - 2;
            data.length = tile.structure.volLength;
            data.width = tile.structure.volWidth;
            data.fluidType = tile.structure.fluidStored;

            if (data.location != null && data.height >= 1) {
                field_228858_b_.textureManager.bindTexture(PlayerContainer.field_226615_c_);
                RenderSystem.pushMatrix();
                RenderSystem.enableCull();
                RenderSystem.enableBlend();
                RenderSystem.disableLighting();
                RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
                setLightmapDisabled(true);
                FluidRenderer.translateToOrigin(data.location);
                GlowInfo glowInfo = MekanismRenderer.enableGlow(data.fluidType);
                MekanismRenderer.color(data.fluidType, (float) data.fluidType.getAmount() / (float) tile.clientCapacity);
                if (data.fluidType.getFluid().getAttributes().isGaseous(data.fluidType)) {
                    FluidRenderer.getTankDisplay(data).render();
                } else {
                    FluidRenderer.getTankDisplay(data, tile.prevScale).render();
                }

                MekanismRenderer.resetColor();
                MekanismRenderer.disableGlow(glowInfo);
                RenderSystem.popMatrix();

                for (ValveData valveData : tile.valveViewing) {
                    RenderSystem.pushMatrix();
                    FluidRenderer.translateToOrigin(valveData.location);
                    GlowInfo valveGlowInfo = MekanismRenderer.enableGlow(data.fluidType);
                    MekanismRenderer.color(data.fluidType);
                    FluidRenderer.getValveDisplay(ValveRenderData.get(data, valveData)).render();
                    MekanismRenderer.disableGlow(valveGlowInfo);
                    RenderSystem.popMatrix();
                }
                MekanismRenderer.resetColor();
                setLightmapDisabled(false);
                RenderSystem.enableLighting();
                RenderSystem.disableBlend();
                RenderSystem.disableCull();
            }
        }
    }

    @Override
    public boolean isGlobalRenderer(TileEntityDynamicTank tile) {
        return tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.fluidStored.getAmount() > 0;
    }
}