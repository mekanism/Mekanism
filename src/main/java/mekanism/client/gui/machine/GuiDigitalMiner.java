package mekanism.client.gui.machine;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
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
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MinerEnergyContainer;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.miner.ThreadMinerSearch.State;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.network.to_server.PacketGuiButtonPress;
import mekanism.common.network.to_server.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

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
        addRenderableWidget(new GuiInnerScreen(this, 7, 19, 77, 69, () -> {
            List<Component> list = new ArrayList<>();
            ILangEntry runningType;
            if (tile.getEnergyContainer().getEnergyPerTick().greaterThan(tile.getEnergyContainer().getMaxEnergy())) {
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
        }).spacing(1).clearFormat());
        addRenderableWidget(new GuiDigitalSwitch(this, 19, 56, EJECT, tile::getDoEject, MekanismLang.AUTO_EJECT.translate(),
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.AUTO_EJECT_BUTTON, tile)), SwitchType.LOWER_ICON));
        addRenderableWidget(new GuiDigitalSwitch(this, 38, 56, INPUT, tile::getDoPull, MekanismLang.AUTO_PULL.translate(),
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.AUTO_PULL_BUTTON, tile)), SwitchType.LOWER_ICON));
        addRenderableWidget(new GuiDigitalSwitch(this, 57, 56, SILK, tile::getSilkTouch, MekanismLang.MINER_SILK.translate(),
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.SILK_TOUCH_BUTTON, tile)), SwitchType.LOWER_ICON));
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 157, 39, 47))
              .warning(WarningType.NOT_ENOUGH_ENERGY, () -> {
                  MinerEnergyContainer energyContainer = tile.getEnergyContainer();
                  return energyContainer.getEnergyPerTick().greaterThan(energyContainer.getEnergy());
              });
        addRenderableWidget(new GuiVisualsTab(this, tile));
        addRenderableWidget(new GuiSlot(SlotType.DIGITAL, this, 64, 21).setRenderAboveSlots().validity(() -> tile.missingStack)
              .with(() -> tile.missingStack.isEmpty() ? SlotOverlay.CHECK : null)
              .hover(getOnHover(() -> tile.missingStack.isEmpty() ? MekanismLang.MINER_WELL.translate() : MekanismLang.MINER_MISSING_BLOCK.translate())));
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
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.START_BUTTON, tile))));
        stopButton = addRenderableWidget(new TranslationButton(this, 87, buttonStart + 17, 61, 18, MekanismLang.BUTTON_STOP,
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.STOP_BUTTON, tile))));
        configButton = addRenderableWidget(new TranslationButton(this, 87, buttonStart + 34, 61, 18, MekanismLang.BUTTON_CONFIG,
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(ClickedTileButton.DIGITAL_MINER_CONFIG, tile))));
        addRenderableWidget(new TranslationButton(this, 87, buttonStart + 51, 61, 18, MekanismLang.MINER_RESET,
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.RESET_BUTTON, tile))));
        updateEnabledButtons();
        trackWarning(WarningType.FILTER_HAS_BLACKLISTED_ELEMENT, () -> tile.getFilters().stream().anyMatch(MinerFilter::hasBlacklistedElement));
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
    protected void drawForegroundText(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}