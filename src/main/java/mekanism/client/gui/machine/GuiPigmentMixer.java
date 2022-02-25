package mekanism.client.gui.machine;

import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.ref.WeakReference;
import javax.annotation.Nonnull;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiPigmentGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.GuiProgress.ColorDetails;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.tile.machine.TileEntityPigmentMixer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiPigmentMixer extends GuiConfigurableTile<TileEntityPigmentMixer, MekanismTileContainer<TileEntityPigmentMixer>> {

    public GuiPigmentMixer(MekanismTileContainer<TileEntityPigmentMixer> container, Inventory inv, Component title) {
        super(container, inv, title);
        inventoryLabelY += 2;
        titleLabelX = 5;
        titleLabelY = 5;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiHorizontalPowerBar(this, tile.getEnergyContainer(), 115, 75))
              .warning(WarningType.NOT_ENOUGH_ENERGY, tile.getWarningCheck(RecipeError.NOT_ENOUGH_ENERGY))
              .warning(WarningType.NOT_ENOUGH_ENERGY_REDUCED_RATE, tile.getWarningCheck(RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE));
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getEnergyUsed));
        addRenderableWidget(new GuiPigmentGauge(() -> tile.leftInputTank, () -> tile.getPigmentTanks(null), GaugeType.STANDARD, this, 25, 13))
              .warning(WarningType.NO_MATCHING_RECIPE, tile.getWarningCheck(RecipeError.NOT_ENOUGH_LEFT_INPUT));
        addRenderableWidget(new GuiPigmentGauge(() -> tile.outputTank, () -> tile.getPigmentTanks(null), GaugeType.STANDARD, this, 79, 4))
              .warning(WarningType.NO_SPACE_IN_OUTPUT, tile.getWarningCheck(RecipeError.NOT_ENOUGH_OUTPUT_SPACE));
        addRenderableWidget(new GuiPigmentGauge(() -> tile.rightInputTank, () -> tile.getPigmentTanks(null), GaugeType.STANDARD, this, 133, 13))
              .warning(WarningType.NO_MATCHING_RECIPE, tile.getWarningCheck(RecipeError.NOT_ENOUGH_RIGHT_INPUT));
        addRenderableWidget(new GuiProgress(tile::getActive, ProgressType.SMALL_RIGHT, this, 47, 39).jeiCategory(tile).colored(new LeftColorDetails()))
              .warning(WarningType.INPUT_DOESNT_PRODUCE_OUTPUT, tile.getWarningCheck(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT));
        addRenderableWidget(new GuiProgress(tile::getActive, ProgressType.SMALL_LEFT, this, 101, 39).jeiCategory(tile).colored(new RightColorDetails()))
              .warning(WarningType.INPUT_DOESNT_PRODUCE_OUTPUT, tile.getWarningCheck(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT));
    }

    @Override
    protected void drawForegroundText(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        drawString(matrix, title, titleLabelX, titleLabelY, titleTextColor());
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    private class LeftColorDetails extends PigmentColorDetails {

        @Override
        public int getColorFrom() {
            return tile == null ? 0xFFFFFFFF : getColor(tile.leftInputTank.getType().getColorRepresentation());
        }
    }

    private class RightColorDetails extends PigmentColorDetails {

        @Override
        public int getColorFrom() {
            return tile == null ? 0xFFFFFFFF : getColor(tile.rightInputTank.getType().getColorRepresentation());
        }
    }

    private abstract class PigmentColorDetails implements ColorDetails {

        private WeakReference<PigmentMixingRecipe> cachedRecipe;

        @Override
        public abstract int getColorFrom();

        @Override
        public int getColorTo() {
            if (tile == null) {
                //Should never actually be null, but just in case check it to make intellij happy
                return 0xFFFFFFFF;
            }
            if (tile.outputTank.isEmpty()) {
                //If the pigment tank is empty, try looking up the recipe and grabbing the color from it
                if (!tile.leftInputTank.isEmpty() && !tile.rightInputTank.isEmpty()) {
                    PigmentStack leftInput = tile.leftInputTank.getStack();
                    PigmentStack rightInput = tile.rightInputTank.getStack();
                    PigmentMixingRecipe recipe;
                    if (cachedRecipe == null) {
                        recipe = getRecipeAndCache();
                    } else {
                        recipe = cachedRecipe.get();
                        if (recipe == null || !isValid(recipe, leftInput, rightInput)) {
                            recipe = getRecipeAndCache();
                        }
                    }
                    if (recipe != null) {
                        return getColor(recipe.getOutput(leftInput, rightInput).getChemicalColorRepresentation());
                    }
                }
                return 0xFFFFFFFF;
            }
            return getColor(tile.outputTank.getType().getColorRepresentation());
        }

        private PigmentMixingRecipe getRecipeAndCache() {
            PigmentMixingRecipe recipe = tile.getRecipe(0);
            if (recipe == null) {
                cachedRecipe = null;
            } else {
                cachedRecipe = new WeakReference<>(recipe);
            }
            return recipe;
        }

        private boolean isValid(PigmentMixingRecipe recipe, PigmentStack leftInput, PigmentStack rightInput) {
            return (recipe.getLeftInput().testType(leftInput) && recipe.getRightInput().testType(rightInput)) ||
                   (recipe.getLeftInput().testType(rightInput) && recipe.getRightInput().testType(leftInput));
        }

        protected int getColor(int tint) {
            if ((tint & 0xFF000000) == 0) {
                return 0xFF000000 | tint;
            }
            return tint;
        }
    }
}