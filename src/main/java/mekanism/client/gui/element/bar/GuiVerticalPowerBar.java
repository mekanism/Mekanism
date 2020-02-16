package mekanism.client.gui.element.bar;

import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiVerticalPowerBar extends GuiVerticalBar<IBarInfoHandler> {

    private static final ResourceLocation ENERGY_BAR = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "vertical_power_bar.png");
    private static final int texWidth = 4;
    private static final int texHeight = 52;

    //TODO: For this and elements like it we should not allow clicking them even if the on click does nothing (we don't want a click sound to be made)
    public GuiVerticalPowerBar(IGuiWrapper gui, IStrictEnergyStorage tile, int x, int y) {
        super(ENERGY_BAR, gui, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy()).getTextComponent();
            }

            @Override
            public double getLevel() {
                return tile.getEnergy() / tile.getMaxEnergy();
            }
        }, x, y, texWidth + 2, texHeight + 2);
    }

    public GuiVerticalPowerBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y) {
        super(ENERGY_BAR, gui, handler, x, y, texWidth + 2, texHeight + 2);
    }

    @Override
    protected void renderBarOverlay(int mouseX, int mouseY, float partialTicks) {
        int displayInt = (int) (getHandler().getLevel() * texHeight);
        //TODO: Fix this, broke it while trying to make reading the numbers cleaner
        guiObj.drawModalRectWithCustomSizedTexture(x + 1, y + 1 + (texHeight - displayInt), 0, 0, texWidth, displayInt, texWidth, texHeight);
    }
}