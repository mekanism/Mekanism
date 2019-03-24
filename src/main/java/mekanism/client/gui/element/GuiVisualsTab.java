package mekanism.client.gui.element;

import java.util.Arrays;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.sound.SoundHandler;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiVisualsTab extends GuiTileEntityElement<TileEntityDigitalMiner> {

    public GuiVisualsTab(IGuiWrapper gui, TileEntityDigitalMiner tile, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiVisualsTab.png"), gui, def, tile);
    }

    @Override
    public Rectangle4i getBounds(int guiWidth, int guiHeight) {
        return new Rectangle4i(guiWidth - 26, guiHeight + 6, 26, 26);
    }

    @Override
    protected boolean inBounds(int xAxis, int yAxis) {
        return xAxis >= -21 && xAxis <= -3 && yAxis >= 10 && yAxis <= 28;
    }

    @Override
    public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight) {
        mc.renderEngine.bindTexture(RESOURCE);
        guiObj.drawTexturedRect(guiWidth - 26, guiHeight + 6, 0, 0, 26, 26);
        guiObj.drawTexturedRect(guiWidth - 21, guiHeight + 10, 26, inBounds(xAxis, yAxis) ? 0 : 18, 18, 18);
        mc.renderEngine.bindTexture(defaultLocation);
    }

    @Override
    public void renderForeground(int xAxis, int yAxis) {
        mc.renderEngine.bindTexture(RESOURCE);
        if (inBounds(xAxis, yAxis)) {
            if (tileEntity.radius <= 64) {
                displayTooltip(
                      LangUtils.localize("gui.visuals") + ": " + LangUtils.transOnOff(tileEntity.clientRendering),
                      xAxis, yAxis);
            } else {
                displayTooltips(Arrays.asList(
                      LangUtils.localize("gui.visuals") + ": " + LangUtils.transOnOff(tileEntity.clientRendering),
                      TextFormatting.RED.toString() + LangUtils.localize("mekanism.gui.visuals.toobig")
                ), xAxis, yAxis);
            }
        }
        mc.renderEngine.bindTexture(defaultLocation);
    }

    @Override
    public void preMouseClicked(int xAxis, int yAxis, int button) {
    }

    @Override
    public void mouseClicked(int xAxis, int yAxis, int button) {
        if (button == 0 && inBounds(xAxis, yAxis)) {
            tileEntity.clientRendering = !tileEntity.clientRendering;
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
        }
    }
}