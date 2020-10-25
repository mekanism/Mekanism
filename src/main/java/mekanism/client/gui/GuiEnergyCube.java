package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import javax.annotation.Nonnull;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiEnergyCube extends GuiConfigurableTile<TileEntityEnergyCube, MekanismTileContainer<TileEntityEnergyCube>> {

    public GuiEnergyCube(MekanismTileContainer<TileEntityEnergyCube> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiRedstoneControlTab(this, tile));
        addButton(new GuiSecurityTab(this, tile));
        addButton(new GuiEnergyGauge(tile.getEnergyContainer(), GaugeType.WIDE, this, 55, 18));
        addButton(new GuiEnergyTab(() -> Arrays.asList(MekanismLang.MATRIX_INPUT_RATE.translate(EnergyDisplay.of(tile.getInputRate())),
              MekanismLang.MAX_OUTPUT.translate(EnergyDisplay.of(tile.getTier().getOutput()))), this));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventory.getDisplayName(), playerInventoryTitleX, playerInventoryTitleY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}