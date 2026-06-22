package mekanism.client.gui.element.tab;

import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import org.jspecify.annotations.Nullable;

public class GuiTargetDirectionTab extends GuiInsetToggleElement<QIOItemViewerContainer> {

    @Nullable
    private static final Tooltip QIO_TRANSFER_TO_WINDOW = TooltipUtils.create(MekanismLang.QIO_TRANSFER_TO_WINDOW);
    @Nullable
    private static final Tooltip QIO_TRANSFER_TO_FREQUENCY = TooltipUtils.create(MekanismLang.QIO_TRANSFER_TO_FREQUENCY);

    public GuiTargetDirectionTab(IGuiWrapper gui, QIOItemViewerContainer holder, int y) {
        super(gui, holder, -26, y, 26, 18, true, Mekanism.rl("button/crafting_in"), Mekanism.rl("button/crafting_out"), QIOItemViewerContainer::shiftClickIntoFrequency);
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        //Note: This is backwards as it describes what the button will be doing
        setTooltip(dataSource.shiftClickIntoFrequency() ? QIO_TRANSFER_TO_WINDOW : QIO_TRANSFER_TO_FREQUENCY);
    }

    @Override
    protected int getTabColor(GuiGraphicsExtractor guiGraphics) {
        return MekanismRenderer.color(SpecialColors.TAB_TARGET_DIRECTION);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        dataSource.toggleTargetDirection();
    }
}