package mekanism.client.gui;

import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

@OnlyIn(Dist.CLIENT)
public abstract class GuiEmbeddedGaugeTile<TILE extends TileEntityMekanism, CONTAINER extends MekanismTileContainer<TILE>> extends GuiMekanismTile<TILE, CONTAINER> {

    protected GuiEmbeddedGaugeTile(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
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
        MekanismRenderer.color(fluid);
        TextureAtlasSprite fluidTexture = MekanismRenderer.getFluidTexture(fluid, FluidType.STILL);
        minecraft.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        while (true) {
            int renderRemaining;
            if (scale > 16) {
                renderRemaining = 16;
                scale -= 16;
            } else {
                renderRemaining = scale;
                scale = 0;
            }
            drawTexturedRectFromIcon(guiLeft + xPos, guiTop + yPos + 58 - renderRemaining - start, fluidTexture, 16, renderRemaining);
            start += 16;
            if (renderRemaining == 0 || scale == 0) {
                break;
            }
        }
        MekanismRenderer.resetColor();
        minecraft.textureManager.bindTexture(getGaugeResource());
        drawTexturedRect(guiLeft + xPos, guiTop + yPos, 176, side == 0 ? 0 : 54, 16, 54);
    }
}