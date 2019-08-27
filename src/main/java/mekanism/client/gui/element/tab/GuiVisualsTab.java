package mekanism.client.gui.element.tab;

import java.util.Arrays;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiVisualsTab extends GuiInsetElement<TileEntityDigitalMiner> {

    public GuiVisualsTab(IGuiWrapper gui, TileEntityDigitalMiner tile, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "visuals.png"), gui, def, tile, -26, 6, 26, 18);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        ITextComponent visualsComponent = TextComponentUtil.build(Translation.of("mekanism.gui.visuals"), ": ", BooleanStateDisplay.OnOff.of(tileEntity.clientRendering));
        if (tileEntity.getRadius() <= 64) {
            displayTooltip(visualsComponent, mouseX, mouseY);
        } else {
            displayTooltips(Arrays.asList(visualsComponent, TextComponentUtil.build(EnumColor.RED, Translation.of("mekanism.gui.visuals.toobig"))), mouseX, mouseY);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        tileEntity.clientRendering = !tileEntity.clientRendering;
    }
}