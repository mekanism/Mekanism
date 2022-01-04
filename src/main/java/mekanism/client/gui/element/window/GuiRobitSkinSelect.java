package mekanism.client.gui.element.window;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.robit.RobitSkin;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.scroll.GuiRobitSkinSelectScroll;
import mekanism.client.gui.robit.GuiRobitMain;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteractionEntity;
import mekanism.common.network.to_server.PacketRobit;

public class GuiRobitSkinSelect extends GuiWindow {

    private final GuiRobitSkinSelectScroll selection;
    private final EntityRobit robit;

    public GuiRobitSkinSelect(GuiRobitMain gui, int x, int y, EntityRobit robit) {
        super(gui, x, y, 168, 190, WindowType.SKIN_SELECT);
        this.robit = robit;
        selection = addChild(new GuiRobitSkinSelectScroll(gui(), relativeX + 6, relativeY + 18, this.robit, () -> gui.getMenu().getUnlockedSkins()));
        addChild(new TranslationButton(gui, relativeX + width / 2 - 61, relativeY + 165, 60, 20, MekanismLang.BUTTON_CANCEL, this::close));
        addChild(new TranslationButton(gui, relativeX + width / 2 + 1, relativeY + 165, 60, 20, MekanismLang.BUTTON_CONFIRM, () -> {
            RobitSkin selectedSkin = selection.getSelectedSkin();
            if (selectedSkin != robit.getSkin()) {
                Mekanism.packetHandler().sendToServer(new PacketRobit(robit, selectedSkin));
            }
            close();
        }));
        Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteractionEntity.CONTAINER_TRACK_SKIN_SELECT, this.robit, MekanismContainer.SKIN_SELECT_WINDOW));
        gui.getMenu().startTracking(MekanismContainer.SKIN_SELECT_WINDOW, gui.getMenu());
    }

    @Override
    public void close() {
        super.close();
        Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteractionEntity.CONTAINER_STOP_TRACKING, robit, MekanismContainer.SKIN_SELECT_WINDOW));
        ((MekanismContainer) ((GuiMekanism<?>) gui()).getMenu()).stopTracking(MekanismContainer.SKIN_SELECT_WINDOW);
    }

    @Override
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        drawTitleText(matrix, MekanismLang.ROBIT_SKIN_SELECT.translate(), 7);
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