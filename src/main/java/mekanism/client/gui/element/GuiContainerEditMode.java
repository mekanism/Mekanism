package mekanism.client.gui.element;

import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.base.IFluidContainerManager;
import mekanism.common.network.PacketContainerEditMode;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiContainerEditMode extends GuiInsetElement<TileEntityMekanism> {

    //TODO: Can we generate the overlay dynamically
    private static final ResourceLocation BOTH = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "gun_powder.png");
    private static final ResourceLocation FILL = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "container_edit_mode_fill.png");
    private static final ResourceLocation EMPTY = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "container_edit_mode_empty.png");

    public GuiContainerEditMode(IGuiWrapper gui, TileEntityMekanism tile, ResourceLocation def) {
        super(BOTH, gui, def, tile, gui.getWidth(), 138, 26, 18);
    }

    @Override
    protected ResourceLocation getResource() {
        switch (((IFluidContainerManager) tile).getContainerEditMode()) {
            case FILL:
                return FILL;
            case EMPTY:
                return EMPTY;
        }
        return super.getResource();
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        displayTooltip(((IFluidContainerManager) tile).getContainerEditMode().getTextComponent(), mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        ContainerEditMode current = ((IFluidContainerManager) tile).getContainerEditMode();
        Mekanism.packetHandler.sendToServer(new PacketContainerEditMode(Coord4D.get(tile), current.getNext()));
    }

    @Override
    protected void colorTab() {
        MekanismRenderer.color(EnumColor.DARK_BLUE);
    }
}
