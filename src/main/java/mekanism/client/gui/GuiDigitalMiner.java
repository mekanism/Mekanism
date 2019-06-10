package mekanism.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.client.gui.element.tab.GuiVisualsTab;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.content.miner.ThreadMinerSearch.State;
import mekanism.common.inventory.container.ContainerDigitalMiner;
import mekanism.common.network.PacketDigitalMinerGui.DigitalMinerGuiMessage;
import mekanism.common.network.PacketDigitalMinerGui.MinerGuiPacket;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiDigitalMiner extends GuiMekanismTile<TileEntityDigitalMiner> {

    private GuiButton startButton;
    private GuiButton stopButton;
    private GuiButton configButton;

    public GuiDigitalMiner(InventoryPlayer inventory, TileEntityDigitalMiner tile) {
        super(tile, new ContainerDigitalMiner(inventory, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiSecurityTab(this, tileEntity, resource));
        addGuiElement(new GuiUpgradeTab(this, tileEntity, resource));
        addGuiElement(new GuiPowerBar(this, tileEntity, resource, 163, 23));
        addGuiElement(new GuiVisualsTab(this, tileEntity, resource));
        addGuiElement(new GuiEnergyInfo(() -> {
            double perTick = tileEntity.getPerTick();
            String multiplier = MekanismUtils.getEnergyDisplay(perTick);
            ArrayList<String> ret = new ArrayList<>(4);
            ret.add(LangUtils.localize("mekanism.gui.digitalMiner.capacity") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy()));
            ret.add(LangUtils.localize("gui.needed") + ": " + multiplier + "/t");
            if (perTick > tileEntity.getMaxEnergy()) {
                ret.add(TextFormatting.RED + LangUtils.localize("mekanism.gui.insufficientbuffer"));
            }
            ret.add(LangUtils.localize("mekanism.gui.bufferfree") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy() - tileEntity.getEnergy()));
            return ret;
        }, this, resource));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 151, 5).with(SlotOverlay.POWER));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 143, 26));
        ySize += 64;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        startButton = new GuiButton(0, guiLeft + 69, guiTop + 17, 60, 20, LangUtils.localize("gui.start"));
        if (tileEntity.searcher.state != State.IDLE && tileEntity.running) {
            startButton.enabled = false;
        }
        stopButton = new GuiButton(1, guiLeft + 69, guiTop + 37, 60, 20, LangUtils.localize("gui.stop"));
        if (tileEntity.searcher.state == State.IDLE || !tileEntity.running) {
            stopButton.enabled = false;
        }
        configButton = new GuiButton(2, guiLeft + 69, guiTop + 57, 60, 20, LangUtils.localize("gui.config"));
        if (tileEntity.searcher.state != State.IDLE) {
            configButton.enabled = false;
        }
        buttonList.add(startButton);
        buttonList.add(stopButton);
        buttonList.add(configButton);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == 0) {
            TileNetworkList data = TileNetworkList.withContents(3);
            Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, data));
        } else if (guibutton.id == 1) {
            TileNetworkList data = TileNetworkList.withContents(4);
            Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, data));
        } else if (guibutton.id == 2) {
            Mekanism.packetHandler.sendToServer(new DigitalMinerGuiMessage(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), 0, 0, 0));
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        startButton.enabled = tileEntity.searcher.state == State.IDLE || !tileEntity.running;
        stopButton.enabled = tileEntity.searcher.state != State.IDLE && tileEntity.running;
        configButton.enabled = tileEntity.searcher.state == State.IDLE;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(tileEntity.getName(), 69, 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        String runningType;
        if (tileEntity.getPerTick() > tileEntity.getMaxEnergy()) {
            runningType = LangUtils.localize("mekanism.gui.digitalMiner.lowPower");
        } else if (tileEntity.running) {
            runningType = LangUtils.localize("gui.digitalMiner.running");
        } else {
            runningType = LangUtils.localize("gui.idle");
        }
        fontRenderer.drawString(runningType, 9, 10, 0x00CD00);
        fontRenderer.drawString(tileEntity.searcher.state.desc, 9, 19, 0x00CD00);

        fontRenderer.drawString(LangUtils.localize("gui.eject") + ": " + LangUtils.localize("gui." + (tileEntity.doEject ? "on" : "off")), 9, 30, 0x00CD00);
        fontRenderer.drawString(LangUtils.localize("gui.digitalMiner.pull") + ": " + LangUtils.localize("gui." + (tileEntity.doPull ? "on" : "off")), 9, 39, 0x00CD00);
        fontRenderer.drawString(LangUtils.localize("gui.digitalMiner.silk") + ": " + LangUtils.localize("gui." + (tileEntity.silkTouch ? "on" : "off")), 9, 48, 0x00CD00);
        fontRenderer.drawString(LangUtils.localize("gui.digitalMiner.toMine") + ":", 9, 59, 0x00CD00);
        fontRenderer.drawString("" + tileEntity.clientToMine, 9, 68, 0x00CD00);

        if (!tileEntity.missingStack.isEmpty()) {
            drawColorIcon(144, 27, EnumColor.DARK_RED, 0.8F);
            renderItem(tileEntity.missingStack, 144, 27);
        } else {
            mc.getTextureManager().bindTexture(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiSlot.png"));
            drawTexturedModalRect(143, 26, SlotOverlay.CHECK.textureX, SlotOverlay.CHECK.textureY, 18, 18);
        }

        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 164 && xAxis <= 168 && yAxis >= 25 && yAxis <= 77) {
            drawHoveringText(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()), xAxis, yAxis);
        } else if (xAxis >= 147 && xAxis <= 161 && yAxis >= 47 && yAxis <= 61) {
            drawHoveringText(LangUtils.localize("gui.autoEject"), xAxis, yAxis);
        } else if (xAxis >= 147 && xAxis <= 161 && yAxis >= 63 && yAxis <= 77) {
            drawHoveringText(LangUtils.localize("gui.digitalMiner.autoPull"), xAxis, yAxis);
        } else if (xAxis >= 144 && xAxis <= 160 && yAxis >= 27 && yAxis <= 43) {
            if (!tileEntity.missingStack.isEmpty()) {
                drawHoveringText(LangUtils.localize("gui.digitalMiner.missingBlock"), xAxis, yAxis);
            } else {
                drawHoveringText(LangUtils.localize("gui.well"), xAxis, yAxis);
            }
        } else if (xAxis >= 131 && xAxis <= 145 && yAxis >= 47 && yAxis <= 61) {
            drawHoveringText(LangUtils.localize("gui.digitalMiner.reset"), xAxis, yAxis);
        } else if (xAxis >= 131 && xAxis <= 145 && yAxis >= 63 && yAxis <= 77) {
            drawHoveringText(LangUtils.localize("gui.digitalMiner.silkTouch"), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        int displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiLeft + 164, guiTop + 25 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
        boolean xBounds1 = xAxis >= 147 && xAxis <= 161;
        boolean xBounds2 = xAxis >= 131 && xAxis <= 145;
        boolean yBounds1 = yAxis >= 47 && yAxis <= 61;
        boolean yBounds2 = yAxis >= 63 && yAxis <= 77;
        drawTexturedModalRect(guiLeft + 147, guiTop + 47, 180, xBounds1 && yBounds1, 14);
        drawTexturedModalRect(guiLeft + 147, guiTop + 63, 194, xBounds1 && yBounds2, 14);
        drawTexturedModalRect(guiLeft + 131, guiTop + 47, 208, xBounds2 && yBounds1, 14);
        drawTexturedModalRect(guiLeft + 131, guiTop + 63, 222, xBounds2 && yBounds2, 14);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            int xAxis = mouseX - guiLeft;
            int yAxis = mouseY - guiTop;
            if (xAxis >= 147 && xAxis <= 161 && yAxis >= 47 && yAxis <= 61) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                TileNetworkList data = TileNetworkList.withContents(0);
                Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, data));
            } else if (xAxis >= 147 && xAxis <= 161 && yAxis >= 63 && yAxis <= 77) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                TileNetworkList data = TileNetworkList.withContents(1);
                Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, data));
            } else if (xAxis >= 131 && xAxis <= 145 && yAxis >= 47 && yAxis <= 61) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                TileNetworkList data = TileNetworkList.withContents(5);
                Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, data));
            } else if (xAxis >= 131 && xAxis <= 145 && yAxis >= 63 && yAxis <= 77) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                TileNetworkList data = TileNetworkList.withContents(9);
                Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, data));
            }
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiDigitalMiner.png");
    }
}