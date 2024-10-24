package mekanism.client.gui.machine;

import java.util.ArrayList;
import java.util.List;
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
import mekanism.client.gui.element.tab.GuiVisualsTab;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MinerEnergyContainer;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.miner.ThreadMinerSearch.State;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.network.to_server.button.PacketTileButtonPress;
import mekanism.common.network.to_server.button.PacketTileButtonPress.ClickedTileButton;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiDigitalMiner extends GuiMekanismTile<TileEntityDigitalMiner, MekanismTileContainer<TileEntityDigitalMiner>> {

    private static final ResourceLocation EJECT = MekanismUtils.getResource(ResourceType.GUI, "switch/eject.png");
    private static final ResourceLocation INPUT = MekanismUtils.getResource(ResourceType.GUI, "switch/input.png");
    private static final ResourceLocation SILK = MekanismUtils.getResource(ResourceType.GUI, "switch/silk.png");

    private MekanismButton startButton;
    private MekanismButton stopButton;
    private MekanismButton configButton;

    public GuiDigitalMiner(MekanismTileContainer<TileEntityDigitalMiner> container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight += 76;
        inventoryLabelY = imageHeight - 94;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        int missingStackX = 64;
        addRenderableWidget(new GuiInnerScreen(this, 7, 19, 77, 69, () -> {
            List<Component> list = new ArrayList<>();
            ILangEntry runningType;
            if (tile.getEnergyContainer().getEnergyPerTick() > tile.getEnergyContainer().getMaxEnergy()) {
                runningType = MekanismLang.MINER_LOW_POWER;
            } else if (tile.isRunning()) {
                runningType = MekanismLang.MINER_RUNNING;
            } else {
                runningType = MekanismLang.IDLE;
            }
            list.add(runningType.translate());
            list.add(tile.searcher.state.getTextComponent());
            list.add(MekanismLang.MINER_TO_MINE.translate(TextUtils.format(tile.getToMine())));
            return list;
        }) {
            @Override
            protected int getMaxTextWidth(int row) {
                if (row < 2) {
                    return missingStackX - relativeX + 4;
                }
                return super.getMaxTextWidth(row);
            }
        }).clearSpacing().clearFormat();
        addRenderableWidget(new GuiDigitalSwitch(this, 19, 56, EJECT, tile::getDoEject, (element, mouseX, mouseY) ->
              PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.AUTO_EJECT_BUTTON, ((GuiDigitalMiner) element.gui()).tile)), SwitchType.LOWER_ICON))
              .setTooltip(MekanismLang.AUTO_EJECT);
        addRenderableWidget(new GuiDigitalSwitch(this, 38, 56, INPUT, tile::getDoPull, (element, mouseX, mouseY) ->
              PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.AUTO_PULL_BUTTON, ((GuiDigitalMiner) element.gui()).tile)), SwitchType.LOWER_ICON))
              .setTooltip(MekanismLang.AUTO_PULL);
        addRenderableWidget(new GuiDigitalSwitch(this, 57, 56, SILK, tile::getSilkTouch, (element, mouseX, mouseY) ->
              PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.SILK_TOUCH_BUTTON, ((GuiDigitalMiner) element.gui()).tile)), SwitchType.LOWER_ICON))
              .setTooltip(MekanismLang.MINER_SILK);
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 157, 39, 47))
              .warning(WarningType.NOT_ENOUGH_ENERGY, () -> {
                  MinerEnergyContainer energyContainer = tile.getEnergyContainer();
                  return energyContainer.getEnergyPerTick() > energyContainer.getEnergy();
              });
        addRenderableWidget(new GuiVisualsTab(this, tile));
        addRenderableWidget(new GuiSlot(SlotType.DIGITAL, this, missingStackX, 21).setRenderAboveSlots().validity(() -> tile.missingStack)
              .with(() -> tile.missingStack.isEmpty() ? SlotOverlay.CHECK : null)
              .hover(element -> ((GuiDigitalMiner) element.gui()).tile.missingStack.isEmpty() ? List.of(MekanismLang.MINER_WELL.translate()) : List.of(MekanismLang.MINER_MISSING_BLOCK.translate())));
        addRenderableWidget(new GuiEnergyTab(this, () -> {
            MinerEnergyContainer energyContainer = tile.getEnergyContainer();
            return List.of(
                  MekanismLang.MINER_ENERGY_CAPACITY.translate(EnergyDisplay.of(energyContainer.getMaxEnergy())),
                  MekanismLang.NEEDED_PER_TICK.translate(EnergyDisplay.of(energyContainer.getEnergyPerTick())),
                  MekanismLang.MINER_BUFFER_FREE.translate(EnergyDisplay.of(energyContainer.getNeeded()))
            );
        }));

        int buttonStart = 19;
        startButton = addRenderableWidget(new TranslationButton(this, 87, buttonStart, 61, 18, MekanismLang.BUTTON_START,
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.START_BUTTON, ((GuiDigitalMiner) element.gui()).tile))));
        stopButton = addRenderableWidget(new TranslationButton(this, 87, buttonStart + 17, 61, 18, MekanismLang.BUTTON_STOP,
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.STOP_BUTTON, ((GuiDigitalMiner) element.gui()).tile))));
        configButton = addRenderableWidget(new TranslationButton(this, 87, buttonStart + 34, 61, 18, MekanismLang.BUTTON_CONFIG,
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketTileButtonPress(ClickedTileButton.DIGITAL_MINER_CONFIG, ((GuiDigitalMiner) element.gui()).tile))));
        addRenderableWidget(new TranslationButton(this, 87, buttonStart + 51, 61, 18, MekanismLang.MINER_RESET,
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.RESET_BUTTON, ((GuiDigitalMiner) element.gui()).tile))));
        updateEnabledButtons();
        trackWarning(WarningType.FILTER_HAS_BLACKLISTED_ELEMENT, () -> tile.getFilterManager().anyEnabledMatch(MinerFilter::hasBlacklistedElement));
        trackWarning(WarningType.NO_SPACE_IN_OUTPUT_OVERFLOW, tile::hasOverflow);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        updateEnabledButtons();
    }

    private void updateEnabledButtons() {
        startButton.active = tile.searcher.state == State.IDLE || !tile.isRunning();
        stopButton.active = tile.searcher.state != State.IDLE && tile.isRunning();
        configButton.active = tile.searcher.state == State.IDLE;
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}