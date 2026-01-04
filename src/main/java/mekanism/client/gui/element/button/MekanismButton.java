package mekanism.client.gui.element.button;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Objects;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public void onClick(@NotNull MouseButtonEvent event, boolean isDoubleClick) {
        int button = event.button();
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            onLeftClick.onClick(this, event, isDoubleClick);
        } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            if (onRightClick != null) {
                onRightClick.onClick(this, event, isDoubleClick);
            }
        }
    }

    @Override
    public boolean isValidClickButton(@NotNull MouseButtonInfo buttonInfo) {
        //Only allow right-clicking if we have a right click behavior/action
        return buttonInfo.button() == InputConstants.MOUSE_BUTTON_LEFT || buttonInfo.button() == InputConstants.MOUSE_BUTTON_RIGHT && onRightClick != null;
    }

    @Override
    public boolean keyPressed(@NotNull KeyEvent event) {
        //From AbstractButton with an additional check of validating that it is focused
        if (this.active && this.visible && this.isFocused() && event.isSelection()) {
            playDownSound(minecraft.getSoundManager());
            return onLeftClick.onClick(this, getButtonX() + getButtonWidth() / 2.0, getButtonY() + getButtonHeight() / 2.0);
        }
        return super.keyPressed(event);
    }

}