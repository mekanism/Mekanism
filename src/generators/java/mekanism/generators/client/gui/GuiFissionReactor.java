package mekanism.generators.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiBigLight;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiMergedTankGauge;
import mekanism.client.gui.element.graph.GuiDoubleGraph;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.client.gui.element.tab.GuiWarningTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.IWarningTracker;
import mekanism.common.network.PacketUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.BooleanStateDisplay.ActiveDisabled;
import mekanism.common.util.text.TextUtils;
import mekanism.generators.client.gui.element.GuiFissionReactorTab;
import mekanism.generators.client.gui.element.GuiFissionReactorTab.FissionReactorTab;
import mekanism.generators.client.recipe_viewer.GeneratorsRVRecipeType;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.network.to_server.PacketGeneratorsGuiInteract;
import mekanism.generators.common.network.to_server.PacketGeneratorsGuiInteract.GeneratorsGuiInteraction;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiFissionReactor extends GuiMekanismTile<TileEntityFissionReactorCasing, MekanismTileContainer<TileEntityFissionReactorCasing>> {

    private TranslationButton activateButton;
    private TranslationButton scramButton;

    private GuiDoubleGraph heatGraph;

    public GuiFissionReactor(MekanismTileContainer<TileEntityFissionReactorCasing> container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth = 195;
        imageHeight += 89;
        inventoryLabelX = 6;
        inventoryLabelY = imageHeight - 92;
        titleLabelY = 5;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiFissionReactorTab(this, tile, FissionReactorTab.STAT));
        addRenderableWidget(new GuiInnerScreen(this, 45, 17, 105, 56, () -> {
            FissionReactorMultiblockData multiblock = tile.getMultiblock();
            return List.of(
                  MekanismLang.STATUS.translate(multiblock.isActive() ? EnumColor.BRIGHT_GREEN : EnumColor.RED, ActiveDisabled.of(multiblock.isActive())),
                  GeneratorsLang.GAS_BURN_RATE.translate(multiblock.lastBurnRate),
                  GeneratorsLang.FISSION_HEATING_RATE.translate(TextUtils.format(multiblock.lastBoilRate)),
                  MekanismLang.TEMPERATURE.translate(tile.getTempColor(), MekanismUtils.getTemperatureDisplay(multiblock.heatCapacitor.getTemperature(), TemperatureUnit.KELVIN, true)),
                  GeneratorsLang.FISSION_DAMAGE.translate(tile.getDamageColor(), tile.getDamageString())
            );
        }).spacing(1).recipeViewerCategories(GeneratorsRVRecipeType.FISSION));
        addRenderableWidget(new GuiMergedTankGauge<>(() -> tile.getMultiblock().coolantTank, tile::getMultiblock, GaugeType.STANDARD, this, 6, 13)
              .setLabel(GeneratorsLang.FISSION_COOLANT_TANK.translateColored(EnumColor.AQUA)));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.getMultiblock().fuelTank, () -> tile.getMultiblock().getChemicalTanks((Direction) null), GaugeType.STANDARD, this, 25, 13)
              .setLabel(GeneratorsLang.FISSION_FUEL_TANK.translateColored(EnumColor.DARK_GREEN)));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.getMultiblock().heatedCoolantTank, () -> tile.getMultiblock().getChemicalTanks((Direction) null), GaugeType.STANDARD, this, 152, 13)
              .setLabel(GeneratorsLang.FISSION_HEATED_COOLANT_TANK.translateColored(EnumColor.ORANGE)));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.getMultiblock().wasteTank, () -> tile.getMultiblock().getChemicalTanks((Direction) null), GaugeType.STANDARD, this, 171, 13)
              .setLabel(GeneratorsLang.FISSION_WASTE_TANK.translateColored(EnumColor.BROWN)));
        addRenderableWidget(new GuiHeatTab(this, () -> {
            Component environment = MekanismUtils.getTemperatureDisplay(tile.getMultiblock().lastEnvironmentLoss, TemperatureUnit.KELVIN, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }));
        activateButton = addRenderableWidget(new TranslationButton(this, 6, 75, 81, 16, GeneratorsLang.FISSION_ACTIVATE,
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketGeneratorsGuiInteract(GeneratorsGuiInteraction.FISSION_ACTIVE,
                    ((GuiFissionReactor) element.gui()).tile, 1)), () -> EnumColor.DARK_GREEN) {
            @Override
            public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
                super.renderForeground(guiGraphics, mouseX, mouseY);
                if (!active && tile.getMultiblock().isForceDisabled()) {
                    active = true;
                    //Temporarily set active to true, so we can easily check if the mouse is over the button
                    // Note: We can't just use the delayed tooltip rendering as it doesn't work for inactive buttons
                    if (isMouseOverCheckWindows(mouseX, mouseY)) {
                        PoseStack pose = guiGraphics.pose();
                        pose.pushPose();
                        //Offset to fix rendering position
                        pose.translate(-getGuiLeft(), -getGuiTop(), 0);
                        //Tooltip.MAX_WIDTH = 170
                        guiGraphics.renderTooltip(font, font.split(GeneratorsLang.FISSION_FORCE_DISABLED.translate(), 170), mouseX, mouseY);
                        pose.popPose();
                    }
                    active = false;
                }
            }
        });
        scramButton = addRenderableWidget(new TranslationButton(this, 89, 75, 81, 16, GeneratorsLang.FISSION_SCRAM,
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketGeneratorsGuiInteract(GeneratorsGuiInteraction.FISSION_ACTIVE,
                    ((GuiFissionReactor) element.gui()).tile, 0)), () -> EnumColor.DARK_RED));
        addRenderableWidget(new GuiBigLight(this, 173, 76, tile.getMultiblock()::isActive));
        addRenderableWidget(new GuiDynamicHorizontalRateBar(this, new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return MekanismUtils.getTemperatureDisplay(tile.getMultiblock().heatCapacitor.getTemperature(), TemperatureUnit.KELVIN, true);
            }

            @Override
            public double getLevel() {
                return Math.min(1, tile.getMultiblock().heatCapacitor.getTemperature() / FissionReactorMultiblockData.MAX_DAMAGE_TEMPERATURE);
            }
        }, 5, 102, imageWidth - 12));
        heatGraph = addRenderableWidget(new GuiDoubleGraph(this, 5, 123, imageWidth - 10, 38,
              temp -> MekanismUtils.getTemperatureDisplay(temp, TemperatureUnit.KELVIN, true)));
        heatGraph.setMinScale(1_600);
        updateButtons();
    }

    private void updateButtons() {
        FissionReactorMultiblockData multiblock = tile.getMultiblock();
        activateButton.active = !multiblock.isActive() && !multiblock.isForceDisabled();
        scramButton.active = multiblock.isActive();
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        updateButtons();
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        drawScrollingString(guiGraphics, MekanismLang.TEMPERATURE_LONG.translate(""), 0, 93, TextAlignment.LEFT, titleTextColor(), 5, false);
        drawScrollingString(guiGraphics, GeneratorsLang.FISSION_HEAT_GRAPH.translate(), 0, 114, TextAlignment.LEFT, titleTextColor(), 5, false);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        heatGraph.addData(tile.getMultiblock().heatCapacitor.getTemperature());
    }

    @Override
    protected void addWarningTab(IWarningTracker warningTracker) {
        //Move the tab to the right side of the gui so it doesn't intersect the heat tab
        addRenderableWidget(new GuiWarningTab(this, warningTracker, false));
    }
}