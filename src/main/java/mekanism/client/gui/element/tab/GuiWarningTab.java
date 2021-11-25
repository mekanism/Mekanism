package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.warning.IWarningTracker;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.text.ITextComponent;

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
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        minecraft.textureManager.bind(getResource());
        blit(matrix, x, y, 0, 0, width, height, width, height);
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        //Note: We don't need to check if there are any warnings or not as if there aren't the warning tab goes away
        List<ITextComponent> info = new ArrayList<>();
        info.add(MekanismLang.ISSUES.translateColored(EnumColor.YELLOW));
        info.addAll(warningTracker.getWarnings());
        displayTooltips(matrix, info, mouseX, mouseY);
    }
}