package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiRateBar extends GuiElement {

    private final IRateInfoHandler handler;

    public GuiRateBar(IGuiWrapper gui, IRateInfoHandler h, ResourceLocation def, int x, int y) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "rate_bar.png"), gui, def, x, y, 8, 60);
        handler = h;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(RESOURCE);
        guiObj.drawTexturedRect(x, y, 0, 0, width, height);
        if (handler.getLevel() > 0) {
            int displayInt = (int) (handler.getLevel() * 58);
            //TODO: Check this
            guiObj.drawTexturedRect(x + 1, y + height - 1 - displayInt, 8, height - 2 - displayInt, width - 2, displayInt);
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

    public static abstract class IRateInfoHandler {

        public ITextComponent getTooltip() {
            return null;
        }

        public abstract double getLevel();
    }
}