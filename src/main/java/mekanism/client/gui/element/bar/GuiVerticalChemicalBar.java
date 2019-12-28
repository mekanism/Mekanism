package mekanism.client.gui.element.bar;

import javax.annotation.Nonnull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTank;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiVerticalChemicalBar.ChemicalInfoProvider;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiVerticalChemicalBar<CHEMICAL extends Chemical<CHEMICAL>> extends GuiVerticalBar<ChemicalInfoProvider<CHEMICAL>> {

    private static final int texWidth = 4;
    private static final int texHeight = 52;

    public GuiVerticalChemicalBar(IGuiWrapper gui, ChemicalInfoProvider<CHEMICAL> infoProvider, ResourceLocation def, int x, int y) {
        super(PlayerContainer.field_226615_c_, gui, infoProvider, def, x, y, texWidth + 2, texHeight + 2);
    }

    @Override
    protected void renderBarOverlay(int mouseX, int mouseY, float partialTicks) {
        CHEMICAL type = getHandler().getType();
        if (!type.isEmptyType()) {
            int displayInt = (int) (getHandler().getLevel() * texHeight);
            MekanismRenderer.color(type);
            guiObj.drawTexturedRectFromIcon(x + 1, y + 1 + (texHeight - displayInt), MekanismRenderer.getChemicalTexture(type), texWidth, displayInt);
            MekanismRenderer.resetColor();
        }
    }

    //Note the GuiBar.IBarInfoHandler is needed, as it cannot compile and resolve just IBarInfoHandler
    public interface ChemicalInfoProvider<CHEMICAL extends Chemical<CHEMICAL>> extends GuiVerticalBar.IBarInfoHandler {

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