package mekanism.client.gui.element;

import mekanism.api.Coord4D;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiMatrixTab.MatrixTab;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMatrixTab extends GuiTabElement<TileEntityInductionCasing, MatrixTab> {

    public GuiMatrixTab(IGuiWrapper gui, TileEntityInductionCasing tileEntityInductionCasing, MatrixTab type, int y, ResourceLocation def) {
        super(gui, tileEntityInductionCasing, type, y, def);
    }

    public enum MatrixTab implements TabType {
        MAIN("GuiEnergyTab.png", 49, "gui.main"),
        STAT("GuiStatsTab.png", 50, "gui.stats");

        private final String path;
        private final int guiId;
        private final String desc;

        MatrixTab(String s, int id, String s1) {
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