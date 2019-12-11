package mekanism.client.gui.element.tab;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public abstract class GuiTabElementType<TILE extends TileEntity, TAB extends Enum<?> & TabType<TILE>> extends GuiInsetElement<TILE> {

    private final TAB tabType;

    public GuiTabElementType(IGuiWrapper gui, TILE tile, TAB type, ResourceLocation def) {
        super(type.getResource(), gui, def, tile, -26, type.getYPos(), 26, 18);
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