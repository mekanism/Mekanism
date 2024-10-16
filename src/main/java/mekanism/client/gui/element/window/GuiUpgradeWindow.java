package mekanism.client.gui.element.window;

import java.util.EnumMap;
import java.util.Map;
import mekanism.api.Upgrade;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.DigitalButton;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.custom.GuiSupportedUpgrades;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.scroll.GuiUpgradeScrollList;
import mekanism.client.gui.element.slot.GuiVirtualSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GuiUpgradeWindow extends GuiWindow {

    private final Map<Upgrade, WrappedTextRenderer> upgradeTypeData = new EnumMap<>(Upgrade.class);
    private final WrappedTextRenderer noSelection = new WrappedTextRenderer(this, MekanismLang.UPGRADE_NO_SELECTION.translate());
    private final TileEntityMekanism tile;
    private final MekanismButton removeButton;
    private final GuiUpgradeScrollList scrollList;
    private final GuiInnerScreen rightScreen;

    private long msSelected;

    public GuiUpgradeWindow(IGuiWrapper gui, int x, int y, TileEntityMekanism tile, SelectedWindowData windowData) {
        super(gui, x, y, 198, 76 + Math.max(18, 12 * GuiSupportedUpgrades.calculateNeededRows(gui)), windowData);
        if (windowData.type != WindowType.UPGRADE) {
            throw new IllegalArgumentException("Upgrade windows must have an upgrade window type");
        }
        this.tile = tile;
        interactionStrategy = InteractionStrategy.ALL;
        scrollList = addChild(new GuiUpgradeScrollList(gui, relativeX + 6, relativeY + 18, 50, tile.getComponent(), () -> {
            updateEnabledButtons();
            msSelected = Util.getMillis();
        }));
        addChild(new GuiSupportedUpgrades(gui, relativeX + 6, relativeY + 68, tile.getComponent().getSupportedTypes()));
        rightScreen = addChild(new GuiInnerScreen(gui, scrollList.getRelativeRight(), relativeY + 18, 59, 50));
        addChild(new GuiProgress(() -> this.tile.getComponent().getScaledUpgradeProgress(), ProgressType.INSTALLING, gui, rightScreen.getRelativeRight() + 3, relativeY + 37));
        addChild(new GuiProgress(() -> 0, ProgressType.UNINSTALLING, gui, rightScreen.getRelativeRight() + 3, relativeY + 58));
        removeButton = addChild(new DigitalButton(gui, scrollList.getRelativeRight() + 1, relativeY + 54, 56, 12, MekanismLang.UPGRADE_UNINSTALL, (element, mouseX, mouseY) -> {
            if (scrollList.hasSelection()) {
                return PacketUtils.sendToServer(new PacketGuiInteract(Screen.hasShiftDown() ? GuiInteraction.REMOVE_ALL_UPGRADE : GuiInteraction.REMOVE_UPGRADE,
                      this.tile, scrollList.getSelection().ordinal()));
            }
            return false;
        })).setTooltip(MekanismLang.UPGRADE_UNINSTALL_TOOLTIP);
        MekanismTileContainer<?> container = (MekanismTileContainer<?>) ((GuiMekanism<?>) gui()).getMenu();
        addChild(new GuiVirtualSlot(this, SlotType.NORMAL, gui, rightScreen.getRelativeRight() + 2, relativeY + 18, container.getUpgradeSlot()));
        addChild(new GuiVirtualSlot(this, SlotType.NORMAL, gui, rightScreen.getRelativeRight() + 2, relativeY + 72, container.getUpgradeOutputSlot()));
        updateEnabledButtons();
        container.startTracking(MekanismContainer.UPGRADE_WINDOW, tile.getComponent());
        PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.CONTAINER_TRACK_UPGRADES, tile, MekanismContainer.UPGRADE_WINDOW));
    }

    @Override
    public void close() {
        super.close();
        PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.CONTAINER_STOP_TRACKING, tile, MekanismContainer.UPGRADE_WINDOW));
        ((MekanismContainer) ((GuiMekanism<?>) gui()).getMenu()).stopTracking(MekanismContainer.UPGRADE_WINDOW);
    }

    private void updateEnabledButtons() {
        removeButton.active = scrollList.hasSelection();
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        drawTitleText(guiGraphics, MekanismLang.UPGRADES.translate(), 5);
        if (scrollList.hasSelection()) {
            Upgrade selectedType = scrollList.getSelection();
            int amount = tile.getComponent().getUpgrades(selectedType);
            WrappedTextRenderer textRenderer = upgradeTypeData.get(selectedType);
            if (textRenderer == null) {
                textRenderer = new WrappedTextRenderer(this, MekanismLang.UPGRADE_TYPE.translate(selectedType));
                upgradeTypeData.put(selectedType, textRenderer);
            }
            int screenWidth = rightScreen.getWidth() - 2;
            int lines = textRenderer.renderWithScale(guiGraphics, rightScreen.getRelativeX() + 2, rightScreen.getRelativeY() + 2, TextAlignment.LEFT, screenTextColor(),
                  screenWidth - 2, 0.6F);
            int textY = 4 + 6 * lines;
            rightScreen.drawScaledScrollingString(guiGraphics, MekanismLang.UPGRADE_COUNT.translate(amount, selectedType.getMax()), 0, textY,  TextAlignment.LEFT,
                  screenTextColor(), screenWidth, 2, false, 0.6F, msSelected);
            for (Component component : UpgradeUtils.getInfo(tile, selectedType)) {
                //Note: We add the six here instead of after to account for the line above this for loop that draws the upgrade count
                textY += 6;
                rightScreen.drawScaledScrollingString(guiGraphics, component, 0, textY, TextAlignment.LEFT, screenTextColor(), screenWidth, 2,
                      false, 0.6F, msSelected);
            }
        } else {
            noSelection.renderWithScale(guiGraphics, rightScreen.getRelativeX() + 2, rightScreen.getRelativeY() + 2, TextAlignment.LEFT, screenTextColor(), 56, 0.8F);
        }
    }
}