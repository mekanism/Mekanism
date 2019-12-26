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
import net.minecraft.util.ResourceLocation;

public class GuiUpgradeTab extends GuiInsetElement<TileEntity> {

    public GuiUpgradeTab(IGuiWrapper gui, TileEntity tile, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "upgrade.png"), gui, def, tile, gui.getWidth(), 6, 26, 18);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        displayTooltip(MekanismLang.UPGRADES.translate(), mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.UPGRADE_MANAGEMENT, tile.getPos()));
    }
}