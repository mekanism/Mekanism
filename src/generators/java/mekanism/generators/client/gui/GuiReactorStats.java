package mekanism.generators.client.gui;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mekanism.generators.client.gui.element.GuiReactorTab;
import mekanism.generators.client.gui.element.GuiReactorTab.ReactorTab;
import mekanism.generators.common.inventory.container.reactor.info.ReactorStatsContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiReactorStats extends GuiReactorInfo<ReactorStatsContainer> {

    private static final NumberFormat nf = NumberFormat.getIntegerInstance();

    public GuiReactorStats(ReactorStatsContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiEnergyInfo(() -> tileEntity.isFormed() ? Arrays.asList(
              TextComponentUtil.build(Translation.of("mekanism.gui.storing"), ": ", EnergyDisplay.of(tileEntity.getEnergy(), tileEntity.getMaxEnergy())),
              TextComponentUtil.build(Translation.of("mekanism.gui.producing"), ": ",
                    EnergyDisplay.of(tileEntity.getReactor().getPassiveGeneration(false, true)), "/t")) : Collections.emptyList(), this, resource));
        addButton(new GuiReactorTab(this, tileEntity, ReactorTab.HEAT, resource));
        addButton(new GuiReactorTab(this, tileEntity, ReactorTab.FUEL, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), 46, 6, 0x404040);
        if (tileEntity.isFormed()) {
            drawString(TextComponentUtil.build(EnumColor.DARK_GREEN, Translation.of("mekanism.gui.passive")), 6, 26, 0x404040);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.minInject"), ": " + tileEntity.getReactor().getMinInjectionRate(false)),
                  16, 36, 0x404040);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.ignition"), ": ",
                  MekanismUtils.getTemperatureDisplay(tileEntity.getReactor().getIgnitionTemperature(false), TemperatureUnit.AMBIENT)), 16, 46, 0x404040);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.maxPlasma"), ": ",
                  MekanismUtils.getTemperatureDisplay(tileEntity.getReactor().getMaxPlasmaTemperature(false), TemperatureUnit.AMBIENT)), 16, 56, 0x404040);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.maxCasing"), ": ",
                  MekanismUtils.getTemperatureDisplay(tileEntity.getReactor().getMaxCasingTemperature(false), TemperatureUnit.AMBIENT)), 16, 66, 0x404040);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.passiveGeneration"), ": ",
                  EnergyDisplay.of(tileEntity.getReactor().getPassiveGeneration(false, false)), "/t"), 16, 76, 0x404040);
            drawString(TextComponentUtil.build(EnumColor.DARK_BLUE, Translation.of("mekanism.gui.active")), 6, 92, 0x404040);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.minInject"), ": " + tileEntity.getReactor().getMinInjectionRate(true)),
                  16, 102, 0x404040);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.ignition"), ": ",
                  MekanismUtils.getTemperatureDisplay(tileEntity.getReactor().getIgnitionTemperature(true), TemperatureUnit.AMBIENT)), 16, 112, 0x404040);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.maxPlasma"), ": ",
                  MekanismUtils.getTemperatureDisplay(tileEntity.getReactor().getMaxPlasmaTemperature(true), TemperatureUnit.AMBIENT)), 16, 122, 0x404040);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.maxCasing"), ": ",
                  MekanismUtils.getTemperatureDisplay(tileEntity.getReactor().getMaxCasingTemperature(true), TemperatureUnit.AMBIENT)), 16, 132, 0x404040);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.passiveGeneration"), ": ",
                  EnergyDisplay.of(tileEntity.getReactor().getPassiveGeneration(true, false)), "/t"), 16, 142, 0x404040);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.steamProduction"),
                  ": " + nf.format(tileEntity.getReactor().getSteamPerTick(false)) + "mB/t"), 16, 152, 0x404040);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}