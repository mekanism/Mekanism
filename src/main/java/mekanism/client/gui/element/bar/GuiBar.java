package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiBar<INFO extends IBarInfoHandler> extends GuiElement {

    private final INFO handler;

    public GuiBar(IGuiWrapper gui, INFO handler, ResourceLocation def, int x, int y, int width, int height) {
        //TODO: Bump the width by 2? for the border of the bar image? Or maybe remove border
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "bar.png"), gui, def, x, y, width, height);
        this.handler = handler;
    }

    public INFO getHandler() {
        return handler;
    }

    protected abstract void renderBarOverlay(int mouseX, int mouseY, float partialTicks);

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(RESOURCE);
        guiObj.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, 6, 55);
        if (handler.getLevel() > 0) {
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

        default ITextComponent getTooltip() {
            return null;
        }

        double getLevel();
    }
}