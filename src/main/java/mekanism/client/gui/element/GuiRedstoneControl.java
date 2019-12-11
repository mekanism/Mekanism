package mekanism.client.gui.element;

import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.network.PacketRedstoneControl;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class GuiRedstoneControl extends GuiInsetElement<TileEntity> {

    private static final ResourceLocation DISABLED = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "gun_powder.png");
    private static final ResourceLocation HIGH = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "redstone_control_high.png");
    private static final ResourceLocation LOW = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "redstone_control_low.png");
    private static final ResourceLocation PULSE = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "redstone_control_pulse.png");

    public GuiRedstoneControl(IGuiWrapper gui, TileEntity tile, ResourceLocation def) {
        super(DISABLED, gui, def, tile, gui.getWidth(), 138, 26, 18);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        displayTooltip(TextComponentUtil.build(((IRedstoneControl) tileEntity).getControlType()), mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        IRedstoneControl control = (IRedstoneControl) tileEntity;
        RedstoneControl next = control.getControlType().getNext(mode -> mode != RedstoneControl.PULSE || control.canPulse());
        Mekanism.packetHandler.sendToServer(new PacketRedstoneControl(Coord4D.get(tileEntity), next));
    }

    @Override
    protected ResourceLocation getResource() {
        switch (((IRedstoneControl) tileEntity).getControlType()) {
            case HIGH:
                return HIGH;
            case LOW:
                return LOW;
            case PULSE:
                return PULSE;
        }
        return super.getResource();
    }

    @Override
    protected void colorTab() {
        MekanismRenderer.color(EnumColor.DARK_RED);
    }
}