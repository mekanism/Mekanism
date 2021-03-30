package mekanism.client.gui.element.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.network.to_server.PacketRobit;

public class GuiRobitRename extends GuiWindow {

    private final GuiTextField nameChangeField;
    private final EntityRobit robit;

    public GuiRobitRename(IGuiWrapper gui, int x, int y, EntityRobit robit) {
        super(gui, x, y, 122, 58);
        this.robit = robit;
        addChild(new TranslationButton(gui, this.x + 31, this.y + 32, 60, 20, MekanismLang.BUTTON_CONFIRM, this::changeName));
        addChild(nameChangeField = new GuiTextField(gui, relativeX + 21, relativeY + 17, 80, 12));
        nameChangeField.setMaxStringLength(12);
        nameChangeField.setCanLoseFocus(false);
        nameChangeField.setFocused(true);
        nameChangeField.setEnterHandler(this::changeName);
    }

    private void changeName() {
        if (!nameChangeField.getText().isEmpty()) {
            Mekanism.packetHandler.sendToServer(new PacketRobit(robit.getId(), TextComponentUtil.getString(nameChangeField.getText())));
            close();
        }
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
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