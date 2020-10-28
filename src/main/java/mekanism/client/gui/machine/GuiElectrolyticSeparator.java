package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.GuiGasMode;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.machine.TileEntityElectrolyticSeparator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiElectrolyticSeparator extends GuiConfigurableTile<TileEntityElectrolyticSeparator, MekanismTileContainer<TileEntityElectrolyticSeparator>> {

    public GuiElectrolyticSeparator(MekanismTileContainer<TileEntityElectrolyticSeparator> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiRedstoneControlTab(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiEnergyTab(tile.getEnergyContainer(), () -> tile.clientEnergyUsed, this));
        addButton(new GuiFluidGauge(() -> tile.fluidTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 5, 10));
        addButton(new GuiGasGauge(() -> tile.leftTank, () -> tile.getGasTanks(null), GaugeType.SMALL, this, 58, 18));
        addButton(new GuiGasGauge(() -> tile.rightTank, () -> tile.getGasTanks(null), GaugeType.SMALL, this, 100, 18));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        addButton(new GuiSecurityTab(this, tile));
        addButton(new GuiProgress(tile::getActive, ProgressType.BI, this, 80, 30).jeiCategory(tile));
        addButton(new GuiGasMode(this, guiLeft + 7, guiTop + 72, false, () -> tile.dumpLeft, tile.getPos(), 0));
        addButton(new GuiGasMode(this, guiLeft + 159, guiTop + 72, true, () -> tile.dumpRight, tile.getPos(), 1));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}