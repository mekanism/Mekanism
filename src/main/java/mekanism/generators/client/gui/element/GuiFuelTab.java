package mekanism.generators.client.gui.element;

import mekanism.api.Coord4D;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiTabElement;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiFuelTab extends GuiTabElement<TileEntityReactorController> {

    public GuiFuelTab(IGuiWrapper gui, TileEntityReactorController tile, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiFuelTab.png"), gui, def, tile, 34);
    }

    @Override
    public void displayForegroundTooltip(int xAxis, int yAxis) {
        displayTooltip(LangUtils.localize("gui.fuel"), xAxis, yAxis);
    }

    @Override
    public void buttonClicked() {
        Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tileEntity), 1, 12));
    }
}