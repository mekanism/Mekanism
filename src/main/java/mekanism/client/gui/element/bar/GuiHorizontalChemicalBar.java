package mekanism.client.gui.element.bar;

import mekanism.api.chemical.Chemical;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiVerticalChemicalBar.ChemicalInfoProvider;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class GuiHorizontalChemicalBar<CHEMICAL extends Chemical<CHEMICAL>> extends GuiBar<ChemicalInfoProvider<CHEMICAL>> {

    public GuiHorizontalChemicalBar(IGuiWrapper gui, ChemicalInfoProvider<CHEMICAL> infoProvider, int x, int y, int width, int height) {
        super(AtlasTexture.LOCATION_BLOCKS_TEXTURE, gui, infoProvider, x, y, width, height);
    }

    @Override
    protected void renderBarOverlay(int mouseX, int mouseY, float partialTicks) {
        CHEMICAL type = getHandler().getType();
        if (!type.isEmptyType()) {
            //TODO: Unify this code some, as there is a lot of code we have for drawing "tiled" but it is duplicated all over the place
            int scale = (int) (getHandler().getLevel() * (width - 2));
            MekanismRenderer.color(type);
            TextureAtlasSprite icon = MekanismRenderer.getChemicalTexture(type);
            minecraft.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            int start = 0;
            int x = this.x + 1;
            int y = this.y + 1;
            while (scale > 0) {
                int renderRemaining;
                if (scale > 16) {
                    renderRemaining = 16;
                    scale -= 16;
                } else {
                    renderRemaining = scale;
                    scale = 0;
                }
                //TODO: We should be able to unify GuiVerticalChemicalBar and this if we make it so it can properly extend the tiling in
                // both directions
                guiObj.drawTexturedRectFromIcon(x + start, y, icon, renderRemaining, height - 2);
                start += 16;
            }
            MekanismRenderer.resetColor();
        }
    }
}