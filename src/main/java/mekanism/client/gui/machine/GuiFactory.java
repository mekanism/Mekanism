package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiDumpButton;
import mekanism.client.gui.element.bar.GuiChemicalBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiSortingTab;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.factory.TileEntityItemStackGasToItemStackFactory;
import mekanism.common.tile.factory.TileEntityMetallurgicInfuserFactory;
import mekanism.common.tile.factory.TileEntitySawingFactory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiFactory extends GuiConfigurableTile<TileEntityFactory<?>, MekanismTileContainer<TileEntityFactory<?>>> {

    public GuiFactory(MekanismTileContainer<TileEntityFactory<?>> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        if (tile.hasSecondaryResourceBar()) {
            imageHeight += 11;
            inventoryLabelY = 85;
        } else if (tile instanceof TileEntitySawingFactory) {
            imageHeight += 21;
            inventoryLabelY = 95;
        } else {
            inventoryLabelY = 75;
        }
        if (tile.tier == FactoryTier.ULTIMATE) {
            imageWidth += 34;
            inventoryLabelX = 26;
        }
        titleLabelY = 4;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addButton(new GuiSortingTab(this, tile));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), imageWidth - 12, 16, tile instanceof TileEntitySawingFactory ? 73 : 52));
        addButton(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getLastUsage));
        if (tile.hasSecondaryResourceBar()) {
            if (tile instanceof TileEntityMetallurgicInfuserFactory) {
                TileEntityMetallurgicInfuserFactory factory = (TileEntityMetallurgicInfuserFactory) this.tile;
                addButton(new GuiChemicalBar<>(this, GuiChemicalBar.getProvider(factory.getInfusionTank(), tile.getInfusionTanks(null)), 7, 76,
                      tile.tier == FactoryTier.ULTIMATE ? 172 : 138, 4, true));
                addButton(new GuiDumpButton<>(this, factory, tile.tier == FactoryTier.ULTIMATE ? 182 : 148, 76));
            } else if (tile instanceof TileEntityItemStackGasToItemStackFactory) {
                TileEntityItemStackGasToItemStackFactory factory = (TileEntityItemStackGasToItemStackFactory) this.tile;
                addButton(new GuiChemicalBar<>(this, GuiChemicalBar.getProvider(factory.getGasTank(), tile.getGasTanks(null)), 7, 76,
                      tile.tier == FactoryTier.ULTIMATE ? 172 : 138, 4, true));
                addButton(new GuiDumpButton<>(this, factory, tile.tier == FactoryTier.ULTIMATE ? 182 : 148, 76));
            }
        }

        int baseX = tile.tier == FactoryTier.BASIC ? 55 : tile.tier == FactoryTier.ADVANCED ? 35 : tile.tier == FactoryTier.ELITE ? 29 : 27;
        int baseXMult = tile.tier == FactoryTier.BASIC ? 38 : tile.tier == FactoryTier.ADVANCED ? 26 : 19;
        for (int i = 0; i < tile.tier.processes; i++) {
            int cacheIndex = i;
            addProgress(new GuiProgress(() -> tile.getScaledProgress(1, cacheIndex), ProgressType.DOWN, this, 4 + baseX + (i * baseXMult), 33));
        }
    }

    private void addProgress(GuiProgress progressBar) {
        if (tile.getFactoryType() == FactoryType.SMELTING) {
            addButton(progressBar.jeiCategories(MekanismBlocks.ENERGIZED_SMELTER.getRegistryName()));
        } else if (tile.getFactoryType() == FactoryType.ENRICHING) {
            addButton(progressBar.jeiCategories(MekanismBlocks.ENRICHMENT_CHAMBER.getRegistryName()));
        } else if (tile.getFactoryType() == FactoryType.CRUSHING) {
            addButton(progressBar.jeiCategories(MekanismBlocks.CRUSHER.getRegistryName()));
        } else if (tile.getFactoryType() == FactoryType.COMPRESSING) {
            addButton(progressBar.jeiCategories(MekanismBlocks.OSMIUM_COMPRESSOR.getRegistryName()));
        } else if (tile.getFactoryType() == FactoryType.COMBINING) {
            addButton(progressBar.jeiCategories(MekanismBlocks.COMBINER.getRegistryName()));
        } else if (tile.getFactoryType() == FactoryType.PURIFYING) {
            addButton(progressBar.jeiCategories(MekanismBlocks.PURIFICATION_CHAMBER.getRegistryName()));
        } else if (tile.getFactoryType() == FactoryType.INJECTING) {
            addButton(progressBar.jeiCategories(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER.getRegistryName()));
        } else if (tile.getFactoryType() == FactoryType.INFUSING) {
            addButton(progressBar.jeiCategories(MekanismBlocks.METALLURGIC_INFUSER.getRegistryName()));
        } else if (tile.getFactoryType() == FactoryType.SAWING) {
            addButton(progressBar.jeiCategories(MekanismBlocks.PRECISION_SAWMILL.getRegistryName()));
        }
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, inventory.getDisplayName(), inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}