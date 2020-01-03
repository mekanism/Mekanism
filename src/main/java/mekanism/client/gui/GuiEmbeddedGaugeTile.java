package mekanism.client.gui;

import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public abstract class GuiEmbeddedGaugeTile<TILE extends TileEntityMekanism, CONTAINER extends MekanismTileContainer<TILE>> extends GuiMekanismTile<TILE, CONTAINER> {

    protected GuiEmbeddedGaugeTile(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    protected abstract ResourceLocation getGaugeResource();

    protected void displayGauge(int xPos, int yPos, int scale, @Nonnull FluidStack fluid) {
        displayGauge(xPos, yPos, scale, fluid, 0);
    }

    protected void displayGauge(int xPos, int yPos, int scale, @Nonnull FluidStack fluid, int side /*0-left, 1-right*/) {
        if (fluid.isEmpty()) {
            return;
        }
        MekanismRenderer.color(fluid);
        TextureAtlasSprite fluidTexture = MekanismRenderer.getFluidTexture(fluid, FluidType.STILL);
        minecraft.textureManager.bindTexture(PlayerContainer.field_226615_c_);
        int start = 0;
        int x = guiLeft + xPos;
        int y = guiTop + yPos;
        while (scale > 0) {
            int renderRemaining;
            if (scale > 16) {
                renderRemaining = 16;
                scale -= 16;
            } else {
                renderRemaining = scale;
                scale = 0;
            }
            drawTexturedRectFromIcon(x, y + 58 - renderRemaining - start, fluidTexture, 16, renderRemaining);
            start += 16;
        }
        MekanismRenderer.resetColor();
        minecraft.textureManager.bindTexture(getGaugeResource());
        drawTexturedRect(x, y, 176, side == 0 ? 0 : 54, 16, 54);
    }
}