package mekanism.client.gui.machine;

import java.lang.ref.WeakReference;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.GuiProgress.ColorDetails;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.tile.machine.TileEntityPigmentExtractor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public class GuiPigmentExtractor extends GuiConfigurableTile<TileEntityPigmentExtractor, MekanismTileContainer<TileEntityPigmentExtractor>> {

    private static final int ENERGY_BAR_X = 115;

    public GuiPigmentExtractor(MekanismTileContainer<TileEntityPigmentExtractor> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiHorizontalPowerBar(this, tile.energyContainer(), ENERGY_BAR_X, 75))
              .warning(WarningType.NOT_ENOUGH_ENERGY, tile.getWarningCheck(RecipeError.NOT_ENOUGH_ENERGY));
        addRenderableWidget(new GuiEnergyTab(this, tile.energyContainer(), tile::getActive));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.pigmentTank, tile::getChemicalTanks, GaugeType.STANDARD, this, 131, 13))
              .warning(WarningType.NO_SPACE_IN_OUTPUT, tile.getWarningCheck(RecipeError.NOT_ENOUGH_OUTPUT_SPACE));
        addRenderableWidget(new GuiProgress(tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 64, 40).recipeViewerCategory(tile).colored(new PigmentColorDetails()))
              .warning(WarningType.INPUT_DOESNT_PRODUCE_OUTPUT, tile.getWarningCheck(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT));
    }

    @Override
    protected void drawForegroundText(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics, ENERGY_BAR_X);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    private class PigmentColorDetails implements ColorDetails {

        @Nullable
        private WeakReference<ItemStackToChemicalRecipe> cachedRecipe;

        @Override
        public int getColorFrom() {
            return CommonColors.WHITE;
        }

        @Override
        public int getColorTo() {
            if (tile.pigmentTank.isEmpty()) {
                //If the pigment tank is empty, try looking up the recipe and grabbing the color from it
                IInventorySlot inputSlot = tile.getInputSlot();
                if (!inputSlot.isEmpty()) {
                    ItemResource input = inputSlot.resource();
                    ItemStackToChemicalRecipe recipe;
                    if (cachedRecipe == null) {
                        recipe = getRecipeAndCache();
                    } else {
                        recipe = cachedRecipe.get();
                        if (recipe == null || !recipe.getInput().testType(input)) {
                            recipe = getRecipeAndCache();
                        }
                    }
                    if (recipe != null) {
                        ChemicalStackTemplate output = recipe.getOutput(input.toStack(inputSlot.amountAsInt()));
                        return output.typeHolder().value().getColorRepresentation();
                    }
                }
                return CommonColors.WHITE;
            }
            return tile.pigmentTank.resource().getChemicalColorRepresentation();
        }

        @Nullable
        private ItemStackToChemicalRecipe getRecipeAndCache() {
            ItemStackToChemicalRecipe recipe = tile.getRecipe(0);
            if (recipe == null) {
                cachedRecipe = null;
            } else {
                cachedRecipe = new WeakReference<>(recipe);
            }
            return recipe;
        }
    }
}