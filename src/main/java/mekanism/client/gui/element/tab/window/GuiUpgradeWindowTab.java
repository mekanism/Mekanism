package mekanism.client.gui.element.tab.window;

import java.util.function.Supplier;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiUpgradeWindow;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.tile.base.TileEntityMekanism;

public class GuiUpgradeWindowTab extends GuiWindowCreatorTab<TileEntityMekanism, GuiUpgradeWindowTab> {

    private static final SelectedWindowData WINDOW_DATA = new SelectedWindowData(WindowType.UPGRADE);

    public GuiUpgradeWindowTab(IGuiWrapper gui, TileEntityMekanism tile, Supplier<GuiUpgradeWindowTab> elementSupplier) {
        super(Mekanism.rl("button/upgrade"), gui, tile, gui.getXSize(), 6, 26, 18, false, elementSupplier);
        setTooltip(MekanismLang.UPGRADES);
    }

    @Override
    protected int getTabColor() {
        return SpecialColors.TAB_UPGRADE.argb();
    }

    @Override
    protected GuiWindow createWindow(SelectedWindowData windowData) {
        return new GuiUpgradeWindow(gui(), (getGuiWidth() - 198) / 2, 15, dataSource, windowData);
    }

    @Override
    protected SelectedWindowData getNextWindowData() {
        return WINDOW_DATA;
    }
}