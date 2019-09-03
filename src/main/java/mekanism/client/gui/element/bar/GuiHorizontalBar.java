package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiHorizontalBar<INFO extends IBarInfoHandler> extends GuiBar<INFO> {

    private static final ResourceLocation BAR = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "horizontal_bar.png");

    public GuiHorizontalBar(ResourceLocation resource, IGuiWrapper gui, INFO handler, ResourceLocation def, int x, int y, int width, int height) {
        //TODO: Bump the width by 2? for the border of the bar image? Or maybe remove border
        super(resource, gui, handler, def, x, y, width, height);
    }

    @Override
    public void renderBar() {
        minecraft.textureManager.bindTexture(BAR);
        guiObj.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, 54, 6);
    }
}