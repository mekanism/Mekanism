package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.SpecialColors;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiSlurryGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.machine.TileEntityChemicalWasher;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiChemicalWasher extends GuiConfigurableTile<TileEntityChemicalWasher, MekanismTileContainer<TileEntityChemicalWasher>> {

    public GuiChemicalWasher(MekanismTileContainer<TileEntityChemicalWasher> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        titleY = 4;
    }

    @Override
    protected void initPreSlots() {
        //Add the side holder before the slots, as it holds a couple of the slots
        addButton(GuiSideHolder.create(this, xSize, 66, 57, false, SpecialColors.TAB_CHEMICAL_WASHER));
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiDownArrow(this, xSize + 8, 91));
        addButton(new GuiSecurityTab(this, tile));
        addButton(new GuiRedstoneControlTab(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiHorizontalPowerBar(this, tile.getEnergyContainer(), 115, 75));
        addButton(new GuiEnergyTab(tile.getEnergyContainer(), () -> tile.clientEnergyUsed, this));
        addButton(new GuiFluidGauge(() -> tile.fluidTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 7, 13));
        addButton(new GuiSlurryGauge(() -> tile.inputTank, () -> tile.getSlurryTanks(null), GaugeType.STANDARD, this, 28, 13));
        addButton(new GuiSlurryGauge(() -> tile.outputTank, () -> tile.getSlurryTanks(null), GaugeType.STANDARD, this, 131, 13));
        addButton(new GuiProgress(tile::getActive, ProgressType.LARGE_RIGHT, this, 64, 39).jeiCategory(tile));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}