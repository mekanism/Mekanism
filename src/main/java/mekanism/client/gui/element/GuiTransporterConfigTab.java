package mekanism.client.gui.element;

import mekanism.api.Coord4D;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTransporterConfigTab extends GuiTileEntityElement<TileEntity> {

    private final int yPos;

    public GuiTransporterConfigTab(IGuiWrapper gui, int y, TileEntity tile, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiTransporterConfigTab.png"), gui, def, tile);
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
            displayTooltip(LangUtils.localize("gui.configuration.transporter"), xAxis, yAxis);
        }
        mc.renderEngine.bindTexture(defaultLocation);
    }

    @Override
    public void preMouseClicked(int xAxis, int yAxis, int button) {
    }

    @Override
    public void mouseClicked(int xAxis, int yAxis, int button) {
        if (button == 0 && inBounds(xAxis, yAxis)) {
            Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tileEntity), 0, 51));
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
        }
    }
}