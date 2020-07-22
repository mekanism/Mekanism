package mekanism.client.gui.element.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;

/**
 * Extends our "Widget" class (GuiElement) instead of Button so that we can easier utilize common code
 */
public class MekanismButton extends GuiElement {

    private final IHoverable onHover;
    private final Runnable onLeftClick;
    private final Runnable onRightClick;

    public MekanismButton(IGuiWrapper gui, int x, int y, int width, int height, ITextComponent text, Runnable onLeftClick, IHoverable onHover) {
        this(gui, x, y, width, height, text, onLeftClick, onLeftClick, onHover);
        //TODO: Decide if default implementation for right clicking should be do nothing, or act as left click
    }

    public MekanismButton(IGuiWrapper gui, int x, int y, int width, int height, ITextComponent text, Runnable onLeftClick, Runnable onRightClick, IHoverable onHover) {
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
                playDownSound(Minecraft.getInstance().getSoundHandler());
                onLeftClick();
                return true;
            }
        }
        return false;
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        if (onHover != null) {
            onHover.onHover(this, matrix, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.active && this.visible && isHovered()) {
            if (button == 1) {
                //Right clicked
                playDownSound(Minecraft.getInstance().getSoundHandler());
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