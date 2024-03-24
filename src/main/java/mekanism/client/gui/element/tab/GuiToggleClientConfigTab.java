package mekanism.client.gui.element.tab;

import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import mekanism.api.text.ILangEntry;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedBooleanValue;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiToggleClientConfigTab extends GuiInsetToggleElement<IGuiWrapper> {

    private final Boolean2ObjectFunction<ILangEntry> langEntry;
    private final CachedBooleanValue config;

    public GuiToggleClientConfigTab(IGuiWrapper gui, int y, boolean left, ResourceLocation overlay, ResourceLocation flipped, CachedBooleanValue config, Boolean2ObjectFunction<ILangEntry> langEntry) {
        super(gui, gui, left ? -26 : gui.getXSize(), y, 26, 18, left, overlay, flipped, config);
        this.config = config;
        this.langEntry = langEntry;
    }

    @Override
    public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderToolTip(guiGraphics, mouseX, mouseY);
        displayTooltips(guiGraphics, mouseX, mouseY, langEntry.apply(config.get()).translate());
    }

    @Override
    protected void colorTab(GuiGraphics guiGraphics) {
        MekanismRenderer.color(guiGraphics, SpecialColors.TAB_JEI_REJECTS_TARGET);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        config.set(!config.get());
        MekanismConfig.client.save();
    }
}