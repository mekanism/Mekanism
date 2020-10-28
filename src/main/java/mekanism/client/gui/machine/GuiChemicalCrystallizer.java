package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.merged.MergedChemicalTank.Current;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.custom.GuiCrystallizerScreen;
import mekanism.client.gui.element.custom.GuiCrystallizerScreen.IOreInfo;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiMergedChemicalTankGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.machine.TileEntityChemicalCrystallizer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiChemicalCrystallizer extends GuiConfigurableTile<TileEntityChemicalCrystallizer, MekanismTileContainer<TileEntityChemicalCrystallizer>> {

    private GuiCrystallizerScreen crystallizerScreen;

    public GuiChemicalCrystallizer(MekanismTileContainer<TileEntityChemicalCrystallizer> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        titleY = 4;
    }

    @Override
    public void init() {
        super.init();
        addButton(crystallizerScreen = new GuiCrystallizerScreen(this, 31, 13, new IOreInfo() {
            @Nonnull
            @Override
            public BoxedChemicalStack getInputChemical() {
                Current current = tile.inputTank.getCurrent();
                return current == Current.EMPTY ? BoxedChemicalStack.EMPTY : BoxedChemicalStack.box(tile.inputTank.getTankFromCurrent(current).getStack());
            }

            @Nullable
            @Override
            public ChemicalCrystallizerRecipe getRecipe() {
                CachedRecipe<ChemicalCrystallizerRecipe> cachedRecipe = tile.getUpdatedCache(0);
                return cachedRecipe == null ? null : cachedRecipe.getRecipe();
            }
        }));
        addButton(new GuiSecurityTab(this, tile));
        addButton(new GuiRedstoneControlTab(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 157, 23));
        addButton(new GuiEnergyTab(tile.getEnergyContainer(), tile::getActive, this));
        addButton(new GuiMergedChemicalTankGauge<>(() -> tile.inputTank, () -> tile, GaugeType.STANDARD, this, 7, 4));
        addButton(new GuiProgress(tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 53, 61).jeiCategory(tile));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        crystallizerScreen.tick();
    }
}