package mekanism.client.gui.element.window;

import java.util.Collections;
import mekanism.api.RelativeSide;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.SideDataButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.MekClickType;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.network.to_server.configuration_update.PacketEjectColor;
import mekanism.common.network.to_server.configuration_update.PacketInputColor;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class GuiTransporterConfig<TILE extends TileEntityMekanism & ISideConfiguration> extends GuiWindow {

    private final TILE tile;

    public GuiTransporterConfig(IGuiWrapper gui, int x, int y, TILE tile, SelectedWindowData windowData) {
        super(gui, x, y, 156, 119, windowData);
        if (windowData.type != WindowType.TRANSPORTER_CONFIG) {
            throw new IllegalArgumentException("Transporter configs must have a transporter config window type");
        }
        this.tile = tile;
        interactionStrategy = InteractionStrategy.ALL;
        addChild(new GuiInnerScreen(gui, relativeX + 38, relativeY + 15, 80, 12,
              () -> Collections.singletonList(MekanismLang.STRICT_INPUT_ENABLED.translate(OnOff.of(tile.getEjector().hasStrictInput())))));
        addChild(new GuiSlot(SlotType.NORMAL, gui, relativeX + 111, relativeY + 48));
        addChild(new MekanismImageButton(gui, relativeX + 136, relativeY + 6, 14, 16, getButtonLocation("exclamation"),
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.STRICT_INPUT, this.tile))))
              .setTooltip(MekanismLang.STRICT_INPUT);
        addChild(new ColorButton(gui, relativeX + 112, relativeY + 49, 16, 16, () -> this.tile.getEjector().getOutputColor(),
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketEjectColor(this.tile.getBlockPos(), MekClickType.left(Screen.hasShiftDown()))),
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketEjectColor(this.tile.getBlockPos(), MekClickType.RIGHT))));
        addSideDataButton(RelativeSide.BOTTOM, 41, 64 + 16);
        addSideDataButton(RelativeSide.TOP, 41, 34);
        addSideDataButton(RelativeSide.FRONT, 41, 57);
        addSideDataButton(RelativeSide.BACK, 18, 64 + 16);
        addSideDataButton(RelativeSide.LEFT, 18, 57);
        addSideDataButton(RelativeSide.RIGHT, 64, 57);
        ((MekanismContainer) ((GuiMekanism<?>) gui()).getMenu()).startTracking(MekanismContainer.TRANSPORTER_CONFIG_WINDOW, this.tile.getEjector());
        PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.CONTAINER_TRACK_EJECTOR, this.tile, MekanismContainer.TRANSPORTER_CONFIG_WINDOW));
    }

    private void addSideDataButton(RelativeSide side, int xPos, int yPos) {
        SideDataButton button = addChild(new SideDataButton(gui(), relativeX + xPos, relativeY + yPos, side,
              () -> tile.getConfig().getDataType(TransmissionType.ITEM, side), () -> tile.getEjector().getInputColor(side), tile, PacketInputColor::new, false));
        if (!tile.getEjector().isInputSideEnabled(side)) {
            button.active = false;
        }
    }

    @Override
    public void close() {
        super.close();
        PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.CONTAINER_STOP_TRACKING, tile, MekanismContainer.TRANSPORTER_CONFIG_WINDOW));
        ((MekanismContainer) ((GuiMekanism<?>) gui()).getMenu()).stopTracking(MekanismContainer.TRANSPORTER_CONFIG_WINDOW);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        drawTitleText(guiGraphics, MekanismLang.TRANSPORTER_CONFIG.translate(), 5);
        drawScrollingString(guiGraphics, MekanismLang.INPUT.translate(), 18, 105, TextAlignment.CENTER, subheadingTextColor(), 68, 0, false);
        drawScrollingString(guiGraphics, MekanismLang.OUTPUT.translate(), 86, 68, TextAlignment.CENTER, subheadingTextColor(), width - 86, 4, false);
    }

    @Override
    protected int getTitlePadEnd() {
        return super.getTitlePadEnd() + 18;
    }
}