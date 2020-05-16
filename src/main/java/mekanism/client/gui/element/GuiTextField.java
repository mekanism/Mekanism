package mekanism.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import java.util.Arrays;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.Color;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.glfw.GLFW;

/**
 * GuiElement wrapper of TextFieldWidget for more control
 *
 * @author aidancbrady
 */
public class GuiTextField extends GuiTexturedElement {

    private TextFieldWidget textField;
    private Runnable enterHandler;
    private InputValidator inputValidator;
    private Consumer<String> responder;

    private BackgroundType backgroundType = BackgroundType.DEFAULT;
    private IconType iconType;

    private int textOffsetX, textOffsetY;
    private float textScale = 1.0F;

    private MekanismImageButton checkmarkButton;

    public GuiTextField(IGuiWrapper gui, int x, int y, int width, int height) {
        super(null, gui, x, y, width, height);

        textField = new TextFieldWidget(getFont(), this.x, this.y, width, height, "");
        textField.setEnableBackgroundDrawing(false);
        textField.setResponder(s -> {
            if (responder != null) {
                responder.accept(s);
            }
            if (checkmarkButton != null) {
                checkmarkButton.active = !textField.getText().isEmpty();
            }
        });
        guiObj.addFocusListener(this);
        updateTextField();
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

    public GuiTextField setInputValidator(InputValidator inputValidator) {
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
        // width is scaled based on text scale
        textField.setWidth(Math.round((width - (checkmarkButton != null ? textField.getHeight() + 2 : 0) - (iconType != null ? iconType.getOffsetX() : 0)) * (1 / textScale)));
        textField.x = x + textOffsetX + 2 + (iconType != null ? iconType.getOffsetX() : 0);
        textField.y = y + textOffsetY + 1 + (int) ((height / 2F) - 4);
    }

    @Override
    public void onWindowClose() {
        super.onWindowClose();
        guiObj.removeFocusListener(this);
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
        boolean prevFocus = textField.isFocused();
        double scaledX = mouseX;
        // figure out the proper mouse placement based on text scaling
        if (textScale != 1.0F && scaledX > textField.x) {
            scaledX = Math.min(scaledX, textField.x) + (scaledX - textField.x) * (1F / textScale);
        }
        boolean ret = textField.mouseClicked(scaledX, mouseY, button);
        // detect if we're now focused
        if (!prevFocus && textField.isFocused()) {
            guiObj.focusChange(this);
        }
        return ret || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void drawButton(int mouseX, int mouseY) {
        backgroundType.render(this);
        if (textScale != 1F) {
            // hacky. we should write our own renderer at some point.
            float reverse = (1 / textScale) - 1;
            float yAdd = 4 - (textScale * 8) / 2F;
            RenderSystem.pushMatrix();
            RenderSystem.scalef(textScale, textScale, textScale);
            RenderSystem.translated(textField.x * reverse, (textField.y) * reverse + yAdd * (1 / textScale), 0);
            textField.render(mouseX, mouseY, 0);
            RenderSystem.popMatrix();
        } else {
            textField.render(mouseX, mouseY, 0);
        }
        MekanismRenderer.resetColor();
        if (iconType != null) {
            minecraft.textureManager.bindTexture(iconType.getIcon());
            blit(x + 2, y + (height / 2) - (int) Math.ceil(iconType.getHeight() / 2F), 0, 0, iconType.getWidth(), iconType.getHeight(), iconType.getWidth(), iconType.getHeight());
        }
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
            guiObj.focusChange(this);
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

    public interface InputValidator {

        InputValidator ALL = (c) -> true;
        InputValidator DIGIT = Character::isDigit;
        InputValidator LETTER = Character::isLetter;
        InputValidator DECIMAL = or(DIGIT, from('.'));
        InputValidator SCI_NOTATION = or(DECIMAL, from('E'));

        InputValidator FILTER_CHARS = from('*', '-', ' ', '|', '_', '\'', ':', '/');
        InputValidator FREQUENCY_CHARS = from('-', ' ', '|', '\'', '\"', '_', '+', ':', '(', ')', '?', '!', '/', '@', '$', '`', '~', ',', '.', '#');

        static InputValidator from(char... chars) {
            return new SetInputValidator(chars);
        }

        static InputValidator or(InputValidator... validators) {
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

    private static final int DEFAULT_BORDER_COLOR = 0xA0A0A0;
    private static final int DEFAULT_BACKGROUND_COLOR = 0x000000;

    private static final IntSupplier SCREEN_COLOR = () -> Color.packOpaque(MekanismConfig.client.guiScreenTextColor.get());
    private static final IntSupplier DARK_SCREEN_COLOR = () -> Color.argb(SCREEN_COLOR.getAsInt()).darken(0.4).argb();

    public enum BackgroundType {
        INNER_SCREEN(field -> GuiUtils.renderBackgroundTexture(GuiInnerScreen.SCREEN, 32, 32, field.x - 1, field.y - 1, field.width + 2, field.height + 2, 256, 256)),
        ELEMENT_HOLDER(field -> GuiUtils.renderBackgroundTexture(GuiElementHolder.HOLDER, 2, 2, field.x - 1, field.y - 1, field.width + 2, field.height + 2, 256, 256)),
        DEFAULT(field -> {
            GuiUtils.fill(field.x - 1, field.y - 1, field.width + 2, field.height + 2, DEFAULT_BORDER_COLOR);
            GuiUtils.fill(field.x, field.y, field.width, field.height, DEFAULT_BACKGROUND_COLOR);
        }),
        DIGITAL(field -> {
            GuiUtils.fill(field.x - 1, field.y - 1, field.width + 2, field.height + 2, field.textField.isFocused() ? SCREEN_COLOR.getAsInt() : DARK_SCREEN_COLOR.getAsInt());
            GuiUtils.fill(field.x, field.y, field.width, field.height, DEFAULT_BACKGROUND_COLOR);
        }),
        NONE(field -> {
        });

        private final Consumer<GuiTextField> renderFunction;

        BackgroundType(Consumer<GuiTextField> renderFunction) {
            this.renderFunction = renderFunction;
        }

        public void render(GuiTextField field) {
            renderFunction.accept(field);
        }
    }

    public enum ButtonType {
        NORMAL((field, callback) -> new MekanismImageButton(field.guiObj, field.guiObj.getLeft() + field.relativeX + field.width - field.height, field.guiObj.getTop() + field.relativeY, field.height, 12,
              MekanismUtils.getResource(ResourceType.GUI_BUTTON, "checkmark.png"), callback)),
        DIGITAL((field, callback) -> {
            MekanismImageButton ret = new MekanismImageButton(field.guiObj, field.guiObj.getLeft() + field.relativeX + field.width - field.height, field.guiObj.getTop() + field.relativeY, field.height, 12,
                  MekanismUtils.getResource(ResourceType.GUI_BUTTON, "checkmark_digital.png"), callback);
            ret.setButtonBackground(ButtonBackground.DIGITAL);
            return ret;
        });

        private final BiFunction<GuiTextField, Runnable, MekanismImageButton> buttonCreator;

        ButtonType(BiFunction<GuiTextField, Runnable, MekanismImageButton> buttonCreator) {
            this.buttonCreator = buttonCreator;
        }

        public MekanismImageButton getButton(GuiTextField field, Runnable callback) {
            return buttonCreator.apply(field, callback);
        }
    }

    public enum IconType {
        DIGITAL(MekanismUtils.getResource(ResourceType.GUI, "digital_text_input.png"), 4, 7);

        private final ResourceLocation icon;
        private final int xSize, ySize;

        IconType(ResourceLocation icon, int xSize, int ySize) {
            this.icon = icon;
            this.xSize = xSize;
            this.ySize = ySize;
        }

        public ResourceLocation getIcon() {
            return icon;
        }

        public int getWidth() {
            return xSize;
        }

        public int getHeight() {
            return ySize;
        }

        public int getOffsetX() {
            return xSize + 4;
        }
    }
}
