package mekanism.client.gui.element.window;

import mekanism.api.robit.RobitSkin;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.scroll.GuiRobitSkinSelectScroll;
import mekanism.client.gui.robit.GuiRobitMain;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteractionEntity;
import mekanism.common.network.to_server.robit.PacketRobitSkin;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceKey;

public class GuiRobitSkinSelect extends GuiWindow {

    private final GuiRobitSkinSelectScroll selection;
    private final EntityRobit robit;

    public GuiRobitSkinSelect(GuiRobitMain gui, int x, int y, EntityRobit robit) {
        super(gui, x, y, 168, 190, WindowType.SKIN_SELECT);
        this.robit = robit;
        selection = addChild(new GuiRobitSkinSelectScroll(gui(), relativeX + 6, relativeY + 18, this.robit, () -> gui.getMenu().getUnlockedSkins()));
        addChild(new TranslationButton(gui, relativeX + width / 2 - 61, relativeY + 165, 60, 20, MekanismLang.BUTTON_CANCEL, this::close));
        addChild(new TranslationButton(gui, relativeX + width / 2 + 1, relativeY + 165, 60, 20, MekanismLang.BUTTON_CONFIRM, (element, mouseX, mouseY) -> {
            ResourceKey<RobitSkin> selectedSkin = selection.getSelectedSkin();
            if (selectedSkin != this.robit.getSkin()) {
                PacketUtils.sendToServer(new PacketRobitSkin(this.robit, selectedSkin));
            }
            return close(element, mouseX, mouseY);
        }));
        gui.getMenu().startTracking(MekanismContainer.SKIN_SELECT_WINDOW, gui.getMenu());
        PacketUtils.sendToServer(new PacketGuiInteract(GuiInteractionEntity.CONTAINER_TRACK_SKIN_SELECT, this.robit, MekanismContainer.SKIN_SELECT_WINDOW));
    }

    @Override
    public void close() {
        super.close();
        PacketUtils.sendToServer(new PacketGuiInteract(GuiInteractionEntity.CONTAINER_STOP_TRACKING, robit, MekanismContainer.SKIN_SELECT_WINDOW));
        ((MekanismContainer) ((GuiMekanism<?>) gui()).getMenu()).stopTracking(MekanismContainer.SKIN_SELECT_WINDOW);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        drawTitleText(guiGraphics, MekanismLang.ROBIT_SKIN_SELECT.translate(), 7);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        // only allow clicks here
        return true;
    }

    @Override
    protected boolean isFocusOverlay() {
        return true;
    }
}