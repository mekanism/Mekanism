package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.warning.IWarningTracker;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GuiWarningTab extends GuiTexturedElement {

    private final IWarningTracker warningTracker;

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
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        RenderSystem.setShaderTexture(0, getResource());
        blit(matrix, x, y, 0, 0, width, height, width, height);
    }

    @Override
    public void renderToolTip(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        //Note: We don't need to check if there are any warnings or not as if there aren't the warning tab goes away
        List<Component> info = new ArrayList<>();
        info.add(MekanismLang.ISSUES.translateColored(EnumColor.YELLOW));
        info.addAll(warningTracker.getWarnings());
        displayTooltips(matrix, mouseX, mouseY, info);
    }
}