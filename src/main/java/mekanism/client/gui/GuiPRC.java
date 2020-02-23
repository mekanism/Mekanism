package mekanism.client.gui;

import java.util.Arrays;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityPressurizedReactionChamber;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiPRC extends GuiMekanismTile<TileEntityPressurizedReactionChamber, MekanismTileContainer<TileEntityPressurizedReactionChamber>> {

    public GuiPRC(MekanismTileContainer<TileEntityPressurizedReactionChamber> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiSideConfigurationTab(this, tile));
        addButton(new GuiTransporterConfigTab(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiEnergyInfo(() -> {
            //TODO: Use a getter for the cached recipe
            CachedRecipe<PressurizedReactionRecipe> recipe = tile.getUpdatedCache(0);
            double extra = recipe == null ? 0 : recipe.getRecipe().getEnergyRequired();
            double energyPerTick = MekanismUtils.getEnergyPerTick(tile, tile.getBaseStorage() + extra);
            return Arrays.asList(MekanismLang.USING.translate(EnergyDisplay.of(energyPerTick)),
                  MekanismLang.NEEDED.translate(EnergyDisplay.of(tile.getNeededEnergy())));
        }, this));
        addButton(new GuiFluidGauge(() -> tile.inputFluidTank, GaugeType.STANDARD_YELLOW, this, 5, 10));
        addButton(new GuiGasGauge(() -> tile.inputGasTank, GaugeType.STANDARD_RED, this, 28, 10));
        addButton(new GuiGasGauge(() -> tile.outputGasTank, GaugeType.SMALL_BLUE, this, 140, 40));
        addButton(new GuiVerticalPowerBar(this, tile, 164, 15));
        addButton(new GuiProgress(tile::getScaledProgress, ProgressType.RIGHT, this, 77, 38));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}