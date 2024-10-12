package mekanism.client.gui.element.tab.window;

import java.util.function.Supplier;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiTransporterConfig;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;

public class GuiTransporterConfigTab<TILE extends TileEntityMekanism & ISideConfiguration> extends GuiWindowCreatorTab<TILE, GuiTransporterConfigTab<TILE>> {

    private static final SelectedWindowData WINDOW_DATA = new SelectedWindowData(WindowType.TRANSPORTER_CONFIG);

    public GuiTransporterConfigTab(IGuiWrapper gui, TILE tile, Supplier<GuiTransporterConfigTab<TILE>> elementSupplier) {
        super(MekanismUtils.getResource(ResourceType.GUI, "transporter_config.png"), gui, tile, -26, 34, 26, 18, true, elementSupplier);
        setTooltip(MekanismLang.TRANSPORTER_CONFIG);
    }

    @Override
    protected void colorTab(GuiGraphics guiGraphics) {
        MekanismRenderer.color(guiGraphics, SpecialColors.TAB_TRANSPORTER);
    }

    @Override
    protected GuiWindow createWindow(SelectedWindowData windowData) {
        return new GuiTransporterConfig<>(gui(), (getGuiWidth() - 156) / 2, 15, dataSource, windowData);
    }

    @Override
    protected SelectedWindowData getNextWindowData() {
        return WINDOW_DATA;
    }
}