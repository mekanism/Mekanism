package mekanism.client.gui.element.bar;

import javax.annotation.Nonnull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTank;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiVerticalChemicalBar.ChemicalInfoProvider;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;

public class GuiVerticalChemicalBar<CHEMICAL extends Chemical<CHEMICAL>> extends GuiBar<ChemicalInfoProvider<CHEMICAL>> {

    public GuiVerticalChemicalBar(IGuiWrapper gui, ChemicalInfoProvider<CHEMICAL> infoProvider, int x, int y) {
        super(AtlasTexture.LOCATION_BLOCKS_TEXTURE, gui, infoProvider, x, y, 4, 52);
    }

    @Override
    protected void renderBarOverlay(int mouseX, int mouseY, float partialTicks) {
        CHEMICAL type = getHandler().getType();
        if (!type.isEmptyType()) {
            //TODO: Unify this code some, as there is a lot of code we have for drawing "tiled" but it is duplicated all over the place
            int scale = (int) (getHandler().getLevel() * (height - 2));
            MekanismRenderer.color(type);
            TextureAtlasSprite icon = MekanismRenderer.getChemicalTexture(type);
            minecraft.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            int start = 0;
            int x = this.x + 1;
            int y = this.y + height - 1;
            while (scale > 0) {
                int renderRemaining;
                if (scale > 16) {
                    renderRemaining = 16;
                    scale -= 16;
                } else {
                    renderRemaining = scale;
                    scale = 0;
                }
                guiObj.drawTexturedRectFromIcon(x, y - renderRemaining - start, icon, width - 2, renderRemaining);
                start += 16;
            }
            MekanismRenderer.resetColor();
        }
    }

    //Note the GuiBar.IBarInfoHandler is needed, as it cannot compile and resolve just IBarInfoHandler
    public interface ChemicalInfoProvider<CHEMICAL extends Chemical<CHEMICAL>> extends GuiBar.IBarInfoHandler {

        @Nonnull
        CHEMICAL getType();
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> ChemicalInfoProvider<CHEMICAL> getProvider(ChemicalTank<CHEMICAL, STACK> tank) {
        return new ChemicalInfoProvider<CHEMICAL>() {
            @Nonnull
            @Override
            public CHEMICAL getType() {
                return tank.getType();
            }

            @Override
            public ITextComponent getTooltip() {
                if (tank.isEmpty()) {
                    return MekanismLang.EMPTY.translate();
                }
                return MekanismLang.GENERIC_STORED.translate(tank.getType(), tank.getStored());
            }

            @Override
            public double getLevel() {
                return (double) tank.getStored() / (double) tank.getCapacity();
            }
        };
    }
}