package mekanism.client.gui.element.bar;

import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiBar<INFO extends IBarInfoHandler> extends GuiTexturedElement {

    private final INFO handler;

    public GuiBar(ResourceLocation resource, IGuiWrapper gui, INFO handler, ResourceLocation def, int x, int y, int width, int height) {
        //TODO: Bump the width by 2? for the border of the bar image? Or maybe remove border
        super(resource, gui, def, x, y, width, height);
        this.handler = handler;
    }

    public INFO getHandler() {
        return handler;
    }

    protected abstract void renderBarOverlay(int mouseX, int mouseY, float partialTicks);

    protected abstract void renderBar();

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        renderBar();
        if (handler.getLevel() > 0) {
            minecraft.textureManager.bindTexture(getResource());
            renderBarOverlay(mouseX, mouseY, partialTicks);
        }
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        ITextComponent tooltip = handler.getTooltip();
        if (tooltip != null) {
            displayTooltip(tooltip, mouseX, mouseY);
        }
    }

    public interface IBarInfoHandler {

        @Nullable
        default ITextComponent getTooltip() {
            return null;
        }

        double getLevel();
    }
}