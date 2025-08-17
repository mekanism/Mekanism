package mekanism.client.gui.element.tab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.warning.IWarningTracker;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiWarningTab extends GuiTexturedElement {

    private static final ResourceLocation WARNING_LEFT = MekanismUtils.getResource(ResourceType.GUI_TAB, "warning_info_left.png");
    private static final ResourceLocation WARNING_RIGHT = MekanismUtils.getResource(ResourceType.GUI_TAB, "warning_info_right.png");

    private final IWarningTracker warningTracker;
    private List<Component> lastInfo = Collections.emptyList();
    @Nullable
    private Tooltip lastTooltip;

    public GuiWarningTab(IGuiWrapper gui, IWarningTracker warningTracker, boolean left) {
        super(left ? WARNING_LEFT : WARNING_RIGHT, gui, left ? -26 : gui.getXSize(), 109, 26, 26);
        this.warningTracker = warningTracker;
        updateVisibility();
    }

    private void updateVisibility() {
        visible = warningTracker.hasWarning();
    }

    @Override
    public void tick() {
        super.tick();
        //Ensure the visibility of the warning tab is correct based on if we have any warnings to display
        updateVisibility();
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blit(getResource(), relativeX, relativeY, 0, 0, width, height, width, height);
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        //Note: We don't need to check if there are any warnings or not as if there aren't the warning tab goes away
        List<Component> info = new ArrayList<>();
        info.add(MekanismLang.ISSUES.translateColored(EnumColor.YELLOW));
        info.addAll(warningTracker.getWarnings());
        if (!info.equals(lastInfo)) {
            lastInfo = info;
            lastTooltip = TooltipUtils.create(info);
        }
        setTooltip(lastTooltip);
    }
}