package mekanism.client.gui.element.tab;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public abstract class GuiTabElementType<TILE extends TileEntity, TAB extends Enum & TabType> extends GuiTabElement<TILE> {

    protected final TAB tabType;

    public GuiTabElementType(IGuiWrapper gui, TILE tile, TAB type, int y, ResourceLocation def) {
        super(type.getResource(), gui, def, tile, y);
        tabType = type;
    }

    @Override
    public void buttonClicked() {
        tabType.openGui(tileEntity);
    }

    @Override
    public void displayForegroundTooltip(int xAxis, int yAxis) {
        displayTooltip(tabType.getDesc(), xAxis, yAxis);
    }
}