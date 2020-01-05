package mekanism.client.gui;

import java.util.Arrays;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.button.MekanismImageButton;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.RotaryCondensentratorContainer;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiRotaryCondensentrator extends GuiMekanismTile<TileEntityRotaryCondensentrator, RotaryCondensentratorContainer> {

    public GuiRotaryCondensentrator(RotaryCondensentratorContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiSecurityTab<>(this, tile, resource));
        addButton(new GuiRedstoneControl(this, tile, resource));
        addButton(new GuiUpgradeTab(this, tile, resource));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 4, 24).with(SlotOverlay.PLUS));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 4, 55).with(SlotOverlay.MINUS));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 154, 24));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 154, 55));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 154, 4).with(SlotOverlay.POWER));
        addButton(new GuiHorizontalPowerBar(this, tile, resource, 115, 75));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(MekanismLang.USING.translate(EnergyDisplay.of(tile.clientEnergyUsed)),
              MekanismLang.NEEDED.translate(EnergyDisplay.of(tile.getNeededEnergy()))), this, resource));
        addButton(new GuiFluidGauge(() -> tile.fluidTank, GuiGauge.Type.STANDARD, this, resource, 133, 13));
        addButton(new GuiGasGauge(() -> tile.gasTank, GuiGauge.Type.STANDARD, this, resource, 25, 13));
        addButton(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tile.getActive() ? 1 : 0;
            }

            @Override
            public boolean isActive() {
                return !tile.mode;
            }
        }, ProgressBar.LARGE_RIGHT, this, resource, 62, 38));
        addButton(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tile.getActive() ? 1 : 0;
            }

            @Override
            public boolean isActive() {
                return tile.mode;
            }
        }, ProgressBar.LARGE_LEFT, this, resource, 62, 38));
        addButton(new MekanismImageButton(this, getGuiLeft() + 4, getGuiTop() + 4, 18, getButtonLocation("toggle"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(0))), getOnHover(MekanismLang.CONDENSENTRATOR_TOGGLE)));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 4, 0x404040);
        drawString((tile.mode ? MekanismLang.DECONDENSENTRATING : MekanismLang.CONDENSENTRATING).translate(), 6, (getYSize() - 94) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        int displayInt = tile.getScaledEnergyLevel(52);
        drawTexturedRect(getGuiLeft() + 116, getGuiTop() + 76, 176, 36, displayInt, 4);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "rotary_condensentrator.png");
    }
}