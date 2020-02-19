package mekanism.client.gui.element.bar;

import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiHorizontalPowerBar extends GuiBar<IBarInfoHandler> {

    private static final ResourceLocation ENERGY_BAR = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "horizontal_power_bar.png");
    private static final int texWidth = 52;
    private static final int texHeight = 4;

    private final double widthScale;

    public GuiHorizontalPowerBar(IGuiWrapper gui, IStrictEnergyStorage tile, int x, int y) {
        this(gui, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy()).getTextComponent();
            }

            @Override
            public double getLevel() {
                return tile.getEnergy() / tile.getMaxEnergy();
            }
        }, x, y, texWidth);
    }

    public GuiHorizontalPowerBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int desiredWidth) {
        super(ENERGY_BAR, gui, handler, x, y, desiredWidth, texHeight);
        widthScale = desiredWidth / (double) texWidth;
    }

    @Override
    protected void renderBarOverlay(int mouseX, int mouseY, float partialTicks) {
        int displayInt = (int) (getHandler().getLevel() * texWidth);
        guiObj.drawModalRectWithCustomSizedTexture(x + 1, y + 1, calculateScaled(widthScale, displayInt), texHeight, 0, 0, displayInt, texHeight, texWidth, texHeight);
    }
}