package mekanism.client.gui.machine;

import java.lang.ref.WeakReference;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.custom.GuiQIOCrystallizerScreen;
import mekanism.client.gui.element.custom.GuiQIOCrystallizerScreen.IOreInfo;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.tile.machine.TileEntityChemicalCrystallizer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiChemicalCrystallizer extends GuiConfigurableTile<TileEntityChemicalCrystallizer, MekanismTileContainer<TileEntityChemicalCrystallizer>> {

    private final IOreInfo oreInfo = new OreInfo();
    private GuiElement inputGauge;

    public GuiChemicalCrystallizer(MekanismTileContainer<TileEntityChemicalCrystallizer> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        titleLabelY = 4;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 157, 23))
              .warning(WarningType.NOT_ENOUGH_ENERGY, tile.getWarningCheck(RecipeError.NOT_ENOUGH_ENERGY));
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));
        inputGauge = addRenderableWidget(new GuiChemicalGauge(() -> tile.inputTank, () -> tile.getChemicalTanks(null), GaugeType.STANDARD, this, 7, 4))
              .warning(WarningType.NO_MATCHING_RECIPE, tile.getWarningCheck(RecipeError.NOT_ENOUGH_INPUT));
        addRenderableWidget(new GuiProgress(tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 53, 61).recipeViewerCategory(tile))
              .warning(WarningType.INPUT_DOESNT_PRODUCE_OUTPUT, tile.getWarningCheck(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT));
        addRenderableWidget(new GuiQIOCrystallizerScreen(this, 31, 13, 115, 42, oreInfo));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleTextWithOffset(guiGraphics, inputGauge.getRelativeRight(), tile.getEnergySlotX());
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    private class OreInfo implements IOreInfo {

        private WeakReference<ChemicalCrystallizerRecipe> cachedRecipe;

        @NotNull
        @Override
        public ChemicalStack getInputChemical() {
            return tile.inputTank.getStack();
        }

        @Nullable
        @Override
        public ChemicalCrystallizerRecipe getRecipe() {
            ChemicalStack input = getInputChemical();
            if (input.isEmpty()) {
                return null;
            }
            ChemicalCrystallizerRecipe recipe;
            if (cachedRecipe == null) {
                recipe = getRecipeAndCache();
            } else {
                recipe = cachedRecipe.get();
                if (recipe == null || !recipe.testType(input)) {
                    recipe = getRecipeAndCache();
                }
            }
            return recipe;
        }

        private ChemicalCrystallizerRecipe getRecipeAndCache() {
            ChemicalCrystallizerRecipe recipe = tile.getRecipe(0);
            if (recipe == null) {
                cachedRecipe = null;
            } else {
                cachedRecipe = new WeakReference<>(recipe);
            }
            return recipe;
        }
    }
}