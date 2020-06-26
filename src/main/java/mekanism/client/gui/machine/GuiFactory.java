package mekanism.client.gui.machine;

import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiDumpButton;
import mekanism.client.gui.element.bar.GuiChemicalBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSortingTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.MekanismLang;
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
            ySize += 11;
        } else if (tile instanceof TileEntitySawingFactory) {
            ySize += 21;
        }
        if (tile.tier == FactoryTier.ULTIMATE) {
            xSize += 34;
        }
        dynamicSlots = true;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiRedstoneControlTab(this, tile));
        func_230480_a_(new GuiSecurityTab<>(this, tile));
        func_230480_a_(new GuiUpgradeTab(this, tile));
        func_230480_a_(new GuiSortingTab(this, tile));
        func_230480_a_(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), getXSize() - 12, 16, tile instanceof TileEntitySawingFactory ? 73 : 52));
        func_230480_a_(new GuiEnergyTab(tile.getEnergyContainer(), this));
        if (tile.hasSecondaryResourceBar()) {
            if (tile instanceof TileEntityMetallurgicInfuserFactory) {
                TileEntityMetallurgicInfuserFactory factory = (TileEntityMetallurgicInfuserFactory) this.tile;
                func_230480_a_(new GuiChemicalBar<>(this, GuiChemicalBar.getProvider(factory.getInfusionTank(), tile.getInfusionTanks(null)), 7, 76,
                      tile.tier == FactoryTier.ULTIMATE ? 172 : 138, 4, true));
                func_230480_a_(new GuiDumpButton<>(this, factory, tile.tier == FactoryTier.ULTIMATE ? 182 : 148, 76));
            } else if (tile instanceof TileEntityItemStackGasToItemStackFactory) {
                TileEntityItemStackGasToItemStackFactory factory = (TileEntityItemStackGasToItemStackFactory) this.tile;
                func_230480_a_(new GuiChemicalBar<>(this, GuiChemicalBar.getProvider(factory.getGasTank(), tile.getGasTanks(null)), 7, 76,
                      tile.tier == FactoryTier.ULTIMATE ? 172 : 138, 4, true));
                func_230480_a_(new GuiDumpButton<>(this, factory, tile.tier == FactoryTier.ULTIMATE ? 182 : 148, 76));
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
            func_230480_a_(progressBar.jeiCategories(MekanismBlocks.ENERGIZED_SMELTER.getRegistryName()));
        } else if (tile.getFactoryType() == FactoryType.ENRICHING) {
            func_230480_a_(progressBar.jeiCategories(MekanismBlocks.ENRICHMENT_CHAMBER.getRegistryName()));
        } else if (tile.getFactoryType() == FactoryType.CRUSHING) {
            func_230480_a_(progressBar.jeiCategories(MekanismBlocks.CRUSHER.getRegistryName()));
        } else if (tile.getFactoryType() == FactoryType.COMPRESSING) {
            func_230480_a_(progressBar.jeiCategories(MekanismBlocks.OSMIUM_COMPRESSOR.getRegistryName()));
        } else if (tile.getFactoryType() == FactoryType.COMBINING) {
            func_230480_a_(progressBar.jeiCategories(MekanismBlocks.COMBINER.getRegistryName()));
        } else if (tile.getFactoryType() == FactoryType.PURIFYING) {
            func_230480_a_(progressBar.jeiCategories(MekanismBlocks.PURIFICATION_CHAMBER.getRegistryName()));
        } else if (tile.getFactoryType() == FactoryType.INJECTING) {
            func_230480_a_(progressBar.jeiCategories(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER.getRegistryName()));
        } else if (tile.getFactoryType() == FactoryType.INFUSING) {
            func_230480_a_(progressBar.jeiCategories(MekanismBlocks.METALLURGIC_INFUSER.getRegistryName()));
        } else if (tile.getFactoryType() == FactoryType.SAWING) {
            func_230480_a_(progressBar.jeiCategories(MekanismBlocks.PRECISION_SAWMILL.getRegistryName()));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText(4);
        drawString(MekanismLang.INVENTORY.translate(), tile.tier == FactoryTier.ULTIMATE ? 26 : 8,
              tile.hasSecondaryResourceBar() ? 85 : tile instanceof TileEntitySawingFactory ? 95 : 75, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}