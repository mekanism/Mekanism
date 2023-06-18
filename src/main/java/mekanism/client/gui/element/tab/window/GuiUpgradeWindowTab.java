package mekanism.client.gui.element.tab.window;

import java.util.function.Supplier;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiUpgradeWindow;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class GuiUpgradeWindowTab extends GuiWindowCreatorTab<TileEntityMekanism, GuiUpgradeWindowTab> {

    public GuiUpgradeWindowTab(IGuiWrapper gui, TileEntityMekanism tile, Supplier<GuiUpgradeWindowTab> elementSupplier) {
        super(MekanismUtils.getResource(ResourceType.GUI, "upgrade.png"), gui, tile, gui.getWidth(), 6, 26, 18, false, elementSupplier);
    }

    @Override
    public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderToolTip(guiGraphics, mouseX, mouseY);
        displayTooltips(guiGraphics, mouseX, mouseY, MekanismLang.UPGRADES.translate());
    }

    @Override
    protected void colorTab() {
        MekanismRenderer.color(SpecialColors.TAB_UPGRADE);
    }

    @Override
    protected GuiWindow createWindow() {
        return new GuiUpgradeWindow(gui(), getGuiWidth() / 2 - 156 / 2, 15, dataSource);
    }
}