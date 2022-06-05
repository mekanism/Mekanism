package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.tile.interfaces.IHasVisualization;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.network.chat.Component;

public class GuiVisualsTab extends GuiInsetElement<IHasVisualization> {

    public GuiVisualsTab(IGuiWrapper gui, IHasVisualization hasVisualization) {
        super(MekanismUtils.getResource(ResourceType.GUI, "visuals.png"), gui, hasVisualization, -26, 6, 26, 18, true);
    }

    @Override
    public void renderToolTip(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        Component visualsComponent = MekanismLang.MINER_VISUALS.translate(OnOff.of(dataSource.isClientRendering()));
        if (dataSource.canDisplayVisuals()) {
            displayTooltips(matrix, mouseX, mouseY, visualsComponent);
        } else {
            displayTooltips(matrix, mouseX, mouseY, visualsComponent, MekanismLang.MINER_VISUALS_TOO_BIG.translateColored(EnumColor.RED));
        }
    }

    @Override
    protected void colorTab() {
        MekanismRenderer.color(SpecialColors.TAB_VISUALS);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        dataSource.toggleClientRendering();
    }
}