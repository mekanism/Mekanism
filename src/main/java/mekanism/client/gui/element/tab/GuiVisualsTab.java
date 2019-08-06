package mekanism.client.gui.element.tab;

import java.util.Arrays;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiVisualsTab extends GuiTabElement<TileEntityDigitalMiner> {

    public GuiVisualsTab(IGuiWrapper gui, TileEntityDigitalMiner tile, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiVisualsTab.png"), gui, def, tile, 6);
    }

    @Override
    public void displayForegroundTooltip(int xAxis, int yAxis) {
        if (tileEntity.getRadius() <= 64) {
            displayTooltip(LangUtils.localize("gui.visuals") + ": " + LangUtils.transOnOff(tileEntity.clientRendering), xAxis, yAxis);
        } else {
            displayTooltips(Arrays.asList(LangUtils.localize("gui.visuals") + ": " + LangUtils.transOnOff(tileEntity.clientRendering),
                  TextFormatting.RED.toString() + LangUtils.localize("mekanism.gui.visuals.toobig")), xAxis, yAxis);
        }
    }

    @Override
    public void buttonClicked() {
        tileEntity.clientRendering = !tileEntity.clientRendering;
    }
}