package mekanism.client.gui;

import java.io.IOException;
import java.util.Arrays;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.button.GuiButtonImageMek;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerRotaryCondensentrator;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiRotaryCondensentrator extends GuiMekanismTile<TileEntityRotaryCondensentrator> {

    private GuiButtonImageMek toggleButton;

    public GuiRotaryCondensentrator(InventoryPlayer inventory, TileEntityRotaryCondensentrator tile) {
        super(tile, new ContainerRotaryCondensentrator(inventory, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiSecurityTab(this, tileEntity, resource));
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiUpgradeTab(this, tileEntity, resource));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 4, 24).with(SlotOverlay.PLUS));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 4, 55).with(SlotOverlay.MINUS));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 154, 24));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 154, 55));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 154, 4).with(SlotOverlay.POWER));
        addGuiElement(new GuiEnergyInfo(() -> {
            String usage = MekanismUtils.getEnergyDisplay(tileEntity.clientEnergyUsed);
            return Arrays.asList(LangUtils.localize("gui.using") + ": " + usage + "/t",
                  LangUtils.localize("gui.needed") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy() - tileEntity.getEnergy()));
        }, this, resource));
        addGuiElement(new GuiFluidGauge(() -> tileEntity.fluidTank, GuiGauge.Type.STANDARD, this, resource, 133, 13));
        addGuiElement(new GuiGasGauge(() -> tileEntity.gasTank, GuiGauge.Type.STANDARD, this, resource, 25, 13));
        addGuiElement(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.getActive() ? 1 : 0;
            }

            @Override
            public boolean isActive() {
                return tileEntity.mode == 0;
            }
        }, ProgressBar.LARGE_RIGHT, this, resource, 62, 38));
        addGuiElement(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.getActive() ? 1 : 0;
            }

            @Override
            public boolean isActive() {
                return tileEntity.mode == 1;
            }
        }, ProgressBar.LARGE_LEFT, this, resource, 62, 38));
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        toggleButton = new GuiButtonImageMek(0, guiLeft + 4, guiTop + 4, 18, 18, 176, 18, -18, getGuiLocation());
        buttonList.add(toggleButton);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == toggleButton.id) {
            Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, TileNetworkList.withContents(0)));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(tileEntity.getName(), (xSize / 2) - (fontRenderer.getStringWidth(tileEntity.getName()) / 2), 4, 0x404040);
        fontRenderer.drawString(tileEntity.mode == 0 ? LangUtils.localize("gui.condensentrating")
                                                     : LangUtils.localize("gui.decondensentrating"), 6, (ySize - 94) + 2, 0x404040);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (toggleButton.isMouseOver()) {
            drawHoveringText(LangUtils.localize("gui.rotaryCondensentrator.toggleOperation"), xAxis, yAxis);
        } else if (xAxis >= 116 && xAxis <= 168 && yAxis >= 76 && yAxis <= 80) {
            drawHoveringText(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        int displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiLeft + 116, guiTop + 76, 176, 36, displayInt, 4);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png");
    }
}