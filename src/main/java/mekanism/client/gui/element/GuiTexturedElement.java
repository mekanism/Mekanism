package mekanism.client.gui.element;

import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class GuiTexturedElement extends GuiElement {

    protected final ResourceLocation resource;

    public GuiTexturedElement(ResourceLocation resource, IGuiWrapper gui, int x, int y, int width, int height) {
        super(gui, x, y, width, height);
        this.resource = resource;
    }

    protected ResourceLocation getResource() {
        return resource;
    }

    public interface IInfoHandler {

        List<Component> getInfo();
    }
}