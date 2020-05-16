package mekanism.client.gui.element.tab;

import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IRedstoneControl;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiRedstoneControlTab extends GuiInsetElement<TileEntityMekanism> {

    private static final ResourceLocation DISABLED = MekanismUtils.getResource(ResourceType.GUI, "gun_powder.png");
    private static final ResourceLocation HIGH = MekanismUtils.getResource(ResourceType.GUI, "redstone_control_high.png");
    private static final ResourceLocation LOW = MekanismUtils.getResource(ResourceType.GUI, "redstone_control_low.png");
    private static final ResourceLocation PULSE = MekanismUtils.getResource(ResourceType.GUI, "redstone_control_pulse.png");

    public GuiRedstoneControlTab(IGuiWrapper gui, TileEntityMekanism tile) {
        super(DISABLED, gui, tile, gui.getWidth(), 138, 26, 18);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        displayTooltip(((IRedstoneControl) tile).getControlType().getTextComponent(), mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.NEXT_REDSTONE_CONTROL, tile));
    }

    @Override
    protected ResourceLocation getOverlay() {
        switch (((IRedstoneControl) tile).getControlType()) {
            case HIGH:
                return HIGH;
            case LOW:
                return LOW;
            case PULSE:
                return PULSE;
        }
        return super.getOverlay();
    }

    @Override
    protected void colorTab() {
        MekanismRenderer.color(EnumColor.DARK_RED);
    }
}