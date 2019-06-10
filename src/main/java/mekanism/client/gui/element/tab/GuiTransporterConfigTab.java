package mekanism.client.gui.element.tab;

import mekanism.api.Coord4D;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab.TransporterConfigTab;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTransporterConfigTab extends GuiTabElement<TileEntity, TransporterConfigTab> {

    public GuiTransporterConfigTab(IGuiWrapper gui, int y, TileEntity tile, ResourceLocation def) {
        super(gui, tile, TransporterConfigTab.CONFIG, y, def);
    }

    public enum TransporterConfigTab implements TabType {
        CONFIG("GuiTransporterConfigTab.png", 51, "gui.configuration.transporter");

        private final String path;
        private final int guiId;
        private final String desc;

        TransporterConfigTab(String s, int id, String s1) {
            path = s;
            guiId = id;
            desc = s1;
        }

        @Override
        public ResourceLocation getResource() {
            return MekanismUtils.getResource(ResourceType.GUI_ELEMENT, path);
        }

        @Override
        public void openGui(TileEntity tile) {
            Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tile), 0, guiId));
        }

        @Override
        public String getDesc() {
            return LangUtils.localize(desc);
        }
    }
}