package mekanism.generators.client.gui;

import java.util.List;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import mekanism.generators.client.gui.element.GuiTurbineTab;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.content.turbine.TurbineValidator;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiTurbineStats extends GuiMekanismTile<TileEntityTurbineCasing, EmptyTileContainer<TileEntityTurbineCasing>> {

    public GuiTurbineStats(EmptyTileContainer<TileEntityTurbineCasing> container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth += 14;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiTurbineTab(this, tile, TurbineTab.MAIN));
        addRenderableWidget(new GuiEnergyTab(this, () -> {
            EnergyDisplay storing;
            EnergyDisplay producing;
            TurbineMultiblockData multiblock = tile.getMultiblock();
            if (multiblock.isFormed()) {
                storing = EnergyDisplay.of(multiblock.energyContainer);

                double steamPerBlade = MekanismConfig.general.maxEnergyPerSteam.get() / (double) TurbineValidator.MAX_BLADES;
                int bladeCount = Math.min(multiblock.blades, multiblock.coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get());
                long producingLong = MathUtils.clampToLong(steamPerBlade * multiblock.clientFlow * bladeCount);
                producing = EnergyDisplay.of(producingLong);
            } else {
                storing = EnergyDisplay.ZERO;
                producing = EnergyDisplay.ZERO;
            }
            return List.of(MekanismLang.STORING.translate(storing), GeneratorsLang.PRODUCING_AMOUNT.translate(producing));
        }));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        TurbineMultiblockData multiblock = tile.getMultiblock();
        if (multiblock.isFormed()) {
            Component limiting = GeneratorsLang.IS_LIMITING.translateColored(EnumColor.DARK_RED);
            int lowerVolume = multiblock.lowerVolume;
            int dispersers = multiblock.getDispersers();
            int vents = multiblock.vents;
            drawScrollingString(guiGraphics, GeneratorsLang.TURBINE_TANK_VOLUME.translate(lowerVolume), 0, 26, TextAlignment.LEFT, titleTextColor(), 6, false);
            boolean dispersersLimiting = lowerVolume * dispersers * MekanismGeneratorsConfig.generators.turbineDisperserChemicalFlow.get()
                                         < vents * MekanismGeneratorsConfig.generators.turbineVentChemicalFlow.get();
            boolean ventsLimiting = lowerVolume * dispersers * MekanismGeneratorsConfig.generators.turbineDisperserChemicalFlow.get()
                                    > vents * MekanismGeneratorsConfig.generators.turbineVentChemicalFlow.get();
            drawScrollingString(guiGraphics, GeneratorsLang.TURBINE_STEAM_FLOW.translate(), 0, 40, TextAlignment.LEFT, subheadingTextColor(), 6, false);
            drawScrollingString(guiGraphics, GeneratorsLang.TURBINE_DISPERSERS.translate(dispersers, dispersersLimiting ? limiting : ""), 4, 49, TextAlignment.LEFT, titleTextColor(), getXSize() - 4, 6, false);
            drawScrollingString(guiGraphics, GeneratorsLang.TURBINE_VENTS.translate(vents, ventsLimiting ? limiting : ""), 4, 58, TextAlignment.LEFT, titleTextColor(), getXSize() - 4, 6, false);
            int coils = multiblock.coils;
            int blades = multiblock.blades;
            drawScrollingString(guiGraphics, GeneratorsLang.TURBINE_PRODUCTION.translate(), 0, 72, TextAlignment.LEFT, subheadingTextColor(), 6, false);
            drawScrollingString(guiGraphics, GeneratorsLang.TURBINE_BLADES.translate(blades, coils * 4 > blades ? limiting : ""), 4, 81, TextAlignment.LEFT, titleTextColor(), getXSize() - 4, 6, false);
            drawScrollingString(guiGraphics, GeneratorsLang.TURBINE_COILS.translate(coils, coils * 4 < blades ? limiting : ""), 4, 90, TextAlignment.LEFT, titleTextColor(), getXSize() - 4, 6, false);
            drawScrollingString(guiGraphics, GeneratorsLang.TURBINE_MAX_PRODUCTION.translate(EnergyDisplay.of(multiblock.getMaxProduction())), 0, 104, TextAlignment.LEFT, titleTextColor(), 6, false);
            drawScrollingString(guiGraphics, GeneratorsLang.TURBINE_MAX_WATER_OUTPUT.translate(TextUtils.format(multiblock.getMaxWaterOutput())), 0, 113, TextAlignment.LEFT, titleTextColor(), 6, false);
        }
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}