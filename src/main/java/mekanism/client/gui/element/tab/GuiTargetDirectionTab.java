package mekanism.client.gui.element.tab;

import mekanism.api.text.ILangEntry;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class GuiTargetDirectionTab extends GuiInsetToggleElement<QIOItemViewerContainer> {

    public GuiTargetDirectionTab(IGuiWrapper gui, QIOItemViewerContainer holder, int y) {
        super(gui, holder, -26, y, 26, 18, true, getButtonLocation("crafting_in"), getButtonLocation("crafting_out"), QIOItemViewerContainer::shiftClickIntoFrequency);
    }

    @Override
    public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderToolTip(guiGraphics, mouseX, mouseY);
        //Note: This is backwards as it describes what the button will be doing
        ILangEntry langEntry = dataSource.shiftClickIntoFrequency() ? MekanismLang.QIO_TRANSFER_TO_WINDOW : MekanismLang.QIO_TRANSFER_TO_FREQUENCY;
        displayTooltips(guiGraphics, mouseX, mouseY, langEntry.translate());
    }

    @Override
    protected void colorTab(GuiGraphics guiGraphics) {
        MekanismRenderer.color(guiGraphics, SpecialColors.TAB_TARGET_DIRECTION);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        dataSource.toggleTargetDirection();
    }
}