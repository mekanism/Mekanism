package mekanism.generators.client.gui;

import java.util.Arrays;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiGasGenerator extends GuiMekanismTile<TileEntityGasGenerator, MekanismTileContainer<TileEntityGasGenerator>> {

    public GuiGasGenerator(MekanismTileContainer<TileEntityGasGenerator> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiRedstoneControlTab(this, tile));
        func_230480_a_(new GuiSecurityTab<>(this, tile));
        func_230480_a_(new GuiEnergyTab(() -> Arrays.asList(
              GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(tile.getGenerationRate().multiply(tile.getUsed()).multiply(tile.getMaxBurnTicks()))),
              MekanismLang.MAX_OUTPUT.translate(EnergyDisplay.of(tile.getMaxOutput()))), this));
        func_230480_a_(new GuiGasGauge(() -> tile.fuelTank, () -> tile.getGasTanks(null), GaugeType.WIDE, this, 55, 18));
        func_230480_a_(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText();
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, titleTextColor());
        ITextComponent component = GeneratorsLang.GAS_BURN_RATE.translate(tile.getUsed());
        drawString(component, getXSize() - 8 - getStringWidth(component), (getYSize() - 96) + 2, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}