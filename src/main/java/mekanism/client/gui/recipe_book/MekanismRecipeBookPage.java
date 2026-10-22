package mekanism.client.gui.recipe_book;

import java.util.List;
import java.util.function.Supplier;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.resource.IResourceContainer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.SlotSelectTime;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public class MekanismRecipeBookPage extends RecipeBookPage {

    private final Supplier<Font> fontSupplier;

    public MekanismRecipeBookPage(MekanismRecipeBookComponent<?> parent, SlotSelectTime slotSelectTime, Supplier<Font> fontSupplier, List<? extends IResourceContainer<?>> inputs) {
        this.fontSupplier = fontSupplier;
        super(parent, slotSelectTime, false);
        this.overlay = new MekanismOverlayRecipeComponent(this.overlay, fontSupplier, inputs);
        this.buttons.clear();
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            this.buttons.add(new MekanismRecipeButton(slotSelectTime));
        }
    }

    @Override
    public void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.minecraft.gui.screen() != null && this.hoveredButton instanceof MekanismRecipeButton recipeButton && !this.overlay.isVisible()) {
            Object displayStack = recipeButton.getGeneralDisplayStack();
            switch (displayStack) {
                case ItemStack stack -> {
                    Identifier tooltipStyle = stack.get(DataComponents.TOOLTIP_STYLE);
                    graphics.setComponentTooltipForNextFrame(fontSupplier.get(), recipeButton.getTooltipText(stack), mouseX, mouseY, stack, tooltipStyle);
                }
                case FluidStack stack -> {
                    Identifier tooltipStyle = stack.get(DataComponents.TOOLTIP_STYLE);
                    graphics.setComponentTooltipForNextFrame(fontSupplier.get(), recipeButton.getTooltipText(stack), mouseX, mouseY, tooltipStyle);
                }
                case ChemicalStack stack -> graphics.setComponentTooltipForNextFrame(fontSupplier.get(), recipeButton.getTooltipText(stack), mouseX, mouseY);
                default -> {
                }
            }
        }
    }
}