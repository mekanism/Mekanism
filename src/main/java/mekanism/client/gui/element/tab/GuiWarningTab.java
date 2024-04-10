package mekanism.client.gui.element.tab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.MultiLineTooltip;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.warning.IWarningTracker;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiWarningTab extends GuiTexturedElement {

    private final IWarningTracker warningTracker;
    private List<Component> lastInfo = Collections.emptyList();
    @Nullable
    private Tooltip lastTooltip;

    public GuiWarningTab(IGuiWrapper gui, IWarningTracker warningTracker, int y) {
        super(MekanismUtils.getResource(ResourceType.GUI_TAB, "warning_info.png"), gui, -26, y, 26, 26);
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
            lastTooltip = MultiLineTooltip.createMulti(info);
        }
        setTooltip(lastTooltip);
    }
}