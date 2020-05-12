package mekanism.client.gui.element.tab;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiWindow;
import mekanism.client.gui.element.custom.GuiSideConfiguration;
import mekanism.common.MekanismLang;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

public class GuiSideConfigurationTab extends GuiWindowCreatorTab {

    public GuiSideConfigurationTab(IGuiWrapper gui, TileEntityMekanism tile) {
        super(MekanismUtils.getResource(ResourceType.GUI, "configuration.png"), gui, tile, -26, 6, 26, 18);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        displayTooltip(MekanismLang.SIDE_CONFIG.translate(), mouseX, mouseY);
    }

    @Override
    public GuiWindow createWindow() {
        return new GuiSideConfiguration(guiObj, guiObj.getWidth() / 2 - 156 / 2, 15, tile);
    }
}