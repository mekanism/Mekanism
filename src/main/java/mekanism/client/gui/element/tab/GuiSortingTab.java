package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import org.jetbrains.annotations.NotNull;

public class GuiSortingTab extends GuiInsetElement<TileEntityFactory<?>> {

    public GuiSortingTab(IGuiWrapper gui, TileEntityFactory<?> tile) {
        super(MekanismUtils.getResource(ResourceType.GUI, "sorting.png"), gui, tile, -26, 62, 35, 18, true);
    }

    @Override
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        drawTextScaledBound(matrix, OnOff.of(dataSource.isSorting()).getTextComponent(), x + 3, y + 24, titleTextColor(), 21);
        MekanismRenderer.resetColor();
    }

    @Override
    public void renderToolTip(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        displayTooltips(matrix, mouseX, mouseY, MekanismLang.AUTO_SORT.translate());
    }

    @Override
    protected void colorTab() {
        MekanismRenderer.color(SpecialColors.TAB_FACTORY_SORT);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.AUTO_SORT_BUTTON, dataSource));
    }
}