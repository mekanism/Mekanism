package mekanism.client.gui.element.tab.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiTransporterConfig;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

public class GuiTransporterConfigTab<TILE extends TileEntityMekanism & ISideConfiguration> extends GuiWindowCreatorTab<TILE, GuiTransporterConfigTab<TILE>> {

    public GuiTransporterConfigTab(IGuiWrapper gui, TILE tile, Supplier<GuiTransporterConfigTab<TILE>> elementSupplier) {
        super(MekanismUtils.getResource(ResourceType.GUI, "transporter_config.png"), gui, tile, -26, 34, 26, 18, true, elementSupplier);
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        displayTooltip(matrix, MekanismLang.TRANSPORTER_CONFIG.translate(), mouseX, mouseY);
    }

    @Override
    protected void colorTab() {
        MekanismRenderer.color(SpecialColors.TAB_TRANSPORTER);
    }

    @Override
    protected GuiWindow createWindow() {
        return new GuiTransporterConfig<>(guiObj, guiObj.getWidth() / 2 - 156 / 2, 15, dataSource);
    }
}