package mekanism.client.gui.element.bar;

import javax.annotation.Nonnull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTank;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiChemicalBar.ChemicalInfoProvider;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;

public class GuiChemicalBar<CHEMICAL extends Chemical<CHEMICAL>> extends GuiBar<ChemicalInfoProvider<CHEMICAL>> {

    private final boolean horizontal;

    public GuiChemicalBar(IGuiWrapper gui, ChemicalInfoProvider<CHEMICAL> infoProvider, int x, int y, int width, int height, boolean horizontal) {
        super(AtlasTexture.LOCATION_BLOCKS_TEXTURE, gui, infoProvider, x, y, width, height);
        this.horizontal = horizontal;
    }

    @Override
    protected void renderBarOverlay(int mouseX, int mouseY, float partialTicks) {
        CHEMICAL type = getHandler().getType();
        if (!type.isEmptyType()) {
            double level = getHandler().getLevel();
            if (level > 0) {
                MekanismRenderer.color(type);
                TextureAtlasSprite icon = MekanismRenderer.getChemicalTexture(type);
                if (horizontal) {
                    drawTiledSprite(x + 1, y + 1, height - 2, (int) (level * (width - 2)), height - 2, icon);
                } else {
                    drawTiledSprite(x + 1, y + 1, height - 2, width - 2, (int) (level * (height - 2)), icon);
                }
                MekanismRenderer.resetColor();
            }
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
                } else if (tank.getStored() == Integer.MAX_VALUE) {
                    return MekanismLang.GENERIC_STORED.translate(tank.getType(), MekanismLang.INFINITE);
                }
                return MekanismLang.GENERIC_STORED.translate(tank.getType(), tank.getStored());
            }

            @Override
            public double getLevel() {
                return tank.getStored() / (double) tank.getCapacity();
            }
        };
    }
}