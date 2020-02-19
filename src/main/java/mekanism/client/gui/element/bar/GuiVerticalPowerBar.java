package mekanism.client.gui.element.bar;

import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiVerticalPowerBar extends GuiBar<IBarInfoHandler> {

    private static final ResourceLocation ENERGY_BAR = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "vertical_power_bar.png");
    private static final int texWidth = 4;
    private static final int texHeight = 52;

    private final double heightScale;

    public GuiVerticalPowerBar(IGuiWrapper gui, IStrictEnergyStorage tile, int x, int y) {
        this(gui, tile, x, y, texHeight);
    }

    public GuiVerticalPowerBar(IGuiWrapper gui, IStrictEnergyStorage tile, int x, int y, int desiredHeight) {
        this(gui, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy()).getTextComponent();
            }

            @Override
            public double getLevel() {
                return tile.getEnergy() / tile.getMaxEnergy();
            }
        }, x, y, desiredHeight);
    }

    public GuiVerticalPowerBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y) {
        this(gui, handler, x, y, texHeight);
    }

    public GuiVerticalPowerBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int desiredHeight) {
        super(ENERGY_BAR, gui, handler, x, y, texWidth, desiredHeight);
        heightScale = desiredHeight / (double) texHeight;
    }

    @Override
    protected void renderBarOverlay(int mouseX, int mouseY, float partialTicks) {
        int displayInt = (int) (getHandler().getLevel() * texHeight);
        int scaled = calculateScaled(heightScale, displayInt);
        guiObj.drawModalRectWithCustomSizedTexture(x + 1, y + height - 1 - scaled, texWidth, scaled, 0, 0, texWidth, displayInt, texWidth, texHeight);
    }
}