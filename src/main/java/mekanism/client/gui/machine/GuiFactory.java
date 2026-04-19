package mekanism.client.gui.machine;

import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiDumpButton;
import mekanism.client.gui.element.bar.GuiChemicalBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiSortingTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.factory.TileEntityItemStackChemicalToItemStackFactory;
import mekanism.common.tile.factory.TileEntitySawingFactory;
import mekanism.common.tile.interfaces.IHasDumpButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiFactory extends GuiConfigurableTile<TileEntityFactory<?>, MekanismTileContainer<TileEntityFactory<?>>> {

    @Nullable
    private GuiDumpButton<?> dumpButton;

    private static int calcHeight(MekanismTileContainer<TileEntityFactory<?>> container) {
        TileEntityFactory<?> tile = container.getTileEntity();
        int imageHeight = DEFAULT_IMAGE_HEIGHT;
        if (tile.hasSecondaryResourceBar()) {
            imageHeight += 11;
        } else if (tile instanceof TileEntitySawingFactory) {
            imageHeight += 21;
        }
        return imageHeight;
    }

    private static int calcWidth(MekanismTileContainer<TileEntityFactory<?>> container) {
        TileEntityFactory<?> tile = container.getTileEntity();
        int imageWidth = DEFAULT_IMAGE_WIDTH;
        if (tile.tier == FactoryTier.ULTIMATE) {
            imageWidth += 34;
        }
        return imageWidth;
    }

    public GuiFactory(MekanismTileContainer<TileEntityFactory<?>> container, Inventory inv, Component title) {
        super(container, inv, title, calcWidth(container), calcHeight(container));
        if (tile.hasSecondaryResourceBar()) {
            inventoryLabelY = 85;
        } else if (tile instanceof TileEntitySawingFactory) {
            inventoryLabelY = 95;
        } else {
            inventoryLabelY = 75;
        }
        if (tile.tier == FactoryTier.ULTIMATE) {
            inventoryLabelX = 26;
        }
        titleLabelY = 4;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiSortingTab(this, tile));
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), imageWidth - 12, 16, tile instanceof TileEntitySawingFactory ? 73 : 52))
              .warning(WarningType.NOT_ENOUGH_ENERGY, tile.getWarningCheck(RecipeError.NOT_ENOUGH_ENERGY, 0));
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getLastUsage));
        if (tile.hasSecondaryResourceBar()) {
            if (tile instanceof TileEntityItemStackChemicalToItemStackFactory factory) {
                addRenderableWidget(new GuiChemicalBar(this, GuiChemicalBar.getProvider(factory.getChemicalTank(), tile.getChemicalTanks(null)), 7, 76,
                      tile.tier == FactoryTier.ULTIMATE ? 172 : 138, 4, true))
                      .warning(WarningType.NO_MATCHING_RECIPE, tile.getWarningCheck(RecipeError.NOT_ENOUGH_SECONDARY_INPUT, 0));
                dumpButton = addRenderableWidget(new GuiDumpButton<>(this, (TileEntityFactory<?> & IHasDumpButton) tile, tile.tier == FactoryTier.ULTIMATE ? 182 : 148, 76));
            }
        }

        int baseX = tile.tier == FactoryTier.BASIC ? 55 : tile.tier == FactoryTier.ADVANCED ? 35 : tile.tier == FactoryTier.ELITE ? 29 : 27;
        int baseXMult = tile.tier == FactoryTier.BASIC ? 38 : tile.tier == FactoryTier.ADVANCED ? 26 : 19;
        for (int i = 0; i < tile.tier.processes; i++) {
            int cacheIndex = i;
            addRenderableWidget(new GuiProgress(() -> tile.getScaledProgress(1, cacheIndex), ProgressType.DOWN, this, 4 + baseX + (i * baseXMult), 33))
                  .recipeViewerCategory(tile)
                  //Only can happen if recipes change because inputs are sanitized in the factory based on the output
                  .warning(WarningType.INPUT_DOESNT_PRODUCE_OUTPUT, tile.getWarningCheck(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT, cacheIndex));
        }
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics, dumpButton == null ? getXSize() : dumpButton.getRelativeX());
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}