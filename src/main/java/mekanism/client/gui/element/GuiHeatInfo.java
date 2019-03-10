package mekanism.client.gui.element;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils.TempType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiHeatInfo extends GuiElement {

    public IInfoHandler infoHandler;

    public GuiHeatInfo(IInfoHandler handler, IGuiWrapper gui, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiHeatInfo.png"), gui, def);

        infoHandler = handler;
    }

    @Override
    public Rectangle4i getBounds(int guiWidth, int guiHeight) {
        return new Rectangle4i(guiWidth - 26, guiHeight + 138, 26, 26);
    }

    @Override
    public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight) {
        mc.renderEngine.bindTexture(RESOURCE);

        guiObj.drawTexturedRect(guiWidth - 26, guiHeight + 112, 0, 0, 26, 26);

        mc.renderEngine.bindTexture(defaultLocation);
    }

    @Override
    public void renderForeground(int xAxis, int yAxis) {
        if (xAxis >= -21 && xAxis <= -3 && yAxis >= 116 && yAxis <= 134) {

            List<String> info = new ArrayList<>(infoHandler.getInfo());

            info.add(LangUtils.localize("gui.unit") + ": " + general.tempUnit);
            displayTooltips(info, xAxis, yAxis);
        }
    }

    @Override
    public void preMouseClicked(int xAxis, int yAxis, int button) {
    }

    @Override
    public void mouseClicked(int xAxis, int yAxis, int button) {
        if (button == 0) {
            if (xAxis >= -21 && xAxis <= -3 && yAxis >= 116 && yAxis <= 134) {
                general.tempUnit = TempType.values()[(general.tempUnit.ordinal() + 1) % TempType.values().length];
            }
        }
    }
}
