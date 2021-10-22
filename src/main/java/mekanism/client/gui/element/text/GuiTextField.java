package mekanism.client.gui.element.text;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import mekanism.api.functions.CharPredicate;
import mekanism.api.functions.CharUnaryOperator;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.lib.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

/**
 * GuiElement wrapper of TextFieldWidget for more control
 *
 * @author aidancbrady
 */
public class GuiTextField extends GuiElement {

    public static final int DEFAULT_BORDER_COLOR = 0xFFA0A0A0;
    public static final int DEFAULT_BACKGROUND_COLOR = 0xFF000000;
    public static final IntSupplier SCREEN_COLOR = SpecialColors.TEXT_SCREEN::argb;
    public static final IntSupplier DARK_SCREEN_COLOR = () -> Color.argb(SCREEN_COLOR.getAsInt()).darken(0.4).argb();

    private final TextFieldWidget textField;
    private Runnable enterHandler;
    private CharPredicate inputValidator;
    private CharUnaryOperator inputTransformer;
    private Consumer<String> responder;

    private BackgroundType backgroundType = BackgroundType.DEFAULT;
    private IconType iconType;

    private int textOffsetX, textOffsetY;
    private float textScale = 1.0F;

    private MekanismImageButton checkmarkButton;

    public GuiTextField(IGuiWrapper gui, int x, int y, int width, int height) {
        super(gui, x, y, width, height);

        textField = new TextFieldWidget(getFont(), this.x, this.y, width, height, StringTextComponent.EMPTY);
        textField.setBordered(false);
        textField.setResponder(s -> {
            if (responder != null) {
                responder.accept(s);
            }
            if (checkmarkButton != null) {
                checkmarkButton.active = !textField.getValue().isEmpty();
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

    public GuiTextField setInputTransformer(CharUnaryOperator inputTransformer) {
        this.inputTransformer = inputTransformer;
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
        checkmarkButton = addChild(type.getButton(this, () -> {
            callback.run();
            setFocused(true);
        }));
        checkmarkButton.active = false;
        updateTextField();
        return this;
    }

    private void updateTextField() {
        //width is scaled based on text scale
        int iconOffsetX = iconType != null ? iconType.getOffsetX() : 0;
        textField.setWidth(Math.round((width - (checkmarkButton != null ? textField.getHeight() + 2 : 0) - iconOffsetX) * (1 / textScale)));
        textField.setX(x + textOffsetX + 2 + iconOffsetX);
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
        if (textScale == 1F) {
            renderTextField(matrix, mouseX, mouseY, partialTicks);
        } else {
            // hacky. we should write our own renderer at some point.
            float reverse = (1 / textScale) - 1;
            float yAdd = 4 - (textScale * 8) / 2F;
            matrix.pushPose();
            matrix.scale(textScale, textScale, textScale);
            matrix.translate(textField.x * reverse, (textField.y) * reverse + yAdd * (1 / textScale), 0);
            renderTextField(matrix, mouseX, mouseY, partialTicks);
            matrix.popPose();
        }
        MekanismRenderer.resetColor();
        if (iconType != null) {
            minecraft.textureManager.bind(iconType.getIcon());
            blit(matrix, x + 2, y + (height / 2) - (int) Math.ceil(iconType.getHeight() / 2F), 0, 0, iconType.getWidth(), iconType.getHeight(), iconType.getWidth(), iconType.getHeight());
        }
    }

    private void renderTextField(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        //Apply matrix via render system so that it applies to the highlight
        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(matrix.last().pose());
        textField.render(new MatrixStack(), mouseX, mouseY, partialTicks);
        RenderSystem.popMatrix();
    }

    @Override
    public boolean hasPersistentData() {
        return true;
    }

    @Override
    public void syncFrom(GuiElement element) {
        super.syncFrom(element);
        textField.setValue(((GuiTextField) element).getText());
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
            } else if (Screen.isPaste(keyCode)) {
                //Manual handling of textField#keyPressed for pasting so that we can filter things as needed
                String text = Minecraft.getInstance().keyboardHandler.getClipboard();
                if (inputTransformer != null || inputValidator != null) {
                    boolean transformed = false;
                    char[] charArray = text.toCharArray();
                    for (int i = 0; i < charArray.length; i++) {
                        char c = charArray[i];
                        if (inputTransformer != null) {
                            c = inputTransformer.applyAsChar(c);
                            charArray[i] = c;
                            transformed = true;
                        }
                        if (inputValidator != null && !inputValidator.test(c)) {
                            //Contains an invalid character fail
                            return false;
                        }
                    }
                    if (transformed) {
                        text = String.copyValueOf(charArray);
                    }
                }
                textField.insertText(text);
            } else {
                textField.keyPressed(keyCode, scanCode, modifiers);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        if (canWrite()) {
            if (inputTransformer != null) {
                c = inputTransformer.applyAsChar(c);
            }
            if (inputValidator == null || inputValidator.test(c)) {
                return textField.charTyped(c, keyCode);
            }
            return false;
        }
        return super.charTyped(c, keyCode);
    }

    public String getText() {
        return textField.getValue();
    }

    public void setVisible(boolean visible) {
        textField.setVisible(visible);
    }

    public void setMaxStringLength(int length) {
        textField.setMaxLength(length);
    }

    public void setTextColor(int color) {
        textField.setTextColor(color);
    }

    public void setEnabled(boolean enabled) {
        textField.setEditable(enabled);
    }

    public void setCanLoseFocus(boolean canLoseFocus) {
        //TODO: Improve handling of when this is set to false in regards to focus changing with tab or things
        textField.setCanLoseFocus(canLoseFocus);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        textField.setFocus(focused);
        if (focused) {
            gui().focusChange(this);
        }
    }

    public boolean canWrite() {
        return textField.canConsumeInput();
    }

    public void setText(String text) {
        textField.setValue(text);
    }

    public void setResponder(Consumer<String> responder) {
        this.responder = responder;
    }
}