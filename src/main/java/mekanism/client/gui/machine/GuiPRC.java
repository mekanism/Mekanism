package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.machine.TileEntityPressurizedReactionChamber;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiPRC extends GuiConfigurableTile<TileEntityPressurizedReactionChamber, MekanismTileContainer<TileEntityPressurizedReactionChamber>> {

    public GuiPRC(MekanismTileContainer<TileEntityPressurizedReactionChamber> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiRedstoneControlTab(this, tile));
        func_230480_a_(new GuiSecurityTab<>(this, tile));
        func_230480_a_(new GuiUpgradeTab(this, tile));
        func_230480_a_(new GuiEnergyTab(tile.getEnergyContainer(), this));
        func_230480_a_(new GuiFluidGauge(() -> tile.inputFluidTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 5, 10));
        func_230480_a_(new GuiGasGauge(() -> tile.inputGasTank, () -> tile.getGasTanks(null), GaugeType.STANDARD, this, 28, 10));
        func_230480_a_(new GuiGasGauge(() -> tile.outputGasTank, () -> tile.getGasTanks(null), GaugeType.SMALL, this, 140, 40));
        func_230480_a_(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 163, 16));
        func_230480_a_(new GuiProgress(tile::getScaledProgress, ProgressType.RIGHT, this, 77, 38).jeiCategory(tile));
    }

    @Override
    protected void func_230451_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTextScaledBound(matrix, tile.getName(), getXSize() / 3 - 7, 6, titleTextColor(), 2 * getXSize() / 3);
        drawString(matrix, MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, titleTextColor());
        super.func_230451_b_(matrix, mouseX, mouseY);
    }
}