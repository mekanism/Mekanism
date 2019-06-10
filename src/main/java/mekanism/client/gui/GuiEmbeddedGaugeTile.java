package mekanism.client.gui;

import mekanism.client.render.GLSMHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
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
        int start = 0;
        GLSMHelper.INSTANCE.color(fluid);
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
            drawTexturedModalRect(guiLeft + xPos, guiTop + yPos + 58 - renderRemaining - start, fluidTexture, 16, renderRemaining);
            start += 16;
            if (renderRemaining == 0 || scale == 0) {
                break;
            }
        }
        GLSMHelper.INSTANCE.resetColor();
        mc.renderEngine.bindTexture(getGaugeResource());
        drawTexturedModalRect(guiLeft + xPos, guiTop + yPos, 176, side == 0 ? 0 : 54, 16, 54);
    }
}