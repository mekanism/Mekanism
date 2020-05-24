package mekanism.client.gui.element.tab;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiWindow;
import mekanism.client.gui.element.custom.GuiTransporterConfig;
import mekanism.common.MekanismLang;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

public class GuiTransporterConfigTab extends GuiWindowCreatorTab {

    public GuiTransporterConfigTab(IGuiWrapper gui, TileEntityMekanism tile) {
        super(MekanismUtils.getResource(ResourceType.GUI, "transporter_config.png"), gui, tile, -26, 34, 26, 18, true);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        displayTooltip(MekanismLang.TRANSPORTER_CONFIG.translate(), mouseX, mouseY);
    }

    @Override
    public GuiWindow createWindow() {
        return new GuiTransporterConfig(guiObj, guiObj.getWidth() / 2 - 156 / 2, 15, tile);
    }
}