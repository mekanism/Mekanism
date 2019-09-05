package mekanism.client.gui.element;

import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//TODO: Now these are full fledged widgets figure out if they have to offload things to the main gui obj still
@OnlyIn(Dist.CLIENT)
public abstract class GuiTexturedElement extends GuiElement {

    protected final ResourceLocation resource;
    protected final ResourceLocation defaultLocation;

    public GuiTexturedElement(ResourceLocation resource, IGuiWrapper gui, ResourceLocation def, int x, int y, int width, int height) {
        super(gui, gui.getLeft() + x, gui.getTop() + y, width, height, "");
        this.resource = resource;
        defaultLocation = def;
    }

    protected ResourceLocation getResource() {
        return resource;
    }

    public interface IInfoHandler {

        List<ITextComponent> getInfo();
    }
}