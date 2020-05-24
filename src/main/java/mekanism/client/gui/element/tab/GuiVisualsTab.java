package mekanism.client.gui.element.tab;

import java.util.Arrays;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.common.MekanismLang;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.util.text.ITextComponent;

public class GuiVisualsTab extends GuiInsetElement<TileEntityDigitalMiner> {

    public GuiVisualsTab(IGuiWrapper gui, TileEntityDigitalMiner tile) {
        super(MekanismUtils.getResource(ResourceType.GUI, "visuals.png"), gui, tile, -26, 6, 26, 18, true);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        ITextComponent visualsComponent = MekanismLang.MINER_VISUALS.translate(OnOff.of(tile.clientRendering));
        if (tile.getRadius() <= 64) {
            displayTooltip(visualsComponent, mouseX, mouseY);
        } else {
            displayTooltips(Arrays.asList(visualsComponent, MekanismLang.MINER_VISUALS_TOO_BIG.translateColored(EnumColor.RED)), mouseX, mouseY);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        tile.clientRendering = !tile.clientRendering;
    }
}