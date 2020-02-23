package mekanism.client.gui;

import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.GasToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.gui.element.GuiCrystallizerScreen;
import mekanism.client.gui.element.GuiCrystallizerScreen.IOreInfo;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiChemicalCrystallizer extends GuiMekanismTile<TileEntityChemicalCrystallizer, MekanismTileContainer<TileEntityChemicalCrystallizer>> {

    private GuiCrystallizerScreen crystallizerScreen;

    public GuiChemicalCrystallizer(MekanismTileContainer<TileEntityChemicalCrystallizer> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(crystallizerScreen = new GuiCrystallizerScreen(this, 27, 13, new IOreInfo() {
            @Nonnull
            @Override
            public GasStack getInputGas() {
                return tile.inputTank.getStack();
            }

            @Nullable
            @Override
            public GasToItemStackRecipe getRecipe() {
                CachedRecipe<GasToItemStackRecipe> cachedRecipe = tile.getUpdatedCache(0);
                return cachedRecipe == null ? null : cachedRecipe.getRecipe();
            }
        }));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiVerticalPowerBar(this, tile, 160, 23));
        addButton(new GuiSideConfigurationTab(this, tile));
        addButton(new GuiTransporterConfigTab(this, tile));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(MekanismLang.USING.translate(EnergyDisplay.of(tile.getEnergyPerTick())),
              MekanismLang.NEEDED.translate(EnergyDisplay.of(tile.getNeededEnergy()))), this));
        addButton(new GuiGasGauge(() -> tile.inputTank, GaugeType.STANDARD, this, 5, 4));
        addButton(new GuiProgress(tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 53, 61));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), 37, 4, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        crystallizerScreen.tick();
    }
}