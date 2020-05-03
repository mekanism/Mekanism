package mekanism.client.gui.element.custom;

import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.base.IFluidContainerManager;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiContainerEditMode<TILE extends TileEntityMekanism & IFluidContainerManager> extends GuiInsetElement<TILE> {

    private static final ResourceLocation BOTH = MekanismUtils.getResource(ResourceType.GUI, "gun_powder.png");
    private static final ResourceLocation FILL = MekanismUtils.getResource(ResourceType.GUI, "container_edit_mode_fill.png");
    private static final ResourceLocation EMPTY = MekanismUtils.getResource(ResourceType.GUI, "container_edit_mode_empty.png");

    public GuiContainerEditMode(IGuiWrapper gui, TILE tile) {
        super(BOTH, gui, tile, gui.getWidth(), 138, 26, 18);
    }

    @Override
    protected ResourceLocation getOverlay() {
        switch (tile.getContainerEditMode()) {
            case FILL:
                return FILL;
            case EMPTY:
                return EMPTY;
        }
        return super.getOverlay();
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        displayTooltip(tile.getContainerEditMode().getTextComponent(), mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.NEXT_MODE, tile));
    }

    @Override
    protected void colorTab() {
        MekanismRenderer.color(EnumColor.DARK_BLUE);
    }
}
