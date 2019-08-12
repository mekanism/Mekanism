package mekanism.generators.client.gui;

import java.util.Arrays;
import mekanism.api.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.common.config_old.MekanismConfigOld;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mekanism.generators.client.gui.element.GuiTurbineTab;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.TurbineUpdateProtocol;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiTurbineStats extends GuiMekanismTile<TileEntityTurbineCasing> {

    public GuiTurbineStats(PlayerInventory inventory, TileEntityTurbineCasing tile) {
        super(tile, new ContainerNull(inventory.player, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiTurbineTab(this, tileEntity, TurbineTab.MAIN, resource));
        addGuiElement(new GuiEnergyInfo(() -> {
            double producing = tileEntity.structure == null ? 0 : tileEntity.structure.clientFlow * (MekanismConfigOld.current().general.maxEnergyPerSteam.get() / TurbineUpdateProtocol.MAX_BLADES) *
                                                                  Math.min(tileEntity.structure.blades, tileEntity.structure.coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get());
            return Arrays.asList(TextComponentUtil.build(Translation.of("mekanism.gui.storing"), ": ", EnergyDisplay.of(tileEntity.getEnergy(), tileEntity.getMaxEnergy())),
                  TextComponentUtil.build(Translation.of("mekanism.gui.producing"), ": ", EnergyDisplay.of(producing), "/t"));
        }, this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawCenteredText(TextComponentUtil.build(Translation.of("mekanism.gui.turbineStates")), 0, xSize, 6, 0x404040);
        if (tileEntity.structure != null) {
            ITextComponent limiting = TextComponentUtil.build(EnumColor.DARK_RED, " (", Translation.of("mekanism.gui.limiting"), ")");
            int lowerVolume = tileEntity.structure.lowerVolume;
            int clientDispersers = tileEntity.structure.clientDispersers;
            int vents = tileEntity.structure.vents;
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.tankVolume"), ": " + lowerVolume), 8, 26, 0x404040);
            boolean dispersersLimiting = lowerVolume * clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get()
                                         < vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get();
            boolean ventsLimiting = lowerVolume * clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get()
                                    > vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get();
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.steamFlow")), 8, 40, 0x797979);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.dispersers"),
                  ": " + clientDispersers, (dispersersLimiting ? limiting : "")), 14, 49, 0x404040);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.vents"), ": " + vents, (ventsLimiting ? limiting : "")), 14, 58, 0x404040);
            int coils = tileEntity.structure.coils;
            int blades = tileEntity.structure.blades;
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.production")), 8, 72, 0x797979);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.blades"), ": " + blades, (coils * 4 > blades ? limiting : "")), 14, 81, 0x404040);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.coils"), ": " + coils, (coils * 4 < blades ? limiting : "")), 14, 90, 0x404040);
            double energyMultiplier = (MekanismConfigOld.current().general.maxEnergyPerSteam.get() / TurbineUpdateProtocol.MAX_BLADES) *
                                      Math.min(blades, coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get());
            double rate = lowerVolume * (clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get());
            rate = Math.min(rate, vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.maxProduction"), ": ", EnergyDisplay.of(rate * energyMultiplier)), 8, 104, 0x404040);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.maxWaterOutput"),
                  ": " + tileEntity.structure.condensers * MekanismGeneratorsConfig.generators.condenserRate.get() + " mB/t"), 8, 113, 0x404040);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiNull.png");
    }
}