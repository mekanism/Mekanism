package mekanism.client.gui.element;

import mekanism.api.Coord4D;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMatrixTab extends GuiTileEntityElement<TileEntityInductionCasing> {

    private final MatrixTab tabType;
    private final int yPos;

    public GuiMatrixTab(IGuiWrapper gui, TileEntityInductionCasing tile, MatrixTab type, int y, ResourceLocation def) {
        super(type.getResource(), gui, def, tile);
        tabType = type;
        yPos = y;
    }

    @Override
    public Rectangle4i getBounds(int guiWidth, int guiHeight) {
        return new Rectangle4i(guiWidth - 26, guiHeight + yPos, 26, 26);
    }

    @Override
    protected boolean inBounds(int xAxis, int yAxis) {
        return xAxis >= -21 && xAxis <= -3 && yAxis >= yPos + 4 && yAxis <= yPos + 22;
    }

    @Override
    public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight) {
        mc.renderEngine.bindTexture(RESOURCE);
        guiObj.drawTexturedRect(guiWidth - 26, guiHeight + yPos, 0, 0, 26, 26);
        guiObj.drawTexturedRect(guiWidth - 21, guiHeight + yPos + 4, 26, inBounds(xAxis, yAxis) ? 0 : 18, 18, 18);
        mc.renderEngine.bindTexture(defaultLocation);
    }

    @Override
    public void renderForeground(int xAxis, int yAxis) {
        mc.renderEngine.bindTexture(RESOURCE);
        if (inBounds(xAxis, yAxis)) {
            displayTooltip(tabType.getDesc(), xAxis, yAxis);
        }
        mc.renderEngine.bindTexture(defaultLocation);
    }

    @Override
    public void preMouseClicked(int xAxis, int yAxis, int button) {
    }

    @Override
    public void mouseClicked(int xAxis, int yAxis, int button) {
        if (button == 0 && inBounds(xAxis, yAxis)) {
            tabType.openGui(tileEntity);
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
        }
    }

    public enum MatrixTab {
        MAIN("GuiEnergyTab.png", 49, "gui.main"),
        STAT("GuiStatsTab.png", 50, "gui.stats");

        private String path;
        private int guiId;
        private String desc;

        MatrixTab(String s, int id, String s1) {
            path = s;
            guiId = id;
            desc = s1;
        }

        public ResourceLocation getResource() {
            return MekanismUtils.getResource(ResourceType.GUI_ELEMENT, path);
        }

        public void openGui(TileEntity tile) {
            Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tile), 0, guiId));
        }

        public String getDesc() {
            return LangUtils.localize(desc);
        }
    }
}