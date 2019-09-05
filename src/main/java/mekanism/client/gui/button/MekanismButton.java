package mekanism.client.gui.button;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//Extends our "Widget" class (GuiElement) instead of Button so that we can easier utilize common code
@OnlyIn(Dist.CLIENT)
public class MekanismButton extends GuiElement {

    private final IHoverable onHover;
    private final IPressable onLeftClick;
    private final IPressable onRightClick;

    public MekanismButton(IGuiWrapper gui, int x, int y, int width, int height, String text, IPressable onLeftClick, IHoverable onHover) {
        this(gui, x, y, width, height, text, onLeftClick, onLeftClick, onHover);
        //TODO: Decide if default implementation for right clicking should be do nothing, or act as left click
    }

    public MekanismButton(IGuiWrapper gui, int x, int y, int width, int height, String text, IPressable onLeftClick, IPressable onRightClick, IHoverable onHover) {
        super(gui, x, y, width, height, text);
        this.onHover = onHover;
        this.onLeftClick = onLeftClick;
        this.onRightClick = onRightClick;
    }

    protected void onPress() {
        if (onLeftClick != null) {
            onLeftClick.onPress(this);
        }
    }

    private void click() {
        if (onLeftClick != null) {
            onLeftClick.onPress(this);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        click();
    }

    @Override
    public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
        //From AbstractButton
        if (this.active && this.visible) {
            if (keyCode == 257 || keyCode == 32 || keyCode == 335) {
                playDownSound(Minecraft.getInstance().getSoundHandler());
                click();
                return true;
            }
        }
        return false;
    }


    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        if (onHover != null) {
            onHover.onHover(this, mouseX, mouseY);
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
            onRightClick.onPress(this);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @FunctionalInterface
    public interface IHoverable {

        void onHover(MekanismButton button, int mouseX, int mouseY);
    }

    @OnlyIn(Dist.CLIENT)
    @FunctionalInterface
    public interface IPressable {

        void onPress(MekanismButton button);
    }
}