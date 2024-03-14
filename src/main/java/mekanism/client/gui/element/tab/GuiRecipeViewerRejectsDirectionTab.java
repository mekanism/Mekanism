package mekanism.client.gui.element.tab;

import mekanism.api.text.ILangEntry;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class GuiRecipeViewerRejectsDirectionTab extends GuiInsetToggleElement<IGuiWrapper> {

    public GuiRecipeViewerRejectsDirectionTab(IGuiWrapper gui, int y) {
        super(gui, gui, -26, y, 26, 18, true, getButtonLocation("recipe_viewer_frequency"), getButtonLocation("recipe_viewer_inventory"),
              MekanismConfig.client.qioRejectsToInventory);
    }

    @Override
    public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderToolTip(guiGraphics, mouseX, mouseY);
        //Note: This is backwards as it describes what the button will be doing
        ILangEntry langEntry = MekanismConfig.client.qioRejectsToInventory.get() ? MekanismLang.QIO_REJECTS_TO_INVENTORY : MekanismLang.QIO_REJECTS_TO_FREQUENCY;
        displayTooltips(guiGraphics, mouseX, mouseY, langEntry.translate());
    }

    @Override
    protected void colorTab(GuiGraphics guiGraphics) {
        MekanismRenderer.color(guiGraphics, SpecialColors.TAB_JEI_REJECTS_TARGET);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        MekanismConfig.client.qioRejectsToInventory.set(!MekanismConfig.client.qioRejectsToInventory.get());
        MekanismConfig.client.save();
    }
}