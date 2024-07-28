package mekanism.client.gui.element.button;

import java.util.Objects;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * Extends our "Widget" class (GuiElement) instead of Button so that we can easier utilize common code
 */
public class MekanismButton extends GuiElement {

    @NotNull
    private final IClickable onLeftClick;
    @Nullable
    private final IClickable onRightClick;

    public MekanismButton(IGuiWrapper gui, int x, int y, int width, int height, Component text, @NotNull IClickable onLeftClick) {
        this(gui, x, y, width, height, text, onLeftClick, onLeftClick);
        //TODO: Decide if default implementation for right clicking should be do nothing, or act as left click
    }

    public MekanismButton(IGuiWrapper gui, int x, int y, int width, int height, Component text, @NotNull IClickable onLeftClick, @Nullable IClickable onRightClick) {
        super(gui, x, y, width, height, text);
        this.onLeftClick = Objects.requireNonNull(onLeftClick, "Buttons must have a left click behavior");
        this.onRightClick = onRightClick;
        this.clickSound = BUTTON_CLICK_SOUND;
        setButtonBackground(ButtonBackground.DEFAULT);
    }

    @Override
    public MekanismButton setTooltip(ILangEntry langEntry) {
        super.setTooltip(langEntry);
        return this;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            onLeftClick.onClick(this, mouseX, mouseY);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (onRightClick != null) {
                onRightClick.onClick(this, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean isValidClickButton(int button) {
        //Only allow right-clicking if we have a right click behavior/action
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && onRightClick != null;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //From AbstractButton with an additional check of validating that it is focused
        if (this.active && this.visible && this.isFocused() && CommonInputs.selected(keyCode)) {
            playDownSound(minecraft.getSoundManager());
            return onLeftClick.onClick(this, getButtonX() + getButtonWidth() / 2.0, getButtonY() + getButtonHeight() / 2.0);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

}