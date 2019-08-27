package mekanism.client.gui.element;

import mekanism.api.Coord4D;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import mekanism.common.base.IFluidContainerManager;
import mekanism.common.network.PacketContainerEditMode;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiContainerEditMode extends GuiInsetElement<TileEntityMekanism> {

    public GuiContainerEditMode(IGuiWrapper gui, TileEntityMekanism tile, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "container_edit_mode.png"), gui, def, tile, 176, 138, 26, 18);
    }

    @Override
    protected int getXOffset() {
        return width + (innerSize * ((IFluidContainerManager) tileEntity).getContainerEditMode().ordinal());
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        displayTooltip(TextComponentUtil.build(((IFluidContainerManager) tileEntity).getContainerEditMode()), mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        ContainerEditMode current = ((IFluidContainerManager) tileEntity).getContainerEditMode();
        int ordinalToSet = current.ordinal() < (ContainerEditMode.values().length - 1) ? current.ordinal() + 1 : 0;
        Mekanism.packetHandler.sendToServer(new PacketContainerEditMode(Coord4D.get(tileEntity), ContainerEditMode.values()[ordinalToSet]));
    }
}
