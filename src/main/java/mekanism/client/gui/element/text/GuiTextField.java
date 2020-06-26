package mekanism.client.gui.element.text;

import java.util.function.Consumer;
import java.util.function.IntSupplier;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.systems.RenderSystem;
import mekanism.api.functions.CharPredicate;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiRelativeElement;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.Color;
import net.minecraft.client.gui.widget.TextFieldWidget;

/**
 * GuiElement wrapper of TextFieldWidget for more control
 *
 * @author aidancbrady
 */
public class GuiTextField extends GuiRelativeElement {

    public static final int DEFAULT_BORDER_COLOR = 0xA0A0A0;
    public static final int DEFAULT_BACKGROUND_COLOR = 0x000000;
    public static final IntSupplier SCREEN_COLOR = () -> Color.packOpaque(MekanismConfig.client.guiScreenTextColor.get());
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

        textField = new TextFieldWidget(getFont(), this.field_230690_l_, this.field_230691_m_, width, height, "");
        textField.setEnableBackgroundDrawing(false);
        textField.setResponder(s -> {
            if (responder != null) {
                responder.accept(s);
            }
            if (checkmarkButton != null) {
                checkmarkButton.field_230693_o_ = !textField.getText().isEmpty();
            }
        });
        guiObj.addFocusListener(this);
        updateTextField();
    }

    @Override
    public void resize(int prevLeft, int prevTop, int left, int top) {
        super.resize(prevLeft, prevTop, left, top);
        //Ensure we also update the positions of the text field
        textField.field_230690_l_ = textField.field_230690_l_ - prevLeft + left;
        textField.field_230691_m_ = textField.field_230691_m_ - prevTop + top;
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
        checkmarkButton.field_230693_o_ = false;
        updateTextField();
        return this;
    }

    private void updateTextField() {
        // width is scaled based on text scale
        textField.setWidth(Math.round((field_230688_j_ - (checkmarkButton != null ? textField.getHeight() + 2 : 0) - (iconType != null ? iconType.getOffsetX() : 0)) * (1 / textScale)));
        textField.field_230690_l_ = field_230690_l_ + textOffsetX + 2 + (iconType != null ? iconType.getOffsetX() : 0);
        textField.field_230691_m_ = field_230691_m_ + textOffsetY + 1 + (int) ((field_230689_k_ / 2F) - 4);
    }

    public boolean isTextFieldFocused() {
        return textField.isFocused();
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
    public boolean func_231044_a_(double mouseX, double mouseY, int button) {
        boolean prevFocus = isTextFieldFocused();
        double scaledX = mouseX;
        // figure out the proper mouse placement based on text scaling
        if (textScale != 1.0F && scaledX > textField.field_230690_l_) {
            scaledX = Math.min(scaledX, textField.field_230690_l_) + (scaledX - textField.field_230690_l_) * (1F / textScale);
        }
        boolean ret = textField.func_231044_a_(scaledX, mouseY, button);
        // detect if we're now focused
        if (!prevFocus && isTextFieldFocused()) {
            guiObj.focusChange(this);
        }
        return ret || super.func_231044_a_(mouseX, mouseY, button);
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
            RenderSystem.translated(textField.field_230690_l_ * reverse, (textField.field_230691_m_) * reverse + yAdd * (1 / textScale), 0);
            textField.render(mouseX, mouseY, 0);
            RenderSystem.popMatrix();
        } else {
            textField.render(mouseX, mouseY, 0);
        }
        MekanismRenderer.resetColor();
        if (iconType != null) {
            minecraft.textureManager.bindTexture(iconType.getIcon());
            blit(field_230690_l_ + 2, field_230691_m_ + (field_230689_k_ / 2) - (int) Math.ceil(iconType.getHeight() / 2F), 0, 0, iconType.getWidth(), iconType.getHeight(), iconType.getWidth(), iconType.getHeight());
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
    public boolean func_231046_a_(int keyCode, int scanCode, int modifiers) {
        if (canWrite()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape to make the whole interface go away
                return false;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                enterHandler.run();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_TAB) {
                guiObj.incrementFocus(this);
                return true;
            }
            textField.func_231046_a_(keyCode, scanCode, modifiers);
            return true;
        }
        return super.func_231046_a_(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean func_231042_a_(char c, int keyCode) {
        if (canWrite()) {
            if (inputValidator == null || inputValidator.test(c)) {
                return textField.func_231042_a_(c, keyCode);
            }
            return false;
        }
        return super.func_231042_a_(c, keyCode);
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
}