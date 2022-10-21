package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IFluidContainerManager;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiContainerEditModeTab<TILE extends TileEntityMekanism & IFluidContainerManager> extends GuiInsetElement<TILE> {

    private static final ResourceLocation BOTH = MekanismUtils.getResource(ResourceType.GUI, "container_edit_mode_both.png");
    private static final ResourceLocation FILL = MekanismUtils.getResource(ResourceType.GUI, "container_edit_mode_fill.png");
    private static final ResourceLocation EMPTY = MekanismUtils.getResource(ResourceType.GUI, "container_edit_mode_empty.png");

    public GuiContainerEditModeTab(IGuiWrapper gui, TILE tile) {
        super(BOTH, gui, tile, gui.getWidth(), 138, 26, 18, false);
    }

    @Override
    protected ResourceLocation getOverlay() {
        ContainerEditMode containerEditMode = dataSource.getContainerEditMode();
        if (containerEditMode == ContainerEditMode.FILL) {
            return FILL;
        } else if (containerEditMode == ContainerEditMode.EMPTY) {
            return EMPTY;
        }
        return super.getOverlay();
    }

    @Override
    public void renderToolTip(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        displayTooltips(matrix, mouseX, mouseY, dataSource.getContainerEditMode().getTextComponent());
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.NEXT_MODE, dataSource));
    }

    @Override
    protected void colorTab() {
        MekanismRenderer.color(SpecialColors.TAB_CONTAINER_EDIT_MODE);
    }
}
