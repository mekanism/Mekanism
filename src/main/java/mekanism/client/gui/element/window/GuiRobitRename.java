package mekanism.client.gui.element.window;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.network.to_server.PacketRobit;

public class GuiRobitRename extends GuiWindow {

    private final GuiTextField nameChangeField;
    private final EntityRobit robit;

    public GuiRobitRename(IGuiWrapper gui, int x, int y, EntityRobit robit) {
        super(gui, x, y, 122, 58, WindowType.RENAME);
        this.robit = robit;
        addChild(new TranslationButton(gui, relativeX + 31, relativeY + 32, 60, 20, MekanismLang.BUTTON_CONFIRM, this::changeName));
        nameChangeField = addChild(new GuiTextField(gui, relativeX + 21, relativeY + 17, 80, 12));
        nameChangeField.setMaxLength(12);
        nameChangeField.setCanLoseFocus(false);
        nameChangeField.setFocused(true);
        nameChangeField.setEnterHandler(this::changeName);
    }

    private void changeName() {
        String name = nameChangeField.getText().trim();
        if (!name.isEmpty()) {
            Mekanism.packetHandler().sendToServer(new PacketRobit(robit, name));
            close();
        }
    }

    @Override
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        drawTitleText(matrix, MekanismLang.ROBIT_RENAME.translate(), 7);
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