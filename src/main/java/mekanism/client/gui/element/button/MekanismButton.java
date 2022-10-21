package mekanism.client.gui.element.button;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Extends our "Widget" class (GuiElement) instead of Button so that we can easier utilize common code
 */
public class MekanismButton extends GuiElement {

    @Nullable
    private final IHoverable onHover;
    @Nullable
    private final Runnable onLeftClick;
    @Nullable
    private final Runnable onRightClick;

    public MekanismButton(IGuiWrapper gui, int x, int y, int width, int height, Component text, @Nullable Runnable onLeftClick, @Nullable IHoverable onHover) {
        this(gui, x, y, width, height, text, onLeftClick, onLeftClick, onHover);
        //TODO: Decide if default implementation for right clicking should be do nothing, or act as left click
    }

    public MekanismButton(IGuiWrapper gui, int x, int y, int width, int height, Component text, @Nullable Runnable onLeftClick, @Nullable Runnable onRightClick,
          @Nullable IHoverable onHover) {
        super(gui, x, y, width, height, text);
        this.onHover = onHover;
        this.onLeftClick = onLeftClick;
        this.onRightClick = onRightClick;
        playClickSound = true;
        setButtonBackground(ButtonBackground.DEFAULT);
    }

    private void onLeftClick() {
        if (onLeftClick != null) {
            onLeftClick.run();
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        onLeftClick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //From AbstractButton
        if (this.active && this.visible && this.isFocused()) {
            if (keyCode == 257 || keyCode == 32 || keyCode == 335) {
                playDownSound(Minecraft.getInstance().getSoundManager());
                onLeftClick();
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.active && this.visible && isHoveredOrFocused()) {
            if (button == 1) {
                //Right-clicked
                playDownSound(Minecraft.getInstance().getSoundManager());
                onRightClick();
                return true;
            }
        }
        return false;
    }

    //TODO: Add right click support to GuiElement
    protected void onRightClick() {
        if (onRightClick != null) {
            onRightClick.run();
        }
    }
}