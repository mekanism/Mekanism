package mekanism.client.gui.element.tab;

import mekanism.api.Coord4D;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketSimpleGui;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiSideConfigurationTab extends GuiTabElement<TileEntity> {

    public GuiSideConfigurationTab(IGuiWrapper gui, TileEntity tile, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiConfigurationTab.png"), gui, def, tile, 6);
    }

    @Override
    public void displayForegroundTooltip(int xAxis, int yAxis) {
        displayTooltip(TextComponentUtil.build(Translation.of("mekanism.gui.configuration.side")), xAxis, yAxis);
    }

    @Override
    public void buttonClicked() {
        Mekanism.packetHandler.sendToServer(new PacketSimpleGui(Coord4D.get(tileEntity), 0, 9));
    }
}