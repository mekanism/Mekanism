package mekanism.client.gui.element.custom;

import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiWindow;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.network.PacketRobit;

public class GuiRobitRename extends GuiWindow {

    private final GuiTextField nameChangeField;
    private final EntityRobit robit;

    public GuiRobitRename(IGuiWrapper gui, int x, int y, EntityRobit robit) {
        super(gui, x, y, 122, 58);
        this.robit = robit;
        addChild(new TranslationButton(gui, this.field_230690_l_ + 31, this.field_230691_m_ + 32, 60, 20, MekanismLang.BUTTON_CONFIRM, this::changeName));
        addChild(nameChangeField = new GuiTextField(gui, relativeX + 21, relativeY + 17, 80, 12));
        nameChangeField.setMaxStringLength(12);
        nameChangeField.setFocused(true);
        nameChangeField.setEnterHandler(this::changeName);
    }

    private void changeName() {
        if (!nameChangeField.getText().isEmpty()) {
            Mekanism.packetHandler.sendToServer(new PacketRobit(robit.getEntityId(), TextComponentUtil.getString(nameChangeField.getText())));
            close();
        }
    }

    @Override
    public void renderForeground(int mouseX, int mouseY) {
        super.renderForeground(mouseX, mouseY);
        drawTitleText(MekanismLang.ROBIT_RENAME.translate(), 7);
    }
}