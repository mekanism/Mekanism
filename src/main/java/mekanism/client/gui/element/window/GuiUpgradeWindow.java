package mekanism.client.gui.element.window;

import com.mojang.blaze3d.vertex.PoseStack;
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
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GuiUpgradeWindow extends GuiWindow {

    private final Map<Upgrade, WrappedTextRenderer> upgradeTypeData = new EnumMap<>(Upgrade.class);
    private final WrappedTextRenderer noSelection = new WrappedTextRenderer(this, MekanismLang.UPGRADE_NO_SELECTION.translate());
    private final TileEntityMekanism tile;
    private final MekanismButton removeButton;
    private final GuiUpgradeScrollList scrollList;

    public GuiUpgradeWindow(IGuiWrapper gui, int x, int y, TileEntityMekanism tile) {
        super(gui, x, y, 156, 76 + 12 * GuiSupportedUpgrades.calculateNeededRows(), WindowType.UPGRADE);
        this.tile = tile;
        interactionStrategy = InteractionStrategy.ALL;
        scrollList = addChild(new GuiUpgradeScrollList(gui, relativeX + 6, relativeY + 18, 66, 50, tile.getComponent(), this::updateEnabledButtons));
        addChild(new GuiSupportedUpgrades(gui, relativeX + 6, relativeY + 68, tile.getComponent().getSupportedTypes()));
        addChild(new GuiInnerScreen(gui, relativeX + 72, relativeY + 18, 59, 50));
        addChild(new GuiProgress(() -> this.tile.getComponent().getScaledUpgradeProgress(), ProgressType.INSTALLING, gui, relativeX + 134, relativeY + 37));
        addChild(new GuiProgress(() -> 0, ProgressType.UNINSTALLING, gui, relativeX + 134, relativeY + 59));
        removeButton = addChild(new DigitalButton(gui, relativeX + 73, relativeY + 54, 56, 12,
              MekanismLang.UPGRADE_UNINSTALL, () -> {
            if (scrollList.hasSelection()) {
                Mekanism.packetHandler().sendToServer(new PacketGuiInteract(Screen.hasShiftDown() ? GuiInteraction.REMOVE_ALL_UPGRADE : GuiInteraction.REMOVE_UPGRADE,
                      this.tile, scrollList.getSelection().ordinal()));
            }
        }, getOnHover(MekanismLang.UPGRADE_UNINSTALL_TOOLTIP)));
        MekanismTileContainer<?> container = (MekanismTileContainer<?>) ((GuiMekanism<?>) gui()).getMenu();
        addChild(new GuiVirtualSlot(this, SlotType.NORMAL, gui, relativeX + 133, relativeY + 18, container.getUpgradeSlot()));
        addChild(new GuiVirtualSlot(this, SlotType.NORMAL, gui, relativeX + 133, relativeY + 73, container.getUpgradeOutputSlot()));
        updateEnabledButtons();
        Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.CONTAINER_TRACK_UPGRADES, tile, MekanismContainer.UPGRADE_WINDOW));
        container.startTracking(MekanismContainer.UPGRADE_WINDOW, tile.getComponent());
    }

    @Override
    public void close() {
        super.close();
        Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.CONTAINER_STOP_TRACKING, tile, MekanismContainer.UPGRADE_WINDOW));
        ((MekanismContainer) ((GuiMekanism<?>) gui()).getMenu()).stopTracking(MekanismContainer.UPGRADE_WINDOW);
    }

    private void updateEnabledButtons() {
        removeButton.active = scrollList.hasSelection();
    }

    @Override
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        drawTitleText(matrix, MekanismLang.UPGRADES.translate(), 5);
        if (scrollList.hasSelection()) {
            Upgrade selectedType = scrollList.getSelection();
            int amount = tile.getComponent().getUpgrades(selectedType);
            int textY = relativeY + 20;
            WrappedTextRenderer textRenderer = upgradeTypeData.computeIfAbsent(selectedType, type -> new WrappedTextRenderer(this, MekanismLang.UPGRADE_TYPE.translate(type)));
            int lines = textRenderer.renderWithScale(matrix, relativeX + 74, textY, screenTextColor(), 56, 0.6F);
            textY += 6 * lines + 2;
            drawTextWithScale(matrix, MekanismLang.UPGRADE_COUNT.translate(amount, selectedType.getMax()), relativeX + 74, textY, screenTextColor(), 0.6F);
            for (Component component : UpgradeUtils.getInfo(tile, selectedType)) {
                //Note: We add the six here instead of after to account for the line above this for loop that draws the upgrade count
                textY += 6;
                drawTextWithScale(matrix, component, relativeX + 74, textY, screenTextColor(), 0.6F);
            }
        } else {
            noSelection.renderWithScale(matrix, relativeX + 74, relativeY + 20, screenTextColor(), 56, 0.8F);
        }
    }
}