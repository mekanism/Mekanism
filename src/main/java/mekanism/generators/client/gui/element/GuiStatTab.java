package mekanism.generators.client.gui.element;

import mekanism.api.Coord4D;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
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
public class GuiStatTab extends GuiElement {

    TileEntity tileEntity;

    public GuiStatTab(IGuiWrapper gui, TileEntity tile, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiStatsTab.png"), gui, def);

        tileEntity = tile;
    }

    @Override
    public Rectangle4i getBounds(int guiWidth, int guiHeight) {
        return new Rectangle4i(guiWidth - 26, guiHeight + 62, 26, 26);
    }

    @Override
    public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight) {
        mc.renderEngine.bindTexture(RESOURCE);

        guiObj.drawTexturedRect(guiWidth - 26, guiHeight + 62, 0, 0, 26, 26);

        if (xAxis >= -21 && xAxis <= -3 && yAxis >= 66 && yAxis <= 84) {
            guiObj.drawTexturedRect(guiWidth - 21, guiHeight + 66, 26, 0, 18, 18);
        } else {
            guiObj.drawTexturedRect(guiWidth - 21, guiHeight + 66, 26, 18, 18, 18);
        }

        mc.renderEngine.bindTexture(defaultLocation);
    }

    @Override
    public void renderForeground(int xAxis, int yAxis) {
        mc.renderEngine.bindTexture(RESOURCE);

        if (xAxis >= -21 && xAxis <= -3 && yAxis >= 66 && yAxis <= 84) {
            displayTooltip(LangUtils.localize("gui.stats"), xAxis, yAxis);
        }

        mc.renderEngine.bindTexture(defaultLocation);
    }

    @Override
    public void preMouseClicked(int xAxis, int yAxis, int button) {
    }

    @Override
    public void mouseClicked(int xAxis, int yAxis, int button) {
        if (button == 0) {
            if (xAxis >= -21 && xAxis <= -3 && yAxis >= 66 && yAxis <= 84) {
                Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tileEntity), 1, 13));
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            }
        }
    }
}
