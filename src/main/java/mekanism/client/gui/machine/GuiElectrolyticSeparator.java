package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
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
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.machine.TileEntityElectrolyticSeparator;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiElectrolyticSeparator extends GuiConfigurableTile<TileEntityElectrolyticSeparator, MekanismTileContainer<TileEntityElectrolyticSeparator>> {

    public GuiElectrolyticSeparator(MekanismTileContainer<TileEntityElectrolyticSeparator> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiRedstoneControlTab(this, tile));
        func_230480_a_(new GuiUpgradeTab(this, tile));
        func_230480_a_(new GuiEnergyTab(() -> Arrays.asList(MekanismLang.USING.translate(EnergyDisplay.of(tile.clientEnergyUsed)),
              MekanismLang.NEEDED.translate(EnergyDisplay.of(tile.getEnergyContainer().getNeeded()))), this));
        func_230480_a_(new GuiFluidGauge(() -> tile.fluidTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 5, 10));
        func_230480_a_(new GuiGasGauge(() -> tile.leftTank, () -> tile.getGasTanks(null), GaugeType.SMALL, this, 58, 18));
        func_230480_a_(new GuiGasGauge(() -> tile.rightTank, () -> tile.getGasTanks(null), GaugeType.SMALL, this, 100, 18));
        func_230480_a_(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        func_230480_a_(new GuiSecurityTab<>(this, tile));
        func_230480_a_(new GuiProgress(() -> tile.getActive() ? 1 : 0, ProgressType.BI, this, 80, 30).jeiCategory(tile));
        func_230480_a_(new GuiGasMode(this, getGuiLeft() + 7, getGuiTop() + 72, false, () -> tile.dumpLeft, tile.getPos(), 0));
        func_230480_a_(new GuiGasMode(this, getGuiLeft() + 159, getGuiTop() + 72, true, () -> tile.dumpRight, tile.getPos(), 1));
    }

    @Override
    protected void func_230451_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        super.func_230451_b_(matrix, mouseX, mouseY);
    }
}