package mekanism.client.gui;

import java.util.ArrayList;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.button.MekanismButton;
import mekanism.client.gui.button.MekanismImageButton;
import mekanism.client.gui.button.TranslationButton;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.client.gui.element.tab.GuiVisualsTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.content.miner.ThreadMinerSearch.State;
import mekanism.common.inventory.container.tile.DigitalMinerContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiDigitalMiner extends GuiMekanismTile<TileEntityDigitalMiner, DigitalMinerContainer> {

    private MekanismButton startButton;
    private MekanismButton stopButton;
    private MekanismButton configButton;

    public GuiDigitalMiner(DigitalMinerContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize += 64;
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiRedstoneControl(this, tile, resource));
        addButton(new GuiSecurityTab<>(this, tile, resource));
        addButton(new GuiUpgradeTab(this, tile, resource));
        addButton(new GuiVerticalPowerBar(this, tile, resource, 163, 23));
        addButton(new GuiVisualsTab(this, tile, resource));
        addButton(new GuiEnergyInfo(() -> {
            double perTick = tile.getPerTick();
            ArrayList<ITextComponent> ret = new ArrayList<>(4);
            ret.add(MekanismLang.MINER_ENERGY_CAPACITY.translate(EnergyDisplay.of(tile.getMaxEnergy())));
            ret.add(MekanismLang.NEEDED_PER_TICK.translate(EnergyDisplay.of(perTick)));
            if (perTick > tile.getMaxEnergy()) {
                ret.add(MekanismLang.INSUFFICIENT_BUFFER.translateColored(EnumColor.RED));
            }
            ret.add(MekanismLang.BUFFER_FREE.translate(EnergyDisplay.of(tile.getNeededEnergy())));
            return ret;
        }, this, resource));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 151, 5).with(SlotOverlay.POWER));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 143, 26));

        addButton(startButton = new TranslationButton(this, guiLeft + 69, guiTop + 17, 60, 20, MekanismLang.BUTTON_START,
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(3)))));
        addButton(stopButton = new TranslationButton(this, guiLeft + 69, guiTop + 37, 60, 20, MekanismLang.BUTTON_STOP,
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(4)))));
        addButton(configButton = new TranslationButton(this, guiLeft + 69, guiTop + 57, 60, 20, MekanismLang.BUTTON_CONFIG,
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.DIGITAL_MINER_CONFIG, tile.getPos()))));
        addButton(new MekanismImageButton(this, guiLeft + 131, guiTop + 47, 14, getButtonLocation("reset"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(5))), getOnHover(MekanismLang.MINER_RESET)));
        addButton(new MekanismImageButton(this, guiLeft + 131, guiTop + 63, 14, getButtonLocation("silk_touch"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(9))), getOnHover(MekanismLang.MINER_SILK)));
        addButton(new MekanismImageButton(this, guiLeft + 147, guiTop + 47, 14, getButtonLocation("auto_eject"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(0))), getOnHover(MekanismLang.AUTO_EJECT)));
        addButton(new MekanismImageButton(this, guiLeft + 147, guiTop + 63, 14, getButtonLocation("auto_pull"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(1))), getOnHover(MekanismLang.AUTO_PULL)));
        updateEnabledButtons();
    }

    @Override
    public void tick() {
        super.tick();
        updateEnabledButtons();
    }

    private void updateEnabledButtons() {
        startButton.active = tile.searcher.state == State.IDLE || !tile.running;
        stopButton.active = tile.searcher.state != State.IDLE && tile.running;
        configButton.active = tile.searcher.state == State.IDLE;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), 69, 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (ySize - 96) + 2, 0x404040);
        ILangEntry runningType;
        if (tile.getPerTick() > tile.getMaxEnergy()) {
            runningType = MekanismLang.MINER_LOW_POWER;
        } else if (tile.running) {
            runningType = MekanismLang.MINER_RUNNING;
        } else {
            runningType = MekanismLang.IDLE;
        }
        drawString(runningType.translate(), 9, 10, 0x00CD00);
        drawString(tile.searcher.state.getTextComponent(), 9, 19, 0x00CD00);

        drawString(MekanismLang.EJECT.translate(OnOff.of(tile.doEject)), 9, 30, 0x00CD00);
        drawString(MekanismLang.MINER_AUTO_PULL.translate(OnOff.of(tile.doPull)), 9, 39, 0x00CD00);
        drawString(MekanismLang.MINER_SILK_ENABLED.translate(OnOff.of(tile.silkTouch)), 9, 48, 0x00CD00);
        drawString(MekanismLang.MINER_TO_MINE.translate(tile.clientToMine), 9, 59, 0x00CD00);

        if (!tile.missingStack.isEmpty()) {
            drawColorIcon(144, 27, EnumColor.DARK_RED, 0.8F);
            renderItem(tile.missingStack, 144, 27);
        } else {
            minecraft.getTextureManager().bindTexture(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "slot.png"));
            drawTexturedRect(143, 26, SlotOverlay.CHECK.textureX, SlotOverlay.CHECK.textureY, 18, 18);
        }

        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 164 && xAxis <= 168 && yAxis >= 25 && yAxis <= 77) {
            displayTooltip(EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy()).getTextComponent(), xAxis, yAxis);
        } else if (xAxis >= 144 && xAxis <= 160 && yAxis >= 27 && yAxis <= 43) {
            if (!tile.missingStack.isEmpty()) {
                displayTooltip(MekanismLang.MINER_MISSING_BLOCK.translate(), xAxis, yAxis);
            } else {
                displayTooltip(MekanismLang.MINER_WELL.translate(), xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        int displayInt = tile.getScaledEnergyLevel(52);
        drawTexturedRect(guiLeft + 164, guiTop + 25 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "digital_miner.png");
    }
}