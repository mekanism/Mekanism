package mekanism.client.gui.machine;

import java.util.function.BooleanSupplier;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.display.recipe.CondensentratingRecipeDisplay;
import mekanism.api.recipes.display.recipe.DecondensentratingRecipeDisplay;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.button.ToggleButton;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.IProgressInfoHandler.IBooleanProgressInfoHandler;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.recipe_book.IGhostRecipeComponent;
import mekanism.client.gui.recipe_book.MekanismRecipeBookComponent;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.machine.TileEntityRotaryCondensentrator;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SlotSelectTime;
import net.minecraft.network.chat.Component;
import net.minecraft.util.TriState;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public class GuiRotaryCondensentrator extends GuiConfigurableTile<TileEntityRotaryCondensentrator, MekanismTileContainer<TileEntityRotaryCondensentrator>> {

    private static final int ENERGY_BAR_X = 115;
    private TriState lastMode = TriState.DEFAULT;

    public GuiRotaryCondensentrator(MekanismTileContainer<TileEntityRotaryCondensentrator> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        titleLabelY -= 2;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(GuiTexturedElement.downArrow(this, 9, 44));
        addRenderableWidget(GuiTexturedElement.downArrow(this, 159, 44));
        addRenderableWidget(new GuiHorizontalPowerBar(this, tile.energyContainer(), ENERGY_BAR_X, 75))
              .warning(WarningType.NOT_ENOUGH_ENERGY, tile.getWarningCheck(RecipeError.NOT_ENOUGH_ENERGY))
              .warning(WarningType.NOT_ENOUGH_ENERGY_REDUCED_RATE, tile.getWarningCheck(RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE));
        addRenderableWidget(new GuiEnergyTab(this, tile.energyContainer(), tile::getEnergyUsed));
        GuiFluidGauge fluidGauge = addRenderableWidget(new GuiFluidGauge(() -> tile.fluidTank, tile::getFluidTanks, GaugeType.STANDARD, this, 133, 13));
        fluidGauge.warning(WarningType.NO_MATCHING_RECIPE, tile.getWarningCheck(TileEntityRotaryCondensentrator.NOT_ENOUGH_FLUID_INPUT_ERROR))
              .warning(WarningType.NO_SPACE_IN_OUTPUT, tile.getWarningCheck(TileEntityRotaryCondensentrator.NOT_ENOUGH_SPACE_FLUID_OUTPUT_ERROR));
        GuiChemicalGauge chemicalGauge = addRenderableWidget(new GuiChemicalGauge(() -> tile.gasTank, tile::getChemicalTanks, GaugeType.STANDARD, this, 25, 13));
        chemicalGauge.warning(WarningType.NO_MATCHING_RECIPE, tile.getWarningCheck(TileEntityRotaryCondensentrator.NOT_ENOUGH_GAS_INPUT_ERROR))
              .warning(WarningType.NO_SPACE_IN_OUTPUT, tile.getWarningCheck(TileEntityRotaryCondensentrator.NOT_ENOUGH_SPACE_GAS_OUTPUT_ERROR));
        addRenderableWidget(new GuiProgress(new IBooleanProgressInfoHandler() {
            @Override
            public boolean fillProgressBar() {
                return tile.getActive();
            }

            @Override
            public boolean isActive() {
                return !tile.getMode();
            }

            @Override
            public boolean skipBackgroundIfInactive() {
                return true;
            }
        }, ProgressType.LARGE_RIGHT, this, 64, 39).recipeViewerCategories(RecipeViewerRecipeType.CONDENSENTRATING))
              .warning(WarningType.INPUT_DOESNT_PRODUCE_OUTPUT, tile.getWarningCheck(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT));
        addRenderableWidget(new GuiProgress(new IBooleanProgressInfoHandler() {
            @Override
            public boolean fillProgressBar() {
                return tile.getActive();
            }

            @Override
            public boolean isActive() {
                return tile.getMode();
            }

            @Override
            public boolean skipBackgroundIfInactive() {
                return true;
            }
        }, ProgressType.LARGE_LEFT, this, 64, 39).recipeViewerCategories(RecipeViewerRecipeType.DECONDENSENTRATING))
              .warning(WarningType.INPUT_DOESNT_PRODUCE_OUTPUT, tile.getWarningCheck(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT));
        addRenderableWidget(new ToggleButton(this, 4, 4, tile::getMode,
              (element, _, _) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.NEXT_MODE, ((GuiRotaryCondensentrator) element.gui()).tile))))
              .setTooltip(MekanismLang.CONDENSENTRATOR_TOGGLE);

        //Add a component to both input and output lists that properly redirects to the corresponding gauge
        DirectionalGhostRecipeComponent recipeComponent = new DirectionalGhostRecipeComponent(tile::getMode, fluidGauge, chemicalGauge);
        addRecipeBookComponentNoRender(recipeComponent, false);
        addRecipeBookComponentNoRender(recipeComponent, true);
    }

    @Override
    protected void drawForegroundText(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        ILangEntry modeLang = tile.getMode() ? MekanismLang.DECONDENSENTRATING : MekanismLang.CONDENSENTRATING;
        drawScrollingString(guiGraphics, modeLang.translate(), 4, imageHeight - 92, TextAlignment.LEFT, titleTextColor(), ENERGY_BAR_X - 4, 2, false);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        TriState state = TriState.from(tile.getMode());
        if (lastMode != state) {
            lastMode = state;
            assert recipeBookComponent != null;
            recipeBookComponent.recipesUpdated();
        }
    }

    @Override
    protected MekanismRecipeBookComponent<?> createRecipeBookComponent() {
        return new MekanismRecipeBookComponent<>(menu, tile, registryAccess(), this::font) {
            @Override
            protected void selectMatchingRecipes(RecipeCollection collection, StackedItemContents stackedContents) {
                //Only match the recipes that match for the given mode
                if (tile.getMode()) {
                    selectMatchingRecipes(collection, stackedContents, display -> display instanceof DecondensentratingRecipeDisplay);
                } else {
                    selectMatchingRecipes(collection, stackedContents, display -> display instanceof CondensentratingRecipeDisplay);
                }
            }
        };
    }

    private record DirectionalGhostRecipeComponent(BooleanSupplier modeSupplier, GuiFluidGauge fluidGauge, GuiChemicalGauge chemicalGauge)
          implements IGhostRecipeComponent {

        @Override
        public void setGhostData(SlotDisplay contents, Iterable<SlotDisplay> recipeContext, ContextMap context, SlotSelectTime slotSelectTime, boolean isResultSlot) {
            boolean mode = modeSupplier.getAsBoolean();
            if (mode) {//fluid to chemical
                if (isResultSlot) {
                    chemicalGauge.setGhostData(contents, recipeContext, context, slotSelectTime, true);
                } else {
                    fluidGauge.setGhostData(contents, recipeContext, context, slotSelectTime, false);
                }
            } else {//chemical to fluid
                if (isResultSlot) {
                    fluidGauge.setGhostData(contents, recipeContext, context, slotSelectTime, true);
                } else {
                    chemicalGauge.setGhostData(contents, recipeContext, context, slotSelectTime, false);
                }
            }
        }

        @Override
        public void clearGhostData() {
            fluidGauge.clearGhostData();
            chemicalGauge.clearGhostData();
        }
    }
}