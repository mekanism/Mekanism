package mekanism.client.gui;

import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public abstract class GuiEmbeddedGaugeTile<TILE extends TileEntityMekanism, CONTAINER extends MekanismTileContainer<TILE>> extends GuiMekanismTile<TILE, CONTAINER> {

    protected GuiEmbeddedGaugeTile(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    protected abstract ResourceLocation getGaugeResource();

    protected void displayGauge(int xPos, int yPos, int scale, @Nonnull FluidStack fluid, int side /*0-left, 1-right*/) {
        if (!fluid.isEmpty()) {
            MekanismRenderer.color(fluid);
            displayGauge(xPos, yPos, scale, side, MekanismRenderer.getFluidTexture(fluid, FluidType.STILL));
        }
    }

    protected void displayGauge(int xPos, int yPos, int scale, int side /*0-left, 1-right*/, TextureAtlasSprite sprite) {
        //TODO: Make this use the newer method of displaying the contents. Holding off until this gets moved to properly just being a GuiGauge instead though
        minecraft.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        int start = 0;
        int x = getGuiLeft() + xPos;
        int y = getGuiTop() + yPos;
        int shiftedY = y + 58;
        while (scale > 0) {
            int renderRemaining;
            if (scale > 16) {
                renderRemaining = 16;
                scale -= 16;
            } else {
                renderRemaining = scale;
                scale = 0;
            }
            drawTexturedRectFromIcon(x, shiftedY - renderRemaining - start, sprite, 16, renderRemaining);
            start += 16;
        }
        //Reset the color so that it does not leak into drawing our gauge or other gui elements
        MekanismRenderer.resetColor();
        minecraft.textureManager.bindTexture(getGaugeResource());
        drawTexturedRect(x, y, 176, side == 0 ? 0 : 54, 16, 54);
    }
}