package mekanism.client.gui;

import java.util.Collections;
import mekanism.client.gui.element.GuiGraph;
import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.client.gui.element.tab.GuiBoilerTab;
import mekanism.client.gui.element.tab.GuiBoilerTab.BoilerTab;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.inventory.container.tile.BoilerStatsContainer;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiBoilerStats extends GuiMekanismTile<TileEntityBoilerCasing, BoilerStatsContainer> {

    private GuiGraph boilGraph;
    private GuiGraph maxGraph;

    public GuiBoilerStats(BoilerStatsContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiBoilerTab(this, tileEntity, BoilerTab.MAIN, resource));
        addButton(new GuiHeatInfo(() -> {
            TemperatureUnit unit = EnumUtils.TEMPERATURE_UNITS[MekanismConfig.general.tempUnit.get().ordinal()];
            String environment = UnitDisplayUtils.getDisplayShort(tileEntity.getLastEnvironmentLoss() * unit.intervalSize, false, unit);
            return Collections.singletonList(TextComponentUtil.build(Translation.of("gui.mekanism.dissipated"), ": " + environment + "/t"));
        }, this, resource));
        addButton(boilGraph = new GuiGraph(this, resource, 8, 83, 160, 36, data ->
              TextComponentUtil.build(Translation.of("gui.mekanism.boilRate"), ": " + data + " mB/t")));
        addButton(maxGraph = new GuiGraph(this, resource, 8, 122, 160, 36, data ->
              TextComponentUtil.build(Translation.of("gui.mekanism.maxBoil"), ": " + data + " mB/t")));
        maxGraph.enableFixedScale((int) ((tileEntity.getSuperheatingElements() * MekanismConfig.general.superheatingHeatTransfer.get()) /
                                         SynchronizedBoilerData.getHeatEnthalpy()));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawCenteredText(TextComponentUtil.translate("gui.mekanism.boilerStats"), 0, xSize, 6, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("gui.mekanism.maxWater"), ": " + tileEntity.clientWaterCapacity + " mB"), 8, 26, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("gui.mekanism.maxSteam"), ": " + tileEntity.clientSteamCapacity + " mB"), 8, 35, 0x404040);
        drawString(TextComponentUtil.translate("gui.mekanism.heatTransfer"), 8, 49, 0x797979);
        drawString(TextComponentUtil.build(Translation.of("gui.mekanism.superheaters"), ": " + tileEntity.getSuperheatingElements()), 14, 58, 0x404040);
        int boilCapacity = (int) (tileEntity.getSuperheatingElements() * MekanismConfig.general.superheatingHeatTransfer.get() / SynchronizedBoilerData.getHeatEnthalpy());
        drawString(TextComponentUtil.build(Translation.of("gui.mekanism.boilCapacity"), ": " + boilCapacity + " mB/t"), 8, 72, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        boilGraph.addData(tileEntity.getLastBoilRate());
        maxGraph.addData(tileEntity.getLastMaxBoil());
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "boiler_stats.png");
    }
}