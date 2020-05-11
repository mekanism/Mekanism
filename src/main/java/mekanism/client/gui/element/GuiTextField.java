package mekanism.client.gui.element;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import org.lwjgl.glfw.GLFW;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.widget.TextFieldWidget;

/**
 * GuiElement wrapper of TextFieldWidget for more control
 * @author aidancbrady
 *
 */
public class GuiTextField extends GuiTexturedElement {

    private static final int DEFAULT_BORDER_COLOR = 0xA0A0A0;
    private static final int DEFAULT_BACKGROUND_COLOR = 0x000000;

    private TextFieldWidget textField;
    private Runnable enterHandler;
    private InputValidator inputValidator;
    private Consumer<String> responder;

    private boolean manualDrawBackground;

    private MekanismImageButton checkmarkButton;

    public GuiTextField(IGuiWrapper gui, int x, int y, int width, int height) {
        super(null, gui, x, y, width, height);

        textField = new TextFieldWidget(getFont(), this.x, this.y, width, height, "");
        textField.setResponder(s -> {
            if (responder != null) {
                responder.accept(s);
            }
            if (checkmarkButton != null) {
                checkmarkButton.active = !textField.getText().isEmpty();
            }
        });
        guiObj.addFocusListener(this);
    }

    public GuiTextField setEnterHandler(Runnable enterHandler) {
        this.enterHandler = enterHandler;
        return this;
    }

    public GuiTextField setInputValidator(InputValidator inputValidator) {
        this.inputValidator = inputValidator;
        return this;
    }

    public GuiTextField addCheckmarkButton(Runnable callback) {
        addChild(checkmarkButton = new MekanismImageButton(guiObj, guiObj.getLeft() + relativeX + width - height, guiObj.getTop() + relativeY, height, 12,
              MekanismUtils.getResource(ResourceType.GUI_BUTTON, "checkmark.png"), callback));
        checkmarkButton.active = false;
        textField.setEnableBackgroundDrawing(false);
        textField.setWidth(textField.getWidth() - 12);
        textField.x += 2;
        textField.y += 2;
        manualDrawBackground = true;
        return this;
    }

    @Override
    public void onWindowClose() {
        super.onWindowClose();
        guiObj.removeFocusListener(this);
    }

    @Override
    public void move(int changeX, int changeY) {
        super.move(changeX, changeY);
        textField.x = x;
        textField.y = y;

        if (manualDrawBackground) {
            textField.x += 2;
            textField.y += 2;
        }
    }

    @Override
    public void tick() {
        super.tick();
        textField.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean prevFocus = textField.isFocused();
        boolean ret = textField.mouseClicked(mouseX, mouseY, button);
        // detect if we're now focused
        if (!prevFocus && textField.isFocused()) {
            guiObj.focusChange(this);
        }
        return ret || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void drawButton(int mouseX, int mouseY) {
        if (manualDrawBackground) {
            GuiUtils.fill(x - 1, y - 1, width + 2, height + 2, DEFAULT_BORDER_COLOR);
            GuiUtils.fill(x, y, width, height, DEFAULT_BACKGROUND_COLOR);
        }

        textField.render(mouseX, mouseY, 0);
    }

    @Override
    public boolean hasPersistentData() {
        return true;
    }

    @Override
    public void syncFrom(GuiElement element) {
        textField.setText(((GuiTextField) element).getText());
        setFocused(element.isFocused());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (canWrite()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape making the field lose focus
                setFocused(false);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                enterHandler.run();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_TAB) {
                guiObj.incrementFocus(this);
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

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        textField.setFocused2(focused);
    }

    public boolean canWrite() {
        return textField.canWrite();
    }

    public void setText(String text) {
        textField.setText(text);
    }

    public void setResponder(Consumer<String> responder) {
        this.responder = responder;
    }

    public interface InputValidator {

        public static final InputValidator ALL = (c) -> true;
        public static final InputValidator DIGIT = Character::isDigit;
        public static final InputValidator LETTER = Character::isLetter;
        public static final InputValidator DECIMAL = or(DIGIT, from('.'));
        public static final InputValidator SCI_NOTATION = or(DECIMAL, from('E'));

        public static final InputValidator FILTER_CHARS = from('*', '-', ' ', '|', '_', '\'', ':', '/');
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
