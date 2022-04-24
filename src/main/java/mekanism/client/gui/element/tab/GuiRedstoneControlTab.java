package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import mekanism.client.SpecialColors;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.resources.ResourceLocation;

public class GuiRedstoneControlTab extends GuiInsetElement<TileEntityMekanism> {

    private static final ResourceLocation DISABLED = MekanismUtils.getResource(ResourceType.GUI, "redstone_control_disabled.png");
    private static final ResourceLocation HIGH = MekanismUtils.getResource(ResourceType.GUI, "redstone_control_high.png");
    private static final ResourceLocation LOW = MekanismUtils.getResource(ResourceType.GUI, "redstone_control_low.png");

    public GuiRedstoneControlTab(IGuiWrapper gui, TileEntityMekanism tile) {
        super(DISABLED, gui, tile, gui.getWidth(), 137, 26, 18, false);
    }

    @Override
    public void renderToolTip(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        displayTooltips(matrix, mouseX, mouseY, dataSource.getControlType().getTextComponent());
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.NEXT_REDSTONE_CONTROL, dataSource));
    }

    @Override
    protected ResourceLocation getOverlay() {
        return switch (dataSource.getControlType()) {
            case HIGH -> HIGH;
            case LOW -> LOW;
            default -> super.getOverlay();
        };
    }

    @Override
    protected void colorTab() {
        MekanismRenderer.color(SpecialColors.TAB_REDSTONE_CONTROL);
    }

    @Override
    protected void drawBackgroundOverlay(@Nonnull PoseStack matrix) {
        if (dataSource.getControlType() == RedstoneControl.PULSE) {
            GuiUtils.drawSprite(matrix, getButtonX() + 1, getButtonY() + 1, innerWidth - 2, innerHeight - 2, 0, MekanismRenderer.redstonePulse);
        } else {
            super.drawBackgroundOverlay(matrix);
        }
    }
}