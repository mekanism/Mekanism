package mekanism.client.gui;

import java.util.Arrays;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.GuiGasMode;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiElectrolyticSeparator extends GuiMekanismTile<TileEntityElectrolyticSeparator, MekanismTileContainer<TileEntityElectrolyticSeparator>> {

    public GuiElectrolyticSeparator(MekanismTileContainer<TileEntityElectrolyticSeparator> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(MekanismLang.USING.translate(EnergyDisplay.of(tile.clientEnergyUsed)),
              MekanismLang.NEEDED.translate(EnergyDisplay.of(tile.getEnergyContainer().getNeeded()))), this));
        addButton(new GuiFluidGauge(() -> tile.fluidTank, GaugeType.STANDARD, this, 5, 10));
        addButton(new GuiGasGauge(() -> tile.leftTank, GaugeType.SMALL, this, 58, 18));
        addButton(new GuiGasGauge(() -> tile.rightTank, GaugeType.SMALL, this, 100, 18));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiProgress(() -> tile.getActive() ? 1 : 0, ProgressType.BI, this, 80, 30));
        addButton(new GuiGasMode(this, getGuiLeft() + 7, getGuiTop() + 72, false, () -> tile.dumpLeft,
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents((byte) 0)))));
        addButton(new GuiGasMode(this, getGuiLeft() + 159, getGuiTop() + 72, true, () -> tile.dumpRight,
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents((byte) 1)))));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), 45, 6, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}