package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiRateBar extends GuiBar<IBarInfoHandler> {

    public GuiRateBar(IGuiWrapper gui, IBarInfoHandler handler, ResourceLocation def, int x, int y) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "rate_bar.png"), gui, handler, def, x, y, 8, 60);
    }

    @Override
    protected void renderBarOverlay(int mouseX, int mouseY, float partialTicks) {
        //TODO: Check all of this
        int displayInt = (int) (getHandler().getLevel() * 58);
        guiObj.drawModalRectWithCustomSizedTexture(x + 1, y + height - 1 - displayInt, 8, height - 2 - displayInt, width - 2, displayInt, 6, 58);
    }
}