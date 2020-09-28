package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
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
    public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        ITextComponent visualsComponent = MekanismLang.MINER_VISUALS.translate(OnOff.of(tile.clientRendering));
        if (tile.getRadius() <= 64) {
            displayTooltip(matrix, visualsComponent, mouseX, mouseY);
        } else {
            displayTooltips(matrix, Arrays.asList(visualsComponent, MekanismLang.MINER_VISUALS_TOO_BIG.translateColored(EnumColor.RED)), mouseX, mouseY);
        }
    }

    @Override
    protected void colorTab() {
        MekanismRenderer.color(SpecialColors.TAB_DIGITAL_MINER_VISUAL);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        tile.clientRendering = !tile.clientRendering;
    }
}