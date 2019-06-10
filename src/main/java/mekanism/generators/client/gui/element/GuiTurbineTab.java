package mekanism.generators.client.gui.element;

import mekanism.api.Coord4D;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTabElement;
import mekanism.client.gui.element.TabType;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTurbineTab extends GuiTabElement<TileEntityTurbineCasing, TurbineTab> {

    public GuiTurbineTab(IGuiWrapper gui, TileEntityTurbineCasing tileEntityTurbineCasing, TurbineTab type, int y, ResourceLocation def) {
        super(gui, tileEntityTurbineCasing, type, y, def);
    }

    public enum TurbineTab implements TabType {
        MAIN("GuiGasesTab.png", 6, "gui.main"),
        STAT("GuiStatsTab.png", 7, "gui.stats");

        private final String path;
        private final int guiId;
        private final String desc;

        TurbineTab(String s, int id, String s1) {
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
            Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tile), 1, guiId));
        }

        @Override
        public String getDesc() {
            return LangUtils.localize(desc);
        }
    }
}