package mekanism.client.gui.element.tab;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.gui.element.GuiWindow;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.util.ResourceLocation;

public abstract class GuiWindowCreatorTab extends GuiInsetElement<TileEntityMekanism> {

    public GuiWindowCreatorTab(ResourceLocation overlay, IGuiWrapper gui, TileEntityMekanism tile, int x, int y, int height, int innerSize) {
        super(overlay, gui, tile, x, y, height, innerSize);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        GuiWindow window = createWindow();
        window.setListenerTab(this);
        active = false;
        guiObj.addWindow(window);
    }

    public abstract GuiWindow createWindow();
}
