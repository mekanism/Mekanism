package mekanism.client.recipe_viewer.jei;

import mekanism.client.gui.element.GuiElement;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;

public class MekJeiWidget implements IJeiGuiEventListener {

    private final ScreenRectangle area;
    private final GuiElement element;

    public MekJeiWidget(GuiElement element) {
        this.element = element;
        this.area = element.getRectangle();
    }

    @Override
    public final ScreenRectangle getArea() {
        return area;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        //Shift the mouse positions to being global instead of relative
        mouseX += element.getX();
        mouseY += element.getY();
        element.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //Shift the mouse positions to being global instead of relative
        mouseX += element.getX();
        mouseY += element.getY();
        return element.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        //Shift the mouse positions to being global instead of relative
        mouseX += element.getX();
        mouseY += element.getY();
        return element.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        //Shift the mouse positions to being global instead of relative
        mouseX += element.getX();
        mouseY += element.getY();
        return element.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        //Shift the mouse positions to being global instead of relative
        mouseX += element.getX();
        mouseY += element.getY();
        return element.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(double mouseX, double mouseY, int keyCode, int scanCode, int modifiers) {
        return element.keyPressed(keyCode, scanCode, modifiers);
    }
}