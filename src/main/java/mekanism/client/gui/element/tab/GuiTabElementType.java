package mekanism.client.gui.element.tab;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import net.minecraft.tileentity.TileEntity;

public abstract class GuiTabElementType<TILE extends TileEntity, TAB extends Enum<?> & TabType<TILE>> extends GuiInsetElement<TILE> {

    private final TAB tabType;

    public GuiTabElementType(IGuiWrapper gui, TILE tile, TAB type) {
        super(type.getResource(), gui, tile, -26, type.getYPos(), 26, 18, true);
        tabType = type;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        tabType.onClick(tile);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        displayTooltip(tabType.getDescription(), mouseX, mouseY);
    }
}