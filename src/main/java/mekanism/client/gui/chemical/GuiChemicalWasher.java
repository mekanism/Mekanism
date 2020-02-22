package mekanism.client.gui.chemical;

import java.util.Arrays;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiBucketIO;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityChemicalWasher;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;

public class GuiChemicalWasher extends GuiMekanismTile<TileEntityChemicalWasher, MekanismTileContainer<TileEntityChemicalWasher>> {

    private static final ItemStack BUCKET = new ItemStack(Items.WATER_BUCKET);

    public GuiChemicalWasher(MekanismTileContainer<TileEntityChemicalWasher> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiHorizontalPowerBar(this, tile, 115, 75));
        addButton(new GuiBucketIO(this));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(MekanismLang.USING.translate(EnergyDisplay.of(tile.clientEnergyUsed)),
              MekanismLang.NEEDED.translate(EnergyDisplay.of(tile.getNeededEnergy()))), this));
        addButton(new GuiFluidGauge(() -> tile.fluidTank, GaugeType.STANDARD, this, 5, 4));
        addButton(new GuiGasGauge(() -> tile.inputTank, GaugeType.STANDARD, this, 26, 13));
        addButton(new GuiGasGauge(() -> tile.outputTank, GaugeType.STANDARD, this, 133, 13));
        addButton(new GuiProgress(() -> tile.getActive() ? 1 : 0, ProgressBar.LARGE_RIGHT, this, 62, 38));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        drawTexturedRect(getGuiLeft() + 116, getGuiTop() + 76, 176, 0, tile.getScaledEnergyLevel(52), 4);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), 45, 4, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        renderItem(BUCKET, 6, 65);
    }
}