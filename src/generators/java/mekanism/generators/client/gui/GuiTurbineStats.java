package mekanism.generators.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
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
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiTurbineStats extends GuiMekanismTile<TileEntityTurbineCasing, EmptyTileContainer<TileEntityTurbineCasing>> {

    public GuiTurbineStats(EmptyTileContainer<TileEntityTurbineCasing> container, Inventory inv, Component title) {
        super(container, inv, title);
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
                producing = EnergyDisplay.of(MekanismConfig.general.maxEnergyPerSteam.get().divide(TurbineValidator.MAX_BLADES)
                      .multiply(multiblock.clientFlow * Math.min(multiblock.blades,
                            multiblock.coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get())));
            } else {
                storing = EnergyDisplay.ZERO;
                producing = EnergyDisplay.ZERO;
            }
            return List.of(MekanismLang.STORING.translate(storing), GeneratorsLang.PRODUCING_AMOUNT.translate(producing));
        }));
    }

    @Override
    protected void drawForegroundText(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        TurbineMultiblockData multiblock = tile.getMultiblock();
        if (multiblock.isFormed()) {
            Component limiting = GeneratorsLang.IS_LIMITING.translateColored(EnumColor.DARK_RED);
            int lowerVolume = multiblock.lowerVolume;
            int dispersers = multiblock.getDispersers();
            int vents = multiblock.vents;
            drawString(matrix, GeneratorsLang.TURBINE_TANK_VOLUME.translate(lowerVolume), 8, 26, titleTextColor());
            boolean dispersersLimiting = lowerVolume * dispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get()
                                         < vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get();
            boolean ventsLimiting = lowerVolume * dispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get()
                                    > vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get();
            drawString(matrix, GeneratorsLang.TURBINE_STEAM_FLOW.translate(), 8, 40, subheadingTextColor());
            drawString(matrix, GeneratorsLang.TURBINE_DISPERSERS.translate(dispersers, dispersersLimiting ? limiting : ""), 14, 49, titleTextColor());
            drawString(matrix, GeneratorsLang.TURBINE_VENTS.translate(vents, ventsLimiting ? limiting : ""), 14, 58, titleTextColor());
            int coils = multiblock.coils;
            int blades = multiblock.blades;
            drawString(matrix, GeneratorsLang.TURBINE_PRODUCTION.translate(), 8, 72, subheadingTextColor());
            drawString(matrix, GeneratorsLang.TURBINE_BLADES.translate(blades, coils * 4 > blades ? limiting : ""), 14, 81, titleTextColor());
            drawString(matrix, GeneratorsLang.TURBINE_COILS.translate(coils, coils * 4 < blades ? limiting : ""), 14, 90, titleTextColor());
            drawTextScaledBound(matrix, GeneratorsLang.TURBINE_MAX_PRODUCTION.translate(EnergyDisplay.of(multiblock.getMaxProduction())), 8, 104, titleTextColor(), 164);
            drawTextScaledBound(matrix, GeneratorsLang.TURBINE_MAX_WATER_OUTPUT.translate(TextUtils.format(multiblock.getMaxWaterOutput())), 8, 113, titleTextColor(), 164);
        }
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}