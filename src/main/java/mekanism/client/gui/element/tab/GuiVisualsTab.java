package mekanism.client.gui.element.tab;

import java.util.Arrays;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiVisualsTab extends GuiInsetElement<TileEntityDigitalMiner> {

    public GuiVisualsTab(IGuiWrapper gui, TileEntityDigitalMiner tile, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "visuals.png"), gui, def, tile, -26, 6, 26, 18);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        ITextComponent visualsComponent = TextComponentUtil.build(Translation.of("gui.mekanism.visuals"), ": ", OnOff.of(tile.clientRendering));
        if (tile.getRadius() <= 64) {
            displayTooltip(visualsComponent, mouseX, mouseY);
        } else {
            displayTooltips(Arrays.asList(visualsComponent, TextComponentUtil.build(EnumColor.RED, Translation.of("gui.mekanism.visuals.toobig"))), mouseX, mouseY);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        tile.clientRendering = !tile.clientRendering;
    }
}