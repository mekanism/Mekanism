package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.resources.Identifier;

public abstract class GuiTexturedElement extends GuiElement {

    private final Identifier resource;

    //TODO - 26.2: Replace this with something that actually draws it
    public GuiTexturedElement(Identifier resource, IGuiWrapper gui, int x, int y, int width, int height) {
        super(gui, x, y, width, height);
        this.resource = resource;
    }

    protected Identifier getResource() {
        return resource;
    }
}