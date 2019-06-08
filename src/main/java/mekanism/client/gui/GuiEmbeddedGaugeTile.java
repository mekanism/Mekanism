package mekanism.client.gui;

import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public abstract class GuiEmbeddedGaugeTile<TILE extends TileEntityContainerBlock> extends GuiMekanismTile<TILE> {

    protected GuiEmbeddedGaugeTile(TILE tile, Container container) {
        super(tile, container);
    }

    protected abstract ResourceLocation getGaugeResource();

    protected void displayGauge(int xPos, int yPos, int scale, FluidStack fluid) {
        displayGauge(xPos, yPos, scale, fluid, 0);
    }

    protected void displayGauge(int xPos, int yPos, int scale, FluidStack fluid, int side /*0-left, 1-right*/) {
        if (fluid == null) {
            return;
        }
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        int start = 0;
        MekanismRenderHelper renderHelper = new MekanismRenderHelper().color(fluid);
        TextureAtlasSprite fluidTexture = MekanismRenderer.getFluidTexture(fluid, FluidType.STILL);
        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        while (true) {
            int renderRemaining;
            if (scale > 16) {
                renderRemaining = 16;
                scale -= 16;
            } else {
                renderRemaining = scale;
                scale = 0;
            }
            drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos + 58 - renderRemaining - start, fluidTexture, 16, renderRemaining);
            start += 16;
            if (renderRemaining == 0 || scale == 0) {
                break;
            }
        }
        renderHelper.cleanup();
        mc.renderEngine.bindTexture(getGaugeResource());
        drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos, 176, side == 0 ? 0 : 54, 16, 54);
    }
}