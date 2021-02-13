package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiDigitalSwitch;
import mekanism.client.gui.element.GuiDigitalSwitch.SwitchType;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.client.gui.element.tab.GuiVisualsTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MinerEnergyContainer;
import mekanism.common.content.miner.ThreadMinerSearch.State;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiDigitalMiner extends GuiMekanismTile<TileEntityDigitalMiner, MekanismTileContainer<TileEntityDigitalMiner>> {

    private static final ResourceLocation eject = MekanismUtils.getResource(ResourceType.GUI, "switch/eject.png");
    private static final ResourceLocation input = MekanismUtils.getResource(ResourceType.GUI, "switch/input.png");
    private static final ResourceLocation silk = MekanismUtils.getResource(ResourceType.GUI, "switch/silk.png");

    private MekanismButton startButton;
    private MekanismButton stopButton;
    private MekanismButton configButton;

    public GuiDigitalMiner(MekanismTileContainer<TileEntityDigitalMiner> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize += 76;
        playerInventoryTitleY = ySize - 94;
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 7, 19, 77, 69, () -> {
            List<ITextComponent> list = new ArrayList<>();
            ILangEntry runningType;
            if (tile.getEnergyContainer().getEnergyPerTick().greaterThan(tile.getEnergyContainer().getMaxEnergy())) {
                runningType = MekanismLang.MINER_LOW_POWER;
            } else if (tile.running) {
                runningType = MekanismLang.MINER_RUNNING;
            } else {
                runningType = MekanismLang.IDLE;
            }
            list.add(runningType.translate());
            list.add(tile.searcher.state.getTextComponent());
            list.add(MekanismLang.MINER_TO_MINE.translate(TextUtils.format(tile.cachedToMine)));
            return list;
        }).spacing(1).clearFormat());
        addButton(new GuiDigitalSwitch(this, 19, 56, eject, () -> tile.doEject, MekanismLang.AUTO_EJECT.translate(),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.AUTO_EJECT_BUTTON, tile)), SwitchType.LOWER_ICON));
        addButton(new GuiDigitalSwitch(this, 38, 56, input, () -> tile.doPull, MekanismLang.AUTO_PULL.translate(),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.AUTO_PULL_BUTTON, tile)), SwitchType.LOWER_ICON));
        addButton(new GuiDigitalSwitch(this, 57, 56, silk, tile::getSilkTouch, MekanismLang.MINER_SILK.translate(),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.SILK_TOUCH_BUTTON, tile)), SwitchType.LOWER_ICON));
        addButton(new GuiRedstoneControlTab(this, tile));
        addButton(new GuiSecurityTab(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 157, 39, 47));
        addButton(new GuiVisualsTab(this, tile));
        addButton(new GuiSlot(SlotType.DIGITAL, this, 64, 21).setRenderAboveSlots().validity(() -> tile.missingStack)
              .with(() -> tile.missingStack.isEmpty() ? SlotOverlay.CHECK : null)
              .hover(getOnHover(() -> tile.missingStack.isEmpty() ? MekanismLang.MINER_WELL.translate() : MekanismLang.MINER_MISSING_BLOCK.translate())));
        addButton(new GuiEnergyTab(() -> {
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

        int buttonStart = guiTop + 19;
        addButton(startButton = new TranslationButton(this, guiLeft + 87, buttonStart, 61, 18, MekanismLang.BUTTON_START,
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.START_BUTTON, tile))));
        addButton(stopButton = new TranslationButton(this, guiLeft + 87, buttonStart + 17, 61, 18, MekanismLang.BUTTON_STOP,
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.STOP_BUTTON, tile))));
        addButton(configButton = new TranslationButton(this, guiLeft + 87, buttonStart + 34, 61, 18, MekanismLang.BUTTON_CONFIG,
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.DIGITAL_MINER_CONFIG, tile))));
        addButton(new TranslationButton(this, guiLeft + 87, buttonStart + 51, 61, 18, MekanismLang.MINER_RESET,
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.RESET_BUTTON, tile))));
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
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventory.getDisplayName(), playerInventoryTitleX, playerInventoryTitleY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}