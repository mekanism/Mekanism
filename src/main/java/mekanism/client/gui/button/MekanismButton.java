package mekanism.client.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MekanismButton extends Button {

    private final IHoverable onHover;
    private final IPressable onRightClick;

    public MekanismButton(int x, int y, int width, int height, String text, IPressable onPress, IHoverable onHover) {
        this(x, y, width, height, text, onPress, onPress, onHover);
        //TODO: Decide if default implementation for right clicking should be do nothing, or act as left click
    }

    public MekanismButton(int x, int y, int width, int height, String text, IPressable onPress, IPressable onRightClick, IHoverable onHover) {
        super(x, y, width, height, text, onPress);
        this.onHover = onHover;
        this.onRightClick = onRightClick;
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

    protected void onRightClick() {
        if (onRightClick != null) {
            onRightClick.onPress(this);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @FunctionalInterface
    public interface IHoverable {
        void onHover(Button button, int mouseX, int mouseY);
    }
}