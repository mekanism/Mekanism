package mekanism.client.gui.element.tab;

import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiWindow;
import mekanism.client.gui.element.custom.GuiSideConfiguration;
import mekanism.common.MekanismLang;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

public class GuiSideConfigurationTab extends GuiWindowCreatorTab<GuiSideConfigurationTab> {

    public GuiSideConfigurationTab(IGuiWrapper gui, TileEntityMekanism tile, Supplier<GuiSideConfigurationTab> elementSupplier) {
        super(MekanismUtils.getResource(ResourceType.GUI, "configuration.png"), gui, tile, -26, 6, 26, 18, true, elementSupplier);
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