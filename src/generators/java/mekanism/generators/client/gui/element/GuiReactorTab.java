package mekanism.generators.client.gui.element;

import mekanism.api.Coord4D;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiTabElementType;
import mekanism.client.gui.element.tab.TabType;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketSimpleGui;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.generators.client.gui.element.GuiReactorTab.ReactorTab;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiReactorTab extends GuiTabElementType<TileEntityReactorController, ReactorTab> {

    public GuiReactorTab(IGuiWrapper gui, TileEntityReactorController tile, ReactorTab type, ResourceLocation def) {
        super(gui, tile, type, def);
    }

    public enum ReactorTab implements TabType {
        HEAT("GuiHeatTab.png", 11, "mekanism.gui.heat", 6),
        FUEL("GuiFuelTab.png", 12, "mekanism.gui.fuel", 34),
        STAT("GuiStatsTab.png", 13, "mekanism.gui.stats", 62);

        private final String description;
        private final String path;
        private final int guiId;
        private final int yPos;

        ReactorTab(String path, int id, String desc, int y) {
            this.path = path;
            guiId = id;
            description = desc;
            yPos = y;
        }

        @Override
        public ResourceLocation getResource() {
            return MekanismUtils.getResource(ResourceType.GUI_ELEMENT, path);
        }

        @Override
        public void openGui(TileEntity tile) {
            Mekanism.packetHandler.sendToServer(new PacketSimpleGui(Coord4D.get(tile), 1, guiId));
        }

        @Override
        public ITextComponent getDescription() {
            return TextComponentUtil.translate(description);
        }

        @Override
        public int getYPos() {
            return yPos;
        }
    }
}