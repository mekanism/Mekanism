package mekanism.client.gui.element.tab;

import mekanism.api.Coord4D;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiBoilerTab.BoilerTab;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiBoilerTab extends GuiTabElementType<TileEntityBoilerCasing, BoilerTab> {

    public GuiBoilerTab(IGuiWrapper gui, TileEntityBoilerCasing tile, BoilerTab type, ResourceLocation def) {
        super(gui, tile, type, def);
    }

    public enum BoilerTab implements TabType {
        MAIN("GuiGasesTab.png", 54, "gui.main"),
        STAT("GuiStatsTab.png", 55, "gui.stats");

        private final String description;
        private final String path;
        private final int guiId;

        BoilerTab(String path, int id, String desc) {
            this.path = path;
            guiId = id;
            description = desc;
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
            return LangUtils.localize(description);
        }

        @Override
        public int getYPos() {
            return 6;
        }
    }
}