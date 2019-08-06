package mekanism.client.gui.element.tab;

import mekanism.api.Coord4D;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiTransporterConfigTab extends GuiTabElement<TileEntity> {

    public GuiTransporterConfigTab(IGuiWrapper gui, int y, TileEntity tile, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiTransporterConfigTab.png"), gui, def, tile, y);
    }

    @Override
    public void buttonClicked() {
        Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tileEntity), 0, 51));
    }

    @Override
    public void displayForegroundTooltip(int xAxis, int yAxis) {
        displayTooltip(LangUtils.localize("gui.configuration.transporter"), xAxis, yAxis);
    }
}