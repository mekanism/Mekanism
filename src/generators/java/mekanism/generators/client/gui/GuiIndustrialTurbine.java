package mekanism.generators.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.bar.GuiVerticalRateBar;
import mekanism.client.gui.element.button.GuiGasMode;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import mekanism.generators.client.gui.element.GuiTurbineTab;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.content.turbine.TurbineValidator;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiIndustrialTurbine extends GuiMekanismTile<TileEntityTurbineCasing, MekanismTileContainer<TileEntityTurbineCasing>> {

    public GuiIndustrialTurbine(MekanismTileContainer<TileEntityTurbineCasing> container, Inventory inv, Component title) {
        super(container, inv, title);
        inventoryLabelY += 2;
        titleLabelY = 5;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiInnerScreen(this, 50, 18, 112, 50, () -> {
            List<Component> list = new ArrayList<>();
            TurbineMultiblockData multiblock = tile.getMultiblock();
            if (multiblock.isFormed()) {
                list.add(GeneratorsLang.TURBINE_PRODUCTION_AMOUNT.translate(EnergyDisplay.of(multiblock.getProductionRate())));
                list.add(GeneratorsLang.TURBINE_FLOW_RATE.translate(TextUtils.format(multiblock.clientFlow)));
                list.add(GeneratorsLang.TURBINE_CAPACITY.translate(TextUtils.format(multiblock.getSteamCapacity())));
                list.add(GeneratorsLang.TURBINE_MAX_FLOW.translate(TextUtils.format(multiblock.getMaxFlowRate())));
            }
            return list;
        }));
        addRenderableWidget(new GuiTurbineTab(this, tile, TurbineTab.STAT));
        addRenderableWidget(new GuiVerticalPowerBar(this, new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                TurbineMultiblockData multiblock = tile.getMultiblock();
                if (multiblock.isFormed()) {
                    return EnergyDisplay.of(multiblock.energyContainer).getTextComponent();
                }
                return EnergyDisplay.ZERO.getTextComponent();
            }

            @Override
            public double getLevel() {
                TurbineMultiblockData multiblock = tile.getMultiblock();
                if (multiblock.isFormed()) {
                    return multiblock.energyContainer.getEnergy().divideToLevel(multiblock.energyContainer.getMaxEnergy());
                }
                return 1;
            }
        }, 164, 16));
        addRenderableWidget(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return GeneratorsLang.TURBINE_STEAM_INPUT_RATE.translate(TextUtils.format(tile.getMultiblock().lastSteamInput));
            }

            @Override
            public double getLevel() {
                TurbineMultiblockData multiblock = tile.getMultiblock();
                if (!multiblock.isFormed()) {
                    return 0;
                }
                double rate = Math.min(multiblock.lowerVolume * multiblock.getDispersers() * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get(),
                      multiblock.vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
                if (rate == 0) {
                    return 0;
                }
                return Math.min(1, multiblock.lastSteamInput / rate);
            }
        }, 40, 13));
        addRenderableWidget(new GuiGasGauge(() -> tile.getMultiblock().gasTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.MEDIUM, this, 6, 13));
        addRenderableWidget(new GuiEnergyTab(this, () -> {
            EnergyDisplay storing;
            EnergyDisplay producing;
            TurbineMultiblockData multiblock = tile.getMultiblock();
            if (multiblock.isFormed()) {
                storing = EnergyDisplay.of(multiblock.energyContainer);
                producing = EnergyDisplay.of(MekanismConfig.general.maxEnergyPerSteam.get().divide(TurbineValidator.MAX_BLADES)
                      .multiply(multiblock.clientFlow * Math.min(multiblock.blades,
                            multiblock.coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get())));
            } else {
                storing = EnergyDisplay.ZERO;
                producing = EnergyDisplay.ZERO;
            }
            return List.of(MekanismLang.STORING.translate(storing), GeneratorsLang.PRODUCING_AMOUNT.translate(producing));
        }));
        addRenderableWidget(new GuiGasMode(this, 159, 72, true, () -> tile.getMultiblock().dumpMode, tile.getBlockPos(), 0));
    }

    @Override
    protected void drawForegroundText(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}