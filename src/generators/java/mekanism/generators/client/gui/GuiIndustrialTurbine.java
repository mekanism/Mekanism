package mekanism.generators.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.bar.GuiVerticalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import mekanism.generators.client.gui.element.GuiTurbineTab;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiIndustrialTurbine extends GuiMekanismTile<TileEntityTurbineCasing, MekanismTileContainer<TileEntityTurbineCasing>> {

    public GuiIndustrialTurbine(MekanismTileContainer<TileEntityTurbineCasing> container, Inventory inv, Component title) {
        super(container, inv, title, DEFAULT_IMAGE_WIDTH + 14, DEFAULT_IMAGE_HEIGHT);
        inventoryLabelX += 7;
        inventoryLabelY += 2;
        titleLabelY -= 1;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiInnerScreen(this, 50, 13, 126, 60, () -> {
            List<Component> list = new ArrayList<>();
            TurbineMultiblockData multiblock = tile.getMultiblock();
            if (multiblock.isFormed()) {
                list.add(GeneratorsLang.TURBINE_PRODUCTION_AMOUNT.translate(energyDisplayOf(multiblock.getProductionRate())));
                //todo give the percentages a decimal point, or remove the 0.5 extra
                list.add(GeneratorsLang.TURBINE_FLOW_RATE.translate(TextUtils.format(multiblock.clientFlow), TextUtils.format(multiblock.clientFlowRatio * 100)));
                list.add(GeneratorsLang.TURBINE_FLOW_RATIO_REQUIRED.translate(TextUtils.format(multiblock.minFlowRateForSpin * 100)));
                list.add(GeneratorsLang.TURBINE_CAPACITY.translate(TextUtils.format(multiblock.getSteamCapacity())));
                list.add(GeneratorsLang.TURBINE_MAX_FLOW.translate(TextUtils.format(multiblock.getMaxFlowRate())));
            }
            return list;
        }));
        addRenderableWidget(new GuiTurbineTab(this, tile, TurbineTab.STAT));
        addRenderableWidget(new GuiVerticalPowerBar(this, new IBarInfoHandler() {
            @Override
            public Component getTooltip(IGuiWrapper gui) {
                TurbineMultiblockData multiblock = tile.getMultiblock();
                if (multiblock.isFormed()) {
                    return gui.energyDisplayOf(multiblock.energyContainer()).getTextComponent();
                }
                return EnergyDisplay.ZERO.getTextComponent();
            }

            @Override
            public double getLevel() {
                TurbineMultiblockData multiblock = tile.getMultiblock();
                if (multiblock.isFormed()) {
                    return ContainerType.ENERGY.divideToLevel(multiblock.energyContainer());
                }
                return 1;
            }
        }, 178, 13, 58));
        addRenderableWidget(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public Component getTooltip(IGuiWrapper gui) {
                return GeneratorsLang.TURBINE_STEAM_INPUT_RATE.translate(TextUtils.format(tile.getMultiblock().lastSteamInput));
            }

            @Override
            public double getLevel() {
                TurbineMultiblockData multiblock = tile.getMultiblock();
                if (!multiblock.isFormed()) {
                    return 0;
                }
                double rate = Math.min(multiblock.lowerVolume * multiblock.getDispersers() * MekanismGeneratorsConfig.generators.turbineDisperserChemicalFlow.get(),
                      multiblock.vents * MekanismGeneratorsConfig.generators.turbineVentChemicalFlow.get());
                if (rate == 0) {
                    return 0;
                }
                return Math.min(1, multiblock.lastSteamInput / rate);
            }
        }, 40, 13));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.getMultiblock().chemicalTank, () -> tile.getMultiblock().getChemicalTanks(), GaugeType.MEDIUM, this, 6, 13));
        addRenderableWidget(new GuiEnergyTab(this, gui -> {
            EnergyDisplay storing;
            EnergyDisplay producing;
            TurbineMultiblockData multiblock = tile.getMultiblock();
            if (multiblock.isFormed()) {
                storing = gui.energyDisplayOf(multiblock.energyContainer());
                producing = gui.energyDisplayOf(multiblock.getProductionRate());
            } else {
                storing = EnergyDisplay.ZERO;
                producing = EnergyDisplay.ZERO;
            }
            return List.of(MekanismLang.STORING.translate(storing), GeneratorsLang.PRODUCING_AMOUNT.translate(producing));
        }));
    }

    @Override
    protected void drawForegroundText(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics, 99);
        super.drawForegroundText(guiGraphics, mouseX, mouseY, partialTicks);
    }
}