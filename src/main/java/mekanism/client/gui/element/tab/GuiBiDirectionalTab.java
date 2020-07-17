package mekanism.client.gui.element.tab;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public abstract class GuiBiDirectionalTab extends GuiTexturedElement {

    protected GuiBiDirectionalTab(ResourceLocation resource, IGuiWrapper gui, int x, int y, int width, int height) {
        super(resource, gui, x, y, width, height);
    }

    protected abstract void onRightClick(double mouseX, double mouseY);

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        //TODO: We may want to eventually move this logic into GuiElement as it is shared by GuiButton
        if (this.active && this.visible && isHovered()) {
            if (button == 1) {
                //Right clicked
                playDownSound(Minecraft.getInstance().getSoundHandler());
                onRightClick(mouseX, mouseY);
                return true;
            }
        }
        return false;
    }
}