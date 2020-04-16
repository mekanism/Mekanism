package mekanism.generators.client.gui;

import java.util.Arrays;
import java.util.Collections;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiBigLight;
import mekanism.client.gui.element.GuiGraph;
import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.BooleanStateDisplay.ActiveDisabled;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.fission.SynchronizedFissionReactorData;
import mekanism.generators.common.network.PacketGeneratorsGuiInteract;
import mekanism.generators.common.network.PacketGeneratorsGuiInteract.GeneratorsGuiInteraction;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiFissionReactor extends GuiMekanismTile<TileEntityFissionReactorCasing, EmptyTileContainer<TileEntityFissionReactorCasing>> {

    private TranslationButton activateButton;
    private TranslationButton scramButton;

    private GuiGraph heatGraph;

    public GuiFissionReactor(EmptyTileContainer<TileEntityFissionReactorCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        xSize = 195;
        ySize += 6;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 45, 17, 105, 56));
        addButton(new GuiFluidGauge(() -> tile.structure == null ? null : tile.structure.waterTank,
            () -> tile.structure == null ? Collections.emptyList() : tile.structure.getFluidTanks(null), GaugeType.STANDARD, this, 6, 13)
            .setLabel(GeneratorsLang.FISSION_WATER_TANK.translateColored(EnumColor.AQUA)));
        addButton(new GuiGasGauge(() -> tile.structure == null ? null : tile.structure.fuelTank,
            () -> tile.structure == null ? Collections.emptyList() : tile.structure.getGasTanks(null), GaugeType.STANDARD, this, 25, 13)
            .setLabel(GeneratorsLang.FISSION_FUEL_TANK.translateColored(EnumColor.DARK_GREEN)));
        addButton(new GuiGasGauge(() -> tile.structure == null ? null : tile.structure.steamTank,
            () -> tile.structure == null ? Collections.emptyList() : tile.structure.getGasTanks(null), GaugeType.STANDARD, this, 152, 13)
            .setLabel(GeneratorsLang.FISSION_STEAM_TANK.translateColored(EnumColor.GRAY)));
        addButton(new GuiGasGauge(() -> tile.structure == null ? null : tile.structure.wasteTank,
            () -> tile.structure == null ? Collections.emptyList() : tile.structure.getGasTanks(null), GaugeType.STANDARD, this, 171, 13)
            .setLabel(GeneratorsLang.FISSION_WASTE_TANK.translateColored(EnumColor.BROWN)));
        addButton(new GuiHeatInfo(() -> {
            ITextComponent transfer = MekanismUtils.getTemperatureDisplay(tile.getLastTransferLoss(), TemperatureUnit.KELVIN, false);
            ITextComponent environment = MekanismUtils.getTemperatureDisplay(tile.getLastEnvironmentLoss(), TemperatureUnit.KELVIN, false);
            return Arrays.asList(MekanismLang.TRANSFERRED_RATE.translate(transfer), MekanismLang.DISSIPATED_RATE.translate(environment));
        }, this));
        addButton(activateButton = new TranslationButton(this, getGuiLeft() + 6, getGuiTop() + 75, 81, 16, GeneratorsLang.FISSION_ACTIVATE, () -> {
            MekanismGenerators.packetHandler.sendToServer(new PacketGeneratorsGuiInteract(GeneratorsGuiInteraction.FISSION_ACTIVE, tile, 1));
        }, null, () -> EnumColor.DARK_GREEN));
        addButton(scramButton = new TranslationButton(this, getGuiLeft() + 89, getGuiTop() + 75, 81, 16, GeneratorsLang.FISSION_SCRAM, () -> {
            MekanismGenerators.packetHandler.sendToServer(new PacketGeneratorsGuiInteract(GeneratorsGuiInteraction.FISSION_ACTIVE, tile, 0));
        }, null, () -> EnumColor.DARK_RED));
        addButton(new GuiBigLight(this, 173, 76, () -> tile.isReactorActive()));
        addButton(new GuiDynamicHorizontalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismUtils.getTemperatureDisplay(tile.getTemperature(), TemperatureUnit.KELVIN, true);
            }

            @Override
            public double getLevel() {
                return Math.min(1, tile.getTemperature() / SynchronizedFissionReactorData.MAX_DAMAGE_TEMPERATURE);
            }
        }, 5, 104, xSize - 12));
        addButton(heatGraph = new GuiGraph(this, 6, 128, xSize - 12, 36, MekanismLang.TEMPERATURE::translate));
        heatGraph.setMinScale(1_600);
        updateButtons();
    }

    private void updateButtons() {
        activateButton.active = !tile.isReactorActive();
        scramButton.active = tile.isReactorActive();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        updateButtons();

        ITextComponent name = GeneratorsLang.FISSION_REACTOR.translate();
        drawString(name, (getXSize() / 2) - (getStringWidth(name) / 2), 5, 0x404040);
        renderScaledText(MekanismLang.STATUS.translate(tile.isReactorActive() ? EnumColor.BRIGHT_GREEN : EnumColor.RED, ActiveDisabled.of(tile.isReactorActive())), 48, 20, 0x00CD00, 100);
        renderScaledText(MekanismLang.BOIL_RATE.translate(tile.getLastBoilRate()), 48, 29, 0x00CD00, 100);
        renderScaledText(MekanismLang.TEMPERATURE.translate(tile.getTempColor(), MekanismUtils.getTemperatureDisplay(tile.getTemperature(), TemperatureUnit.KELVIN, true)), 48, 38, 0x00CD00, 100);
        renderScaledText(GeneratorsLang.FISSION_DAMAGE.translate(tile.getDamageColor(), tile.getDamageString()), 48, 47, 0x00CD00, 100);
        drawString(MekanismLang.TEMPERATURE_LONG.translate(""), 6, 95, 0x404040);
        drawString(GeneratorsLang.FISSION_HEAT_GRAPH.translate(), 6, 118, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        heatGraph.addData((int) tile.getTemperature());
    }
}
