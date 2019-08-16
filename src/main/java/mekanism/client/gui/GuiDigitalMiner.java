package mekanism.client.gui;

import java.util.ArrayList;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.gui.button.GuiButtonTranslation;
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
import mekanism.common.inventory.container.tile.DigitalMinerContainer;
import mekanism.common.network.PacketDigitalMinerGui;
import mekanism.common.network.PacketDigitalMinerGui.MinerGuiPacket;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiDigitalMiner extends GuiMekanismTile<TileEntityDigitalMiner, DigitalMinerContainer> {

    private Button startButton;
    private Button stopButton;
    private Button configButton;
    private Button resetButton;
    private Button silkTouchButton;
    private Button autoEjectButton;
    private Button autoPullButton;

    public GuiDigitalMiner(DigitalMinerContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiSecurityTab<>(this, tileEntity, resource));
        addGuiElement(new GuiUpgradeTab(this, tileEntity, resource));
        addGuiElement(new GuiPowerBar(this, tileEntity, resource, 163, 23));
        addGuiElement(new GuiVisualsTab(this, tileEntity, resource));
        addGuiElement(new GuiEnergyInfo(() -> {
            double perTick = tileEntity.getPerTick();
            ArrayList<ITextComponent> ret = new ArrayList<>(4);
            ret.add(TextComponentUtil.build(Translation.of("mekanism.gui.digitalMiner.capacity"), ": ", EnergyDisplay.of(tileEntity.getMaxEnergy())));
            ret.add(TextComponentUtil.build(Translation.of("mekanism.gui.needed"), ": ", EnergyDisplay.of(perTick), "/t"));
            if (perTick > tileEntity.getMaxEnergy()) {
                ret.add(TextComponentUtil.build(TextFormatting.RED, Translation.of("mekanism.gui.insufficientbuffer")));
            }
            ret.add(TextComponentUtil.build(Translation.of("mekanism.gui.bufferfree"), ": ", EnergyDisplay.of(tileEntity.getNeededEnergy())));
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
        buttons.add(this.startButton = new GuiButtonTranslation(guiLeft + 69, guiTop + 17, 60, 20, "gui.start",
              onPress -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(3)))));
        buttons.add(this.stopButton = new GuiButtonTranslation(guiLeft + 69, guiTop + 37, 60, 20, "gui.stop",
              onPress -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(4)))));
        buttons.add(this.configButton = new GuiButtonTranslation(guiLeft + 69, guiTop + 57, 60, 20, "gui.config",
              onPress -> Mekanism.packetHandler.sendToServer(new PacketDigitalMinerGui(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), 0, 0, 0))));
        buttons.add(this.resetButton = new GuiButtonDisableableImage(guiLeft + 131, guiTop + 47, 14, 14, 208, 14, -14, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(5)))));
        buttons.add(this.silkTouchButton = new GuiButtonDisableableImage(guiLeft + 131, guiTop + 63, 14, 14, 222, 14, -14, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(9)))));
        buttons.add(this.autoEjectButton = new GuiButtonDisableableImage(guiLeft + 147, guiTop + 47, 14, 14, 180, 14, -14, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(0)))));
        buttons.add(this.autoPullButton = new GuiButtonDisableableImage(guiLeft + 147, guiTop + 63, 14, 14, 194, 14, -14, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(1)))));
        updateEnabledButtons();
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
        drawString(tileEntity.getName(), 69, 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        ITextComponent runningType;
        if (tileEntity.getPerTick() > tileEntity.getMaxEnergy()) {
            runningType = TextComponentUtil.translate("mekanism.gui.digitalMiner.lowPower");
        } else if (tileEntity.running) {
            runningType = TextComponentUtil.translate("gui.digitalMiner.running");
        } else {
            runningType = TextComponentUtil.translate("gui.idle");
        }
        drawString(runningType, 9, 10, 0x00CD00);
        drawString(tileEntity.searcher.state.desc, 9, 19, 0x00CD00);

        drawString(TextComponentUtil.build(Translation.of("mekanism.gui.eject"), ": ", OnOff.of(tileEntity.doEject)), 9, 30, 0x00CD00);
        drawString(TextComponentUtil.build(Translation.of("mekanism.gui.digitalMiner.pull"), ": ", OnOff.of(tileEntity.doPull)), 9, 39, 0x00CD00);
        drawString(TextComponentUtil.build(Translation.of("mekanism.gui.digitalMiner.silk"), ": ", OnOff.of(tileEntity.silkTouch)), 9, 48, 0x00CD00);
        drawString(TextComponentUtil.build(Translation.of("mekanism.gui.digitalMiner.toMine"), ":"), 9, 59, 0x00CD00);
        //TODO: Can this be combined with the one above
        drawString("" + tileEntity.clientToMine, 9, 68, 0x00CD00);

        if (!tileEntity.missingStack.isEmpty()) {
            drawColorIcon(144, 27, EnumColor.DARK_RED, 0.8F);
            renderItem(tileEntity.missingStack, 144, 27);
        } else {
            minecraft.getTextureManager().bindTexture(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiSlot.png"));
            drawTexturedRect(143, 26, SlotOverlay.CHECK.textureX, SlotOverlay.CHECK.textureY, 18, 18);
        }

        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (autoEjectButton.isMouseOver(mouseX, mouseY)) {
            displayTooltip(TextComponentUtil.translate("mekanism.gui.autoEject"), xAxis, yAxis);
        } else if (autoPullButton.isMouseOver(mouseX, mouseY)) {
            displayTooltip(TextComponentUtil.translate("mekanism.gui.digitalMiner.autoPull"), xAxis, yAxis);
        } else if (resetButton.isMouseOver(mouseX, mouseY)) {
            displayTooltip(TextComponentUtil.translate("mekanism.gui.digitalMiner.reset"), xAxis, yAxis);
        } else if (silkTouchButton.isMouseOver(mouseX, mouseY)) {
            displayTooltip(TextComponentUtil.translate("mekanism.gui.digitalMiner.silkTouch"), xAxis, yAxis);
        } else if (xAxis >= 164 && xAxis <= 168 && yAxis >= 25 && yAxis <= 77) {
            displayTooltip(EnergyDisplay.of(tileEntity.getEnergy(), tileEntity.getMaxEnergy()).getTextComponent(), xAxis, yAxis);
        } else if (xAxis >= 144 && xAxis <= 160 && yAxis >= 27 && yAxis <= 43) {
            if (!tileEntity.missingStack.isEmpty()) {
                displayTooltip(TextComponentUtil.translate("mekanism.gui.digitalMiner.missingBlock"), xAxis, yAxis);
            } else {
                displayTooltip(TextComponentUtil.translate("mekanism.gui.well"), xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        int displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedRect(guiLeft + 164, guiTop + 25 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiDigitalMiner.png");
    }
}