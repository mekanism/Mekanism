package mekanism.client.gui;

import java.util.ArrayList;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.client.gui.element.tab.GuiVisualsTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.capabilities.energy.MinerEnergyContainer;
import mekanism.common.content.miner.ThreadMinerSearch.State;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiDigitalMiner extends GuiMekanismTile<TileEntityDigitalMiner, MekanismTileContainer<TileEntityDigitalMiner>> {

    private MekanismButton startButton;
    private MekanismButton stopButton;
    private MekanismButton configButton;

    public GuiDigitalMiner(MekanismTileContainer<TileEntityDigitalMiner> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize += 64;
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 7, 8, 58, 69));
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 163, 25, 50));
        addButton(new GuiVisualsTab(this, tile));
        addButton(new GuiSlot(SlotType.NORMAL, this, 143, 26).validity(() -> tile.missingStack)
              .with(() -> tile.missingStack.isEmpty() ? SlotOverlay.CHECK : null)
              .hover(getOnHover(() -> tile.missingStack.isEmpty() ? MekanismLang.MINER_WELL.translate() : MekanismLang.MINER_MISSING_BLOCK.translate())));
        addButton(new GuiEnergyInfo(() -> {
            MinerEnergyContainer energyContainer = tile.getEnergyContainer();
            FloatingLong perTick = energyContainer.getEnergyPerTick();
            ArrayList<ITextComponent> ret = new ArrayList<>(4);
            ret.add(MekanismLang.MINER_ENERGY_CAPACITY.translate(EnergyDisplay.of(energyContainer.getMaxEnergy())));
            ret.add(MekanismLang.NEEDED_PER_TICK.translate(EnergyDisplay.of(perTick)));
            if (perTick.greaterThan(energyContainer.getMaxEnergy())) {
                ret.add(MekanismLang.MINER_INSUFFICIENT_BUFFER.translateColored(EnumColor.RED));
            }
            ret.add(MekanismLang.MINER_BUFFER_FREE.translate(EnergyDisplay.of(energyContainer.getNeeded())));
            return ret;
        }, this));
        addButton(startButton = new TranslationButton(this, getGuiLeft() + 69, getGuiTop() + 17, 60, 20, MekanismLang.BUTTON_START,
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.START_BUTTON, tile.getPos()))));
        addButton(stopButton = new TranslationButton(this, getGuiLeft() + 69, getGuiTop() + 37, 60, 20, MekanismLang.BUTTON_STOP,
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.STOP_BUTTON, tile.getPos()))));
        addButton(configButton = new TranslationButton(this, getGuiLeft() + 69, getGuiTop() + 57, 60, 20, MekanismLang.BUTTON_CONFIG,
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.DIGITAL_MINER_CONFIG, tile.getPos()))));
        addButton(new MekanismImageButton(this, getGuiLeft() + 131, getGuiTop() + 47, 14, getButtonLocation("reset"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.RESET_BUTTON, tile.getPos())), getOnHover(MekanismLang.MINER_RESET)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 131, getGuiTop() + 63, 14, getButtonLocation("silk_touch"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.SILK_TOUCH_BUTTON, tile.getPos())), getOnHover(MekanismLang.MINER_SILK)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 147, getGuiTop() + 47, 14, getButtonLocation("auto_eject"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.AUTO_EJECT_BUTTON, tile.getPos())), getOnHover(MekanismLang.AUTO_EJECT)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 147, getGuiTop() + 63, 14, getButtonLocation("auto_pull"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.AUTO_PULL_BUTTON, tile.getPos())), getOnHover(MekanismLang.AUTO_PULL)));
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
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, 0x404040);
        ILangEntry runningType;
        if (tile.getEnergyContainer().getEnergyPerTick().greaterThan(tile.getEnergyContainer().getMaxEnergy())) {
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
        drawString(MekanismLang.MINER_SILK_ENABLED.translate(OnOff.of(tile.getSilkTouch())), 9, 48, 0x00CD00);
        drawString(MekanismLang.MINER_TO_MINE.translate(), 9, 59, 0x00CD00);
        drawString(TextComponentUtil.build(tile.cachedToMine), 9, 68, 0x00CD00);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}