package mekanism.client.gui.element.text;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import mekanism.api.functions.CharPredicate;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiRelativeElement;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.lib.Color;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

/**
 * GuiElement wrapper of TextFieldWidget for more control
 *
 * @author aidancbrady
 */
public class GuiTextField extends GuiRelativeElement {

    public static final int DEFAULT_BORDER_COLOR = 0xFFA0A0A0;
    public static final int DEFAULT_BACKGROUND_COLOR = 0xFF000000;
    public static final IntSupplier SCREEN_COLOR = SpecialColors.TEXT_SCREEN::argb;
    public static final IntSupplier DARK_SCREEN_COLOR = () -> Color.argb(SCREEN_COLOR.getAsInt()).darken(0.4).argb();

    private final TextFieldWidget textField;
    private Runnable enterHandler;
    private CharPredicate inputValidator;
    private Consumer<String> responder;

    private BackgroundType backgroundType = BackgroundType.DEFAULT;
    private IconType iconType;

    private int textOffsetX, textOffsetY;
    private float textScale = 1.0F;

    private MekanismImageButton checkmarkButton;

    public GuiTextField(IGuiWrapper gui, int x, int y, int width, int height) {
        super(gui, x, y, width, height);

        textField = new TextFieldWidget(getFont(), this.x, this.y, width, height, StringTextComponent.EMPTY);
        textField.setEnableBackgroundDrawing(false);
        textField.setResponder(s -> {
            if (responder != null) {
                responder.accept(s);
            }
            if (checkmarkButton != null) {
                checkmarkButton.active = !textField.getText().isEmpty();
            }
        });
        gui().addFocusListener(this);
        updateTextField();
    }

    @Override
    public void resize(int prevLeft, int prevTop, int left, int top) {
        super.resize(prevLeft, prevTop, left, top);
        //Ensure we also update the positions of the text field
        textField.x = textField.x - prevLeft + left;
        textField.y = textField.y - prevTop + top;
    }

    public GuiTextField setScale(float textScale) {
        this.textScale = textScale;
        return this;
    }

    public GuiTextField setOffset(int offsetX, int offsetY) {
        this.textOffsetX = offsetX;
        this.textOffsetY = offsetY;
        updateTextField();
        return this;
    }

    public GuiTextField configureDigitalInput(Runnable enterHandler) {
        setBackground(BackgroundType.NONE);
        setIcon(IconType.DIGITAL);
        setTextColor(screenTextColor());
        setEnterHandler(enterHandler);
        addCheckmarkButton(ButtonType.DIGITAL, enterHandler);
        setScale(0.8F);
        return this;
    }

    public GuiTextField configureDigitalBorderInput(Runnable enterHandler) {
        setBackground(BackgroundType.DIGITAL);
        setTextColor(screenTextColor());
        setEnterHandler(enterHandler);
        addCheckmarkButton(ButtonType.DIGITAL, enterHandler);
        setScale(0.8F);
        return this;
    }

    public GuiTextField setEnterHandler(Runnable enterHandler) {
        this.enterHandler = enterHandler;
        return this;
    }

    public GuiTextField setInputValidator(CharPredicate inputValidator) {
        this.inputValidator = inputValidator;
        return this;
    }

    public GuiTextField setBackground(BackgroundType backgroundType) {
        this.backgroundType = backgroundType;
        return this;
    }

    public GuiTextField setIcon(IconType iconType) {
        this.iconType = iconType;
        updateTextField();
        return this;
    }

    public GuiTextField addCheckmarkButton(Runnable callback) {
        return addCheckmarkButton(ButtonType.NORMAL, callback);
    }

    public GuiTextField addCheckmarkButton(ButtonType type, Runnable callback) {
        addChild(checkmarkButton = type.getButton(this, () -> {
            callback.run();
            setFocused(true);
        }));
        checkmarkButton.active = false;
        updateTextField();
        return this;
    }

    private void updateTextField() {
        //width is scaled based on text scale
        textField.setWidth(Math.round((width - (checkmarkButton != null ? textField.getHeightRealms() + 2 : 0) - (iconType != null ? iconType.getOffsetX() : 0)) * (1 / textScale)));
        textField.x = x + textOffsetX + 2 + (iconType != null ? iconType.getOffsetX() : 0);
        textField.y = y + textOffsetY + 1 + (int) ((height / 2F) - 4);
    }

    public boolean isTextFieldFocused() {
        return textField.isFocused();
    }

    @Override
    public void onWindowClose() {
        super.onWindowClose();
        gui().removeFocusListener(this);
    }

    @Override
    public void move(int changeX, int changeY) {
        super.move(changeX, changeY);
        updateTextField();
    }

    @Override
    public void tick() {
        super.tick();
        textField.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean prevFocus = isTextFieldFocused();
        double scaledX = mouseX;
        // figure out the proper mouse placement based on text scaling
        if (textScale != 1.0F && scaledX > textField.x) {
            scaledX = Math.min(scaledX, textField.x) + (scaledX - textField.x) * (1F / textScale);
        }
        boolean ret = textField.mouseClicked(scaledX, mouseY, button);
        // detect if we're now focused
        if (!prevFocus && isTextFieldFocused()) {
            gui().focusChange(this);
        }
        return ret || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        backgroundType.render(this, matrix);
        if (textScale != 1F) {
            // hacky. we should write our own renderer at some point.
            float reverse = (1 / textScale) - 1;
            float yAdd = 4 - (textScale * 8) / 2F;
            matrix.push();
            matrix.scale(textScale, textScale, textScale);
            matrix.translate(textField.x * reverse, (textField.y) * reverse + yAdd * (1 / textScale), 0);
            textField.render(matrix, mouseX, mouseY, 0);
            matrix.pop();
        } else {
            textField.render(matrix, mouseX, mouseY, 0);
        }
        MekanismRenderer.resetColor();
        if (iconType != null) {
            minecraft.textureManager.bindTexture(iconType.getIcon());
            blit(matrix, x + 2, y + (height / 2) - (int) Math.ceil(iconType.getHeight() / 2F), 0, 0, iconType.getWidth(), iconType.getHeight(), iconType.getWidth(), iconType.getHeight());
        }
    }

    @Override
    public boolean hasPersistentData() {
        return true;
    }

    @Override
    public void syncFrom(GuiElement element) {
        super.syncFrom(element);
        textField.setText(((GuiTextField) element).getText());
        setFocused(element.isFocused());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (canWrite()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape to make the whole interface go away
                return false;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                //Handle processing both the enter key and the numpad enter key
                if (enterHandler != null) {
                    enterHandler.run();
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_TAB) {
                gui().incrementFocus(this);
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
            if (inputValidator == null || inputValidator.test(c)) {
                return textField.charTyped(c, keyCode);
            }
            return false;
        }
        return super.charTyped(c, keyCode);
    }

    public String getText() {
        return textField.getText();
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
        if (focused) {
            gui().focusChange(this);
        }
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
}