package mekanism.client.gui.element.tab;

import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;

public class GuiTargetDirectionTab extends GuiInsetToggleElement<QIOItemViewerContainer> {

    private static final Tooltip QIO_TRANSFER_TO_WINDOW = Tooltip.create(MekanismLang.QIO_TRANSFER_TO_WINDOW.translate());
    private static final Tooltip QIO_TRANSFER_TO_FREQUENCY = Tooltip.create(MekanismLang.QIO_TRANSFER_TO_FREQUENCY.translate());

    public GuiTargetDirectionTab(IGuiWrapper gui, QIOItemViewerContainer holder, int y) {
        super(gui, holder, -26, y, 26, 18, true, getButtonLocation("crafting_in"), getButtonLocation("crafting_out"), QIOItemViewerContainer::shiftClickIntoFrequency);
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        //Note: This is backwards as it describes what the button will be doing
        setTooltip(dataSource.shiftClickIntoFrequency() ? QIO_TRANSFER_TO_WINDOW : QIO_TRANSFER_TO_FREQUENCY);
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