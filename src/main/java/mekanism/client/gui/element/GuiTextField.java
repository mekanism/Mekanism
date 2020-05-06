package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.client.gui.widget.TextFieldWidget;

/**
 * GuiElement wrapper of TextFieldWidget for more control
 * @author aidancbrady
 *
 */
public class GuiTextField extends GuiTexturedElement {

    private TextFieldWidget textField;

    public GuiTextField(IGuiWrapper gui, int x, int y, int width, int height) {
        super(null, gui, x, y, width, height);

        textField = new TextFieldWidget(getFont(), this.x, this.y, width, height, "");
    }

    @Override
    public void onMove() {
        super.onMove();
        textField.x = x;
        textField.y = y;
    }

    @Override
    public void tick() {
        super.tick();
        textField.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return textField.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return textField.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        return textField.charTyped(c, keyCode) || super.charTyped(c, keyCode);
    }

    @Override
    public void drawButton(int mouseX, int mouseY) {
        textField.render(mouseX, mouseY, 0);
    }

    public String getText() {
        return textField.getText();
    }

    public void setMaxStringLength(int length) {
        textField.setMaxStringLength(length);
    }

    public void setEnabled(boolean enabled) {
        textField.setEnabled(enabled);
    }

    public void setFocused2(boolean focused) {
        textField.setFocused2(focused);
    }

    public boolean canWrite() {
        return textField.canWrite();
    }

    public void setText(String text) {
        textField.setText(text);
    }
}
