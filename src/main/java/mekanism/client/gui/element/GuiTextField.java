package mekanism.client.gui.element;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import org.lwjgl.glfw.GLFW;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import mekanism.client.gui.IGuiWrapper;
import net.minecraft.client.gui.widget.TextFieldWidget;

/**
 * GuiElement wrapper of TextFieldWidget for more control
 * @author aidancbrady
 *
 */
public class GuiTextField extends GuiTexturedElement {

    private TextFieldWidget textField;
    private Runnable enterHandler;
    private InputValidator inputValidator;

    public GuiTextField(IGuiWrapper gui, int x, int y, int width, int height) {
        super(null, gui, x, y, width, height);

        textField = new TextFieldWidget(getFont(), this.x, this.y, width, height, "");
    }

    public GuiTextField setEnterHandler(Runnable enterHandler) {
        this.enterHandler = enterHandler;
        return this;
    }

    public GuiTextField setInputValidator(InputValidator inputValidator) {
        this.inputValidator = inputValidator;
        return this;
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
    public void drawButton(int mouseX, int mouseY) {
        textField.render(mouseX, mouseY, 0);
    }

    @Override
    public boolean hasPersistentData() {
        return !textField.getText().isEmpty();
    }

    @Override
    public void syncFrom(GuiElement element) {
        textField.setText(((GuiTextField) element).getText());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (canWrite()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape making the field lose focus
                setFocused2(false);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                enterHandler.run();
                return true;
            }
            textField.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        if (canWrite()) {
            if (inputValidator == null || inputValidator.isValid(c)) {
                return textField.charTyped(c, keyCode);
            }
            return false;
        }
        return super.charTyped(c, keyCode);
    }

    public String getText() {
        return textField.getText();
    }

    public void setEnableBackgroundDrawing(boolean enable) {
        textField.setEnableBackgroundDrawing(enable);
    }

    public void setVisible(boolean visible) {
        textField.setVisible(visible);
    }

    public void setMaxStringLength(int length) {
        textField.setMaxStringLength(length);
    }

    public void setTextColor(int color) {
        textField.setTextColor(color);
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

    public void setResponder(Consumer<String> responder) {
        textField.setResponder(responder);
    }

    public interface InputValidator {

        public static final InputValidator ALL = (c) -> true;
        public static final InputValidator DIGIT = Character::isDigit;
        public static final InputValidator LETTER = Character::isLetter;
        public static final InputValidator SCI_NOTATION = or(DIGIT, from('E', '.'));

        public static final InputValidator FILTER_CHARS = from('*', '-', ' ', '|', '_', '\'');
        public static final InputValidator FREQUENCY_CHARS = from('-', ' ', '|', '\'', '\"', '_', '+', ':', '(', ')', '?', '!', '/', '@', '$', '`', '~', ',', '.', '#');

        public static InputValidator from(char... chars) {
            return new SetInputValidator(chars);
        }

        public static InputValidator or(InputValidator... validators) {
            return (c) -> Arrays.stream(validators).anyMatch(v -> v.isValid(c));
        }

        boolean isValid(char c);
    }

    private static class SetInputValidator implements InputValidator {

        private Set<Character> validSet = new CharOpenHashSet();

        public SetInputValidator(char... chars) {
            for (char c : chars) {
                validSet.add(c);
            }
        }

        @Override
        public boolean isValid(char c) {
            return validSet.contains(c);
        }
    }
}
