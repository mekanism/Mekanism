package mekanism.generators.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import javax.annotation.Nonnull;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.client.gui.element.GuiTurbineTab;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.content.turbine.TurbineValidator;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiTurbineStats extends GuiMekanismTile<TileEntityTurbineCasing, EmptyTileContainer<TileEntityTurbineCasing>> {

    public GuiTurbineStats(EmptyTileContainer<TileEntityTurbineCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiTurbineTab(this, tile, TurbineTab.MAIN));
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
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, GeneratorsLang.TURBINE_STATS.translate(), 6);
        TurbineMultiblockData multiblock = tile.getMultiblock();
        if (multiblock.isFormed()) {
            ITextComponent limiting = GeneratorsLang.IS_LIMITING.translateColored(EnumColor.DARK_RED);
            int lowerVolume = multiblock.lowerVolume;
            int clientDispersers = multiblock.clientDispersers;
            int vents = multiblock.vents;
            drawString(matrix, GeneratorsLang.TURBINE_TANK_VOLUME.translate(lowerVolume), 8, 26, titleTextColor());
            boolean dispersersLimiting = lowerVolume * clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get()
                                         < vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get();
            boolean ventsLimiting = lowerVolume * clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get()
                                    > vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get();
            drawString(matrix, GeneratorsLang.TURBINE_STEAM_FLOW.translate(), 8, 40, subheadingTextColor());
            drawString(matrix, GeneratorsLang.TURBINE_DISPERSERS.translate(clientDispersers, dispersersLimiting ? limiting : ""), 14, 49, titleTextColor());
            drawString(matrix, GeneratorsLang.TURBINE_VENTS.translate(vents, ventsLimiting ? limiting : ""), 14, 58, titleTextColor());
            int coils = multiblock.coils;
            int blades = multiblock.blades;
            drawString(matrix, GeneratorsLang.TURBINE_PRODUCTION.translate(), 8, 72, subheadingTextColor());
            drawString(matrix, GeneratorsLang.TURBINE_BLADES.translate(blades, coils * 4 > blades ? limiting : ""), 14, 81, titleTextColor());
            drawString(matrix, GeneratorsLang.TURBINE_COILS.translate(coils, coils * 4 < blades ? limiting : ""), 14, 90, titleTextColor());
            FloatingLong energyMultiplier = MekanismConfig.general.maxEnergyPerSteam.get().divide(TurbineValidator.MAX_BLADES)
                  .multiply(Math.min(blades, coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get()));
            double rate = lowerVolume * (clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get());
            rate = Math.min(rate, vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
            drawString(matrix, GeneratorsLang.TURBINE_MAX_PRODUCTION.translate(EnergyDisplay.of(energyMultiplier.multiply(rate))), 8, 104, titleTextColor());
            drawString(matrix, GeneratorsLang.TURBINE_MAX_WATER_OUTPUT.translate(multiblock.condensers * MekanismGeneratorsConfig.generators.condenserRate.get()), 8, 113, titleTextColor());
        }
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}