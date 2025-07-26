package mekanism.client.gui.element.text;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.UnaryOperator;
import mekanism.api.functions.CharPredicate;
import mekanism.api.functions.CharUnaryOperator;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.common.lib.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.NotNull;
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

    private final ClearingEditBox textField;
    private ContainerEventHandler parent;

    private Runnable enterHandler;
    private CharPredicate inputValidator;
    private CharUnaryOperator inputTransformer;
    private UnaryOperator<String> pasteTransformer;
    private Consumer<String> responder;

    private BackgroundType backgroundType = BackgroundType.DEFAULT;
    private IconType iconType;

    private int textOffsetX, textOffsetY;
    private float textScale = 1.0F;

    private MekanismImageButton checkmarkButton;

    public GuiTextField(IGuiWrapper gui, int x, int y, int width, int height) {
        this(gui, gui, x, y, width, height);
    }

    public GuiTextField(IGuiWrapper gui, ContainerEventHandler parent, int x, int y, int width, int height) {
        super(gui, x, y, width, height);
        this.parent = parent;

        textField = new ClearingEditBox(font(), getX(), getY(), width, height, CommonComponents.EMPTY);
        textField.setBordered(false);
        textField.setResponder(s -> {
            if (responder != null) {
                responder.accept(s);
            }
            if (checkmarkButton != null) {
                checkmarkButton.active = !textField.getValue().isEmpty();
            }
        });
        updateTextField();
    }

    @Override
    public void transferToNewGui(IGuiWrapper gui) {
        boolean guiIsParent = parent == gui();
        super.transferToNewGui(gui);
        if (guiIsParent) {
            parent = gui;
        }
    }

    @Override
    public void resize(int prevLeft, int prevTop, int left, int top) {
        super.resize(prevLeft, prevTop, left, top);
        //Ensure we also update the positions of the text field
        textField.setPosition(textField.getX() - prevLeft + left, textField.getY() - prevTop + top);
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

    public GuiTextField setPasteTransformer(UnaryOperator<String> pasteTransformer) {
        this.pasteTransformer = pasteTransformer;
        return this;
    }

    public GuiTextField setBackground(BackgroundType backgroundType) {
        this.backgroundType = backgroundType;
        this.textField.setBordered(backgroundType == BackgroundType.DEFAULT);
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
        checkmarkButton = addChild(type.getButton(this, (element, mouseX, mouseY) -> {
            callback.run();
            parent.setFocused(this);
            return true;
        }));
        checkmarkButton.active = false;
        updateTextField();
        return this;
    }

    private void updateTextField() {
        //width is scaled based on text scale
        int iconOffsetX = iconType == null ? 0 : iconType.getOffsetX();
        textField.setWidth(Math.round((width - (checkmarkButton == null ? 0 : textField.getHeight() + 2) - iconOffsetX) / textScale));
        textField.setPosition(getX() + textOffsetX + 2 + iconOffsetX, getY() + textOffsetY + 1 + (int) ((height / 2F) - 4));
    }

    public boolean isTextFieldFocused() {
        return textField.isFocused();
    }

    @Override
    public void move(int changeX, int changeY) {
        super.move(changeX, changeY);
        updateTextField();
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return super.isValidClickButton(button) || textField.isValidClickButton(button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double scaledX = mouseX;
        // figure out the proper mouse placement based on text scaling
        if (textScale != 1.0F && scaledX > textField.getX()) {
            scaledX = textField.getX() + (scaledX - textField.getX()) / textScale;
        }
        if (textField.mouseClicked(scaledX, mouseY, button)) {
            return true;
        }
        return super.isValidClickButton(button) && super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        backgroundType.render(this, guiGraphics);
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        //Translate to the top left before attempting to render the text field as vanilla renders widgets from the top left
        pose.translate(-getGuiLeft(), -getGuiTop(), 0);
        if (textScale == 1F) {
            textField.render(guiGraphics, mouseX, mouseY, partialTicks);
        } else {
            // hacky. we should write our own renderer at some point.
            float reverse = (1 - textScale) / textScale;
            pose.scale(textScale, textScale, textScale);
            //Note: We use 4 instead of half line height (4.5) as text fields use 8 for calculating text positioning
            pose.translate(textField.getX() * reverse, (textField.getY() + 4) * reverse, 0);
            textField.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
        pose.popPose();
        if (iconType != null) {
            guiGraphics.blit(iconType.getIcon(), relativeX + 2, relativeY + (height / 2) - Mth.ceil(iconType.getHeight() / 2F), 0, 0, iconType.getWidth(), iconType.getHeight(), iconType.getWidth(), iconType.getHeight());
        }
    }

    @Override
    protected boolean supportsTabNavigation() {
        return true;
    }

    @Override
    public boolean hasPersistentData() {
        return true;
    }

    @Override
    public void syncFrom(GuiElement element) {
        super.syncFrom(element);
        textField.setValue(((GuiTextField) element).getText());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (canWrite()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_TAB) {
                //Manually handle hitting escape to make the whole interface go away
                // and allow using tab to switch focus
                return false;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                //Handle processing both the enter key and the numpad enter key
                if (enterHandler != null) {
                    enterHandler.run();
                }
                return true;
            } else if (Screen.isPaste(keyCode)) {
                //Manual handling of textField#keyPressed for pasting so that we can filter things as needed
                String text = Minecraft.getInstance().keyboardHandler.getClipboard();
                if (pasteTransformer != null) {
                    text = pasteTransformer.apply(text);
                }
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

    public void setMaxLength(int length) {
        textField.setMaxLength(length);
    }

    public void setTextColor(int color) {
        textField.setTextColor(color);
    }

    public void setTextColorUneditable(int color) {
        textField.setTextColorUneditable(color);
    }

    public void setEditable(boolean enabled) {
        textField.setEditable(enabled);
    }

    public void setCanLoseFocus(boolean canLoseFocus) {
        //TODO: Improve handling of when this is set to false in regards to focus changing with tab or things
        textField.setCanLoseFocus(canLoseFocus);
    }

    public void allowColoredText() {
        textField.allowColors = true;
    }

    @Override
    public void setFocused(boolean focused) {
        if (textField.canLoseFocus || focused) {
            super.setFocused(focused);
            textField.setFocused(focused);
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

    private static class ClearingEditBox extends EditBox {

        private boolean allowColors;

        public ClearingEditBox(Font font, int x, int y, int width, int height, Component message) {
            super(font, x, y, width, height, message);
        }

        @Override
        public boolean isValidClickButton(int button) {
            return super.isValidClickButton(button) || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
        }

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                //Allow clearing on right click
                setValue("");
            } else {
                super.onClick(mouseX, mouseY, button);
            }
        }

        @Override
        public void insertText(@NotNull String text) {
            if (allowColors) {
                //Copy of super, but modified to call a custom filter text that allows the section symbol to be used
                // so that the player can enter color codes
                int startIndex = Math.min(getCursorPosition(), this.highlightPos);
                int highlightEndIndex = Math.max(getCursorPosition(), this.highlightPos);
                String value = getValue();
                int spaceLeft = this.maxLength - value.length() - (startIndex - highlightEndIndex);
                if (spaceLeft > 0) {
                    String filtered = filterText(text);
                    int length = filtered.length();
                    if (spaceLeft < length) {
                        if (Character.isHighSurrogate(filtered.charAt(spaceLeft - 1))) {
                            --spaceLeft;
                        }
                        filtered = filtered.substring(0, spaceLeft);
                        length = spaceLeft;
                    }

                    setValue(new StringBuilder(value).replace(startIndex, highlightEndIndex, filtered).toString());
                    setCursorPosition(startIndex + length);
                    setHighlightPos(getCursorPosition());
                }
            } else {
                super.insertText(text);
            }
        }

        private static String filterText(String text) {
            StringBuilder builder = new StringBuilder();
            for (char c : text.toCharArray()) {
                if (StringUtil.isAllowedChatCharacter(c) || c == 167) {
                    builder.append(c);
                }
            }
            return builder.toString();
        }
    }
}