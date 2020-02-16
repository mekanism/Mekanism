package mekanism.client.gui.element.tab;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.tileentity.TileEntity;

public class GuiSideConfigurationTab extends GuiInsetElement<TileEntity> {

    public GuiSideConfigurationTab(IGuiWrapper gui, TileEntity tile) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "configuration.png"), gui, tile, -26, 6, 26, 18);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        displayTooltip(MekanismLang.SIDE_CONFIG.translate(), mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.SIDE_CONFIGURATION, tile.getPos()));
    }
}