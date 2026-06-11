package mekanism.client.gui.element.text;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.common.lib.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

/// GuiElement wrapper of TextFieldWidget for more control
public class GuiTextField extends GuiElement {

    public static final int DEFAULT_BORDER_COLOR = CommonColors.LIGHT_GRAY;
    public static final int DEFAULT_BACKGROUND_COLOR = CommonColors.BLACK;
    public static final IntSupplier SCREEN_COLOR = SpecialColors.TEXT_SCREEN::argb;
    public static final IntSupplier DARK_SCREEN_COLOR = () -> Color.argb(SCREEN_COLOR.getAsInt()).darken(0.4).argb();

    private final ClearingEditBox textField;
    private ContainerEventHandler parent;

    @Nullable
    private Consumer<GuiTextField> enterHandler;
    @Nullable
    private IntPredicate inputValidator;
    @Nullable
    private IntUnaryOperator inputTransformer;
    @Nullable
    private UnaryOperator<String> pasteTransformer;
    @Nullable
    private Consumer<String> responder;

    private BackgroundType backgroundType = BackgroundType.DEFAULT;
    @Nullable
    private IconType iconType;

    private int textOffsetX, textOffsetY;
    private float textScale = 1.0F;

    @Nullable
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

    public GuiTextField configureDigitalInput(Consumer<GuiTextField> enterHandler) {
        setBackground(BackgroundType.NONE);
        setIcon(IconType.DIGITAL);
        setTextColor(screenTextColor());
        setEnterHandler(enterHandler);
        addCheckmarkButton(ButtonType.DIGITAL, enterHandler);
        setScale(0.8F);
        return this;
    }

    public GuiTextField configureDigitalBorderInput(Consumer<GuiTextField> enterHandler) {
        setBackground(BackgroundType.DIGITAL);
        setTextColor(screenTextColor());
        setEnterHandler(enterHandler);
        addCheckmarkButton(ButtonType.DIGITAL, enterHandler);
        setScale(0.8F);
        return this;
    }

    public GuiTextField setEnterHandler(@Nullable Consumer<GuiTextField> enterHandler) {
        this.enterHandler = enterHandler;
        return this;
    }

    public GuiTextField setInputValidator(@Nullable IntPredicate inputValidator) {
        this.inputValidator = inputValidator;
        return this;
    }

    public GuiTextField setInputTransformer(@Nullable IntUnaryOperator inputTransformer) {
        this.inputTransformer = inputTransformer;
        return this;
    }

    public GuiTextField setPasteTransformer(@Nullable UnaryOperator<String> pasteTransformer) {
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

    public GuiTextField addCheckmarkButton(Consumer<GuiTextField> callback) {
        return addCheckmarkButton(ButtonType.NORMAL, callback);
    }

    public GuiTextField addCheckmarkButton(ButtonType type, Consumer<GuiTextField> callback) {
        checkmarkButton = addChild(type.getButton(this, (_, _, _) -> {
            //TODO: Instead of capturing this can we just use the passed element?
            callback.accept(this);
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
    public boolean isValidClickButton(MouseButtonInfo buttonInfo) {
        return super.isValidClickButton(buttonInfo) || textField.isValidClickButton(buttonInfo);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        double scaledX = event.x();
        // figure out the proper mouse placement based on text scaling
        if (textScale != 1.0F && scaledX > textField.getX()) {
            scaledX = textField.getX() + (scaledX - textField.getX()) / textScale;
        }
        //TODO - 26.1: Validate this is fine for how to scale and pass on the mouse button event
        if (textField.mouseClicked(new MouseButtonEvent(scaledX, event.y(), event.buttonInfo()), isDoubleClick)) {
            return true;
        }
        return super.isValidClickButton(event.buttonInfo()) && super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        backgroundType.render(this, guiGraphics);
        Matrix3x2fStack matrix = guiGraphics.pose();
        matrix.pushMatrix();
        //Translate to the top left before attempting to render the text field as vanilla renders widgets from the top left
        matrix.translate(-getGuiLeft(), -getGuiTop());
        if (textScale == 1F) {
            textField.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        } else {
            // hacky. we should write our own renderer at some point.
            float reverse = (1 - textScale) / textScale;
            matrix.scale(textScale, textScale);
            //Note: We use 4 instead of half line height (4.5) as text fields use 8 for calculating text positioning
            matrix.translate(textField.getX() * reverse, (textField.getY() + 4) * reverse);
            textField.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        }
        matrix.popMatrix();
        if (iconType != null) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, iconType.getIcon(), relativeX + 2, relativeY + (height / 2) - Mth.ceil(iconType.getHeight() / 2F), 0, 0, iconType.getWidth(), iconType.getHeight(), iconType.getWidth(), iconType.getHeight());
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
    public boolean keyPressed(KeyEvent event) {
        if (canWrite()) {
            if (event.isEscape() || event.isCycleFocus()) {
                //Manually handle hitting escape to make the whole interface go away
                // and allow using tab to switch focus
                return false;
            } else if (event.isConfirmation()) {
                //Handle processing both the enter key and the numpad enter key
                if (enterHandler != null) {
                    enterHandler.accept(this);
                }
                return true;
            } else if (event.isPaste()) {
                //Manual handling of textField#keyPressed for pasting so that we can filter things as needed
                String text = Minecraft.getInstance().keyboardHandler.getClipboard();
                if (pasteTransformer != null) {
                    text = pasteTransformer.apply(text);
                }
                if (inputTransformer != null || inputValidator != null) {
                    boolean wasTransformed = false;
                    int strLen = text.length();
                    StringBuilder transformed = new StringBuilder(strLen);
                    for (int i = 0; i < strLen; i++) {
                        int codepoint = text.codePointAt(i);
                        if (inputTransformer != null) {
                            codepoint = inputTransformer.applyAsInt(codepoint);
                            transformed.appendCodePoint(codepoint);
                            wasTransformed = true;
                        }
                        if (inputValidator != null && !inputValidator.test(codepoint)) {
                            //Contains an invalid character fail
                            return false;
                        }
                    }
                    if (wasTransformed) {
                        text = transformed.toString();
                    }
                }
                textField.insertText(text);
            } else {
                textField.keyPressed(event);
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (canWrite()) {
            int initialCodepoint = event.codepoint();
            int codepointUsed = initialCodepoint;
            if (inputTransformer != null) {
                codepointUsed = inputTransformer.applyAsInt(initialCodepoint);
            }
            if (inputValidator == null || inputValidator.test(codepointUsed)) {
                if (codepointUsed != initialCodepoint) {
                    event = new CharacterEvent(codepointUsed);
                }
                return textField.charTyped(event);
            }
            return false;
        }
        return super.charTyped(event);
    }

    public String getText() {
        return textField.getValue();
    }

    public GuiTextField setVisible(boolean visible) {
        textField.setVisible(visible);
        return this;
    }

    public GuiTextField setMaxLength(int length) {
        textField.setMaxLength(length);
        return this;
    }

    public GuiTextField setTextColor(int color) {
        textField.setTextColor(color);
        return this;
    }

    public GuiTextField setTextColorUneditable(int color) {
        textField.setTextColorUneditable(color);
        return this;
    }

    public GuiTextField setEditable(boolean enabled) {
        textField.setEditable(enabled);
        return this;
    }

    public GuiTextField setCanLoseFocus(boolean canLoseFocus) {
        //TODO: Improve handling of when this is set to false in regards to focus changing with tab or things
        textField.setCanLoseFocus(canLoseFocus);
        return this;
    }

    public GuiTextField allowColoredText() {
        textField.allowColors = true;
        return this;
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

    public GuiTextField setResponder(Consumer<String> responder) {
        this.responder = responder;
        return this;
    }

    private static class ClearingEditBox extends EditBox {

        private boolean allowColors;

        public ClearingEditBox(Font font, int x, int y, int width, int height, Component message) {
            super(font, x, y, width, height, message);
        }

        @Override
        public boolean isValidClickButton(MouseButtonInfo buttonInfo) {
            return super.isValidClickButton(buttonInfo) || buttonInfo.button() == InputConstants.MOUSE_BUTTON_RIGHT;
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
            if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
                //Allow clearing on right click
                setValue("");
            } else {
                super.onClick(event, isDoubleClick);
            }
        }

        @Override
        public void insertText(String text) {
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