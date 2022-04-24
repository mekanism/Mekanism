package mekanism.client.gui.element.tab.window;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiUpgradeWindow;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

public class GuiUpgradeWindowTab extends GuiWindowCreatorTab<TileEntityMekanism, GuiUpgradeWindowTab> {

    public GuiUpgradeWindowTab(IGuiWrapper gui, TileEntityMekanism tile, Supplier<GuiUpgradeWindowTab> elementSupplier) {
        super(MekanismUtils.getResource(ResourceType.GUI, "upgrade.png"), gui, tile, gui.getWidth(), 6, 26, 18, false, elementSupplier);
    }

    @Override
    public void renderToolTip(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        displayTooltips(matrix, mouseX, mouseY, MekanismLang.UPGRADES.translate());
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