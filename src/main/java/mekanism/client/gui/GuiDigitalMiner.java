package mekanism.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.client.gui.element.tab.GuiVisualsTab;
import mekanism.common.Mekanism;
import mekanism.common.content.miner.ThreadMinerSearch.State;
import mekanism.common.inventory.container.ContainerDigitalMiner;
import mekanism.common.network.PacketDigitalMinerGui;
import mekanism.common.network.PacketDigitalMinerGui.MinerGuiPacket;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiDigitalMiner extends GuiMekanismTile<TileEntityDigitalMiner> {

    public static final int START_BUTTON_ID = 0;
    public static final int STOP_BUTTON_ID = 1;
    public static final int CONFIG_BUTTON_ID = 2;
    public static final int RESET_BUTTON_ID = 3;
    public static final int SILK_TOUCH_BUTTON_ID = 4;
    public static final int AUTOEJECT_BUTTON_ID = 5;
    public static final int AUTO_PULL_BUTTON_ID = 6;
    private Button startButton;
    private Button stopButton;
    private Button configButton;
    private Button resetButton;
    private Button silkTouchButton;
    private Button autoEjectButton;
    private Button autoPullButton;

    public GuiDigitalMiner(PlayerInventory inventory, TileEntityDigitalMiner tile) {
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
            ret.add(LangUtils.localize("mekanism.gui.bufferfree") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getNeededEnergy()));
            return ret;
        }, this, resource));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 151, 5).with(SlotOverlay.POWER));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 143, 26));
        ySize += 64;
    }

    @Override
    public void init() {
        super.init();
        buttons.clear();
        buttons.add(this.startButton = new Button(START_BUTTON_ID, guiLeft + 69, guiTop + 17, 60, 20, LangUtils.localize("gui.start")));
        buttons.add(this.stopButton = new Button(STOP_BUTTON_ID, guiLeft + 69, guiTop + 37, 60, 20, LangUtils.localize("gui.stop")));
        buttons.add(this.configButton = new Button(CONFIG_BUTTON_ID, guiLeft + 69, guiTop + 57, 60, 20, LangUtils.localize("gui.config")));
        buttons.add(this.resetButton = new GuiButtonDisableableImage(RESET_BUTTON_ID, guiLeft + 131, guiTop + 47, 14, 14, 208, 14, -14, getGuiLocation()));
        buttons.add(this.silkTouchButton = new GuiButtonDisableableImage(SILK_TOUCH_BUTTON_ID, guiLeft + 131, guiTop + 63, 14, 14, 222, 14, -14, getGuiLocation()));
        buttons.add(this.autoEjectButton = new GuiButtonDisableableImage(AUTOEJECT_BUTTON_ID, guiLeft + 147, guiTop + 47, 14, 14, 180, 14, -14, getGuiLocation()));
        buttons.add(this.autoPullButton = new GuiButtonDisableableImage(AUTO_PULL_BUTTON_ID, guiLeft + 147, guiTop + 63, 14, 14, 194, 14, -14, getGuiLocation()));
        updateEnabledButtons();
    }

    @Override
    protected void actionPerformed(Button guibutton) throws IOException {
        super.actionPerformed(guibutton);
        switch (guibutton.id) {
            case START_BUTTON_ID:
                Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(3)));
                break;
            case STOP_BUTTON_ID:
                Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(4)));
                break;
            case CONFIG_BUTTON_ID:
                Mekanism.packetHandler.sendToServer(new PacketDigitalMinerGui(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), 0, 0, 0));
                break;
            case RESET_BUTTON_ID:
                Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(5)));
                break;
            case SILK_TOUCH_BUTTON_ID:
                Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(9)));
                break;
            case AUTOEJECT_BUTTON_ID:
                Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(0)));
                break;
            case AUTO_PULL_BUTTON_ID:
                Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(1)));
                break;
        }
    }

    @Override
    public void tick() {
        super.tick();
        updateEnabledButtons();
    }

    private void updateEnabledButtons() {
        startButton.active = tileEntity.searcher.state == State.IDLE || !tileEntity.running;
        stopButton.active = tileEntity.searcher.state != State.IDLE && tileEntity.running;
        configButton.active = tileEntity.searcher.state == State.IDLE;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        font.drawString(tileEntity.getName(), 69, 6, 0x404040);
        font.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        String runningType;
        if (tileEntity.getPerTick() > tileEntity.getMaxEnergy()) {
            runningType = LangUtils.localize("mekanism.gui.digitalMiner.lowPower");
        } else if (tileEntity.running) {
            runningType = LangUtils.localize("gui.digitalMiner.running");
        } else {
            runningType = LangUtils.localize("gui.idle");
        }
        font.drawString(runningType, 9, 10, 0x00CD00);
        font.drawString(tileEntity.searcher.state.desc, 9, 19, 0x00CD00);

        font.drawString(LangUtils.localize("gui.eject") + ": " + LangUtils.localize("gui." + (tileEntity.doEject ? "on" : "off")), 9, 30, 0x00CD00);
        font.drawString(LangUtils.localize("gui.digitalMiner.pull") + ": " + LangUtils.localize("gui." + (tileEntity.doPull ? "on" : "off")), 9, 39, 0x00CD00);
        font.drawString(LangUtils.localize("gui.digitalMiner.silk") + ": " + LangUtils.localize("gui." + (tileEntity.silkTouch ? "on" : "off")), 9, 48, 0x00CD00);
        font.drawString(LangUtils.localize("gui.digitalMiner.toMine") + ":", 9, 59, 0x00CD00);
        font.drawString("" + tileEntity.clientToMine, 9, 68, 0x00CD00);

        if (!tileEntity.missingStack.isEmpty()) {
            drawColorIcon(144, 27, EnumColor.DARK_RED, 0.8F);
            renderItem(tileEntity.missingStack, 144, 27);
        } else {
            minecraft.getTextureManager().bindTexture(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiSlot.png"));
            drawTexturedModalRect(143, 26, SlotOverlay.CHECK.textureX, SlotOverlay.CHECK.textureY, 18, 18);
        }

        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (autoEjectButton.isMouseOver()) {
            displayTooltip(LangUtils.localize("gui.autoEject"), xAxis, yAxis);
        } else if (autoPullButton.isMouseOver()) {
            displayTooltip(LangUtils.localize("gui.digitalMiner.autoPull"), xAxis, yAxis);
        } else if (resetButton.isMouseOver()) {
            displayTooltip(LangUtils.localize("gui.digitalMiner.reset"), xAxis, yAxis);
        } else if (silkTouchButton.isMouseOver()) {
            displayTooltip(LangUtils.localize("gui.digitalMiner.silkTouch"), xAxis, yAxis);
        } else if (xAxis >= 164 && xAxis <= 168 && yAxis >= 25 && yAxis <= 77) {
            displayTooltip(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()), xAxis, yAxis);
        } else if (xAxis >= 144 && xAxis <= 160 && yAxis >= 27 && yAxis <= 43) {
            if (!tileEntity.missingStack.isEmpty()) {
                displayTooltip(LangUtils.localize("gui.digitalMiner.missingBlock"), xAxis, yAxis);
            } else {
                displayTooltip(LangUtils.localize("gui.well"), xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        int displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiLeft + 164, guiTop + 25 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiDigitalMiner.png");
    }
}