package mekanism.generators.client.gui;

import java.util.Arrays;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.client.gui.element.GuiTurbineTab;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.TurbineUpdateProtocol;
import mekanism.generators.common.inventory.container.turbine.TurbineStatsContainer;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiTurbineStats extends GuiMekanismTile<TileEntityTurbineCasing, TurbineStatsContainer> {

    public GuiTurbineStats(TurbineStatsContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiTurbineTab(this, tile, TurbineTab.MAIN, resource));
        addButton(new GuiEnergyInfo(() -> {
            double producing = tile.structure == null ? 0 : tile.structure.clientFlow * (MekanismConfig.general.maxEnergyPerSteam.get() / TurbineUpdateProtocol.MAX_BLADES) *
                                                            Math.min(tile.structure.blades, tile.structure.coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get());
            return Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy())),
                  GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(producing)));
        }, this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawCenteredText(GeneratorsLang.TURBINE_STATS.translate(), 0, getXSize(), 6, 0x404040);
        if (tile.structure != null) {
            ITextComponent limiting = GeneratorsLang.IS_LIMITING.translateColored(EnumColor.DARK_RED);
            int lowerVolume = tile.structure.lowerVolume;
            int clientDispersers = tile.structure.clientDispersers;
            int vents = tile.structure.vents;
            drawString(GeneratorsLang.TURBINE_TANK_VOLUME.translate(lowerVolume), 8, 26, 0x404040);
            boolean dispersersLimiting = lowerVolume * clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get()
                                         < vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get();
            boolean ventsLimiting = lowerVolume * clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get()
                                    > vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get();
            drawString(GeneratorsLang.TURBINE_STEAM_FLOW.translate(), 8, 40, 0x797979);
            drawString(GeneratorsLang.TURBINE_DISPERSERS.translate(clientDispersers, dispersersLimiting ? limiting : ""), 14, 49, 0x404040);
            drawString(GeneratorsLang.TURBINE_VENTS.translate(vents, ventsLimiting ? limiting : ""), 14, 58, 0x404040);
            int coils = tile.structure.coils;
            int blades = tile.structure.blades;
            drawString(GeneratorsLang.TURBINE_PRODUCTION.translate(), 8, 72, 0x797979);
            drawString(GeneratorsLang.TURBINE_BLADES.translate(blades, coils * 4 > blades ? limiting : ""), 14, 81, 0x404040);
            drawString(GeneratorsLang.TURBINE_COILS.translate(coils, coils * 4 < blades ? limiting : ""), 14, 90, 0x404040);
            double energyMultiplier = (MekanismConfig.general.maxEnergyPerSteam.get() / TurbineUpdateProtocol.MAX_BLADES) *
                                      Math.min(blades, coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get());
            double rate = lowerVolume * (clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get());
            rate = Math.min(rate, vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
            drawString(GeneratorsLang.TURBINE_MAX_PRODUCTION.translate(EnergyDisplay.of(rate * energyMultiplier)), 8, 104, 0x404040);
            drawString(GeneratorsLang.TURBINE_MAX_WATER_OUTPUT.translate(tile.structure.condensers * MekanismGeneratorsConfig.generators.condenserRate.get()), 8, 113, 0x404040);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "null.png");
    }
}