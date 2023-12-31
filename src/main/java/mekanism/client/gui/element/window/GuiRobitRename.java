package mekanism.client.gui.element.window;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.robit.PacketRobitName;
import net.minecraft.client.gui.GuiGraphics;

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
            PacketUtils.sendToServer(new PacketRobitName(robit, name));
            close();
        }
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        drawTitleText(guiGraphics, MekanismLang.ROBIT_RENAME.translate(), 7);
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