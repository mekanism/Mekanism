package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import javax.annotation.Nonnull;
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
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.machine.TileEntityChemicalWasher;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiChemicalWasher extends GuiConfigurableTile<TileEntityChemicalWasher, MekanismTileContainer<TileEntityChemicalWasher>> {

    public GuiChemicalWasher(MekanismTileContainer<TileEntityChemicalWasher> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void initPreSlots() {
        //Add the side holder before the slots, as it holds a couple of the slots
        func_230480_a_(new GuiSideHolder(this, getXSize(), 66, 57, false));
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiDownArrow(this, getXSize() + 8, 91));
        func_230480_a_(new GuiSecurityTab<>(this, tile));
        func_230480_a_(new GuiRedstoneControlTab(this, tile));
        func_230480_a_(new GuiUpgradeTab(this, tile));
        func_230480_a_(new GuiHorizontalPowerBar(this, tile.getEnergyContainer(), 115, 75));
        func_230480_a_(new GuiEnergyTab(() -> Arrays.asList(MekanismLang.USING.translate(EnergyDisplay.of(tile.clientEnergyUsed)),
              MekanismLang.NEEDED.translate(EnergyDisplay.of(tile.getEnergyContainer().getNeeded()))), this));
        func_230480_a_(new GuiFluidGauge(() -> tile.fluidTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 7, 13));
        func_230480_a_(new GuiSlurryGauge(() -> tile.inputTank, () -> tile.getSlurryTanks(null), GaugeType.STANDARD, this, 28, 13));
        func_230480_a_(new GuiSlurryGauge(() -> tile.outputTank, () -> tile.getSlurryTanks(null), GaugeType.STANDARD, this, 131, 13));
        func_230480_a_(new GuiProgress(() -> tile.getActive() ? 1 : 0, ProgressType.LARGE_RIGHT, this, 64, 39).jeiCategory(tile));
    }

    @Override
    protected void func_230451_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix, 4);
        super.func_230451_b_(matrix, mouseX, mouseY);
    }
}