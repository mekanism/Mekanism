package mekanism.client.gui.element.button;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Objects;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * Extends our "Widget" class (GuiElement) instead of Button so that we can easier utilize common code
 */
public class MekanismButton extends GuiElement {

    @Nullable
    private final IHoverable onHover;
    @NotNull
    private final Runnable onLeftClick;
    @Nullable
    private final Runnable onRightClick;

    public MekanismButton(IGuiWrapper gui, int x, int y, int width, int height, Component text, @NotNull Runnable onLeftClick, @Nullable IHoverable onHover) {
        this(gui, x, y, width, height, text, onLeftClick, onLeftClick, onHover);
        //TODO: Decide if default implementation for right clicking should be do nothing, or act as left click
    }

    public MekanismButton(IGuiWrapper gui, int x, int y, int width, int height, Component text, @NotNull Runnable onLeftClick, @Nullable Runnable onRightClick,
          @Nullable IHoverable onHover) {
        super(gui, x, y, width, height, text);
        this.onHover = onHover;
        this.onLeftClick = Objects.requireNonNull(onLeftClick, "Buttons must have a left click behavior");
        this.onRightClick = onRightClick;
        this.clickSound = SoundEvents.UI_BUTTON_CLICK;
        setButtonBackground(ButtonBackground.DEFAULT);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            onLeftClick.run();
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
            if (onRightClick != null) {
                onRightClick.run();
            }
        }
    }

    @Override
    public boolean isValidClickButton(int button) {
        //Only allow right-clicking if we have a right click behavior/action
        return button == GLFW.GLFW_MOUSE_BUTTON_1 || button == GLFW.GLFW_MOUSE_BUTTON_2 && onRightClick != null;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //From AbstractButton with an additional check of validating that it is focused
        if (this.active && this.visible && this.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                playDownSound(minecraft.getSoundManager());
                onLeftClick.run();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderToolTip(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        if (onHover != null) {
            onHover.onHover(this, matrix, mouseX, mouseY);
        }
    }
}