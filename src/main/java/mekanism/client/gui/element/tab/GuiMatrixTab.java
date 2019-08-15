package mekanism.client.gui.element.tab;

import mekanism.api.Coord4D;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiMatrixTab.MatrixTab;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketSimpleGui;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiMatrixTab extends GuiTabElementType<TileEntityInductionCasing, MatrixTab> {

    public GuiMatrixTab(IGuiWrapper gui, TileEntityInductionCasing tile, MatrixTab type, ResourceLocation def) {
        super(gui, tile, type, def);
    }

    public enum MatrixTab implements TabType {
        MAIN("GuiEnergyTab.png", 49, "mekanism.gui.main"),
        STAT("GuiStatsTab.png", 50, "mekanism.gui.stats");

        private final String description;
        private final String path;
        private final int guiId;

        MatrixTab(String path, int id, String desc) {
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
            Mekanism.packetHandler.sendToServer(new PacketSimpleGui(Coord4D.get(tile), 0, guiId));
        }

        @Override
        public ITextComponent getDescription() {
            return TextComponentUtil.translate(description);
        }

        @Override
        public int getYPos() {
            return 6;
        }
    }
}