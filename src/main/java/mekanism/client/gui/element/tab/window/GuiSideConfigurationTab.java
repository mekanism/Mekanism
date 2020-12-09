package mekanism.client.gui.element.tab.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.gui.element.window.GuiSideConfiguration;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

public class GuiSideConfigurationTab<TILE extends TileEntityMekanism & ISideConfiguration> extends GuiWindowCreatorTab<TILE, GuiSideConfigurationTab<TILE>> {

    public GuiSideConfigurationTab(IGuiWrapper gui, TILE tile, Supplier<GuiSideConfigurationTab<TILE>> elementSupplier) {
        super(MekanismUtils.getResource(ResourceType.GUI, "configuration.png"), gui, tile, -26, 6, 26, 18, true, elementSupplier);
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        displayTooltip(matrix, MekanismLang.SIDE_CONFIG.translate(), mouseX, mouseY);
    }

    @Override
    protected void colorTab() {
        MekanismRenderer.color(SpecialColors.TAB_CONFIGURATION);
    }

    @Override
    protected GuiWindow createWindow() {
        return new GuiSideConfiguration<>(guiObj, guiObj.getWidth() / 2 - 156 / 2, 15, dataSource);
    }
}