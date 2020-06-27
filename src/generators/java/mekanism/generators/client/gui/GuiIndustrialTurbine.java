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
import mekanism.generators.client.gui.element.GuiTurbineTab;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.TurbineValidator;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiIndustrialTurbine extends GuiMekanismTile<TileEntityTurbineCasing, MekanismTileContainer<TileEntityTurbineCasing>> {

    public GuiIndustrialTurbine(MekanismTileContainer<TileEntityTurbineCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiInnerScreen(this, 50, 18, 112, 50, () -> {
            List<ITextComponent> list = new ArrayList<>();
            if (tile.getMultiblock().isFormed()) {
                FloatingLong energyMultiplier = MekanismConfig.general.maxEnergyPerSteam.get().divide(TurbineValidator.MAX_BLADES)
                      .multiply(Math.min(tile.getMultiblock().blades, tile.getMultiblock().coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get()));
                double rate = tile.getMultiblock().lowerVolume * (tile.getMultiblock().clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get());
                rate = Math.min(rate, tile.getMultiblock().vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
                list.add(GeneratorsLang.TURBINE_PRODUCTION_AMOUNT.translate(EnergyDisplay.of(energyMultiplier.multiply(tile.getMultiblock().clientFlow))));
                list.add(GeneratorsLang.TURBINE_FLOW_RATE.translate(formatInt(tile.getMultiblock().clientFlow)));
                list.add(GeneratorsLang.TURBINE_CAPACITY.translate(formatInt(tile.getMultiblock().getSteamCapacity())));
                list.add(GeneratorsLang.TURBINE_MAX_FLOW.translate(formatInt((long) rate)));
            }
            return list;
        }));
        func_230480_a_(new GuiTurbineTab(this, tile, TurbineTab.STAT));
        func_230480_a_(new GuiVerticalPowerBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                if (!tile.getMultiblock().isFormed()) {
                    return EnergyDisplay.ZERO.getTextComponent();
                }
                return EnergyDisplay.of(tile.getMultiblock().energyContainer.getEnergy(), tile.getMultiblock().energyContainer.getMaxEnergy()).getTextComponent();
            }

            @Override
            public double getLevel() {
                if (!tile.getMultiblock().isFormed()) {
                    return 1;
                }
                return tile.getMultiblock().energyContainer.getEnergy().divideToLevel(tile.getMultiblock().energyContainer.getMaxEnergy());
            }
        }, 164, 16));
        func_230480_a_(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return GeneratorsLang.TURBINE_STEAM_INPUT_RATE.translate(formatInt(tile.getMultiblock().lastSteamInput));
            }

            @Override
            public double getLevel() {
                if (!tile.getMultiblock().isFormed()) {
                    return 0;
                }
                double rate = Math.min(tile.getMultiblock().lowerVolume * tile.getMultiblock().clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get(),
                      tile.getMultiblock().vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
                if (rate == 0) {
                    return 0;
                }
                return tile.getMultiblock().lastSteamInput / rate;
            }
        }, 40, 13));
        func_230480_a_(new GuiGasGauge(() -> tile.getMultiblock().gasTank,
              () -> tile.getMultiblock().getGasTanks(null), GaugeType.MEDIUM, this, 6, 13));
        func_230480_a_(new GuiEnergyTab(() -> {
            EnergyDisplay storing;
            EnergyDisplay producing;
            if (!tile.getMultiblock().isFormed()) {
                storing = EnergyDisplay.ZERO;
                producing = EnergyDisplay.ZERO;
            } else {
                storing = EnergyDisplay.of(tile.getMultiblock().energyContainer.getEnergy(), tile.getMultiblock().energyContainer.getMaxEnergy());
                producing = EnergyDisplay.of(MekanismConfig.general.maxEnergyPerSteam.get().divide(TurbineValidator.MAX_BLADES)
                      .multiply(tile.getMultiblock().clientFlow * Math.min(tile.getMultiblock().blades,
                            tile.getMultiblock().coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get())));
            }
            return Arrays.asList(MekanismLang.STORING.translate(storing), GeneratorsLang.PRODUCING_AMOUNT.translate(producing));
        }, this));
        func_230480_a_(new GuiGasMode(this, getGuiLeft() + 159, getGuiTop() + 72, true, () -> tile.getMultiblock().dumpMode, tile.getPos(), 0));
    }

    @Override
    protected void func_230451_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, GeneratorsLang.TURBINE.translate(), 5);
        drawString(matrix, MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 4, titleTextColor());
        super.func_230451_b_(matrix, mouseX, mouseY);
    }
}