package mekanism.generators.client.gui;

import java.util.Arrays;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiBigLight;
import mekanism.client.gui.element.GuiGraph;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiHybridGauge;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.BooleanStateDisplay.ActiveDisabled;
import mekanism.generators.client.gui.element.GuiFissionReactorTab;
import mekanism.generators.client.gui.element.GuiFissionReactorTab.FissionReactorTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
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
        addButton(new GuiFissionReactorTab(this, tile, FissionReactorTab.STAT));
        addButton(new GuiInnerScreen(this, 45, 17, 105, 56, () -> Arrays.asList(
              MekanismLang.STATUS.translate(tile.isReactorActive() ? EnumColor.BRIGHT_GREEN : EnumColor.RED, ActiveDisabled.of(tile.isReactorActive())),
              GeneratorsLang.GAS_BURN_RATE.translate(tile.getLastBurnRate()),
              GeneratorsLang.FISSION_HEATING_RATE.translate(formatInt(tile.getLastBoilRate())),
              MekanismLang.TEMPERATURE.translate(tile.getTempColor(), MekanismUtils.getTemperatureDisplay(tile.getTemperature(), TemperatureUnit.KELVIN, true)),
              GeneratorsLang.FISSION_DAMAGE.translate(tile.getDamageColor(), tile.getDamageString())
        )).defaultFormat().spacing(2));
        addButton(new GuiHybridGauge(
              () -> tile.getMultiblock().gasCoolantTank, () -> tile.getMultiblock().getGasTanks(null),
              () -> tile.getMultiblock().fluidCoolantTank, () -> tile.getMultiblock().getFluidTanks(null),
              GaugeType.STANDARD, this, 6, 13)
              .setLabel(GeneratorsLang.FISSION_COOLANT_TANK.translateColored(EnumColor.AQUA)));
        addButton(new GuiGasGauge(() -> tile.getMultiblock().fuelTank,
              () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 25, 13)
              .setLabel(GeneratorsLang.FISSION_FUEL_TANK.translateColored(EnumColor.DARK_GREEN)));
        addButton(new GuiGasGauge(() -> tile.getMultiblock().heatedCoolantTank,
              () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 152, 13)
              .setLabel(GeneratorsLang.FISSION_HEATED_COOLANT_TANK.translateColored(EnumColor.ORANGE)));
        addButton(new GuiGasGauge(() -> tile.getMultiblock().wasteTank,
              () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 171, 13)
              .setLabel(GeneratorsLang.FISSION_WASTE_TANK.translateColored(EnumColor.BROWN)));
        addButton(new GuiHeatTab(() -> {
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
                return Math.min(1, tile.getTemperature() / FissionReactorMultiblockData.MAX_DAMAGE_TEMPERATURE);
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

        drawTitleText(GeneratorsLang.FISSION_REACTOR.translate(), 5);
        drawString(MekanismLang.TEMPERATURE_LONG.translate(""), 6, 95, titleTextColor());
        drawString(GeneratorsLang.FISSION_HEAT_GRAPH.translate(), 6, 118, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        heatGraph.addData((int) tile.getTemperature());
    }
}
