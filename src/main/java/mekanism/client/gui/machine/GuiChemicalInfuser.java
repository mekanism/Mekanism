package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.machine.TileEntityChemicalInfuser;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiChemicalInfuser extends GuiConfigurableTile<TileEntityChemicalInfuser, MekanismTileContainer<TileEntityChemicalInfuser>> {

    public GuiChemicalInfuser(MekanismTileContainer<TileEntityChemicalInfuser> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiRedstoneControlTab(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiHorizontalPowerBar(this, tile.getEnergyContainer(), 115, 75));
        addButton(new GuiEnergyTab(() -> Arrays.asList(MekanismLang.USING.translate(EnergyDisplay.of(tile.clientEnergyUsed)),
              MekanismLang.NEEDED.translate(EnergyDisplay.of(tile.getEnergyContainer().getNeeded()))), this));
        addButton(new GuiGasGauge(() -> tile.leftTank, () -> tile.getGasTanks(null), GaugeType.STANDARD, this, 25, 13));
        addButton(new GuiGasGauge(() -> tile.centerTank, () -> tile.getGasTanks(null), GaugeType.STANDARD, this, 79, 4));
        addButton(new GuiGasGauge(() -> tile.rightTank, () -> tile.getGasTanks(null), GaugeType.STANDARD, this, 133, 13));
        addButton(new GuiProgress(tile::getActive, ProgressType.SMALL_RIGHT, this, 47, 39).jeiCategory(tile));
        addButton(new GuiProgress(tile::getActive, ProgressType.SMALL_LEFT, this, 101, 39).jeiCategory(tile));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawString(matrix, MekanismLang.CHEMICAL_INFUSER_SHORT.translate(), 5, 5, titleTextColor());
        drawString(matrix, MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 4, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}