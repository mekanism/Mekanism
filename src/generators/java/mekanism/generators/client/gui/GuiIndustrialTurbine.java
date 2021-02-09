package mekanism.generators.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.math.FloatingLong;
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
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiIndustrialTurbine extends GuiMekanismTile<TileEntityTurbineCasing, MekanismTileContainer<TileEntityTurbineCasing>> {

    public GuiIndustrialTurbine(MekanismTileContainer<TileEntityTurbineCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        playerInventoryTitleY += 2;
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 50, 18, 112, 50, () -> {
            List<ITextComponent> list = new ArrayList<>();
            TurbineMultiblockData multiblock = tile.getMultiblock();
            if (multiblock.isFormed()) {
                FloatingLong energyMultiplier = MekanismConfig.general.maxEnergyPerSteam.get().divide(TurbineValidator.MAX_BLADES)
                      .multiply(Math.min(multiblock.blades, multiblock.coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get()));
                double rate = multiblock.lowerVolume * (multiblock.clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get());
                rate = Math.min(rate, multiblock.vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
                list.add(GeneratorsLang.TURBINE_PRODUCTION_AMOUNT.translate(EnergyDisplay.of(energyMultiplier.multiply(multiblock.clientFlow))));
                list.add(GeneratorsLang.TURBINE_FLOW_RATE.translate(TextUtils.format(multiblock.clientFlow)));
                list.add(GeneratorsLang.TURBINE_CAPACITY.translate(TextUtils.format(multiblock.getSteamCapacity())));
                list.add(GeneratorsLang.TURBINE_MAX_FLOW.translate(TextUtils.format((long) rate)));
            }
            return list;
        }));
        addButton(new GuiTurbineTab(this, tile, TurbineTab.STAT));
        addButton(new GuiVerticalPowerBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                TurbineMultiblockData multiblock = tile.getMultiblock();
                if (multiblock.isFormed()) {
                    return EnergyDisplay.of(multiblock.energyContainer.getEnergy(), multiblock.energyContainer.getMaxEnergy()).getTextComponent();
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
        addButton(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return GeneratorsLang.TURBINE_STEAM_INPUT_RATE.translate(TextUtils.format(tile.getMultiblock().lastSteamInput));
            }

            @Override
            public double getLevel() {
                TurbineMultiblockData multiblock = tile.getMultiblock();
                if (!multiblock.isFormed()) {
                    return 0;
                }
                double rate = Math.min(multiblock.lowerVolume * multiblock.clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get(),
                      multiblock.vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
                if (rate == 0) {
                    return 0;
                }
                return Math.min(1, multiblock.lastSteamInput / rate);
            }
        }, 40, 13));
        addButton(new GuiGasGauge(() -> tile.getMultiblock().gasTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.MEDIUM, this, 6, 13));
        addButton(new GuiEnergyTab(() -> {
            EnergyDisplay storing;
            EnergyDisplay producing;
            TurbineMultiblockData multiblock = tile.getMultiblock();
            if (multiblock.isFormed()) {
                storing = EnergyDisplay.of(multiblock.energyContainer.getEnergy(), multiblock.energyContainer.getMaxEnergy());
                producing = EnergyDisplay.of(MekanismConfig.general.maxEnergyPerSteam.get().divide(TurbineValidator.MAX_BLADES)
                      .multiply(multiblock.clientFlow * Math.min(multiblock.blades,
                            multiblock.coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get())));
            } else {
                storing = EnergyDisplay.ZERO;
                producing = EnergyDisplay.ZERO;
            }
            return Arrays.asList(MekanismLang.STORING.translate(storing), GeneratorsLang.PRODUCING_AMOUNT.translate(producing));
        }, this));
        addButton(new GuiGasMode(this, guiLeft + 159, guiTop + 72, true, () -> tile.getMultiblock().dumpMode, tile.getPos(), 0));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, GeneratorsLang.TURBINE.translate(), 5);
        drawString(matrix, playerInventory.getDisplayName(), playerInventoryTitleX, playerInventoryTitleY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}