package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiWindow;
import mekanism.client.gui.element.custom.GuiTransporterConfig;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

public class GuiTransporterConfigTab extends GuiWindowCreatorTab<GuiTransporterConfigTab> {

    public GuiTransporterConfigTab(IGuiWrapper gui, TileEntityMekanism tile, Supplier<GuiTransporterConfigTab> elementSupplier) {
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
        return new GuiTransporterConfig(guiObj, guiObj.getWidth() / 2 - 156 / 2, 15, dataSource);
    }
}