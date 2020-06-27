package mekanism.client.gui.element.bar;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.jei.interfaces.IJEIRecipeArea;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiHorizontalRateBar extends GuiBar<IBarInfoHandler> implements IJEIRecipeArea<GuiHorizontalRateBar> {

    private static final ResourceLocation RATE_BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "horizontal_rate.png");
    private static final int texWidth = 78;
    private static final int texHeight = 8;

    private ResourceLocation[] recipeCategories;

    public GuiHorizontalRateBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y) {
        super(RATE_BAR, gui, handler, x, y, texWidth, texHeight);
    }

    @Override
    protected void renderBarOverlay(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        int displayInt = (int) (getHandler().getLevel() * texWidth);
        func_238463_a_(matrix, field_230690_l_ + 1, field_230691_m_ + 1, 0, 0, displayInt, texHeight, texWidth, texHeight);
    }

    @Nonnull
    @Override
    public GuiHorizontalRateBar jeiCategories(@Nullable ResourceLocation... recipeCategories) {
        this.recipeCategories = recipeCategories;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation[] getRecipeCategories() {
        return recipeCategories;
    }
}