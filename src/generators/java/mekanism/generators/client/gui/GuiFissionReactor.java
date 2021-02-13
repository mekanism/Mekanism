package mekanism.generators.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import java.util.Collections;
import javax.annotation.Nonnull;
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
import mekanism.common.util.text.TextUtils;
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
        titleY = 5;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiFissionReactorTab(this, tile, FissionReactorTab.STAT));
        addButton(new GuiInnerScreen(this, 45, 17, 105, 56, () -> {
            FissionReactorMultiblockData multiblock = tile.getMultiblock();
            return Arrays.asList(
                  MekanismLang.STATUS.translate(multiblock.isActive() ? EnumColor.BRIGHT_GREEN : EnumColor.RED, ActiveDisabled.of(multiblock.isActive())),
                  GeneratorsLang.GAS_BURN_RATE.translate(multiblock.lastBurnRate),
                  GeneratorsLang.FISSION_HEATING_RATE.translate(TextUtils.format(multiblock.lastBoilRate)),
                  MekanismLang.TEMPERATURE.translate(tile.getTempColor(), MekanismUtils.getTemperatureDisplay(multiblock.heatCapacitor.getTemperature(), TemperatureUnit.KELVIN, true)),
                  GeneratorsLang.FISSION_DAMAGE.translate(tile.getDamageColor(), tile.getDamageString())
            );
        }).defaultFormat().spacing(2));
        addButton(new GuiHybridGauge(() -> tile.getMultiblock().gasCoolantTank, () -> tile.getMultiblock().getGasTanks(null),
              () -> tile.getMultiblock().fluidCoolantTank, () -> tile.getMultiblock().getFluidTanks(null), GaugeType.STANDARD, this, 6, 13)
              .setLabel(GeneratorsLang.FISSION_COOLANT_TANK.translateColored(EnumColor.AQUA)));
        addButton(new GuiGasGauge(() -> tile.getMultiblock().fuelTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 25, 13)
              .setLabel(GeneratorsLang.FISSION_FUEL_TANK.translateColored(EnumColor.DARK_GREEN)));
        addButton(new GuiGasGauge(() -> tile.getMultiblock().heatedCoolantTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 152, 13)
              .setLabel(GeneratorsLang.FISSION_HEATED_COOLANT_TANK.translateColored(EnumColor.ORANGE)));
        addButton(new GuiGasGauge(() -> tile.getMultiblock().wasteTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 171, 13)
              .setLabel(GeneratorsLang.FISSION_WASTE_TANK.translateColored(EnumColor.BROWN)));
        addButton(new GuiHeatTab(() -> {
            ITextComponent environment = MekanismUtils.getTemperatureDisplay(tile.getMultiblock().lastEnvironmentLoss, TemperatureUnit.KELVIN, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }, this));
        addButton(activateButton = new TranslationButton(this, guiLeft + 6, guiTop + 75, 81, 16, GeneratorsLang.FISSION_ACTIVATE,
              () -> MekanismGenerators.packetHandler.sendToServer(new PacketGeneratorsGuiInteract(GeneratorsGuiInteraction.FISSION_ACTIVE, tile, 1)), null,
              () -> EnumColor.DARK_GREEN));
        addButton(scramButton = new TranslationButton(this, guiLeft + 89, guiTop + 75, 81, 16, GeneratorsLang.FISSION_SCRAM,
              () -> MekanismGenerators.packetHandler.sendToServer(new PacketGeneratorsGuiInteract(GeneratorsGuiInteraction.FISSION_ACTIVE, tile, 0)), null,
              () -> EnumColor.DARK_RED));
        addButton(new GuiBigLight(this, 173, 76, tile.getMultiblock()::isActive));
        addButton(new GuiDynamicHorizontalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismUtils.getTemperatureDisplay(tile.getMultiblock().heatCapacitor.getTemperature(), TemperatureUnit.KELVIN, true);
            }

            @Override
            public double getLevel() {
                return Math.min(1, tile.getMultiblock().heatCapacitor.getTemperature() / FissionReactorMultiblockData.MAX_DAMAGE_TEMPERATURE);
            }
        }, 5, 104, xSize - 12));
        addButton(heatGraph = new GuiGraph(this, 6, 128, xSize - 12, 36, MekanismLang.TEMPERATURE::translate));
        heatGraph.setMinScale(1_600);
        updateButtons();
    }

    private void updateButtons() {
        FissionReactorMultiblockData multiblock = tile.getMultiblock();
        activateButton.active = !multiblock.isActive();
        scramButton.active = multiblock.isActive();
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        updateButtons();

        drawTitleText(matrix, GeneratorsLang.FISSION_REACTOR.translate(), titleY);
        drawString(matrix, MekanismLang.TEMPERATURE_LONG.translate(""), 6, 95, titleTextColor());
        drawString(matrix, GeneratorsLang.FISSION_HEAT_GRAPH.translate(), 6, 118, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        heatGraph.addData((int) tile.getMultiblock().heatCapacitor.getTemperature());
    }
}
