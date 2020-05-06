package mekanism.client.gui.element;

import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiTexturedElement extends GuiElement {

    protected final ResourceLocation resource;
    protected int relativeX;
    protected int relativeY;

    public GuiTexturedElement(ResourceLocation resource, IGuiWrapper gui, int x, int y, int width, int height) {
        super(gui, gui.getLeft() + x, gui.getTop() + y, width, height, "");
        this.resource = resource;
        this.relativeX = x;
        this.relativeY = y;
    }

    protected ResourceLocation getResource() {
        return resource;
    }

    public interface IInfoHandler {

        List<ITextComponent> getInfo();
    }
}